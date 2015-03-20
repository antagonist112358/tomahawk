package net.mentalarray.doozie

import java.io._

import org.apache.hadoop.fs.{Path => HdfsPath}

import scala.collection.{immutable, mutable}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try
import scala.xml.XML


// Stold from: http://stackoverflow.com/questions/5260298/how-can-i-obtain-the-default-value-for-a-type-in-scala
// *************************************************************************************************************
class Default[+A](val default: A)

trait LowerPriorityImplicits {
  // Stop AnyRefs from clashing with AnyVals
  implicit def defaultNull[A <: AnyRef]: Default[A] = new Default[A](null.asInstanceOf[A])
}

object Default extends LowerPriorityImplicits {

  implicit object DefaultDouble extends Default[Double](0.0)

  implicit object DefaultFloat extends Default[Float](0.0F)

  implicit object DefaultInt extends Default[Int](0)

  implicit object DefaultLong extends Default[Long](0L)

  implicit object DefaultShort extends Default[Short](0)

  implicit object DefaultByte extends Default[Byte](0)

  implicit object DefaultChar extends Default[Char]('\u0000')

  implicit object DefaultBoolean extends Default[Boolean](false)

  implicit object DefaultUnit extends Default[Unit](())

  implicit def defaultSeq[A]: Default[immutable.Seq[A]] = new Default[immutable.Seq[A]](immutable.Seq())

  implicit def defaultSet[A]: Default[Set[A]] = new Default[Set[A]](Set())

  implicit def defaultMap[A, B]: Default[Map[A, B]] = new Default[Map[A, B]](Map[A, B]())

  implicit def defaultOption[A]: Default[Option[A]] = new Default[Option[A]](None)

  def value[A](implicit value: Default[A]): A = value.default
}

// *************************************************************************************************************

/**
 * Created by kdivincenzo on 9/8/14.
 */
object Utility {

  // Helper classes

  object Generator {
    def apply[T](fn: Unit => T) = fn
  }

  class StringExtensions(s: String) {
    def isNullOrWhitespace: Boolean = {
      (s == null || s.trim.isEmpty)
    }

    def tokenize: Seq[String] = s.split(' ').toSeq
  }

  class MapExtensions[A, -B](map: scala.collection.mutable.Map[A, B]) {
    def addOrReplace(key: A, value: B) {
      if (map.contains(key))
        map(key) = value
      else
        map += Pair(key, value)
    }
  }

  class Activator[T <: AnyRef] private() {

    def createInstance(classRef: Class[_]): T = {
      val retVal = Class.forName(classRef.getName).newInstance()
      retVal.asInstanceOf[T]
    }

  }

  object Activator {
    def apply[T <: AnyRef](): Activator[T] = {
      new Activator[T]()
    }
  }

  object Path {

    private lazy val _classPath = System.getProperty("java.class.path")
      .split(':').distinct.mkString(":")

    def combine(paths: String*): String = {
      var file = new java.io.File(paths(0))

      for (i <- 1 until paths.size) {
        file = new java.io.File(file, paths(i))
      }

      file.getPath
    }

    def getPropXmlAsMap(path: String): Map[String, String] = {
      Map[String, String]() ++ (for (prop <- (XML.loadFile(path) \\ "property"))
      yield (prop \ "name").text -> (prop \ "value").text).toMap
    }

    def file(path: String): File = {
      val file = new File(path)
      if (!file.exists()) {
        throw new Exception("File does not exist: " + path)
      }
      file
    }

    def files(paths: Seq[String]): Seq[File] = {
      val outFiles = mutable.MutableList.empty[File]

      for (path <- paths) {
        val file = new File(path)
        if (!file.exists()) {
          throw new Exception("File does not exist: " + path)
        }
        outFiles += file
      }

      outFiles.toSeq
    }

    def getOrCreateDirectory(path: String): File = {
      val dirPath = new File(path)
      if (!dirPath.exists())
        dirPath.mkdir()

      dirPath
    }

    def classPath: String = _classPath

    private lazy val _classPathJars: List[File] = {

      class JarFileFilter extends FilenameFilter {
        override def accept(dir: File, name: String): Boolean = name.toLowerCase.endsWith(".jar")
      }

      val files = mutable.MutableList.empty[File]
      val filter = new JarFileFilter

      classPath.split(File.pathSeparator).foreach(filePath => {
        val file = new File(filePath)
        if (file.isDirectory) {
          recurseDirectory(files, file, filter)
        } else if (file.getName.toLowerCase.endsWith(".jar")) {
          files += file
        }
      })

      files.toList
    }

    def jarsInClassPath = _classPathJars

    def resolveJarInClassPath(fileName: String): Option[File] = {
      val lcFileName = fileName.toLowerCase
      _classPathJars.foreach(jarFile => {
        if (jarFile.getName.toLowerCase.contains(lcFileName))
          return Some(jarFile)
      })

      None
    }

    def readFile(filePath: String): String = {
      val inStream = new FileInputStream(filePath)
      val reader = new BufferedReader(new InputStreamReader(inStream))
      Helper.readStreamIntoString(reader)
    }

    private def recurseDirectory(foundFiles: mutable.MutableList[File], directory: File, filter: FilenameFilter): Unit = {
      directory.listFiles(filter).foreach(f => f match {
        case dir if dir.isDirectory => recurseDirectory(foundFiles, dir, filter)
        case file => foundFiles += file
      })
    }


  }

  object Hadoop {

    // Typical hadoop configuration files
    def coreSiteFile: String = "core-site.xml"

    def hdfsSiteFile: String = "hdfs-site.xml"

    def mapredSiteFile: String = "mapred-site.xml"

    def sqoopSiteFile: String = "sqoop-site.xml"

    // Gets the full (absolute) HDFS path from a relative (local-*nux style) path
    def toHdfsPath(relativePath: String): String = {
      val base = "hdfs://%s" format Application.settings.getSetting(Setting.HDFSNameNode)
      Path.combine(base, relativePath)
    }

    // Read a file from HDFS into a string
    def readFile(filePath: String): String = {
      val hdfsPath = new HdfsPath(filePath)
      val hdfs = org.apache.hadoop.fs.FileSystem.get(Application.hadoopConfiguration)
      val reader = new BufferedReader(new InputStreamReader(hdfs.open(hdfsPath)))

      Helper.readStreamIntoString(reader)
    }

    def writeFile(filePath: String, data: String, overWriteFlag: Boolean = false,
                  appendIfExists: Boolean = false): Unit = {

      val hdfsPath = new HdfsPath(filePath)
      val hdfs = org.apache.hadoop.fs.FileSystem.get(Application.hadoopConfiguration)
      var buffWrite: BufferedWriter = null

      if (hdfs.exists(hdfsPath)) {
        if (appendIfExists) {
          buffWrite = new BufferedWriter(new OutputStreamWriter(hdfs.append(hdfsPath)))
        } else {
          buffWrite = new BufferedWriter(new OutputStreamWriter(hdfs.create(hdfsPath, overWriteFlag)))
        }
      } else {
        buffWrite = new BufferedWriter(new OutputStreamWriter(hdfs.create(hdfsPath, overWriteFlag)))
      }

      try {
        if (buffWrite != null) {
          buffWrite.write(data)
        }
      } finally {
        buffWrite.close()
      }
    }
  }

  class AtomicWrapper[A](instance: A) {

    // Sync object
    private[this] val lock = new Object()

    // Accessors
    def atomicAction(fn: A => Unit) : Unit = lock.synchronized { fn (instance) }
    def atomicFunc[B](fn: A => B) : B = lock.synchronized { fn(instance) }

  }

  object AtomicWrapper {
    def apply[A](instance: A) = new AtomicWrapper[A](instance)
  }

  protected[Utility] object Helper {

    import scala.util.control.Breaks._

    // Reads the contents of an entire file into a string
    def readStreamIntoString(stream: java.io.Reader, encoding: String = "UTF-8"): String = {
      val outStream = new ByteArrayOutputStream

      try {

        breakable {
          do {
            stream.read() match {
              case -1 => break
              case c => outStream.write(c)
            }
          } while (true)
        }

        outStream.flush

      } finally {
        stream.close
      }

      new String(outStream.toByteArray, encoding)
    }

  }

  // Helper methods
  def notNull[T](x : T) : Option[T] = if (x == null) None else Some(x)

  def makeUUID = java.util.UUID.randomUUID()

  // Implicit classes

  implicit class DelegateExtensions[-TIn, +TOut](fn: TIn => TOut) {
    def toAction(input: => TIn): Unit => TOut = _ => fn(input)
  }

  implicit class OptionExtensions[+T](opt: Option[T]) {
    def ifNotNoneAction(fn: T => Unit) = {
      if (opt.isDefined) {
        fn(opt.get)
      }
    }
  }

  implicit class SafeReferenceOperator[T](in : T) {
    lazy val _instance = notNull(in)
    def ?![B](f : T => B) : Option[B] = if (_instance.isEmpty) None else Some(f(_instance.get))
    def ??(v : => T) : T = if (_instance.isEmpty) v else _instance.get
  }

  implicit class FutureExtensions[+A](future: Future[A]) {

    /**
     * Awaits the result of the future, then provides the result in a Try[A]
     * @return The Success or Failure of the future.
     */
    def await : Try[A] = {
      Await.ready(future, Duration.Inf)
      future.value.get
    }

  }

  // Implicit casts

  implicit def stringExtensions(str: String) = new StringExtensions(str)

  implicit def mapExtensions[A, B](map: scala.collection.mutable.Map[A, B]) = new MapExtensions[A, B](map)


}

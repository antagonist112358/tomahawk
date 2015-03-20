package net.mentalarray.doozie.Internal

import java.io._
import java.net.URLClassLoader
import java.util.jar._

import com.google.common.io.Files
import com.twitter.util.Eval

import scala.tools.nsc.{Global, Settings}

/**
 * Created by kdivincenzo on 10/2/14.
 */

/***
  * Allows compilation of files into classes or into jar files.
 */
class ScalaCompiler {

  def createJar(targetPath: String, sources: String*) : Unit = {
    // Compile the sources
    val classesDirectory = compileFromFiles(sources)
    val targetDir = classesDirectory.getParent

    // Create a manifest to go into the jar
    val manifest = new Manifest
    manifest.getMainAttributes.put(Attributes.Name.MANIFEST_VERSION, "None")
    val jarOut = new JarOutputStream(new FileOutputStream(targetPath), manifest)

    // Write out the jar
    try {
      AddAllToJar(targetDir, jarOut)
    } finally {
      // Close the jarfile
      jarOut.close
      // Delete the classes directory
      classesDirectory.delete
    }

  }

  def createJarFromScript(targetPath: String, scriptFile: String) = {
    val tempDirectory = Files.createTempDir()
    val evalUtil = new Eval(Some(tempDirectory))
    
    val obj = evalUtil[AnyRef](new File(scriptFile))

    // Create a manifest to go into the jar
    val manifest = new Manifest
    manifest.getMainAttributes.put(Attributes.Name.MANIFEST_VERSION, "None")
    val jarOut = new JarOutputStream(new FileOutputStream(targetPath), manifest)

    // Write out the jar
    try {
      AddAllToJar(tempDirectory.getName, jarOut)
    } finally {
      // Close the jarfile
      jarOut.close
      // Delete the classes directory
      tempDirectory.delete
    }
    
    obj.getClass
  }
  
  def createInstance[T](source: String) : T = {
    val evalUtil = new Eval()
    evalUtil[T](Path.file(source))
  }

  def createInstance[T](typeName: String, sources: String*) : T = {
    // Compile the sources
    val classesDirectory = compileFromFiles(sources)

    // Create the loader and load the specific class
    val cl = new URLClassLoader(Array(classesDirectory.toURL))
    val clazz = cl.loadClass(typeName)

    try {

      // Return an instance of the class
      clazz.newInstance().asInstanceOf[T]

    } finally {
      // Cleanup classesDirectory
      classesDirectory.delete()
    }
  }


  // Compiler implementation
  // From: http://stackoverflow.com/questions/19494907/scala-run-time-code-compilation
  private def compileFromFiles(files: Seq[String]) = {

    val compSettings = new Settings()
    val tempDirectory = Files.createTempDir()

    // Set the output directory
    compSettings.outputDirs.setSingleOutput(tempDirectory.toString)

    val comp = new Global(compSettings)
    val crun = new comp.Run

    // Compile the files
    crun.compile(files.toList)

    // return the directory containing the class files
    tempDirectory
  }

  // Handles adding xxx.class files to a new jarfile
  private def addToJar(jarOut: JarOutputStream, file: File, reldir: String): Unit =
  {
    val fName = reldir + file.getName
    val fNameMod = if (file.isDirectory) fName + "/" else fName
    val entry = new JarEntry(fNameMod)
    entry.setTime(file.lastModified)
    jarOut.putNextEntry(entry)
    if (file.isDirectory)
    {
      jarOut.closeEntry
      file.listFiles.foreach(i => addToJar(jarOut, i, fName + "/"))
    }
    else
    {
      val buf = new Array[Byte](1024)
      val in = new FileInputStream(file)
      Stream.continually(in.read(buf)).takeWhile(_ != -1).foreach(jarOut.write(buf, 0, _))
      in.close
      jarOut.closeEntry()
    }
  }

  private def AddAllToJar(targDir: String, jarOut: JarOutputStream): Unit =
    new java.io.File(targDir).listFiles.foreach(i => addToJar(jarOut, i, ""))

}

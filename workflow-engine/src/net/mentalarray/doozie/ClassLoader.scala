package net.mentalarray.doozie

import java.io.File
import java.lang.{ClassLoader => JavaClassLoader}
import java.net.{URL, URLClassLoader}

import scala.reflect.runtime.universe

/**
 * Created by kdivincenzo on 9/27/14.
 */
object ClassLoader extends Internal.InternalClassLoader {

  def createInstanceFrom[T](className: String, jarPath: String) : T = {

    val classLoader = new URLClassLoader(Array[URL](new File(jarPath).toURI.toURL), JavaClassLoader.getSystemClassLoader)
    val classDef = classLoader.loadClass(className)
    val instance = classDef.newInstance

    instance.asInstanceOf[T]
  }

  def findClass[T](classPath: String): T = {

    val loader = universe.runtimeMirror(getClass.getClassLoader())
    val module = loader.staticModule(classPath)

    loader.reflectModule(module).instance.asInstanceOf[T]
  }

  def loadFile(jarPath: String): Unit = InternalClassLoader.addFile(jarPath)

  def loadFile(file: File): Unit = InternalClassLoader.addFile(file)

  def loadURL(url: URL): Unit = InternalClassLoader.addURL(url)

}

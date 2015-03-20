package net.mentalarray.doozie.Internal

import java.security._
/** This object defines an implicit view that will convert Scala functions
  * into PrivilegedAction instances that can be sent to the java.security.AccessController.
  */
object ScalaSecurityImplicits {
  implicit def functionToPrivilegedAction[A](func : Function0[A]) =
    new PrivilegedAction[A] {
      override def run() = func()
    }
}
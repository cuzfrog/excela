package com.github.cuzfrog.utils

import scala.annotation.tailrec
import java.lang.reflect.Constructor

private[cuzfrog] object ClassFinder {
  trait InnerClassFinder {
    /**
     * Find inner classes by class names.
     * If the class you're to look for is an inner class of another inner class, you should add "$" ahead of your class name.
     * @param className the sub class name
     * @param nToSearch the upper limit number of the classes that have the same name+n you attempt to find, which starts from 0.
     * @return a sequence of Class[B]
     */
    def findClasses[B](className: String, nToSearch: Int = 0): Seq[Class[B]] = {
      ClassFinder.findClasses(this.getClass.getName + className, nToSearch)
    }
  }

  trait InstanceGetter[B] {
    /**
     * Create a new instance, provided the class's name.
     */
    def create(className: String, parameter: AnyRef = null, parameters: Seq[AnyRef] = Nil, location: ClassLocation.Location = ClassLocation.Package): B = {
      val path = getPath(className, location, this)
      val foundClasses: Seq[Class[B]] = ClassFinder.findClasses(path)
      val foundClass = foundClasses match {
        case Nil                => throw new IllegalArgumentException("Bad class path:" + path)
        case seq: Seq[Class[B]] => seq.head
      }
      val constructor: Constructor[B] = foundClass.getConstructors.head.asInstanceOf[Constructor[B]]
      import scala.annotation.switch
      (constructor.getParameterCount: @switch) match {
        case 0 => constructor.newInstance()
        case 1 =>
          require(parameter != null, "Parameters are not enough.")
          constructor.newInstance(parameter)
        case 2 =>
          require(parameters.nonEmpty, "Parameters are not enough.")
          constructor.newInstance(parameter, parameters.head)
        case 3 =>
          require(parameters.size > 1, "Parameters are not enough.")
          constructor.newInstance(parameter, parameters.head, parameters(1))
      }
    }
    /**
     * Get companion object, provided the object name.
     */
    def getCompanionObject(className: String, location: ClassLocation.Location = ClassLocation.Package): B = {
      val path = getPath(className, location, this)
      val foundClasses: Seq[Class[B]] = ClassFinder.findClasses(path)
      val foundClass = foundClasses match {
        case Nil                => throw new IllegalArgumentException("Bad class path:" + path)
        case seq: Seq[Class[B]] => seq.head
      }
      import scala.reflect.runtime.{currentMirror => cm }
      val classSymbol = cm.classSymbol(foundClass)
      val moduleSymbol = classSymbol.companion.asModule
      val moduleMirror = cm.reflectModule(moduleSymbol)
      moduleMirror.instance.asInstanceOf[B]
    }
  }

  object ClassLocation {
    sealed trait Location
    case object Package extends Location
    case object Inner extends Location
    case class SubPath(subPath: String) extends Location
  }
  private def getPath(className: String, location: ClassLocation.Location, contextClass: Any): String = location match {
    case ClassLocation.Package          => contextClass.getClass.getPackage.getName + "." + className
    case ClassLocation.Inner            => contextClass.getClass.getName + "$" + className
    case ClassLocation.SubPath(subPath) => contextClass.getClass.getPackage.getName + subPath + className
  }

  /**
   * This trait defines a withName function which takes a string and return a corresponding object
   * from an implicit value Set.
   */
  trait WithName[T] {
    protected val values: Set[T]
    def withName(in: String): T = {
      values.find(_.toString == in) match {
        case Some(o) => o
        case _       => throw new IllegalArgumentException("Bad enum name:" + in)
      }
    }
  }

  /**
   * Find classes by class paths.
   *
   * @param classPath including class name
   * @param nToSearch the upper limit number of the classes that have the same name+n you attempt to find, which starts from 0.
   * @return a sequence of Class[B]
   */
  def findClasses[B](classPath: String, nToSearch: Int = 0): Seq[Class[B]] = {
    @tailrec
    def subLookup(n: Int, cache: Seq[Class[B]]): Seq[Class[B]] = {
      val path = classPath + (if (n == 0) "" else n.toString)
      if (n > nToSearch) cache //return cache
      else {
        val foundClasses = try {
          cache :+ Class.forName(path).asInstanceOf[Class[B]]
        } catch {
          case e @ (_: ClassNotFoundException | _: ClassCastException) => cache //not found
          case e: Throwable =>
            //logger.error("Class looking failed:" + e.getMessage)
            throw e.getCause
        }
        subLookup(n + 1, foundClasses)
      }
    }
    val nStart = 0
    val initialList = Seq().view
    subLookup(nStart, initialList).view.force
  }
}
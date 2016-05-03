package com.github.cuzfrog.utils

import com.typesafe.scalalogging.LazyLogging

private[cuzfrog] object ClassFinder extends LazyLogging {
  trait InnerClassFinder {
    /**
     * Find inner classes by class names.
     * If the class you're to look for is an inner class of another inner class, you should add "$" ahead of your class name.
     * @param className the sub class name
     * @param nToSearch the upper limit number of the classes that have the same name+n you attempt to find
     * @return a sequence of Class[B]
     */
    def findClasses[B](className: String, nToSearch: Int = 1): Seq[Class[B]] = {
      def subLookup(n: Int, cache: Seq[Class[B]]): Seq[Class[B]] = {
        val path = this.getClass.getName + className + (if (n == 0) "" else n.toString)
        try {
          return if(n < nToSearch) subLookup(n + 1, cache :+ Class.forName(path).asInstanceOf[Class[B]]) else cache
        } catch {
          case e: ClassNotFoundException =>
            if (n == 0) subLookup(n + 1, cache) else return cache
          case e: Throwable =>
            logger.error("Class looking failed:" + e.getMessage)
            throw e.getCause
        }
      }
      val nStart = 0
      val initialList = Seq().view
      subLookup(nStart, initialList).view.force
    }
  }
}
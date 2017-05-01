package com.github.cuzfrog.utils

import scala.Predef

trait PrintPrefix {
  val prefix: String = this.getClass.getName
  def printlnp(x: Any): Unit = {
    scala.Predef.println(prefix + x)
  }

  def printp(x: Any): Unit = {
    scala.Predef.print(prefix + x)
  }
}
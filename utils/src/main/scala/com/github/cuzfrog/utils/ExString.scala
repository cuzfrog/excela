package com.github.cuzfrog.utils

private[cuzfrog] object ExString {
  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  implicit class StringIsDefined(in:String){
    def isDefined:Boolean = in != null && in.nonEmpty
  }
}
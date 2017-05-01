package com.github.cuzfrog.utils

object WithNameTest extends App {
  sealed trait SourceType
  object SourceType extends ClassFinder.WithName[SourceType] {
    case object TableFile extends SourceType
    override protected val values:Set[SourceType] = Set(TableFile)
  }
  
  println(SourceType.withName("TableFile"))
}
package com.github.cuzfrog.utils

import scala.collection.immutable.ListMap

object CollectionExMapTest extends App {
  val map1 = Map("k1" -> 3.7)
  val map2:Map[String,Int] = ListMap(Seq("k2" -> 3, "k1" -> 8): _*)

  import com.github.cuzfrog.utils.Collections.ExMap

  val res=map2.leftJoin(map1)(_ + _)(i=>i)
  println(res)
  
  val res2=map2.innerJoin(map1)(_ + _)
  println(res2)
}
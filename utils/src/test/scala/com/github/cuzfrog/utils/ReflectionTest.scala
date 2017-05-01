package com.github.cuzfrog.utils


import reflect.runtime.{universe => ru}

/**
  * Created by cuz on 1/18/17.
  */
object ReflectionTest extends App {
  val m = ru.runtimeMirror(getClass.getClassLoader)
  val clazz = ru.typeOf[Person].typeSymbol.asClass
  val cm = m.reflectClass(clazz)
  val constr = ru.typeOf[Person].decl(ru.termNames.CONSTRUCTOR).asMethod
  val constrm = cm.reflectConstructor(constr)

  val p=constrm("Mike")
  println(p)

  val term=ru.typeOf[Person].decl(ru.TermName("name")).asTerm.accessed.asTerm

  val im= m.reflect(p)
  val fmm= im.reflectField(term)

  fmm.set("Jack")

  println(p)
}

case class Person(name: String)
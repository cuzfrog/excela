package com.github.cuzfrog.utils.macros

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/**
  * Copy every field of two given case classes.
  * Created by cuz on 2/4/17.
  */
private[utils] trait FullCopyTool[A, B] {
  def fullCopy(in: A, that: B): B
}

private object FullCopyTool {
  implicit def materializeFullCopyTool[A, B]: FullCopyTool[A, B] = macro MacroImpl.fullCopyImpl[A, B]
}

private object MacroImpl {
  def fullCopyImpl[A: c.WeakTypeTag, B: c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._
    val tpeA = c.weakTypeOf[A]
    val tpeB = c.weakTypeOf[B]
    val symbol = tpeB.typeSymbol
    if (!symbol.isClass || !symbol.asClass.isCaseClass) {
      c.abort(c.enclosingPosition, s"${symbol.fullName} is not a case class or has the wrong type")
    }
    val copyBody: List[c.universe.Tree] =
      tpeB.decls.filter(!_.isMethod).map(_.asTerm.getter).map(v => q"$v = that $v").toList
    val tree =
      q"""
       new FullCopyTool[$tpeA,$tpeB]{
           def fullCopy(in: $tpeA, that: $tpeB): $tpeB = {
              in.copy(..$copyBody)
           }
       }
     """
    //println(showCode(tree))
    tree
  }
}
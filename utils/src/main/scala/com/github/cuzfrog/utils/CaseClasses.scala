package com.github.cuzfrog.utils

/**
  * Provide extra method for case classes.
  */
object CaseClasses {

  type FullCopyTool[A, B] = com.github.cuzfrog.utils.macros.FullCopyTool[A, B]

  implicit class ExCaseClass[T](in: T) {
    /**
      * Copy every field of that to this case class.
      *
      * @param that the instance containing values to copy
      * @return fully copied `T`
      */
    def fullCopy[B >: T](that: B)(implicit tool: FullCopyTool[T, B]): B = tool.fullCopy(in, that)
  }

}

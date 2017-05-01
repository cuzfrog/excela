package com.github.cuzfrog.utils

private[cuzfrog] trait Timer {

  def timeMethods(nameRegex: String, time: Int = 1): Map[String, Long] = {
    this.getClass.getMethods.filter { m => m.getParameterCount == 0 && m.getName.matches(nameRegex) }
      .map {
        m =>
          val time1 = System.currentTimeMillis
          for (i <- 1 to time) m.invoke(this)
          val time2 = System.currentTimeMillis
          (m.getName -> (time2 - time1))
      }.toMap
  }
}
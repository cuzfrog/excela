package com.github.cuzfrog.utils

import java.time.LocalDate
import java.time.temporal.TemporalUnit

/**
  * Created by cuz on 2/13/17.
  */
object DateTimeTest {
  val d1 = LocalDate.of(2016, 6, 20)
  val d2 = LocalDate.of(2016, 7, 23)

  val period = d1.until(d2)

}

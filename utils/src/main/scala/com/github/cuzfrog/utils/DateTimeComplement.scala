package com.github.cuzfrog.utils

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateTimeComplement {

  implicit class ExLocalDate(in: LocalDate) {
    /**
      * Return a span of days from this date to a specified date.
      */
    def spanByDay(untilDateInclusive: LocalDate): Seq[LocalDate] = {
      val period = ChronoUnit.DAYS.between(in, untilDateInclusive)
      (0L to period).map(plusDays => in.plusDays(plusDays))
    }

    /**
      * Return a span of months from the month of this date to the month of a specified date.
      * The LocalDate standing for month has the day of 1.
      */
    def spanByMonth(untilDateInclusive: LocalDate): Seq[LocalDate] = {
      val period = in.until(untilDateInclusive).getMonths
      val beginDate = this.firstDayOfTheMonth
      (0L to period).map(plusMonths => beginDate.plusMonths(plusMonths))
    }

    /**
      * Return a span of years. Month and day of the returned LocalDate are both 1.
      */
    def spanByYear(untilDateInclusive: LocalDate): Seq[LocalDate] = {
      val period = in.until(untilDateInclusive).getYears
      val beginDate = LocalDate.of(in.getYear, 1, 1)
      (0L to period).map(plusYears => beginDate.plusYears(plusYears))
    }

    def firstDayOfTheMonth: LocalDate = {
      LocalDate.of(in.getYear, in.getMonth, 1)
    }

    def lastDayOfTheMonth: LocalDate = {
      in.plusMonths(1).firstDayOfTheMonth.minusDays(1)
    }

    def lastDayOfTheYear: LocalDate = {
      LocalDate.of(in.getYear, 12, 31)
    }

    /** Use string pattern to format this date directly. */
    def format(pattern: String): String = {
      val formatter = DateTimeFormatter.ofPattern(pattern)
      in.format(formatter)
    }
  }

  implicit class ExDate(in: java.util.Date) {
    /**
      * Convert java.util.Date to java.time.LocalDate
      */
    def toLocalDate: LocalDate = {
      in.toInstant.atZone(ZoneId.systemDefault()).toLocalDate
    }
  }

}
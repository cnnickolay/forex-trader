package org.nikosoft.oanda.bot.ml

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.functions._

object Functions {

  val timeConverterUdf = udf[LocalDateTime, String](rawTime => {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd HHmmHH")
    LocalDateTime.parse(rawTime, formatter)
  })

}

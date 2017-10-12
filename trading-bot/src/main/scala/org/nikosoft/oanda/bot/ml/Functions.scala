package org.nikosoft.oanda.bot.ml

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.execution.streaming.FileStreamSource.Timestamp
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._


object Functions {

/*
  val timeConverterUdf = udf[LocalDateTime, String](rawTime => {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    LocalDateTime.parse(rawTime, formatter)
  })
*/

  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val shrinkTimeUdf = udf[String, String, Integer]((dateTime, step) =>{
    val time = LocalDateTime.parse(dateTime, formatter)
    val stepMillis = step * 60 * 1000
    val offset = time.toInstant(ZoneOffset.UTC).toEpochMilli / stepMillis * stepMillis
    LocalDateTime.ofInstant(Instant.ofEpochMilli(offset), ZoneOffset.UTC).format(formatter)
  })

  val normalize = udf[Int, Int](value => {
    (value.toDouble / 1).toInt
  })

}

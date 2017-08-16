package org.nikosoft.oanda.bot.ml

import org.apache.spark.sql.SparkSession
import Functions._

object Entry extends App {

  val spark = SparkSession.builder().appName("MLProbe1").master("local[*]").getOrCreate()

  import spark.implicits._

  spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ";")
    .load("/Users/niko/projects/oanda-trader/data/EURUSD.csv")
    .withColumnRenamed("_c0", "rawTime")
    .withColumnRenamed("_c1", "open")
    .withColumnRenamed("_c2", "high")
    .withColumnRenamed("_c3", "low")
    .withColumnRenamed("_c4", "close")
    .drop("_c5")
    .head(20)
    .foreach(println)

}

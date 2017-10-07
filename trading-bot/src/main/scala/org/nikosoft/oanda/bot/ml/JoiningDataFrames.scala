package org.nikosoft.oanda.bot.ml

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.nikosoft.oanda.bot.ml.Functions._

object JoiningDataFrames extends App {

  val spark = SparkSession.builder().appName("MLProbe1").master("local[*]").getOrCreate()

  import spark.implicits._

  val m5DF = spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ",")
    .load("/Users/niko/projects/oanda-trader/target/eur_usd_M5.csv")
    .withColumnRenamed("_c0", "m5RawDateTime")
    .withColumnRenamed("_c1", "m5Open")
    .withColumnRenamed("_c2", "m5High")
    .withColumnRenamed("_c3", "m5Low")
    .withColumnRenamed("_c4", "m5Close")
    .withColumnRenamed("_c5", "m5Volume")

  val m10DF = spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ",")
    .load("/Users/niko/projects/oanda-trader/target/eur_usd_M10.csv")
    .withColumnRenamed("_c0", "m10RawDateTime")
    .withColumnRenamed("_c1", "m10Open")
    .withColumnRenamed("_c2", "m10High")
    .withColumnRenamed("_c3", "m10Low")
    .withColumnRenamed("_c4", "m10Close")
    .withColumnRenamed("_c5", "m10Volume")

  val h1DF = spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ",")
    .load("/Users/niko/projects/oanda-trader/target/eur_usd_H1.csv")
    .withColumnRenamed("_c0", "h1RawDateTime")
    .withColumnRenamed("_c1", "h1Open")
    .withColumnRenamed("_c2", "h1High")
    .withColumnRenamed("_c3", "h1Low")
    .withColumnRenamed("_c4", "h1Close")
    .withColumnRenamed("_c5", "h1Volume")

  val joinedDf = spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ",")
    .load("/Users/niko/projects/oanda-trader/target/eur_usd_M1.csv")
    .withColumnRenamed("_c0", "rawDateTime")
    .withColumn("m5Time", shrinkTimeUdf($"rawDateTime", lit(5)))
    .withColumn("m10Time", shrinkTimeUdf($"rawDateTime", lit(10)))
    .withColumn("h1Time", shrinkTimeUdf($"rawDateTime", lit(60)))
    .withColumnRenamed("_c1", "open")
    .withColumnRenamed("_c2", "high")
    .withColumnRenamed("_c3", "low")
    .withColumnRenamed("_c4", "close")
    .withColumnRenamed("_c5", "volume")
    .join(m5DF, $"m5RawDateTime" === $"m5Time")
    .join(m10DF, $"m10RawDateTime" === $"m10Time")
    .join(h1DF, $"h1RawDateTime" === $"h1Time")
    .drop("m5Time", "m10Time", "h1Time", "m5RawDateTime", "m10RawDateTime", "h1RawDateTime")

  joinedDf.printSchema()
  joinedDf.show(100)
}

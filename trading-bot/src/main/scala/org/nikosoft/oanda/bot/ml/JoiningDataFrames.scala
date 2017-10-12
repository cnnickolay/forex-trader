package org.nikosoft.oanda.bot.ml

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{DecimalType, DoubleType, IntegerType}
import org.nikosoft.oanda.bot.ml.Functions._

object JoiningDataFrames extends App {

  val spark = SparkSession.builder().appName("MLProbe1").master("local[*]").getOrCreate()

  import spark.implicits._

  val m5DF = spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ",")
    .load("/Users/niko/projects/oanda-trader/eur_usd_M5.csv")
    .withColumnRenamed("_c0", "m5RawDateTime")
    .withColumnRenamed("_c1", "m5Open")
    .withColumnRenamed("_c2", "m5High")
    .withColumnRenamed("_c3", "m5Low")
    .withColumnRenamed("_c4", "m5Close")
    .withColumnRenamed("_c5", "m5Volume")
    .withColumnRenamed("_c6", "m5Ema21")
    .withColumn("m5Magnitude", $"m5High" - $"m5Low")

  val m10DF = spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ",")
    .load("/Users/niko/projects/oanda-trader/eur_usd_M10.csv")
    .withColumnRenamed("_c0", "m10RawDateTime")
    .withColumnRenamed("_c1", "m10Open")
    .withColumnRenamed("_c2", "m10High")
    .withColumnRenamed("_c3", "m10Low")
    .withColumnRenamed("_c4", "m10Close")
    .withColumnRenamed("_c5", "m10Volume")
    .withColumnRenamed("_c6", "m10Ema21")
    .withColumn("m10Magnitude", $"m10High" - $"m10Low")

  val h1DF = spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ",")
    .load("/Users/niko/projects/oanda-trader/eur_usd_H1.csv")
    .withColumnRenamed("_c0", "h1RawDateTime")
    .withColumnRenamed("_c1", "h1Open")
    .withColumnRenamed("_c2", "h1High")
    .withColumnRenamed("_c3", "h1Low")
    .withColumnRenamed("_c4", "h1Close")
    .withColumnRenamed("_c5", "h1Volume")
    .withColumnRenamed("_c6", "h1Ema21")
    .withColumn("h1Close-1", lag($"h1Close", 1).over(Window.orderBy($"h1RawDateTime")))
    .withColumn("h1Close+1", lead($"h1Close", 1).over(Window.orderBy($"h1RawDateTime")))

  val joinedDf = m5DF
    .withColumn("m10Time", shrinkTimeUdf($"m5RawDateTime", lit(10)))
    .withColumn("h1Time", shrinkTimeUdf($"m5RawDateTime", lit(60)))
    .join(m10DF, $"m10RawDateTime" === $"m10Time")
    .join(h1DF, $"h1RawDateTime" === $"h1Time")
    .drop("m10Time", "h1Time", "m10RawDateTime", "h1Open", "h1High", "h1Low", "h1Volume", "h1Ema21")
    .groupBy("h1RawDateTime", "h1Close-1", "h1Close", "h1Close+1")
    .agg(
      avg($"m5Close").cast(DecimalType(10, 5)).cast(DoubleType).as("m5CloseAvg")
//      collect_list($"m5Close").as("m5CloseArray"),
//      collect_list("m5Magnitude").as("m5MagnitudeArray"),
//      collect_list("m5Ema21").as("m5Ema21Array")
    )
    .withColumn("closeDiff", ($"h1Close" * 100000 - $"h1Close-1" * 100000).cast(IntegerType))
    .withColumn("m5CloseAvgDiff", ($"h1Close" * 100000 - $"m5CloseAvg" * 100000).cast(IntegerType))
    .withColumn("futureDiff", ($"h1Close+1" * 100000 - $"h1Close" * 100000).cast(IntegerType))

  joinedDf.printSchema()
  joinedDf.show(100, truncate = false)
}

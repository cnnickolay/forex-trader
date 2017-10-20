package org.nikosoft.oanda.bot.ml

import java.sql.Timestamp

import org.apache.spark.ml.classification.{DecisionTreeClassifier, GBTClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorAssembler}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

object SimplePerceptron extends App {

  val spark = SparkSession.builder().appName("MLProbe1").master("local[1]").config("spark.default.parallelism", 8).getOrCreate()

  import spark.implicits._

  val struct = StructType(Seq(
    StructField("dateTime", TimestampType),
    StructField("open", DoubleType),
    StructField("high", DoubleType),
    StructField("low", DoubleType),
    StructField("close", DoubleType),
    StructField("volume", DoubleType),
    StructField("rsi14", DoubleType),
    StructField("sma9", DoubleType),
    StructField("ema21", DoubleType),
    StructField("ema50", DoubleType),
    StructField("macdHistogram", DoubleType)
  ))

  val pipsCoef = 100000

  val cutArrayHalfUdf = udf { (array: mutable.WrappedArray[Double]) =>
    val (first, second) = array.splitAt(array.size / 2)
    Seq(first, second)
  }

  val avgUdf = udf { (array: mutable.WrappedArray[Double]) =>
    val x: Int = if (array == null) 0 else (array.sum / array.size).toInt
    x
  }
  val maxUdf = udf { (array: mutable.WrappedArray[Double]) => array.max }
  val minUdf = udf { (array: mutable.WrappedArray[Double]) => array.min }
  val lastElementUdf = udf { (array: mutable.WrappedArray[Double]) => array.last }
  val firstElementUdf = udf { (array: mutable.WrappedArray[Double]) => array.head }
  val margin = 30
  val labelUdf = udf { (max: Double, min: Double) =>
    //    if ((max > margin) || (min < -margin)) "go" else "pass"
    if ((max > margin) && (min > -margin)) "long"
    else if ((max < margin) && (min < -margin)) "short"
    else if ((max > margin) && (min < -margin)) "any"
    else "pass"
  }
  val arrayToPipsUdf = udf { (array: mutable.WrappedArray[Double]) =>
    if (array.length < 2) null else {
      array.map(_ * pipsCoef).map(_.toInt).sliding(2).map { case prev +: next +: _ => next - prev }.toSeq.map(_.toDouble)
    }
  }
  val arrayToVectorUdf = udf { (array: mutable.WrappedArray[Double]) => if (array == null) null else Vectors.dense(array.toArray) }
  val growingValuesUdf = udf { (array: mutable.WrappedArray[Double]) => if (array.size >= 2) array.sliding(2).map { case prev +: next +: _ => next - prev }.forall(_ > 0) else false }
  val reducingValuesUdf = udf { (array: mutable.WrappedArray[Double]) => if (array.size >= 2) array.sliding(2).map { case prev +: next +: _ => next - prev }.forall(_ < 0) else false }
  val allPositivesUdf = udf { (array: mutable.WrappedArray[Double]) => array.forall(_ > 0) }
  val allNegativesUdf = udf { (array: mutable.WrappedArray[Double]) => array.forall(_ < 0) }
  val arrayMinusValueUdf = udf { (array: mutable.WrappedArray[Double], value: Double) => array.map(_ - value).map(_ * pipsCoef).map(_.toInt) }

  def prepareDataframe(path: String) = {
    val df = spark.read.format("com.databricks.spark.csv")
      .option("inferSchema", "true").option("delimiter", ",").schema(struct)
      .load(path).orderBy(asc("dateTime"))
      .withColumn("sma9MinusEma21", round(($"sma9" - $"ema21") * pipsCoef, 0))
      .withColumn("closeMinusSma9", round(($"close" - $"sma9") * pipsCoef, 0))
      .withColumn("closeMinusEma21", round(($"close" - $"ema21") * pipsCoef, 0))
      .withColumn("closeMinusEma50", round(($"close" - $"ema50") * pipsCoef, 0))
      .withColumn("ema21MinusEma50", round(($"ema21" - $"ema50") * pipsCoef, 0))
      .groupBy(window($"dateTime", "30 minutes", "5 minutes"))
      .agg(
        collect_list($"close").as("close"),
        collect_list($"volume").as("volume"),
        collect_list($"rsi14").as("rsi14"),
        collect_list($"closeMinusEma21").as("closeMinusEma21"),
        collect_list($"sma9MinusEma21").as("sma9MinusEma21"),
        collect_list($"closeMinusSma9").as("closeMinusSma9"),
        collect_list($"closeMinusEma50").as("closeMinusEma50"),
        collect_list($"ema21MinusEma50").as("ema21MinusEma50")
      )
      .withColumn("close", cutArrayHalfUdf($"close")).withColumn("closeFuture", $"close" (1)).withColumn("close", $"close" (0))
      .filter(size($"close") > 0)
      .withColumn("closeMaxDiff", ((maxUdf($"closeFuture") - lastElementUdf($"close")) * pipsCoef).cast(IntegerType))
      .withColumn("closeMinDiff", ((minUdf($"closeFuture") - lastElementUdf($"close")) * pipsCoef).cast(IntegerType))
      .withColumn("volume", cutArrayHalfUdf($"volume")(0)).withColumn("volume", avgUdf($"volume"))
      .withColumn("label", labelUdf($"closeMaxDiff", $"closeMinDiff"))
      .withColumn("closeMinusEma21", cutArrayHalfUdf($"closeMinusEma21")(0))
      .withColumn("sma9MinusEma21", cutArrayHalfUdf($"sma9MinusEma21")(0))
      .withColumn("closeMinusSma9", cutArrayHalfUdf($"closeMinusSma9")(0))
      .withColumn("closeMinusEma50", cutArrayHalfUdf($"closeMinusEma50")(0))
      .withColumn("ema21MinusEma50", cutArrayHalfUdf($"ema21MinusEma50")(0))
      .withColumn("rsi14", avgUdf($"rsi14"))
      //      .drop("close", "closeFuture")
      .na.drop()
      .filter($"volume" > 100)
      .cache()

    //    df.groupBy($"label").agg(count($"label")).show

    //    df.filter($"label" === "long").show(50, truncate = false)
    //    df.filter($"label" === "short").show(50, truncate = false)

    /*
        val featuresDf = new VectorAssembler()
          .setInputCols(Array("volume", "atr14", "macdHistogram", "closeMinusEma21"))
          .setOutputCol("features")
          .transform(df)
    */

    //    featuresDf.show(truncate = false)

    df
  }

  //  val df = prepareDataframe("/Users/niko/projects/oanda-trader/eur_usd_train_M5.csv")
  val df = spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ",").schema(struct)
    .load("/Users/niko/projects/oanda-trader/eur_usd_train_M5.csv").orderBy(asc("dateTime"))
    .withColumn("macdHistogram", round($"macdHistogram" * pipsCoef, 0).cast(IntegerType))

  var idx = 0
  val nextIdUdf = udf ((prevValue: Double, value: Double) => {
    if ((prevValue >= 0 && value < 0) || (prevValue < 0 && value >= 0)) {idx = idx + 1; idx}
    else idx
  })
  val w = Window.orderBy(asc("dateTime"))
  val commission = 15
  val stopLossPips = 50
  val takeProfitPips = 50
  val profitUdf = udf ((macdValue: Int, close: mutable.WrappedArray[Double]) => {
    def profit(i: Int) = {
      if (macdValue > 0) ((close(i) - close(1)) * pipsCoef).toInt - commission
      else if (macdValue < 0) ((close(1) - close(i)) * pipsCoef).toInt - commission
      else 0
    }

    val takeProfitAt = close.size - 1//if (close.size > 5) 5 else close.size - 1
    (1 to takeProfitAt).map(profit).foldLeft(0)((profit, next) => {
      if (profit <= -stopLossPips || profit >= takeProfitPips) profit else next
    })
  })
  private val macdTrendThreshold = 5
  val _df = df
    .withColumn("separator", nextIdUdf(lag($"macdHistogram", 1).over(w), $"macdHistogram"))
    .select("dateTime", "close", "macdHistogram", "separator")
    .groupBy("separator").agg(first("dateTime").as("dateTime"), collect_list("macdHistogram").as("macdHistogramArray"), collect_list("close").as("close"))
    .filter(size($"macdHistogramArray") > 1)
    .withColumn("macdHistogram", $"macdHistogramArray"(1))
    .withColumn("profit", profitUdf($"macdHistogram", $"close"))
    .select("dateTime", "profit", "macdHistogram", "close", "macdHistogramArray")
    .filter(($"macdHistogram" > macdTrendThreshold) || ($"macdHistogram" < -macdTrendThreshold))
    .orderBy(asc("dateTime"))

  _df.show(20000, truncate = false)

  _df.groupBy().count().show()
  _df.groupBy().sum("profit").show()

  //  df.filter(allPositivesUdf($"closeMinusEma50"))
  //  df.filter(allNegativesUdf($"closeMinusEma50"))
  //  df.filter(allPositivesUdf($"closeMinusSma9"))
  //  df.filter(allNegativesUdf($"closeMinusSma9"))
  //  df.filter(allPositivesUdf($"sma9MinusEma21"))
  //  df.filter(allNegativesUdf($"sma9MinusEma21"))
  //  df.filter(allPositivesUdf($"closeMinusEma21"))
  //  df.filter(allNegativesUdf($"closeMinusEma21"))

  /*  def trainModel: PipelineModel = Try[PipelineModel] {
      PipelineModel.load("model")
    } match {
      case Success(_model) =>
        println("Recovering saved model")
        _model
      case Failure(_) =>
        println("No saved model found, making new one")
        val df = prepareDataframe("/Users/niko/projects/oanda-trader/eur_usd_train_M5.csv")
          .select("label", "features")

        val labelIndexer = new StringIndexer()
          .setInputCol("label")
          .setOutputCol("indexedLabel")
          .fit(df)

        val gbt = new GBTClassifier()
          .setLabelCol("indexedLabel")
          .setFeaturesCol("features")
          .setMaxIter(10)
          .setMaxBins(10)

        val dt = new DecisionTreeClassifier()
          .setLabelCol("indexedLabel")
          .setFeaturesCol("features")
          .setMaxDepth(5)
          .setMaxBins(10)

        val labelConverter = new IndexToString()
          .setInputCol("prediction")
          .setOutputCol("predictedLabel")
          .setLabels(labelIndexer.labels)

        val pipeline = new Pipeline().setStages(Array(labelIndexer, dt, labelConverter))

        val model = pipeline.fit(df)
        model.save("model")
        model
    }

    val model = trainModel

    // compute accuracy on the test set
    val result = model.transform(prepareDataframe("/Users/niko/projects/oanda-trader/eur_usd_test_M5.csv")).cache()

    val statsDf = result
      //    .filter($"atr14" >= 75)
      .groupBy($"predictedLabel", $"label")
      .count()
      .persist()
    statsDf.show()
    val stats = statsDf
      .collect()
      .map(row => ((row.getAs[String](0), row.getAs[String](1)), row.getAs[Long](2)))
      .toMap

    val truePositive = stats(("short", "any")) + stats(("long", "any")) + stats(("short", "short")) + stats(("long", "long"))
    val falsePositive = stats(("short", "long")) + stats(("long", "short")) + stats(("short", "pass")) + stats(("long", "pass"))

    val truePositivesPercent: Int = ((truePositive.toDouble / (truePositive + falsePositive)) * 100).toInt
    println(s"Total true positives: $truePositive, total false positives: $falsePositive, $truePositivesPercent%")

    def evaluate(metric: String) = {
      val evaluator = new MulticlassClassificationEvaluator()
        .setLabelCol("indexedLabel")
        .setPredictionCol("prediction")
        .setMetricName(metric)
      println(s"Test set $metric = " + evaluator.evaluate(result))
    }*/

  //  evaluate("f1")
}

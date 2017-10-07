package org.nikosoft.oanda.bot.ml

import org.apache.spark.ml.classification.{GBTClassifier, MultilayerPerceptronClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{Bucketizer, StringIndexer, VectorAssembler}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.IntegerType
import org.nikosoft.oanda.bot.ml.Functions._

object SimplePerceptron extends App {

  val spark = SparkSession.builder().appName("MLProbe1").master("local[*]").getOrCreate()

  import spark.implicits._

  val windowForLag = Window.orderBy($"m5RawDateTime")

  val m5DF = spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ",")
    .load("/Users/niko/projects/oanda-trader/eur_usd_M5.csv")
    .withColumnRenamed("_c0", "m5RawDateTime")
    .dropDuplicates("m5RawDateTime")
    .withColumnRenamed("_c1", "m5Open")
    .withColumnRenamed("_c2", "m5High")
    .withColumnRenamed("_c3", "m5Low")
    .withColumnRenamed("_c4", "m5Close")
    .withColumnRenamed("_c5", "m5Volume")
    .withColumnRenamed("_c6", "cmo21Raw")
    .withColumn("close", (($"m5Close" * 100000) - 111000).cast(IntegerType))
    .drop("m5Open", "m5High", "m5Low", "m5Close", "m5Volume")

  val augmentedDf = (0 to 11).foldLeft(m5DF)((df, idx) => df.withColumn(s"$idx", normalize(lead($"close", idx + 1).over(windowForLag) - lead($"close", idx).over(windowForLag))))
    .withColumn("label", (lead($"close", 11).over(windowForLag) - lead($"close", 10).over(windowForLag) > lit(0)).cast("integer"))
    .drop("min", "close", "11")
    .sort(desc("m5RawDateTime"))
    .na.drop()

  val splits = Array(-100.0, -80, -60, -40, -20, 0, 20, 40, 60, 80, 100)
  val cmo21DF = new Bucketizer()
    .setInputCol("cmo21Raw")
    .setOutputCol("cmo21")
    .setSplits(splits)
    .transform(augmentedDf)
    .drop("cmo21Raw")

  val inputs = (9 to 10).map(_.toString).toArray
  val featuresDf = new VectorAssembler().setInputCols(inputs).setOutputCol("features").transform(cmo21DF)

  featuresDf.show(20)

  val Array(train, test) = featuresDf.randomSplit(Array(0.6, 0.4))

  val layers = Array[Int](inputs.length, inputs.length + 2, inputs.length + 2, 2)

  val perceptron = new MultilayerPerceptronClassifier()
    .setLayers(layers)
//    .setTol(1E-6)
    .setBlockSize(128)
    .setSeed(1234L)
    .setMaxIter(100)

  val gbt = new GBTClassifier().setMaxIter(10).setMaxDepth(5)

  val model = perceptron.fit(train)

  // compute accuracy on the test set
  val result = model.transform(test)
  val predictionAndLabels = result.select("prediction", "label")

  def evaluate(metric: String) = {
    val evaluator = new MulticlassClassificationEvaluator().setMetricName(metric)
    println(s"Test set $metric = " + evaluator.evaluate(predictionAndLabels))
  }

  evaluate("accuracy")
  evaluate("weightedPrecision")
  evaluate("weightedRecall")
  evaluate("f1")
}

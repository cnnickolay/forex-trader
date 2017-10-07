package org.nikosoft.oanda.bot.ml

import org.apache.spark.ml.classification.{GBTClassifier, MultilayerPerceptronClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.IntegerType

object SimplePerceptron extends App {

  val spark = SparkSession.builder().appName("MLProbe1").master("local[*]").getOrCreate()

  import spark.implicits._

  val windowForLag = Window.orderBy($"m5RawDateTime")

  val m5DF = spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ",")
    .load("/Users/niko/projects/oanda-trader/target/eur_usd_M1.csv")
    .withColumnRenamed("_c0", "m5RawDateTime")
    .dropDuplicates("m5RawDateTime")
    .withColumnRenamed("_c1", "m5Open")
    .withColumnRenamed("_c2", "m5High")
    .withColumnRenamed("_c3", "m5Low")
    .withColumnRenamed("_c4", "m5Close")
    .withColumnRenamed("_c5", "m5Volume")
    .withColumn("close", (($"m5Close" * 100000) - 111000).cast(IntegerType))
    .drop("m5Open", "m5High", "m5Low", "m5Close", "m5Volume")

  val augmentedDf = (0 to 51).foldLeft(m5DF)((df, idx) => df.withColumn(s"$idx", lead($"close", idx + 1).over(windowForLag) - lead($"close", idx).over(windowForLag)))
    .withColumn("label", ($"51" > lit(0)).cast("integer"))
    .drop("min", "close")
    .filter($"1".isNotNull)
    .filter($"50".isNotNull)
    .filter($"label".isNotNull)
    .sort(desc("m5RawDateTime"))

  val featuresDf = new VectorAssembler().setInputCols((40 to 50).map(_.toString).toArray).setOutputCol("features").transform(augmentedDf)

  augmentedDf.show(20)

  val Array(train, test) = featuresDf.randomSplit(Array(0.6, 0.4), seed = 1234L)

  val layers = Array[Int](11, 10, 10, 10, 10, 5, 2)

  val perceptron = new MultilayerPerceptronClassifier()
    .setLayers(layers)
    .setTol(1E-6)
    .setBlockSize(128)
    .setSeed(1234L)
    .setMaxIter(400)

  val gbt = new GBTClassifier().setMaxIter(10).setMaxDepth(5)

  val model = perceptron.fit(train)

  // compute accuracy on the test set
  val result = model.transform(test)
  val predictionAndLabels = result.select("prediction", "label")
  val evaluator = new MulticlassClassificationEvaluator()
    .setMetricName("accuracy")

  println("Test set accuracy = " + evaluator.evaluate(predictionAndLabels))

}

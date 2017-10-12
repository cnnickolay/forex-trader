package org.apache.spark.ml.classification.genetic

import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{desc, lead, lit, min}
import org.apache.spark.sql.types.IntegerType

object Trainer extends App {

  val spark = SparkSession.builder().appName("MLProbe1").master("local[*]").getOrCreate()

  import spark.implicits._

  val windowForLag = Window.orderBy($"m5RawDateTime")
  val window = Window.orderBy($"m5RawDateTime").rowsBetween(Window.currentRow, 10)

  val m5DF = spark.read.format("com.databricks.spark.csv")
    .option("inferSchema", "true").option("delimiter", ",")
    .load("/Users/niko/projects/oanda-trader/eur_usd_M5.csv")
    .withColumnRenamed("_c0", "m5RawDateTime")
    .dropDuplicates("m5RawDateTime")
    .withColumnRenamed("_c1", "m5Open")
    .withColumnRenamed("_c2", "m5High")
    .withColumnRenamed("_c3", "m5Low")
    //    .withColumnRenamed("_c4", "m5Close")
    .withColumnRenamed("_c5", "m5Volume")
    .withColumnRenamed("_c6", "m5Close")
    .withColumn("close", ($"m5Close" * 100000).cast(IntegerType))
    .drop("m5Open", "m5High", "m5Low", "m5Close", "m5Volume")

  val augmentedDf = (0 to 11).foldLeft(m5DF)((df, idx) => df.withColumn(s"$idx", lead($"close", idx).over(windowForLag) - min($"close").over(window)))
    .withColumn("label", (lead($"close", 12).over(windowForLag) - lead($"close", 11).over(windowForLag) > lit(0)).cast("integer"))
    .drop("min", "close")
    .sort(desc("m5RawDateTime"))
    .na.drop()

  /*
    val splits = Array(-100.0, -80, -60, -40, -20, 0, 20, 40, 60, 80, 100)
    val cmo21DF = new Bucketizer()
      .setInputCol("cmo21Raw")
      .setOutputCol("cmo21")
      .setSplits(splits)
      .transform(augmentedDf)
      .drop("cmo21Raw")
  */

  val inputs = (0 to 10).map(_.toString).toArray
  val featuresDf = new VectorAssembler().setInputCols(inputs).setOutputCol("features").transform(augmentedDf)

  featuresDf.show(20)

  val Array(train, test) = featuresDf.randomSplit(Array(0.6, 0.4))

  val layers = Array[Int](inputs.length, inputs.length + 2, inputs.length + 2, 2)

  val rnd = new java.util.Random()

  def mutateShit() = {
    val model: MultilayerPerceptronClassificationModel = new MultilayerPerceptronClassificationModel("123", layers, Vectors.dense((0 until 366).map(_ => rnd.nextDouble()).toArray))
    println(model.weights.toArray.toList)

    // compute accuracy on the test set
    val result = model.transform(test)
    val predictionAndLabels = result.select("prediction", "label")

    def evaluate(metric: String) = {
      val evaluator = new MulticlassClassificationEvaluator().setMetricName(metric)
      println(s"Test set $metric = " + evaluator.evaluate(predictionAndLabels))
    }

    evaluate("f1")
  }
  (0 to 20).foreach { _ =>
    println("---------------------")
    mutateShit()
  }
}

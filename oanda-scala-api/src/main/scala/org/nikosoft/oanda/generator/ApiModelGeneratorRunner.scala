package org.nikosoft.oanda.generator

import java.nio.file.{Files, Paths}

object ApiModelGeneratorRunner extends App {

  val modelsToParse = Seq(
    ("TransactionModel", "http://developer.oanda.com/rest-live-v20/transaction-df/"),
    ("AccountModel", "http://developer.oanda.com/rest-live-v20/account-df/"),
    ("InstrumentModel", "http://developer.oanda.com/rest-live-v20/instrument-df/"),
    ("OrderModel", "http://developer.oanda.com/rest-live-v20/order-df/"),
    ("TradeModel", "http://developer.oanda.com/rest-live-v20/trade-df/"),
    ("PositionModel", "http://developer.oanda.com/rest-live-v20/position-df/"),
    ("PricingModel", "http://developer.oanda.com/rest-live-v20/pricing-df/"),
    ("PrimitivesModel", "http://developer.oanda.com/rest-live-v20/primitives-df/")
  )

  val models = modelsToParse.map((ApiModelGenerator.generateScalaModel _).tupled)

  val enumImports = (models, modelsToParse.map(_._1)).zipped.flatMap { case (modelText, modelName) =>
    val enums = modelText.lines.filter(_.contains("Enumeration")).map(_.trim.split(" ")(1)).toList
    enums.map(enum => s"import ApiModel.$modelName.$enum.$enum")
  }

  models.map(_.lines.filter(_.contains("Enumeration")).map(_.trim.split(" ")(1)).toList)

  val modelsString: String =
    models
      .map(_.lines.map("  " + _).mkString("\n"))
      .mkString("\n\n")

  val enumerations =
    (modelsString.lines.filter(_.contains("Enumeration")).map(_.trim.split(" ")(1)).toList,
      modelsString.lines.filter(line => line.contains("Model") && !line.contains("Api")).map(_.trim.split(" ")(1)).toList)
      .zipped

  val importStatements = Seq(modelsToParse.map("import ApiModel." + _._1 + "._"), enumImports).flatten.mkString("\n")

  val wrappedModel =
    s"""$importStatements
       |
      |object ApiModel {
       |$modelsString
       |}
    """.stripMargin

  Files.write(Paths.get("src/main/scala/org/nikosoft/oanda/api/model/ApiModel.scala"), wrappedModel.getBytes("utf-8"))

}

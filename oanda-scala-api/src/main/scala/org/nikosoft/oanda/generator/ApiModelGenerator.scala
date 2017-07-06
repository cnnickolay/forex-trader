package org.nikosoft.oanda.generator

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.{HtmlDivision, HtmlElement, HtmlPage, HtmlSpan}
import org.nikosoft.oanda.generator.ApiModelGeneratorParsers.ParameterTable

import scala.collection.JavaConverters._

object ApiModelGenerator {

  private val predefinedClasses = Map(
    "integer" -> "Int",
    "string" -> "String",
    "DecimalNumber" -> "Double",
    "boolean" -> "Boolean"
  )

  private def normalizeValName: (String) => String = {
    case "type" => "`type`"
    case other => other.replaceAll("-", "_")
  }

  def generateScalaModel(objectName: String, url: String): String = {
    val client = new WebClient()
    client.getOptions.setJavaScriptEnabled(false)
    val htmlPage = client.getPage[HtmlPage](url)

    val apiSection = htmlPage.getElementById("content-api-section")

    val endpointHeaders = apiSection.getByXPath[HtmlDivision](".//div[@class='endpoint_header']").asScala.toList
    val definitionBody = apiSection.getByXPath[HtmlDivision](".//div[contains(@class, 'definition_body')]").asScala.toList

    val caseClasses = (endpointHeaders, definitionBody).zipped.map { case (headerElt, definitionElt) =>
      val title = headerElt.getFirstByXPath[HtmlSpan](".//span[@class='method']").asText()
      val definition = headerElt.getFirstByXPath[HtmlSpan](".//span[@class='definition']").asText()

      val parameterTableOption = Option(definitionElt.getFirstByXPath[HtmlElement]("./table[@class='parameter_table']"))
        .map(elt => generateCaseClass(title, definition, ApiModelGeneratorParsers.parseParameterTable(elt.asXml())))
        .map(text => text.lines.map(line => s"  $line").mkString("\n"))

      val generatedJsonSchema = Option(definitionElt.getFirstByXPath[HtmlElement]("./pre[@class='json_schema']"))
        .map(elt => {
          val source = elt.asXml()
          parseModelFromJson(title, definition, source)
        })
      parameterTableOption.getOrElse(generatedJsonSchema.getOrElse(""))
    }
      .filter(_.nonEmpty)
      .mkString("\n")

    s"""object $objectName {
        |$caseClasses
        |}
     """.stripMargin
  }

  private[generator] def parseModelFromJson(title: String, definition: String, source: String): String = {
    val jsonModel = ApiModelGeneratorParsers.parseJsonElementField(source)
    val gen =
      s"""
         |  /**
         |   * $definition
         |   */
         |  case class $title(
         |""".stripMargin

    val content = jsonModel.map(jsonField => {
      val _type = predefinedClasses.getOrElse(jsonField.`type`, jsonField.`type`)
      val defaultValue = jsonField.default.fold("")(value => if (jsonField.`type` == "string") s""" = "$value"""" else s" = $value")
      val finalType = if (jsonField.array) s"Seq[${_type}]" else _type
      s"    /** ${jsonField.description} */\n    ${normalizeValName(jsonField.name)}: $finalType$defaultValue"
    })
      .mkString(",\n")

    s"$gen$content\n  )"
  }

  private[generator] def generateCaseClass(typeValue: String, description: String, parameterTable: ParameterTable): String = parameterTable.headerLeft match {
    case "Value" =>
      val enumParams = parameterTable.values.map {case (title, desc) =>
        s"""  /** $desc */
           |  val ${normalizeValName(title)} = Value""".stripMargin
      }.mkString("\n\n")

      s"""/**
         | * $description
         | */
         |object $typeValue extends Enumeration {
         |  type $typeValue = Value
         |
         |$enumParams
         |}""".stripMargin
    case "Type" =>
      val exampleValue = parameterTable.values.get("Example").fold("")(example => s"\n * Example: $example")
      val formatValue = parameterTable.values.get("Format").fold("")(format => s"\n * Format: $format")
      s"""/**
         | * $description$formatValue$exampleValue
         | */
         |case class $typeValue(value: ${predefinedClasses(parameterTable.headerRight)}) extends AnyVal""".stripMargin
    case unknown => throw new RuntimeException(
      s"""Unknown basic type $unknown
         |ParameterTable: $parameterTable
       """.stripMargin)
  }

}
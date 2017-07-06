package org.nikosoft.oanda.generator

import org.nikosoft.oanda.generator.ApiModelGeneratorParsers.ParameterTable
import org.scalatest.{FunSuite, Matchers}

class ApiModelGeneratorUnitTest extends FunSuite with Matchers {

  test("generate enumeration") {
    val inputTitle = "TradeState"
    val inputDefinition = "The current state of the Trade."
    val inputParameters = ParameterTable("Value", "Description", Map(
      "OPEN" -> "The Trade is currently open",
      "CLOSED" -> "The Trade has been fully closed",
      "CLOSE_WHEN_TRADEABLE" -> "The Trade will be closed as soon as the trade’s instrument becomes tradeable"
    ))
    val expectedOutput =
      """/**
        | * The current state of the Trade.
        | */
        |object TradeState extends Enumeration {
        |  type TradeState = Value
        |
        |  /** The Trade is currently open */
        |  val OPEN = Value
        |
        |  /** The Trade has been fully closed */
        |  val CLOSED = Value
        |
        |  /** The Trade will be closed as soon as the trade’s instrument becomes tradeable */
        |  val CLOSE_WHEN_TRADEABLE = Value
        |}""".stripMargin

    val actualOutput = ApiModelGenerator.generateCaseClass(inputTitle, inputDefinition, inputParameters)
    actualOutput shouldBe expectedOutput
  }

  test("generate anyval type") {
    val inputTitle = "TradeSpecifier"
    val inputDefinition = "The identification of a Trade as referred to by clients"
    val inputParameters = ParameterTable("Type", "string", Map(
      "Format" -> "Either the Trade’s OANDA-assigned TradeID or the Trade’s client-provided ClientID prefixed by the “@” symbol",
      "Example" -> "@my_trade_id"
    ))
    val expectedOutput =
      """/**
         | * The identification of a Trade as referred to by clients
         | * Format: Either the Trade’s OANDA-assigned TradeID or the Trade’s client-provided ClientID prefixed by the “@” symbol
         | * Example: @my_trade_id
         | */
         |case class TradeSpecifier(value: String) extends AnyVal""".stripMargin

    val actualOutput = ApiModelGenerator.generateCaseClass(inputTitle, inputDefinition, inputParameters)
    actualOutput shouldBe expectedOutput
  }

}

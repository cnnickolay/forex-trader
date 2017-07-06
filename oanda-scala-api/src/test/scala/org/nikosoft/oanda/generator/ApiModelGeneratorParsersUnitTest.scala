package org.nikosoft.oanda.generator

import org.nikosoft.oanda.generator.ApiModelGeneratorParsers.ParameterTable
import org.scalatest.{FunSuite, Matchers}

class ApiModelGeneratorParsersUnitTest extends FunSuite with Matchers {

  test("convert large (enum) field to ParameterTable") {
    val input = """
        |<table class="parameter_table">
        |  <thead>
        |    <tr>
        |      <th class="pt_25">
        |        Value
        |      </th>
        |      <th class="pt_75">
        |        Description
        |      </th>
        |    </tr>
        |  </thead>
        |  <tbody>
        |    <tr>
        |      <td>
        |        INTERNAL_SERVER_ERROR
        |      </td>
        |      <td>
        |        An unexpected internal server error has occurred
        |      </td>
        |    </tr>
        |    <tr>
        |      <td>
        |        INSTRUMENT_PRICE_UNKNOWN
        |      </td>
        |      <td>
        |        The system was unable to determine the current price for the Order’s instrument
        |      </td>
        |    </tr>
        |    <tr>
        |      <td>
        |        REPLACING_TRADE_ID_INVALID
        |      </td>
        |      <td>
        |        The replacing Order refers to a different Trade than the Order that is being replaced.
        |      </td>
        |    </tr>
        |  </tbody>
        |</table>
      """.stripMargin

    val parameterTable = ApiModelGeneratorParsers.parseParameterTable(input)
    val expectedParameterTable = ParameterTable("Value", "Description",
      Map(
        "INTERNAL_SERVER_ERROR" -> "An unexpected internal server error has occurred",
        "INSTRUMENT_PRICE_UNKNOWN" -> "The system was unable to determine the current price for the Order’s instrument",
        "REPLACING_TRADE_ID_INVALID" -> "The replacing Order refers to a different Trade than the Order that is being replaced."
      )
    )

    parameterTable shouldBe expectedParameterTable
  }

  test("convert simple field to ParameterTable") {
    val input = """
        |<table class="parameter_table">
        |  <tbody>
        |    <tr>
        |      <th class="pt_15">
        |        Type
        |      </th>
        |      <td class="pt_85">
        |        string
        |      </td>
        |    </tr>
        |    <tr>
        |      <th>
        |        Format
        |      </th>
        |      <td>
        |        String representation of the numerical OANDA-assigned TransactionID
        |      </td>
        |    </tr>
        |    <tr>
        |      <th>
        |        Example
        |      </th>
        |      <td>
        |        1523
        |      </td>
        |    </tr>
        |  </tbody>
        |</table>
      """.stripMargin

    val parameterTable = ApiModelGeneratorParsers.parseParameterTable(input)
    val expectedParameterTable = ParameterTable("Type", "string",
      Map(
        "Format" -> "String representation of the numerical OANDA-assigned TransactionID",
        "Example" -> "1523"
      )
    )

    parameterTable shouldBe expectedParameterTable
  }

}

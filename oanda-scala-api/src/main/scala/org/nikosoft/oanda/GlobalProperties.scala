package org.nikosoft.oanda

import java.io.FileReader
import java.util.Properties

object GlobalProperties {

  private lazy val properties = {
    val _props = new Properties()
    _props.load(new FileReader("oanda-scala-api/src/main/resources/api.properties"))
    _props
  }

  lazy val OandaToken: String = properties.getProperty("token")

  lazy val TradingAccountId: String = properties.getProperty("trading_account_id")

}

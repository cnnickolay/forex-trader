package org.nikosoft.oanda.api

object Errors {
  abstract class Error(errorMessage: String)

  case class ApiErrorResponse(code: Int = -1, errorMessage: String) extends Error(errorMessage)
  case class InternalError(errorMessage: String) extends Error(errorMessage)
}

package org.nikosoft.oanda.api

object Errors {
  abstract class Error(errorMessage: String)

  case class ApiErrorResponse(errorCode: String, errorMessage: String) extends Error(errorMessage)
  case class InternalError(errorMessage: String) extends Error(errorMessage)
}

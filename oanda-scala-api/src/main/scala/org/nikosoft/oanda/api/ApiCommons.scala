package org.nikosoft.oanda.api

import java.util.Properties

import org.apache.http.NoHttpResponseException
import org.json4s.MappingException
import org.json4s.native.Serialization._
import org.nikosoft.oanda.api.Errors.{ApiErrorResponse, Error, InternalError}

import scala.util.{Failure, Success, Try}
import scalaz.Scalaz._
import scalaz.\/

/**
  * Created by Nikolai Cherkezishvili on 21/06/2017
  */
trait ApiCommons {

  implicit val formats = JsonSerializers.formats

  protected val baseUrl = s"https://api-fxtrade.oanda.com/v3"
  protected val streamUrl = s"https://stream-fxtrade.oanda.com/v3"

  private lazy val props: Properties = {
    val properties = new Properties()
    properties.load(getClass.getResourceAsStream("/api.properties"))
    properties
  }

  protected val token = s"Bearer ${props.getProperty("token")}"

  protected def handleRequest[T](content: String)(implicit m: Manifest[T]): \/[Error, T] = Try(read[T](content)) match {
    case Success(accountResponse) => accountResponse.right
    case Failure(t: MappingException) => t.printStackTrace(); InternalError(t.getMessage).left
    case Failure(t: NoHttpResponseException) => t.printStackTrace(); InternalError(t.getMessage).left
    case Failure(_) => read[ApiErrorResponse](content).left
  }

}

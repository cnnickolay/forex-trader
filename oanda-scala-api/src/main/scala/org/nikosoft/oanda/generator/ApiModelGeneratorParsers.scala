package org.nikosoft.oanda.generator

object ApiModelGeneratorParsers {
  case class JsonObjectField(description: String = "", name: String = "", `type`: String = "", array: Boolean = false, default: Option[String] = None)
  case class ParameterTable(headerLeft: String = "", headerRight: String = "", values: Map[String, String] = Map.empty)

  def parseJsonElementField(jsonElement: String): Seq[JsonObjectField] = {
    val simpleTypeWithDefaultRegex = """^(\w+) : \((\w+), default=(\w+)\),?$""".r
    val simpleTypeRegex = """^(\w+) : \((\w+)\),?$""".r
    val arrayComplexTypeRegex = """^(\w+) : \(Array.*>(\w+)<.*\),?$""".r
    val arraySimpleTypeRegex = """^(\w+) : \(Array\[(\w+)\].*\),?$""".r
    val complexTypeRegex = """^(\w+) : \(.*>(\w+)<.*\),?$""".r

//    println(jsonElement)

    def augmentField: (String, JsonObjectField) => JsonObjectField = {
      case (line, field) if line == "#" => field
      case (line, field) if line.startsWith("#") => field.copy(description = (field.description + " " + line.drop(2)).trim)
      case (simpleTypeRegex(fieldName, fieldType), field) => field.copy(name = fieldName, `type` = fieldType)
      case (simpleTypeWithDefaultRegex(fieldName, fieldType, defaultValue), field) => field.copy(name = fieldName, `type` = fieldType, default = Option(defaultValue))
      case (arrayComplexTypeRegex(fieldName, fieldType), field) => field.copy(name = fieldName, `type` = fieldType, array = true)
      case (arraySimpleTypeRegex(fieldName, fieldType), field) => field.copy(name = fieldName, `type` = fieldType, array = true)
      case (complexTypeRegex(fieldName, fieldType), field) => field.copy(name = fieldName, `type` = fieldType)
      case (_, field) => field
    }

    def initializeField: (Seq[JsonObjectField], String) => Seq[JsonObjectField] = {
      case (Nil, line) => Seq(augmentField(line, JsonObjectField()))
      case (fields@head +: _, line) if line.startsWith("#") && !head.name.isEmpty => augmentField(line, JsonObjectField()) +: fields
      case (fields@head +: tail, line) => augmentField(line, head) +: tail
    }

    def normalizeLines: (Seq[String], String) => Seq[String] = {
      case (_normalized, "#") => _normalized
      case (head +: tail, line) if head.startsWith("#") && line.startsWith("#") => (head + line.drop(1)) +: tail
      case (head +: tail, line) if line.endsWith(">") || head.endsWith(">") => (head + line) +: tail
      case (_normalized, line) => line +: _normalized
    }

    val normalizedLines = jsonElement.lines.toList
      .map(_.trim)
      .filterNot { line =>
        line.isEmpty || line.startsWith("{") || line.startsWith("}")
      }.init.tail
      .foldLeft(Seq.empty[String])(normalizeLines)
      .reverse

    val lines = normalizedLines
      .foldLeft(Seq.empty[JsonObjectField])(initializeField)
      .reverse

    lines
  }

  def parseParameterTable(text: String): ParameterTable = {
    val grouped = text.lines.toList
      .map(_.trim)
      .filterNot(line => line.startsWith("<") || line.isEmpty)
      .grouped(2)
      .map{case List(first, second) => (first, second)}
      .toList

    grouped.foldLeft(ParameterTable()) {
      case (parameterTable, (left, right)) if parameterTable.headerLeft.isEmpty => parameterTable.copy(headerLeft = left, headerRight = right)
      case (parameterTable, (left, right)) => parameterTable.copy(values = parameterTable.values + (left -> right))
    }
  }
}
/*
 * scala-exercises - exercises-doobie
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package doobie

import org.scalaexercises.definitions.{Library, Section}

/** doobie is a pure functional JDBC layer for Scala.
 *
 * @param name doobie
 */
object DoobieLibrary extends Library {
  override def owner: String      = "scala-exercises"
  override def repository: String = "exercises-doobie"

  override def color = Some("#E35E31")

  override def sections: List[Section] = List(
    ConnectingToDatabaseSection,
    SelectingDataSection,
    MultiColumnQueriesSection,
    ParameterizedQueriesSection,
    UpdatesSection,
    ErrorHandlingSection
  )

  override def logoPath = "doobie"
}

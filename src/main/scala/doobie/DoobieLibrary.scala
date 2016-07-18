package doobie

import org.scalaexercises.definitions.{Library, Section}

/** doobie is a pure functional JDBC layer for Scala.
  *
  * @param name doobie
  */

object DoobieLibrary extends Library {
  override def owner: String = "scala-exercises"
  override def repository: String = "exercises-doobie"

  override def color = Some("#5B5988")

  override def sections: List[Section] = List(
    ConnectingToDatabaseSection,
    SelectingDataSection,
    MultiColumnQueriesSection
  )
}

/*
 * scala-exercises - exercises-doobie
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package doobie

import doobie.enum.sqlstate.SqlState
import doobie.imports._

object ErrorHandlingSectionHelpers {

  val FOREIGN_KEY_VIOLATION = SqlState("23503")
  val UNIQUE_VIOLATION      = SqlState("23505")

  case class PersonInfo(name: String, age: Option[Int])

  def insert(s: String, a: Option[Int]): ConnectionIO[Long] =
    sql"insert into person (name, age) values ($s, $a)".update
      .withUniqueGeneratedKeys("id")

  def findPersonById(id: Long): ConnectionIO[PersonInfo] =
    sql"select name, age from person where id=$id"
      .query[PersonInfo]
      .unique
}

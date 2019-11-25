/*
 *  scala-exercises - exercises-doobie
 *  Copyright (C) 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
 *
 */

package doobie_lib

import doobie._
import doobie.implicits._

object UpdatesSectionHelpers {

  case class Person(id: Long, name: String, age: Option[Short])

  def insert1(name: String, age: Option[Int]): Update0 =
    sql"""
        INSERT INTO person (name, age) VALUES ($name, $age)
    """.update
}

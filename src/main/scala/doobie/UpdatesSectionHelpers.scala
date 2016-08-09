package doobie

import doobie.imports._
import doobie.free.{ drivermanager => FD }

import java.sql.Connection

import scalaz.concurrent.Task
import scalaz._, Scalaz._

object UpdatesSectionHelpers {

  case class Person(id: Long, name: String, age: Option[Short])

  def insert1(name: String, age: Option[Int]): Update0 =
    sql"""
        INSERT INTO person (name, age) VALUES ($name, $age)
    """.update
}

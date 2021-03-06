/*
 * Copyright 2016-2020 47 Degrees Open Source <https://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package doobielib

import doobie._
import doobie.implicits._

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

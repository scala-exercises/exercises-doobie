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

import cats.implicits._
import DoobieUtils.PersonTable._
import UpdatesSectionHelpers._
import doobie._
import doobie.implicits._
import UpdatesSectionHelpers.Person
import org.scalaexercises.definitions.Section
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * In this section we examine operations that modify data in the database, and ways to retrieve the
 * results of these updates.
 *
 * ==Data Definition==
 * It is uncommon to define database structures at runtime, but '''doobie''' handles it just fine
 * and treats such operations like any other kind of update. And it happens to be useful here!
 *
 * Let’s create a new table, which we will use for the exercises to follow. This looks a lot like
 * our prior usage of the `sql` interpolator, but this time we’re using `update` rather than
 * `query`. The `.run` method gives a `ConnectionIO[Int]` that yields the total number of rows
 * modified.
 *
 * {{{
 * val drop: Update0 =
 * sql"""
 *   DROP TABLE IF EXISTS person
 * """.update
 *
 * val create: Update0 =
 * sql"""
 *   CREATE TABLE person (
 *     id   SERIAL,
 *     name VARCHAR NOT NULL UNIQUE,
 *     age  SMALLINT
 *   )
 * """.update
 * }}}
 *
 * We can compose these and run them together.
 * {{{
 *   (drop, create).mapN(_ + _).transact(xa).unsafeRunSync
 *     // res0: Int = 0
 * }}}
 *
 * ==Inserting==
 * Inserting is straightforward and works just as with selects. Here we define a method that
 * constructs an `Update0` that inserts a row into the `person` table.
 *
 * {{{
 *   def insert1(name: String, age: Option[Short]): Update0 =
 *     sql"insert into person (name, age) values ($name, $age)".update
 * }}}
 *
 * @param name
 *   inserting_and_updating
 */
object UpdatesSection extends AnyFlatSpec with Matchers with Section {

  /**
   * Let's insert a new row by using the recently defined `insert1` method.
   *
   * To make simpler the code we built a method which prepares the database, makes the query and
   * transacts it all:
   *
   * {{{
   * def transactorBlock[A](f: => ConnectionIO[A]): IO[A] =
   *     transactor.use((createPersonTable *> f).transact[IO])
   * }}}
   */
  def insertOneRow(res0: Int) = {

    val insertedRows = transactorBlock(insert1("John", Option(35)).run).unsafeRunSync()

    insertedRows should be(res0)
  }

  /**
   * On the contrary, if we want to insert several rows, there are different ways to do that. A
   * first try could be to use a `for-comprehension` to compose all the single operations.
   */
  def insertSeveralRowsWithForComprehension(res0: Int) = {

    val rows = for {
      row1 <- insert1("Alice", Option(12)).run
      row2 <- insert1("Bob", None).run
      row3 <- insert1("John", Option(17)).run
    } yield row1 + row2 + row3

    val insertedRows = transactorBlock(rows).unsafeRunSync()

    insertedRows should be(res0)
  }

  /**
   * If there is no dependency between the SQL operations, it could be better to use an applicative
   * functor.
   */
  def insertSeveralRowsWithApplicativeFunctor(res0: Int) = {

    val insertedOnePerson = insert1("Alice", Option(12)).run

    val insertedOtherPerson = insert1("Bob", None).run

    val insertedRows =
      transactorBlock((insertedOnePerson, insertedOtherPerson).mapN(_ + _)).unsafeRunSync()

    insertedRows should be(res0)
  }

  /**
   * If all the data to be inserted is represented by a `List`, other way could be by using the
   * Scalaz `traverse` method.
   */
  def insertSeveralRowsWithTraverse(res0: Int) = {

    val people =
      List(("Alice", Option(12)), ("Bob", None), ("John", Option(17)), ("Mary", Option(16)))

    val insertedRows =
      transactorBlock(people.traverse(item => (insert1 _).tupled(item).run)).unsafeRunSync()

    insertedRows.sum should be(res0)
  }

  /**
   * ==Updating==
   * Updating follows the same pattern. For instance, we suppose that we want to modify the age of a
   * person.
   */
  def updateExistingRow(res0: Int, res1: Int, res2: Int) = {

    val result = for {
      insertedRows <- insert1("Alice", Option(12)).run
      updatedRows  <- sql"update person set age = 15 where name = 'Alice'".update.run
      person <- sql"select id, name, age from person where name = 'Alice'".query[Person].unique
    } yield (insertedRows, updatedRows, person)

    val (insertedRows, updatedRows, person) = transactorBlock(result).unsafeRunSync()

    insertedRows should be(res0)
    updatedRows should be(res1)
    person.age should be(Option(res2))
  }

  /**
   * ==Retrieving info==
   * When we insert we usually want the new row back, so let’s do that. First we’ll do it the hard
   * way, by inserting, getting the last used key via `lastVal()`, then selecting the indicated row.
   * {{{
   *   def insert2(name: String, age: Option[Short]): ConnectionIO[Person] =
   *   for {
   *   _  <- sql"insert into person (name, age) values ($name, $age)".update.run
   *   id <- sql"select lastval()".query[Long].unique
   *   p  <- sql"select id, name, age from person where id = $id".query[Person].unique
   *   } yield p
   * }}}
   *
   * This is irritating but it is supported by all databases (although the “get the last used id”
   * function will vary by vendor).
   *
   * Some database (like H2) allow you to return [only] the inserted id, allowing the above
   * operation to be reduced to two statements (see below for an explanation of
   * `withUniqueGeneratedKeys`).
   */
  def retrieveInfo(res0: String, res1: Int) = {

    def insert2_H2(name: String, age: Option[Int]): ConnectionIO[Person] =
      for {
        id <-
          sql"insert into person (name, age) values ($name, $age)".update
            .withUniqueGeneratedKeys[Int]("id")
        p <- sql"select id, name, age from person where id = $id".query[Person].unique
      } yield p

    val person = transactorBlock(insert2_H2("Ramone", Option(42))).unsafeRunSync()

    person.name should be(res0)
    person.age should be(Option(res1))
  }

  /**
   * Other databases (including PostgreSQL) provide a way to do this in one shot by returning
   * multiple specified columns from the inserted row.
   *
   * {{{
   *   def insert3(name: String, age: Option[Int]): ConnectionIO[Person] = {
   *   sql"insert into person (name, age) values ($name, $age)"
   *   .update.withUniqueGeneratedKeys("id", "name", "age")
   *   }
   * }}}
   *
   * The `withUniqueGeneratedKeys` specifies that we expect exactly one row back (otherwise an
   * exception will be thrown), and requires a list of columns to return.
   *
   * This mechanism also works for updates, for databases that support it. In the case of multiple
   * row updates we use `withGeneratedKeys` and get a `Stream[ConnectionIO, Person]` back.
   *
   * {{{
   *   val up = {
   *   sql"update person set age = age + 1 where age is not null"
   *     .update
   *     .withGeneratedKeys[Person]("id", "name", "age")
   *   }
   * }}}
   *
   * ==Batch Updates==
   * '''doobie''' supports batch updating via the `updateMany` and `updateManyWithGeneratedKeys`
   * operations on the `Update` data type. An `Update0`, which is the type of an sql"...".update
   * expression, represents a parameterized statement where the arguments are known. An `Update[A]`
   * is more general, and represents a parameterized statement where the composite argument of type
   * `A` is not known.
   */
  def batchUpdates(res0: Int) = {
    type PersonInfo = (String, Option[Short])

    def insertMany(ps: List[PersonInfo]): ConnectionIO[Int] = {
      val sql = "insert into person (name, age) values (?, ?)"
      Update[PersonInfo](sql).updateMany(ps)
    }

    // Some rows to insert
    val data = List[PersonInfo](("Frank", Some(12)), ("Daddy", None))

    val insertedRows = transactorBlock(insertMany(data)).unsafeRunSync()

    insertedRows should be(res0)
  }

}

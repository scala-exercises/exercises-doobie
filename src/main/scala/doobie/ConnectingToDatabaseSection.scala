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

import cats.effect.IO
import cats.implicits._
import DoobieUtils._
import doobie.implicits._
import doobie._
import org.scalaexercises.definitions.Section
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

/**
 * ==Introduction==
 * doobie is a monadic API that provides a number of data types that all work the same way but
 * describe computations in different contexts.
 *
 * In the doobie high level API the most common types we will deal with have the form
 * ConnectionIO[A], specifying computations that take place in a context where a java.sql.Connection
 * is available, ultimately producing a value of type A.
 *
 * doobie programs are values. You can compose small programs to build larger programs. Once you
 * have constructed a program you wish to run, you interpret it into an effectful target monad of
 * your choice (Task or IO for example) and drop it into your main application wherever you like.
 *
 * ==First programs==
 *
 * Before we can use doobie we need to import some symbols. We will use package imports here as a
 * convenience; this will give us the most commonly-used symbols when working with the high-level
 * API.
 *
 * {{{
 * import doobie._
 * import doobie.implicits._
 * }}}
 *
 * Let’s also bring in Cats.
 *
 * {{{
 * import cats._
 * import cats.effect._
 * import cats.implicits._
 * }}}
 *
 * In the doobie high level API the most common types we will deal with have the form
 * `ConnectionIO[A]`, specifying computations that take place in a context where a
 * `java.sql.Connection` is available, ultimately producing a value of type `A`.
 *
 * So let’s start with a `ConnectionIO` program that simply returns a constant.
 *
 * {{{
 * val program1 = 42.pure[ConnectionIO]
 * // program1: ConnectionIO[Int] = Pure(42)
 * }}}
 *
 * This is a perfectly respectable doobie program, but we can’t run it as-is; we need a `Connection`
 * first. There are several ways to do this, but here let’s use a `Transactor`.
 *
 * '''Note''': DriverManagerTransactors have the advantage of no connection pooling and
 * configuration, so are perfect for testing. The main disadvantage is that it is slower than
 * pooling connection managers, no provides upper bound for concurrent connections and executes
 * blocking operations in an unbounded pool of threads. The `doobie-hikari` add-on provides a
 * `Transactor` implementation backed by a HikariCP connection pool. The connection pool is a
 * lifetime-managed object that must be shut down cleanly, so it is managed as a Resource.
 *
 * {{{
 * import doobie.hikari._
 *
 * // Resource yielding a transactor configured with a bounded connect EC and an unbounded
 * // transaction EC. Everything will be closed and shut down cleanly after use.
 * val transactor: Resource[IO, HikariTransactor[IO]] =
 *   for {
 *     ce <- ExecutionContexts.fixedThreadPool[IO] (32) // our connect EC
 *     be <- Blocker[IO] // our blocking EC
 *     xa <- HikariTransactor.newHikariTransactor[IO] (
 *       "org.h2.Driver", // driver classname
 *       "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", // connect URL
 *       "sa", // username
 *       "", // password
 *       ce, // await connection here
 *       be // execute JDBC operations here
 *     )
 *   } yield xa
 * }}}
 *
 * A `Transactor` is a data type that knows how to connect to a database, hand out connections, and
 * clean them up; and with this knowledge it can transform `ConnectionIO ~> IO`, which gives us a
 * program we can run. Specifically it gives us an `IO` that, when run, will connect to the database
 * and execute single transaction.
 *
 * We are using `cats.effect.IO` as our final effect type, but you can use any monad `M[_]` given
 * `cats.effect.Async[M]`. See Using Your Own Target Monad at the end of this chapter for more
 * details.
 *
 * @param name
 *   connecting_to_database
 */
object ConnectingToDatabaseSection extends AnyFlatSpec with Matchers with Section {

  /**
   * And here we go.
   */
  def constantValue(res0: Int) =
    transactor.use(42.pure[ConnectionIO].transact[IO]).unsafeRunSync() should be(res0)

  /**
   * We have computed a constant. It’s not very interesting because we never ask the database to
   * perform any work, but it’s a first step
   *
   * We are gonna connect to a database to compute a constant. Let’s use the sql string interpolator
   * to construct a query that asks the database to compute a constant. The meaning of this program
   * is “run the query, interpret the resultset as a stream of Int values, and yield its one and
   * only element.”
   */
  def constantValueFromDatabase(res0: Int) =
    transactor.use(sql"select 42".query[Int].unique.transact[IO]).unsafeRunSync() should be(res0)

  /**
   * What if we want to do more than one thing in a transaction? Easy! `ConnectionIO` is a monad, so
   * we can use a for comprehension to compose two smaller programs into one larger program.
   */
  def combineTwoPrograms(res0: (Int, Int)) = {
    val largerProgram = for {
      a <- sql"select 42".query[Int].unique
      b <- sql"select power(5, 2)".query[Int].unique
    } yield (a, b)

    transactor.use(largerProgram.transact[IO]).unsafeRunSync() should be(res0)
  }

  /**
   * The astute among you will note that we don’t actually need a monad to do this; an applicative
   * functor is all we need here. So we could also write the above program as:
   */
  def combineTwoProgramsWithApplicative(res0: Int) = {
    val oneProgram     = sql"select 42".query[Int].unique
    val anotherProgram = sql"select power(5, 2)".query[Int].unique

    transactor.use((oneProgram, anotherProgram).mapN(_ + _).transact[IO]).unsafeRunSync() should be(
      res0
    )
  }
}

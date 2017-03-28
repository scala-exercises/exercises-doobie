/*
 * scala-exercises - exercises-doobie
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package doobie

import doobie.DoobieUtils.PersonTable._
import doobie.ErrorHandlingSectionHelpers._
import doobie.imports._
import org.scalaexercises.definitions.Section
import org.scalatest.{FlatSpec, Matchers}

import scalaz._

/**
 * ==About Exceptions==
 * '''doobie''' allows exceptions to propagate and escape unless they are handled explicitly
 * (exactly as `IO` and `Task` work). This means when a '''doobie''' action (transformed to some
 * target monad) is executed, exceptions can escape.
 *
 * ==The Catchable Typeclass and Derived Combinators==
 * All '''doobie''' monads have associated instances of the `scalaz.Catchable` typeclass, and the
 * provided interpreter requires all target monads to have an instance as well. `Catchable`
 * provides two operations:
 *
 *  - `attempt` converts `M[A]` into `M[Throwable \/ A]`
 *  - `fail` constructs an `M[A]` that fails with a provided `Throwable`
 *
 * So any '''doobie''' program can be lifted into a disjunction simply by adding `.attempt`.
 *
 * {{{
 *   scala> val p = 42.point[ConnectionIO]
 *   p: doobie.imports.ConnectionIO[Int] = Return(42)
 *
 *   scala> p.attempt
 *   res2: doobie.imports.ConnectionIO[scalaz.\/[Throwable,Int]] = Suspend(Attempt(Return(42)))
 * }}}
 *
 * From the `.attempt` combinator we derive the following, available as combinators and as syntax:
 *
 *  - `attemptSome` allows you to catch only specified `Throwable`s.
 *  - `except` recovers with a new action.
 *  - `exceptSome` same, but only for specified `Throwable`s.
 *  - `onException` executes an action on failure, discarding its result.
 *  - `ensuring` executes an action in all cases, generalizing `finally`.
 *
 * From these we can derive combinators that only pay attention to `SQLException`:
 *
 *  - `attemptSql` is like `attempt` but only traps `SQLException`.
 *  - `attemptSomeSql` traps only specified `SQLException`s.
 *  - `exceptSql` recovers from a SQLException with a new action.
 *  - `onSqlException` executes an action on `SQLException` and discards its result.
 *
 * And finally we have a set of combinators that focus on SQLStates.
 *
 *  - `attemptSqlState` is like `attemptSql` but yields `M[SQLState \/ A]`.
 *  - `attemptSomeSqlState` traps only specified `SQLState`s.
 *  - `exceptSqlState` recovers from a `SQLState` with a new action.
 *  - `exceptSomeSqlState` recovers from specified `SQLState`s with a new action.
 *
 * @param name error_handling
 */
object ErrorHandlingSection extends FlatSpec with Matchers with Section {

  /**
   * Let's do some exercises where errors will happen and see how to deal with them.
   *
   * We're going to work with `person` table again, where the `name` column is marked as being
   * unique.
   *
   * {{{
   *     CREATE TABLE IF NOT EXISTS person (
   *     id   IDENTITY,
   *     name VARCHAR NOT NULL UNIQUE,
   *     age  INT
   *     )
   * }}}
   *
   * Alright, let’s define a way to insert instances.
   *
   * {{{
   *     def insert(n: String, a: Option[Int]): ConnectionIO[Long] =
   *     sql"insert into person (name, age) values ($n, $a)"
   *     .update
   *     .withUniqueGeneratedKeys("id")
   * }}}
   *
   * The following exercises will try to insert two people with the same name. The second
   * operation will fail with a unique constraint violation. Let's see how we can avoid this
   * error by using several combinators.
   *
   * A first approach could be to specify the `Throwable` that we want to trap by using
   * `attemptSome` combinator.
   */
  def safeInsertWithAttemptSome(res0: String \/ Long) = {

    def safeInsert(name: String, age: Option[Int]): ConnectionIO[String \/ Long] =
      insert(name, age)
        .attemptSome {
          case e: java.sql.SQLException => "Oops!"
        }

    val insertedRows = for {
      john      <- safeInsert("John", Option(35))
      otherJohn <- safeInsert("John", Option(20))
    } yield otherJohn

    val result = insertedRows
      .transact(xa)
      .run

    result should be(res0)
  }

  /**
   * If we want to trap a specific `SqlState` like `unique constraint violation`, we'll use the
   * `attemptSomeSqlState`. We can specify several `SqlState` values and indicate what value we'll
   * return in each case. We can:
   *
   *  - Use the `SqlState` values provided as constants in the contrib-postgresql add-on
   *  - Create a new `SqlState` value by typing `val UNIQUE_VIOLATION = SqlState("23505")`
   */
  def safeInsertWithAttemptSomeSqlState(res0: String \/ Long) = {

    def safeInsert(name: String, age: Option[Int]): ConnectionIO[String \/ Long] =
      insert(name, age)
        .attemptSomeSqlState {
          case FOREIGN_KEY_VIOLATION => "Another error"
          case UNIQUE_VIOLATION      => "John is already here!"
        }

    val insertedRows = for {
      john      <- safeInsert("John", Option(35))
      otherJohn <- safeInsert("John", Option(20))
    } yield otherJohn

    val result = insertedRows
      .transact(xa)
      .run

    result should be(res0)
  }

  /**
   * Finally we can recover from an error with a new action by using `exceptSqlState`. In this
   * case, if the name already exists, we'll insert the person with a different name.
   */
  def safeInsertWithExceptSqlState(res0: String, res1: Option[Int]) = {

    def safeInsert(name: String, age: Option[Int]): ConnectionIO[Long] =
      insert(name, age)
        .exceptSqlState {
          case UNIQUE_VIOLATION => insert(name + "_20", age)
        }

    val insertedRows = for {
      john      <- safeInsert("John", Option(35))
      otherJohn <- safeInsert("John", Option(20))
      info      <- findPersonById(otherJohn)
    } yield info

    val result = insertedRows
      .transact(xa)
      .run

    result.name should be(res0)
    result.age should be(res1)
  }
}

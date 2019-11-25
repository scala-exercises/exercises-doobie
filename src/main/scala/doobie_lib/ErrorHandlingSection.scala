/*
 *  scala-exercises - exercises-doobie
 *  Copyright (C) 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
 *
 */

package doobie_lib

import DoobieUtils.PersonTable._
import ErrorHandlingSectionHelpers._
import doobie._
import doobie.implicits._
import org.scalaexercises.definitions.Section
import org.scalatest.{FlatSpec, Matchers}

/**
 * ==About Exceptions==
 * Exceptions are a fact of life when interacting with databases, and they are largely nondeterministic;
 * whether an operation will succeed or not depends on unpredictable factors like network health, the
 * current contents of tables, locking state, and so on. So we must decide whether to compute everything
 * in a disjunction like `EitherT[ConnectionIO, Throwable, A]` or allow exceptions to propagate until they
 * are caught explicitly. doobie adopts the second strategy: exceptions are allowed to propagate and
 * escape unless handled explicitly (exactly as IO works). This means when a doobie action (transformed
 * to some target monad) is executed, exceptions can escape.
 *
 * There are three main types of exceptions that are likely to arise:
 *
 * 1. Various types of `IOException` can happen with any kind of I/O, and these exceptions tend to be unrecoverable.
 *
 * 2. Database exceptions, typically as a generic `SQLException` with a vendor-specific `SQLState` identifying the
 * specific error, are raised for common situations such as key violations. Some vendors (PostgreSQL for instance)
 * publish a table of error codes, and in these cases doobie can provide a matching set of exception-handling
 * combinators. However in most cases the error codes must be passed down as folklore or discovered by
 * experimentation. There exist the XOPEN and SQL:2003 standards, but it seems that no vendor adheres closely to
 * these specifications. Some of these errors are recoverable and others aren’t.
 *
 * 3. doobie will raise an `InvariantViolation` in response to invalid type mappings, unknown JDBC constants returned
 * by drivers, observed `NULL` values, and other violations of invariants that doobie assumes. These exceptions
 * indicate programmer error or driver non-compliance and are generally unrecoverable.
 *
 * ==MonadError and Derived Combinators==
 * All '''doobie''' monads have associated instances of the `Async` instance, which extends
 * `MonadError[?[_], Throwable]`. This means `ConnectionIO`, etc., have the following primitive operations:
 *
 *  - `attempt` converts `M[A]` into `M[Either[Throwable, A]]`
 *  - `fail` constructs an `M[A]` that fails with a provided `Throwable`
 *
 * So any '''doobie''' program can be lifted into a disjunction simply by adding `.attempt`.
 *
 * {{{
 *   val p = 42.pure[ConnectionIO]
 * // p: ConnectionIO[Int] = Pure(42)
 *
 * p.attempt
 * // res0: ConnectionIO[Either[Throwable, Int]] = Suspend(
 * //   HandleErrorWith(
 * //     FlatMapped(Pure(42), cats.Monad$$Lambda$8968/1197202183@4abff98c),
 * //     cats.ApplicativeError$$Lambda$9059/715221672@694b43d5
 * //   )
 * // )
 * }}}
 *
 * From the `.attempt` and `fail` combinators we can derive many other operations, as described in the
 * Cats documentation. In addition doobie provides the following specialized combinators that only pay
 * attention to `SQLException`:
 *
 *  - `attemptSql` is like `attempt` but only traps `SQLException`.
 *  - `attemptSomeSql` traps only specified `SQLException`s.
 *  - `exceptSql` recovers from a SQLException with a new action.
 *  - `onSqlException` executes an action on `SQLException` and discards its result.
 *
 * And finally we have a set of combinators that focus on SQLStates.
 *
 *  - `attemptSqlState` is like `attemptSql` but yields `M[Either[SQLState, A]]`.
 *  - `attemptSomeSqlState` traps only specified `SQLState`s.
 *  - `exceptSqlState` recovers from a `SQLState` with a new action.
 *  - `exceptSomeSqlState` recovers from specified `SQLState`s with a new action.
 *
 * @param name error_handling
 **/
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
   * `attemptSql` combinator.
   */
  def safeInsertWithAttemptSome(res0: Either[String, Long]) = {

    def safeInsert(name: String, age: Option[Int]): ConnectionIO[Either[String, Long]] =
      insert(name, age).attemptSql.map {
        case Left(_)      => Left("Oops!")
        case Right(value) => Right(value)
      }

    val insertedRows = for {
      john      <- safeInsert("John", Option(35))
      otherJohn <- safeInsert("John", Option(20))
    } yield otherJohn

    val result = databaseBlock(insertedRows).unsafeRunSync()

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
  def safeInsertWithAttemptSomeSqlState(res0: Either[String, Long]) = {

    def safeInsert(name: String, age: Option[Int]): ConnectionIO[Either[String, Long]] =
      insert(name, age)
        .attemptSomeSqlState {
          case FOREIGN_KEY_VIOLATION => "Another error"
          case UNIQUE_VIOLATION      => "John is already here!"
        }

    val insertedRows = for {
      john      <- safeInsert("John", Option(35))
      otherJohn <- safeInsert("John", Option(20))
    } yield otherJohn

    val result = databaseBlock(insertedRows).unsafeRunSync()

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

    val result = databaseBlock(insertedRows).unsafeRunSync()

    result.name should be(res0)
    result.age should be(res1)
  }
}

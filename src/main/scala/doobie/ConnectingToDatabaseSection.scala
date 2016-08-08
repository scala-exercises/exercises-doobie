package doobie

import doobie.DoobieUtils.CountryTable._
import doobie.imports._
import org.scalaexercises.definitions.Section
import org.scalatest._

import scalaz.Scalaz._
import scalaz._

/** ==Introduction==
  * doobie is a monadic API that provides a number of data types that all work the same way
  * but describe computations in different contexts.
  *
  * In the doobie high level API the most common types we will deal with have the form
  * ConnectionIO[A], specifying computations that take place in a context where a
  * java.sql.Connection is available, ultimately producing a value of type A.
  *
  * doobie programs are values. You can compose small programs to build larger programs. Once you
  * have constructed a program you wish to run, you interpret it into an effectful target monad of
  * your choice (Task or IO for example) and drop it into your main application wherever you like.
  *
  * ==First programs==
  *
  * {{{
  * import doobie.imports._
  * import scalaz._, Scalaz._
  * import scalaz.concurrent.Task
  * }}}
  *
  * So let’s start with a ConnectionIO program that simply returns a constant.
  *
  * {{{
  * val program = 42.point[ConnectionIO]
  * program: ConnectionIO[Int] = Return(42)
  * }}}
  *
  * This is a perfectly respectable doobie program, but we can’t run it as-is; we need a Connection
  * first. There are several ways to do this, but here let’s use a Transactor.
  *
  * {{{
  * val xa = DriverManagerTransactor[Task](
  * driver = "org.postgresql.Driver",
  * url = "jdbc:postgresql:world",
  * user = "postgres",
  * pass = ""
  * )
  * }}}
  *
  * A Transactor is simply a structure that knows how to connect to a database, hand out
  * connections, and clean them up; and with this knowledge it can transform ConnectionIO ~> Task,
  * which gives us something we can run. Specifically it gives us a Task that, when run, will
  * connect to the database and run our program in a single transaction.
  *
  * The DriverManagerTransactor simply delegates to the java.sql.DriverManager to allocate
  * connections, which is fine for development but inefficient for production use.
  *
  * @param name connecting_to_database
  */
object ConnectingToDatabaseSection extends FlatSpec with Matchers with Section {

  /**
    * Right, so let’s do this.
    */
    def constantValue(res0: Int) =
    42.point[ConnectionIO].transact(xa).run should be(res0)

  /** We have computed a constant. It’s not very interesting because we never ask the database to
    * perform any work, but it’s a first step
    *
    * We are gonna connect to a database to compute a constant.
    * Let’s use the sql string interpolator to construct a query that asks the database to compute
    * a constant. The meaning of this program is “run the query, interpret the resultset as
    * a stream of Int values, and yield its one and only element.”
    */
  def constantValueFromDatabase(res0: Int) =
    sql"select 42".query[Int].unique.transact(xa).run should be(res0)

  /** What if we want to do more than one thing in a transaction? Easy! ConnectionIO is a monad,
    * so we can use a for comprehension to compose two smaller programs into one larger program.
    */
  def combineTwoPrograms(res0: (Int, Int)) = {
    val largerProgram = for {
      a <- sql"select 42".query[Int].unique
      b <- sql"select power(5, 2)".query[Int].unique
    } yield (a, b)

    largerProgram.transact(xa).run should be(res0)
  }

  /** The astute among you will note that we don’t actually need a monad to do this; an applicative
    * functor is all we need here. So we could also write the above program as:
    */
  def combineTwoProgramsWithApplicative(res0: Int) = {
    val oneProgram = sql"select 42".query[Int].unique
    val anotherProgram = sql"select power(5, 2)".query[Int].unique

    (oneProgram |@| anotherProgram) { _ + _ }.transact(xa).run should be(res0)
  }
}

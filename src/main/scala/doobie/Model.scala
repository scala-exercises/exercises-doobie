package doobie

object Model {

  case class Country(code: String, name: String, population: Long, gnp: Option[Double])

  val countries = List(
    Country("DEU", "Germany", 82164700, Option(2133367.00)),
    Country("ESP", "Spain", 39441700, Option(553223.00)),
    Country("FRA", "France", 59225700, Option(1424285.00)),
    Country("GBR", "United Kingdom", 59623400, Option(1378330.00)),
    Country("USA", "United States of America", 278357000, Option(8510700.00))
  )
}

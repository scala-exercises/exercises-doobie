package doobie

object Model {

  case class Country(code: String, name: String, population: Long, gnp: Double)

  val countries = List(
    Country("DEU", "Germany", 82164700, 2133367.00),
    Country("ESP", "Spain", 39441700, 553223.00),
    Country("FRA", "France", 59225700, 1424285.00),
    Country("GBR", "United Kingdom", 59623400, 1378330.00),
    Country("USA", "United States of America", 278357000, 8510700.00)
  )
}

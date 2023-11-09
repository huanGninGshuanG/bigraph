package repository

import anorm.SqlParser._
import anorm._
import javax.inject.Inject
import play.api.db._


class LogDaoTest @Inject()(db: Database){

  def getList  = {

    val conn = db.getConnection()

    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT * from pku_simulate_logs ")
      while (rs.next()) {
      val  log =   rs.getObject(1)
        println(log)
        log
      }

    } finally {
      conn.close()
    }
  }


  def test()={
    db.withConnection { implicit c =>
      val result: Boolean = SQL("Select 1").execute()
      println("test方法—result:"+result)
      result
    }
  }

  def test2()={
    db.withConnection { implicit c =>
//      val id: List[String] =
//        SQL("insert into City(name, country) values ({name}, {country})")
//          .on('name -> "Cambridge", 'country -> "New Zealand")
//          .executeInsert();
      val parser =
        str("name") ~ float(3) /* third column as float */ map {
          case name ~ f => (name -> f)
        }

      val product: (String, Float) = SQL("SELECT * FROM prod WHERE id = {id}").
        on('id -> "p").as(parser.single)

      val populations: List[String ~ Int] =
        SQL("SELECT * FROM Country").as((str("name") ~ int("population")).*)

      import anorm.SqlParser.{ int, str, to }

      def display(name: String, population: Int): String =
        s"The population in $name is of $population."

      str("name") ~ int("population") map (to(display _))


      import anorm.{ SQL, SqlParser }, SqlParser.{ int, str }

      // Combinator ~>
      val String = SQL("SELECT * FROM test").as((int("id") ~> str("val")).single)
      // row has to have an int column 'id' and a string 'val' one,
      // keeping only 'val' in result

      val Int = SQL("SELECT * FROM test").as((int("id") <~ str("val")).single)
      // row has to have an int column 'id' and a string 'val' one,
      // keeping only 'id' in result


      var p: ResultSetParser[List[(String,String)]] = {
        str("name") ~ str("language") map(flatten) *
      }


      case class SpokenLanguages(country:String, languages:Seq[String])

      def spokenLanguages(countryCode: String): Option[SpokenLanguages] = {
        val languages: List[(String, String)] = SQL(
          """
      select c.name, l.language from Country c
      join CountryLanguage l on l.CountryCode = c.Code
      where c.code = {code};
    """
        )
          .on("code" -> countryCode)
          .as(str("name") ~ str("language") map(flatten) *)

        languages.headOption.map { f =>
          SpokenLanguages(f._1, languages.map(_._2))
        }
      }

    }
  }


}


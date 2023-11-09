package domain

import play.api.mvc.QueryStringBindable

case class TestData(index: Int, size: Int) {}

object TestData {
  implicit def queryStringBinder(implicit intBinder: QueryStringBindable[Int]) = new QueryStringBindable[TestData] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, TestData]] = {
      for {
        index <- intBinder.bind(key + ".index", params)
        size <- intBinder.bind(key + ".size", params)
      } yield {
        (index, size) match {
          case (Right(index), Right(size)) => Right(TestData(index, size))
          case _ => Left("Unable to bind a Pager")
        }
      }
    }
    override def unbind(key: String, pager: TestData): String = {
      intBinder.unbind(key + ".index", pager.index) + "&" + intBinder.unbind(key + ".size", pager.size)
    }
  }
}

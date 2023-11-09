package repository

import javax.inject.Inject

import play.api.db._

class TestDAO @Inject()(db: Database){

  def getValue : Long = {
    var outL = 0L;
    val conn = db.getConnection()

    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT 9 as testkey ")

      while (rs.next()) {
        outL = rs.getLong("testkey")
      }
    } finally {
      conn.close()
    }
    outL
  }

}

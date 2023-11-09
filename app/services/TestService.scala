package services

import javax.inject.Inject
import repository.{TestDAO, TestRepository}

@javax.inject.Singleton
class TestService @Inject()(testDao: TestDAO, testRepository: TestRepository)  {

    def getValue(): Long = {
      val value = testDao.getValue
      value
    }

    def getResourceById = {
      testRepository.findById("98b7f82c980d11e98afbd0bf9c8f9b69")
    }

  def getResource = {
    testRepository.findResource("98b7f82c980d11e98afbd0bf9c8f9b69")
  }

  def getList(page: Int = 0, pageSize: Int = 10) = {
    testRepository.list(page, pageSize)
  }
}


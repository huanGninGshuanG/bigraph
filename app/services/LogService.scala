package services

import domain.{PkuLog, PkuLogQuery}
import javax.inject.Inject
import repository.LogDao

@javax.inject.Singleton
class LogService @Inject()(logDao: LogDao)  {

  def getList(page: Int = 0, pageSize: Int = 10,filter:PkuLogQuery) = {
    logDao.getList(page, pageSize,filter)
  }


  def getById(id:String) = {
    val value = logDao.getById(id)
    value
  }

  def getByBatchId(batchId:String) = {
    val value = logDao.getByBatchId(batchId)
    value
  }


  def delById(id:String) = {
    val value = logDao.delById(id)
    if(value>0){
      true
    }else{
      false
    }
  }

  def updateById(log:PkuLog) = {
    logDao.updateById(log)
  }

  def save(log:PkuLog) = {
    logDao.save(log)
  }

  def getCount() = {
    logDao.getCount()
  }
}


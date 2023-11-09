package services

import domain.PkuLogItem
import javax.inject.Inject
import repository.{LogItemDao}

@javax.inject.Singleton
class LogItemService @Inject()(logItemDao: LogItemDao)  {

  def getList(page: Int = 0, pageSize: Int = 10,filter:String) = {
    logItemDao.getList(page, pageSize,filter)
  }


  def getById(id:String) = {
    val value = logItemDao.getById(id)
    value
  }

  def delById(id:String) = {
    val value = logItemDao.delById(id)
    if(value>0){
      true
    }else{
      false
    }
  }

  def updateById(log:PkuLogItem) = {
    logItemDao.updateById(log)
  }

  def save(log:PkuLogItem) = {
    logItemDao.save(log)
  }
}


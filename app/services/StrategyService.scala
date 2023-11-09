package services

import domain.PkuStrategy
import javax.inject.Inject
import repository.StrategyDao

@javax.inject.Singleton
class StrategyService @Inject()(strategyDao: StrategyDao)  {

  def getById(id:String) = {
      strategyDao.getById(id)
    }


  def getList(page: Int = 0, pageSize: Int = 10,filter:String) = {
    strategyDao.getList(page, pageSize,filter=("%"+filter+"%"))
  }

  def delById(id:String) = {
    val value = strategyDao.delById(id)
    if(value>0){
      true
    }else{
      false
    }
  }

  def updateById(strategy: PkuStrategy) = {
    strategyDao.updateById(strategy)
  }

  def save(strategy: PkuStrategy) = {
    strategyDao.save(strategy)
  }
}


package services

import domain.PkuResource
import javax.inject.Inject
import repository.ResourceDao

@javax.inject.Singleton
class ResourceService @Inject()(resourceDao: ResourceDao)  {

  def getById(id:String) = {
      resourceDao.getById(id)
    }


  def getList(page: Int = 0, pageSize: Int = 10,filter:String) = {
    resourceDao.getList(page, pageSize,filter=("%"+filter+"%"))
  }

  def delById(id:String) = {
    val value = resourceDao.delById(id)
    if(value>0){
      true
    }else{
      false
    }
  }

  def updateById(resource: PkuResource) = {
    resourceDao.updateById(resource)
  }

  def save(resource: PkuResource) = {
    resourceDao.save(resource)
  }
}


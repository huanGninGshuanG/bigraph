package services

import domain.PkuMenu
import javax.inject.Inject
import repository.MenuDao

@javax.inject.Singleton
class MenuService @Inject()(menuDao: MenuDao)  {

  def tree() = {
      menuDao.tree()
    }

  def getList(page: Int = 0, pageSize: Int = 10,filter:String,parentId:String) = {
    menuDao.getList(page, pageSize,filter=("%"+filter+"%"),parentId)
  }

  def getById(id:String) = {
    menuDao.getById(id)
  }

  def delById(id:String) = {
    val value = menuDao.delById(id)
    if(value>0){
      true
    }else{
      false
    }
  }

  def updateById(pkuMenu: PkuMenu) = {
    menuDao.updateById(pkuMenu)
  }

  def save(pkuMenu: PkuMenu) = {
    menuDao.save(pkuMenu)
  }

}


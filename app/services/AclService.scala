package services

import domain.{PkuAcl, PkuMenu}
import javax.inject.Inject
import repository.AclDao

@javax.inject.Singleton
class AclService @Inject()(aclDao: AclDao,menuService:MenuService)  {

  def getById(id:String) = {
    aclDao.getById(id)
  }


  def getList(page: Int = 0, pageSize: Int = 10,filter:String) = {
    aclDao.getList(page, pageSize,filter=("%"+filter+"%"))
  }

  def delById(id:String) = {
    val value = aclDao.delById(id)
    if(value>0){
      true
    }else{
      false
    }
  }

  def updateById(acl: PkuAcl) = {
    aclDao.updateById(acl)
  }

  def save(acl: PkuAcl) = {
    aclDao.save(acl)
  }

  def delByPrincipalId(id:String) = {
    val value = aclDao.delByPrincipalId(id)
    if(value>0){
      true
    }else{
      false
    }
  }

  def getByPrincipalId(id:String) = {
    aclDao.getByPrincipalId(id)
  }

  def getMenuListByPrincipalId(userId:String) =  {
    val aclList = getByPrincipalId(userId)
    var menuList = List[PkuMenu]();
    for(acl <- aclList){
      menuList = menuList ::: List[PkuMenu]((menuService.getById(acl.menuId)))
    }
    menuList
  }


}



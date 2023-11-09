package services

import domain.PkuUser
import javax.inject.Inject
import repository.UserDao

@javax.inject.Singleton
class UserService @Inject()(userDao: UserDao)  {

  def getById(id:String) = {
    userDao.getById(id)
  }


  def getList(page: Int = 0, pageSize: Int = 10,filter:String) = {
    userDao.getList(page, pageSize,filter=("%"+filter+"%"))
  }

  def delById(id:String) = {
    val value = userDao.delById(id)
    if(value>0){
      true
    }else{
      false
    }
  }

  def updateById(user: PkuUser) = {
    userDao.updateById(user)
  }

  def save(user: PkuUser) = {
    userDao.save(user)
  }

  def getByPass(loginname: String,loginpwd:String) = {
    userDao.getByPass(loginname,loginpwd);
  }

}


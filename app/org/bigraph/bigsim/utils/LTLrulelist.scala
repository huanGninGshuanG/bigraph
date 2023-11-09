package org.bigraph.bigsim.utils

class LTLrulelist {

  object SinglyLinkedListDemo {
    def main(args: Array[String]): Unit = {
      val list = new SinglyLinkedList[Int]()
      list.add(10)
      list.add(20)
      list.add(390)

      list.printInfo()

      list.delete(20)
      list.add(35)
      list.printInfo()
    }
  }

  /**
   * 单向链表
   *
   * @tparam T 列表中存储的元素的类型
   */
  class SinglyLinkedList[T] {
    //头结点
    private var head: Node = _
    //为了提高添加的效率,设置一个尾结点
    private var tail: Node = _

    def add(ele: T): Boolean = {
      if (head == null) {
        //第一次添加元素,则第一个结点置为head节点,尾结点和头节点一致
        head = Node(ele, null)
        tail = head
      } else {
        tail.next = Node(ele, null)
        tail = tail.next // tail 指向新节点
      }
      true
    }

    def delete(ele: T): Boolean = {
      //如果头节点为 null .表示没有元素,所有删除失败
      if (head == null) return false
      //如果头节点要删除,删除当前头节点,将下一个节点设置为头节点,删除的节点是同时也是尾节点,更新尾节点
      if (head.value == ele) {
        if (head.eq(tail)) { //比较是否为同一个对象,等价于java的比较地址值相等
          tail = head.next
        }
        head = head.next
        return true
      } else {
        //删除头节点不是要删除的节点,遍历后面的节点
        var currentNode: Node = head
        var nextNode = currentNode.next
        while (nextNode != null) {
          if (nextNode.value == ele) { //删除
            currentNode.next = nextNode.next //让当前节点下一个节点的下一个节点
            if (nextNode.eq(tail)) {
              //当删除的节点是尾节点,尾节点需要指向当前节点
              tail = currentNode
            }
            return true
          }
          currentNode = nextNode
          nextNode = currentNode.next
        }
      }
      false
    }

    def contain(ele: T): Boolean = {
      if (head == null) return false
      var tmp : Node = head
      do {
        if (tmp.value == ele) return  true
        tmp = tmp.next
      } while (tmp != null)
      false
    }

    /**
     * 打印链表中的元素
     */
    def printInfo(): Unit ={
      if (head == null) return
      var tmp : Node = head
      do{
        print(tmp.value + "-> ")
        tmp = tmp.next
      } while (tmp != null)
      println()
    }

    case class Node(value: T, var next: Node)

  }


}

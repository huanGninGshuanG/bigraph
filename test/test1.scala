import org.bigraph.bigsim.parser.{BGMParser, BGMTerm}
import org.bigraph.bigsim.simulator.CTLSimulator
import org.bigraph.bigsim.simulator.testCTLSimulator.b
import org.bigraph.bigsim.utils.OS
import org.scalatest.FunSuite


object Hello extends App {
  val p = Person("Ink Bai")
  println("Hello from " + p.name)
}

case class Person(var name: String)

class HelloTests extends FunSuite {
  test("CTLModelChecking") {
    val t = BGMParser.parseFromString(OS.rw2Process)
    val b = BGMTerm.toBigraph(t)._2
    val startTime=System.currentTimeMillis()
    val simulator = new CTLSimulator(b)
    simulator.simulate
    var dotStr = simulator.dumpDotForward("")
    //println(dotStr)
    val endTime=System.currentTimeMillis()
    printf("===============time consuming: %d=====================",endTime-startTime)
  }
}
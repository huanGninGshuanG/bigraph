package services

import domain.enums.SimDataTypeEnum.SimDataTypeEnum
import javax.inject.Inject
import org.bigraph.bigsim.model.BiNode
import org.bigraph.bigsim.modelchecker.MCSimulator
import org.bigraph.bigsim.simulator.Simulator
import org.bigraph.bigsim.utils.GlobalCfg
import org.slf4j.{Logger, LoggerFactory}


// Singleton 目前的理解，是开启单例模式
@javax.inject.Singleton
class BigsimService @Inject()(resourceService: ResourceService,
                              strategyService: StrategyService,
                              logService: LogService, 
                              logItemService: LogItemService) { // @Inject() 是注入依赖项（依赖注入技术）

  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  def handler(resourceId: String, strategyId: String,extMap:Map[SimDataTypeEnum,String]): String = {  // extMap目前传入的是一个Map（），是空的，只有前两个Id有效

    MCSimulator.setLogService(logService)
    MCSimulator.setLogItemService(logItemService)

    val start = System.currentTimeMillis()
    logger.info("BigsimService -> {}", start)

    val pkuResource = resourceService.getById(resourceId)

    val strategy = strategyService.getById(strategyId)   // 这两个Service的实现不是研究的重点， 之后修改沿用现有的实现就好了

    logger.debug(s"pkuResource,id->${resourceId}, ${pkuResource.toString} ")

    GlobalCfg.bgmContent = ""

    val isMC: Boolean = false

    var logItemId = ""

    if (isMC) {   // 目前这个分支是关掉的，因为isMC 一直是false

      logItemId = MCSimulator.simulate(pkuResource, strategy)

      BiNode.printAllBiNode()

    } else {
      logItemId = Simulator.simulate(pkuResource, strategy,extMap);    // 这里进入模拟器
    }

    //   ok(GlobalCfg.dotContent);
    var end = System.currentTimeMillis()

    logger.info("  Total:\tstart:" + start + ", end:" + end + ", used:" + (end - start) + " ms")

    logger.debug("kgq: ----------------------handler 结束-------------------------------")
    logItemId
  }


}

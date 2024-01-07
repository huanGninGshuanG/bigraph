package org.bigraph.bigsim.testsuite;

import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.BigraphBuilder;
import org.bigraph.bigsim.model.component.Handle;
import org.bigraph.bigsim.model.component.Node;
import org.bigraph.bigsim.model.component.Root;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.bigraph.bigsim.utils.GlobalCfg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huangningshuang
 * @date 2023/12/31
 * 测试类，生成随机偶图/
 */
public class RandBigraphGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RandBigraphGenerator.class);
    private final List<String> authedUsers = new ArrayList<>();
    private final List<String> authedDevices = new ArrayList<>();
    private final List<Node> devices = new ArrayList<>();
    private final List<Node> allData = new ArrayList<>();
    private Node pdp, pep;
    private int randNum;

    public Node getPDP() {
        return pdp;
    }

    public Node getPEP() {
        return pep;
    }

    public List<Node> getDevices() {
        return devices;
    }

    public Node getDataWithName(String name) {
        for (Node node : allData) {
            if (node.getName().equals(name)) return node;
        }
        throw new RuntimeException("data name mismatch");
    }

    public Node getDeviceWithInfo(String name) {
        for (Node device : devices) {
            Node dInfo = ZTSignature.findChildWithCtrl(device, ZTSignature.DeviceInfo);
            if (dInfo.getName().equals(name)) {
                return device;
            }
        }
        throw new RuntimeException("mismatch device with device info");
    }

    public List<Node> getAllData() {
        return allData;
    }

    public int getRandNum() {
        return randNum;
    }

    public static final int PEPDataPort = 2;
    public static final int PEPPDPPort = 1;
    public static final int PEPDevicePort = 0;

    public Node getRandData() {
        return allData.get((int) (Math.random() * allData.size()));
    }

    Bigraph makePrimeBigraph() {
        BigraphBuilder bb = new BigraphBuilder(ZTSignature.ztSig);
        Root root = bb.addRoot();
        pep = NodeGenerator.newPEP(bb, root);
        // 根据配置项，生成不同数量级节点
        int level = GlobalCfg.testLevel();
        if (level == 0) {
            randNum = (int) (1 + Math.random() * 10);
        } else {
            randNum = (int) 6;
        }
        DebugPrinter.print(logger, "rand num is: " + randNum);
        // 连接device和PEP
        Handle deviceLink = pep.getPort(PEPDevicePort).getHandle();
        for (int i = 0; i < randNum; i++) {
            Node device = NodeGenerator.newDevice(bb, root, authedUsers, authedDevices);
            devices.add(device);
            device.getPort(0).setHandle(deviceLink);
        }
        // 连接PEP和Data
        Handle dataLink = pep.getPort(PEPDataPort).getHandle();
        for (int i = 0; i < randNum; i++) {
            Node data = NodeGenerator.newData(bb, root);
            allData.add(data);
            data.getPort(0).setHandle(dataLink);
        }
        // 建立PDP模块
        pdp = NodeGenerator.newPDP(bb, root, authedUsers, authedDevices);
        // 连接pdp和pep
        Handle pdpHandle = pdp.getPort(0).getHandle();
        pep.getPort(PEPPDPPort).setHandle(pdpHandle);

        Bigraph result = bb.makeBigraph(true);
        result.print();
        return result;
    }
}

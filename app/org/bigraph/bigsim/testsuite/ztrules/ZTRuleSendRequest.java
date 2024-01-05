package org.bigraph.bigsim.testsuite.ztrules;

import org.bigraph.bigsim.BRS.InstantiationMap;
import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.BigraphBuilder;
import org.bigraph.bigsim.model.component.Child;
import org.bigraph.bigsim.model.component.Node;
import org.bigraph.bigsim.model.component.OuterName;
import org.bigraph.bigsim.model.component.Root;
import org.bigraph.bigsim.testsuite.NodeGenerator;
import org.bigraph.bigsim.testsuite.RandBigraphGenerator;
import org.bigraph.bigsim.testsuite.TestBigraphReact;
import org.bigraph.bigsim.testsuite.ZTSignature;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.bigraph.bigsim.utils.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.bigraph.bigsim.testsuite.RandBigraphGenerator.*;

/**
 * @author huangningshuang
 * @date 2024/1/1
 * 零信任偶图衍化反应规则1：发起请求资源的申请，有多少个device就有多少个反应规则
 * 拷贝UserInfo和DeviceInfo到PEP的Staging中
 */
public class ZTRuleSendRequest {
    private static final Logger logger = LoggerFactory.getLogger(ZTRuleSendRequest.class);
    private Bigraph[] redex;
    private Bigraph[] reactum;
    private InstantiationMap[] eta;
    private List<Node> requests = new ArrayList<>();

    public ZTRuleSendRequest(RandBigraphGenerator gen) {
        int num = gen.getRandNum();
        redex = new Bigraph[num];
        reactum = new Bigraph[num];
        eta = new InstantiationMap[num];
        for (int i = 0; i < num; i++) {
            init(gen, i);
            eta[i] = InstantiationMap.getIdMap(reactum[i].bigSites().size());
        }
    }

    public List<Node> getRequests() {
        return requests;
    }

    public InstantiationMap[] getEta() {
        return eta;
    }

    public Bigraph[] getRedex() {
        return redex;
    }

    public Bigraph[] getReactum() {
        return reactum;
    }

    /*
        xx:Device[xx:outername].(xx:UserInfo || xx:DeviceInfo) ||
          xx:PEP[xx:outername, o1:outername, o2:outername].(xx:Staging.$1 || $2) ->
        xx:Device[xx:outername] || xx:PEP[xx:outername, o1:outername, o2:outername].
            (xx:Staging.(xx:Request.(xx:UserInfo || xx:DeviceInfo || xx:Data) || $1) || $2)
     */
    private void init(RandBigraphGenerator gen, int idx) {
        Node device = gen.getDevices().get(idx);
        Node pep = gen.getPEP();
        BigraphBuilder bb = new BigraphBuilder(ZTSignature.ztSig);
        Root root = bb.addRoot();
        // 完成pep拷贝
        Node copyPEP = pep.replicate();
        copyPEP.setParent(root);
        for (Child c : pep.getChildren()) {
            if (c.isNode()) {
                Node p = (Node) c;
                if (p.getControl().getName().equals(ZTSignature.StagingZone)) {
                    Node copyP = p.replicate();
                    copyP.setParent(copyPEP);
                    bb.addSite(copyP);
                    break;
                }
            }
        }
        bb.addSite(copyPEP);
        OuterName o1 = bb.addOuterName("outername_" + NameGenerator.DEFAULT.generate());
        OuterName o2 = bb.addOuterName("outername_" + NameGenerator.DEFAULT.generate());
        copyPEP.getPort(PEPPDPPort).setHandle(o1);
        copyPEP.getPort(PEPDataPort).setHandle(o2);
        // 完成Device拷贝
        Node copyDevice = NodeGenerator.cloneNode(device, bb, root);
        // Device和PEP连接
        OuterName o3 = bb.addOuterName("outername_" + NameGenerator.DEFAULT.generate());
        copyPEP.getPort(PEPDevicePort).setHandle(o3);
        copyDevice.getPort(0).setHandle(o3);

        bb.addNode(copyPEP);
        bb.addNode(copyDevice);
        redex[idx] = bb.makeBigraph(true);
        DebugPrinter.print(logger, "---------------send redex--------------");
        redex[idx].print();

        // 处理reactum
        BigraphBuilder bb1 = new BigraphBuilder(ZTSignature.ztSig);
        Root root1 = bb1.addRoot();
        // 完成pep拷贝
        Node copyPEP1 = pep.replicate();
        copyPEP1.setParent(root1);
        for (Child c : pep.getChildren()) {
            if (c.isNode()) {
                Node p = (Node) c;
                if (p.getControl().getName().equals(ZTSignature.StagingZone)) {
                    Node stagingCopy = p.replicate();
                    stagingCopy.setParent(copyPEP1);
                    Node dUserInfo = ZTSignature.getDeviceInfo(device, ZTSignature.UserInfo);
                    Node dDeviceInfo = ZTSignature.getDeviceInfo(device, ZTSignature.DeviceInfo);
                    Node uCopy = dUserInfo.replicate();
                    Node dCopy = dDeviceInfo.replicate();
                    Node req = NodeGenerator.newRequest(bb1, stagingCopy);
                    NodeGenerator.newDataTag(bb1, req, gen.getRandData().getName());
                    uCopy.setParent(req);
                    dCopy.setParent(req);
                    requests.add(req);
                    bb1.addSite(stagingCopy);
                    break;
                }
            }
        }
        bb1.addSite(copyPEP1);
        OuterName o11 = bb1.addOuterName(o1.getName());
        OuterName o12 = bb1.addOuterName(o2.getName());
        copyPEP1.getPort(PEPPDPPort).setHandle(o11);
        copyPEP1.getPort(PEPDataPort).setHandle(o12);
        // 完成Device拷贝
        Node copyDevice1 = device.replicate();
        copyDevice1.setParent(root1);
        // Device和PEP连接
        OuterName o13 = bb1.addOuterName(o3.getName());
        copyPEP1.getPort(PEPDevicePort).setHandle(o13);
        copyDevice1.getPort(0).setHandle(o13);

        bb1.addNode(copyPEP1);
        bb1.addNode(copyDevice1);
        reactum[idx] = bb1.makeBigraph(true);
        DebugPrinter.print(logger, "---------------send reactum--------------");
        reactum[idx].print();
    }
}

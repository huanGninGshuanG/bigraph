package org.bigraph.bigsim.testsuite.ztrules;

import org.bigraph.bigsim.BRS.InstantiationMap;
import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.BigraphBuilder;
import org.bigraph.bigsim.model.component.*;
import org.bigraph.bigsim.testsuite.NodeGenerator;
import org.bigraph.bigsim.testsuite.RandBigraphGenerator;
import org.bigraph.bigsim.testsuite.ZTSignature;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.bigraph.bigsim.utils.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author huangningshuang
 * @date 2024/1/3
 * 零信任偶图衍化规则3：销毁连接
 * 需要将ActiveZone中Connection与Data的连接断开，删掉Connection，重新连接PEP和Data
 */
public class ZTRuleFinishRem {
    private static final Logger logger = LoggerFactory.getLogger(ZTRuleFinishRem.class);
    private Bigraph[] redex;
    private Bigraph[] reactum;
    private InstantiationMap[] eta;

    public ZTRuleFinishRem(RandBigraphGenerator gen, ZTRuleConnection conn) {
        List<Node> conns = conn.getConns();
        int num = conns.size();
        redex = new Bigraph[num];
        reactum = new Bigraph[num];
        eta = new InstantiationMap[num];
        for (int i = 0; i < num; i++) {
            init(gen, conns.get(i), i);
            eta[i] = InstantiationMap.getIdMap(reactum[i].bigSites().size());
        }
    }

    public Bigraph[] getRedex() {
        return redex;
    }

    public Bigraph[] getReactum() {
        return reactum;
    }

    public InstantiationMap[] getEta() {
        return eta;
    }

    /*
        xx:Device[o1:outername] ||xx:PEP[o1:outername, xx:outername, o2:outername].
            ($0 || xx:ActiveZone.(Connection[o4:outername].(xx:UserInfo || xx:DeviceInfo || xx:DataTag) || $1)) ||
        xx:PDP[xx:outername].($2 || xx:AuthUser.($3 || xx:UserInfo) || xx:AuthDevice.($4 || xx:DeviceInfo)) ||
        xx:Data[o4:outername] || (i1:innername连接o4) ->
        xx:Device[o1:outername].(xx:UserInfo || xx:DeviceInfo) ||
        xx:PEP[o1:outername, xx:outername, o2:outername].($0 || xx:ActiveZone.($1)) ||
        xx:PDP[xx:outername].($2 || xx:AuthUser.($3 || xx:UserInfo) || xx:AuthDevice.($4 || xx:DeviceInfo)) ||
        xx:Data[o4:outername] || (i1:innername连接o4)
    */
    private void init(RandBigraphGenerator gen, Node conn, int idx) {
        Node pep = gen.getPEP();
        Node pdp = gen.getPDP();
        Node activeZone = ZTSignature.findChildWithCtrl(pep, ZTSignature.ActiveZone);
        Node uAuth = ZTSignature.getAuthNode(pdp, ZTSignature.AuthUser);
        Node dAuth = ZTSignature.getAuthNode(pdp, ZTSignature.AuthDevice);
        Node uInfo = ZTSignature.findChildWithCtrl(conn, ZTSignature.UserInfo);
        Node dInfo = ZTSignature.findChildWithCtrl(conn, ZTSignature.DeviceInfo);
        Node dataTag = ZTSignature.findChildWithCtrl(conn, ZTSignature.DataTag);
        Node data = gen.getDataWithName(dataTag.getName());
        Node device = gen.getDeviceWithInfo(dInfo.getName());

        // 处理redex
        BigraphBuilder bb = new BigraphBuilder(ZTSignature.ztSig);
        Root root = bb.addRoot();
        OuterName o1 = bb.addOuterName(NameGenerator.DEFAULT.generate());
        OuterName o2 = bb.addOuterName(NameGenerator.DEFAULT.generate());
        OuterName o3 = bb.addOuterName(NameGenerator.DEFAULT.generate());
        OuterName o4 = bb.addOuterName(NameGenerator.DEFAULT.generate());
        InnerName i1 = bb.addInnerName(NameGenerator.DEFAULT.generate(), o4);

        // 处理pep
        Node pepRedex = pep.replicate();
        pepRedex.setParent(root);
        bb.addSite(pepRedex);
        pepRedex.getPort(RandBigraphGenerator.PEPDevicePort).setHandle(o1);
        pepRedex.getPort(RandBigraphGenerator.PEPPDPPort).setHandle(o2);
        pepRedex.getPort(RandBigraphGenerator.PEPDataPort).setHandle(o3);

        Node activeRedex = activeZone.replicate();
        activeRedex.setParent(pepRedex);
        Node connRedex = NodeGenerator.cloneNodeWithoutSite(conn, activeRedex);
        bb.addSite(activeRedex);
        connRedex.getPort(0).setHandle(o4);

        // 处理pdp
        Node pdpRedex = pdp.replicate();
        pdpRedex.setParent(root);
        bb.addSite(pdpRedex);

        Node uAuthCopy = uAuth.replicate();
        uAuthCopy.setParent(pdpRedex);
        bb.addSite(uAuthCopy);
        Node dAuthCopy = dAuth.replicate();
        dAuthCopy.setParent(pdpRedex);
        bb.addSite(dAuthCopy);

        Node uInfoCopy = uInfo.replicate();
        Node dInfoCopy = dInfo.replicate();
        uInfoCopy.setParent(uAuthCopy);
        dInfoCopy.setParent(dAuthCopy);
        pdpRedex.getPort(0).setHandle(o2);

        // 处理data
        Node dataRedex = data.replicate();
        dataRedex.setParent(root);
        dataRedex.getPort(0).setHandle(o4);

        // 处理Device
        Node deviceRedex = device.replicate();
        deviceRedex.setParent(root);
        deviceRedex.getPort(0).setHandle(o1);

        redex[idx] = bb.makeBigraph(true);
        DebugPrinter.print(logger, "---------------finish redex--------------");
        redex[idx].print();

        // 处理reactum
        /*
        *   xx:Device[o1:outername].(xx:UserInfo || xx:DeviceInfo) ||
            xx:PEP[o1:outername, xx:outername, o2:outername].($0 || xx:ActiveZone.($1)) ||
            xx:PDP[xx:outername].($2 || xx:AuthUser.($3 || xx:UserInfo) || xx:AuthDevice.($4 || xx:DeviceInfo)) ||
            xx:Data[o4:outername] || (i1:innername连接o4)
        * */
        BigraphBuilder bb1 = new BigraphBuilder(ZTSignature.ztSig);
        Root root1 = bb1.addRoot();
        OuterName o11 = bb1.addOuterName(o1.getName());
        OuterName o21 = bb1.addOuterName(o2.getName());
        OuterName o31 = bb1.addOuterName(o3.getName());
        OuterName o41 = bb1.addOuterName(o4.getName());
        InnerName i11 = bb1.addInnerName(i1.getName(), o41);

        // 处理pep
        Node pepReact = pep.replicate();
        pepReact.setParent(root1);
        bb1.addSite(pepReact);
        Node activeReact = activeZone.replicate();
        activeReact.setParent(pepReact);
        bb1.addSite(activeReact);
        pepReact.getPort(RandBigraphGenerator.PEPDevicePort).setHandle(o11);
        pepReact.getPort(RandBigraphGenerator.PEPPDPPort).setHandle(o21);
        pepReact.getPort(RandBigraphGenerator.PEPDataPort).setHandle(o31);

        // 处理pdp
        Node pdpReact = pdp.replicate();
        pdpReact.setParent(root1);
        bb1.addSite(pdpReact);

        Node uAuthReact = uAuth.replicate();
        uAuthReact.setParent(pdpReact);
        bb1.addSite(uAuthReact);
        Node dAuthReact = dAuth.replicate();
        dAuthReact.setParent(pdpReact);
        bb1.addSite(dAuthReact);

        Node uInfoReact = uInfo.replicate();
        Node dInfoReact = dInfo.replicate();
        uInfoReact.setParent(uAuthReact);
        dInfoReact.setParent(dAuthReact);
        pdpReact.getPort(0).setHandle(o21);

        // 处理data
        Node dataReact = data.replicate();
        dataReact.setParent(root1);
        dataReact.getPort(0).setHandle(o41);

        // 处理device
        Node deviceReact = NodeGenerator.cloneNodeWithoutSite(device, root1);
        deviceReact.getPort(0).setHandle(o11);

        reactum[idx] = bb1.makeBigraph(true);
        DebugPrinter.print(logger, "---------------finish reactum--------------");
        reactum[idx].print();
    }
}

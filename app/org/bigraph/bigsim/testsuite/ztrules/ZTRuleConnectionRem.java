package org.bigraph.bigsim.testsuite.ztrules;

import org.bigraph.bigsim.BRS.InstantiationMap;
import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.BigraphBuilder;
import org.bigraph.bigsim.model.component.InnerName;
import org.bigraph.bigsim.model.component.Node;
import org.bigraph.bigsim.model.component.OuterName;
import org.bigraph.bigsim.model.component.Root;
import org.bigraph.bigsim.testsuite.NodeGenerator;
import org.bigraph.bigsim.testsuite.RandBigraphGenerator;
import org.bigraph.bigsim.testsuite.ZTSignature;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.bigraph.bigsim.utils.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huangningshuang
 * @date 2024/1/1
 * 零信任偶图衍化规则2：PEP将Staging模块中的信息交由PDP处理
 * 如果某个信息内的userInfo和DeviceInfo在PDP的auth列表中 -> 对应的Info放入activeZone
 */
public class ZTRuleConnectionRem {
    private static final Logger logger = LoggerFactory.getLogger(ZTRuleConnectionRem.class);
    private Bigraph[] redex;
    private Bigraph[] reactum;
    private InstantiationMap[] eta;
    private List<Node> conns = new ArrayList<>();

    public ZTRuleConnectionRem(RandBigraphGenerator gen, ZTRuleSendRequest sendRule) {
        List<Node> reqs = sendRule.getRequests();
        int num = reqs.size();
        redex = new Bigraph[num];
        reactum = new Bigraph[num];
        eta = new InstantiationMap[num];
        for (int i = 0; i < num; i++) {
            init(gen, reqs.get(i), i);
            eta[i] = InstantiationMap.getIdMap(reactum[i].bigSites().size());
        }
    }

    public List<Node> getConns() {
        return conns;
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
          xx:PEP[o1:outername, xx:outername, o2:outername].
            (xx:Staging.(xx:Request.(xx:UserInfo || xx:DeviceInfo || xx:DataTag) || $0) || xx:ActiveZone.$1) ||
          xx:PDP[xx:outername].($2 || xx:AuthUser.($3 || xx:UserInfo) || xx:AuthDevice.($4 || xx:DeviceInfo)) ||
          xx:Data[o4:outername] || (i1:innername连接o4:outername)->
          xx:PEP[o1:outername, xx:outername, o2:outername].
            (xx:Staging.$0 || xx:ActiveZone.(Connection[o4:outername].(xx:UserInfo || xx:DeviceInfo || xx:DataTag) || $1)) ||
          xx:PDP[xx:outername].($2 || xx:AuthUser.($3 || xx:UserInfo) || xx:AuthDevice.($4 || xx:DeviceInfo)) ||
          xx:Data[o4:outername] || (i1:innername连接o4:outername)
         */
    private void init(RandBigraphGenerator gen, Node req, int idx) {
        Node uInfo = ZTSignature.findChildWithCtrl(req, ZTSignature.UserInfo);
        Node dInfo = ZTSignature.findChildWithCtrl(req, ZTSignature.DeviceInfo);
        Node dataTag = ZTSignature.findChildWithCtrl(req, ZTSignature.DataTag);
        Node data = gen.getDataWithName(dataTag.getName());
        Node pep = gen.getPEP();
        Node active = ZTSignature.findChildWithCtrl(pep, ZTSignature.ActiveZone);
        Node staging = ZTSignature.findChildWithCtrl(pep, ZTSignature.StagingZone);
        Node pdp = gen.getPDP();
        Node uAuth = ZTSignature.getAuthNode(pdp, ZTSignature.AuthUser);
        Node dAuth = ZTSignature.getAuthNode(pdp, ZTSignature.AuthDevice);

        // 处理---------------redex---------------
        BigraphBuilder bb = new BigraphBuilder(ZTSignature.ztSig);
        Root root = bb.addRoot();
        OuterName o1 = bb.addOuterName("outername_" + NameGenerator.DEFAULT.generate());
        OuterName o2 = bb.addOuterName("outername_" + NameGenerator.DEFAULT.generate());
        OuterName o3 = bb.addOuterName("outername_" + NameGenerator.DEFAULT.generate());
        OuterName o4 = bb.addOuterName("outername_" + NameGenerator.DEFAULT.generate());
        InnerName i1 = bb.addInnerName(NameGenerator.DEFAULT.generate(), o4);

        // 处理pep
        Node pepCopy = pep.replicate();
        pepCopy.setParent(root);
        Node activeCopy = active.replicate();
        Node stagingCopy = staging.replicate();
        Node reqCopy = NodeGenerator.cloneNodeWithoutSite(req, stagingCopy);
        activeCopy.setParent(pepCopy);
        stagingCopy.setParent(pepCopy);
        bb.addSite(stagingCopy);
        bb.addSite(activeCopy);
        pepCopy.getPort(RandBigraphGenerator.PEPPDPPort).setHandle(o2);
        pepCopy.getPort(RandBigraphGenerator.PEPDataPort).setHandle(o3);
        pepCopy.getPort(RandBigraphGenerator.PEPDevicePort).setHandle(o1);

        // 处理pdp
        Node pdpCopy = pdp.replicate();
        pdpCopy.setParent(root);
        bb.addSite(pdpCopy);
        Node uAuthCopy = uAuth.replicate();
        uAuthCopy.setParent(pdpCopy);
        bb.addSite(uAuthCopy);
        Node dAuthCopy = dAuth.replicate();
        dAuthCopy.setParent(pdpCopy);
        bb.addSite(dAuthCopy);
        Node uInfoCopy = uInfo.replicate();
        Node dInfoCopy = dInfo.replicate();
        uInfoCopy.setParent(uAuthCopy);
        dInfoCopy.setParent(dAuthCopy);
        pdpCopy.getPort(0).setHandle(o2);

        // 处理data
        Node dataCopy = data.replicate();
        dataCopy.getPort(0).setHandle(o4); // data可能连接PEP第三个端口，也可能已经连接了Connection节点
        dataCopy.setParent(root);
        redex[idx] = bb.makeBigraph(true);
        DebugPrinter.print(logger, "---------------conn redex--------------");
        redex[idx].print();

        // 处理---------------reactum---------------
        /*
        * xx:PEP[o1:outername, xx:outername, o2:outername].
            (xx:Staging.$0 || xx:ActiveZone.(xx:Connection[o4:edge].(xx:UserInfo || xx:DeviceInfo || xx:DataTag) || $1)) ||
          xx:PDP[xx:outername].($2 || xx:AuthUser.($3 || xx:UserInfo) || xx:AuthDevice.($4 || xx:DeviceInfo)) ||
          xx:Data[o4:edge]
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
        pepReact.getPort(RandBigraphGenerator.PEPDevicePort).setHandle(o11);
        pepReact.getPort(RandBigraphGenerator.PEPPDPPort).setHandle(o21);
        pepReact.getPort(RandBigraphGenerator.PEPDataPort).setHandle(o31);

        Node stagingReact = staging.replicate();
        stagingReact.setParent(pepReact);
        bb1.addSite(stagingReact);

        Node activeReact = active.replicate();
        activeReact.setParent(pepReact);
        bb1.addSite(activeReact);

        Node conn = NodeGenerator.newConn(bb1, activeReact);
        Node uInfoReact = uInfo.replicate();
        Node dInfoReact = dInfo.replicate();
        Node tagReact = dataTag.replicate();
        uInfoReact.setParent(conn);
        dInfoReact.setParent(conn);
        tagReact.setParent(conn);
        conn.getPort(0).setHandle(o41);
        conns.add(conn);

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
        Node uInfoReact1 = uInfo.replicate();
        Node dInfoReact1 = dInfo.replicate();
        uInfoReact1.setParent(uAuthReact);
        dInfoReact1.setParent(dAuthReact);
        pdpReact.getPort(0).setHandle(o21);

        Node dataReact = data.replicate();
        dataReact.getPort(0).setHandle(o41);
        dataReact.setParent(root1);

        reactum[idx] = bb1.makeBigraph(true);
        DebugPrinter.print(logger, "---------------conn reactum--------------");
        reactum[idx].print();
    }
}

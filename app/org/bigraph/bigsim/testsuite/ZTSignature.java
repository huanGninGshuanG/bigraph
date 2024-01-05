package org.bigraph.bigsim.testsuite;

import org.bigraph.bigsim.model.component.Child;
import org.bigraph.bigsim.model.component.Control;
import org.bigraph.bigsim.model.component.Node;
import org.bigraph.bigsim.model.component.Signature;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author huangningshuang
 * @date 2024/1/1
 */
public class ZTSignature {
    private static final Logger logger = LoggerFactory.getLogger(ZTSignature.class);

    // control常量字符串
    public static final String PDP = "PDP";
    public static final String PEP = "PEP";
    public static final String Device = "Device";
    public static final String DATA = "Data";
    public static final String AuthUser = "AuthUser";
    public static final String AuthDevice = "AuthDevice";
    public static final String CDMHist = "CDMHist";
    public static final String ActiveZone = "ActiveZone";
    public static final String StagingZone = "StagingZone";
    public static final String UserInfo = "UserInfo";
    public static final String DeviceInfo = "DeviceInfo";
    public static final String Connection = "Connection";
    public static final String Request = "Request";
    public static final String DataTag = "DataTag";

    public static Signature ztSig = new Signature(Arrays.asList(
            new Control(PDP, true, 1),
            new Control(PEP, true, 3),
            new Control(Device, true, 1),
            new Control(DATA, true, 1),
            // PDP中内容
            new Control(AuthUser, true, 0),
            new Control(AuthDevice, true, 0),
            new Control(CDMHist, true, 0), // 不合法的访问历史
            // PEP中内容
            new Control(ActiveZone, true, 0), // PEP中已建立连接部分
            new Control(StagingZone, true, 0), // PEP中还没有进行决策的部分
            // Device中内容
            new Control(UserInfo, true, 0),
            new Control(DeviceInfo, true, 0),
            // ActiveZone中内容
            new Control(Connection, true, 1),
            // Staging中内容
            new Control(Request, true, 0),
            new Control(DataTag, true, 0)
    ));

    public static Node findChildWithCtrl(Node node, String ctrlName) {
        for (Child c : node.getChildren()) {
            if (c.isNode()) {
                Node p = (Node) c;
                if (p.getControl().getName().equals(ctrlName)) return p;
            }
        }
        return null;
    }

    public static Node getDeviceInfo(Node device, String ctrlName) {
        if (!device.getControl().getName().equals(ZTSignature.Device))
            throw new RuntimeException("mismatch device control");
        Node res = findChildWithCtrl(device, ctrlName);
        if (res == null)
            throw new RuntimeException("device without " + ctrlName);
        return res;
    }

    public static Node getAuthNode(Node pdp, String ctrlName) {
        if (!pdp.getControl().getName().equals(ZTSignature.PDP))
            throw new RuntimeException("mismatch pdp control");
        Node res = findChildWithCtrl(pdp, ctrlName);
        if (res == null)
            throw new RuntimeException("pdp without " + ctrlName);
        return res;
    }
}

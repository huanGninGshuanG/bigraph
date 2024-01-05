package org.bigraph.bigsim.testsuite;

import org.bigraph.bigsim.data.Data;
import org.bigraph.bigsim.model.BigraphBuilder;
import org.bigraph.bigsim.model.component.*;
import org.bigraph.bigsim.utils.NameGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author huangningshuang
 * @date 2024/1/1
 */
public class NodeGenerator {
    public static Node newData(BigraphBuilder bb, Root root) {
        String name = ZTSignature.DATA + "_" + NameGenerator.DEFAULT.generate();
        return bb.addNode(name, ZTSignature.DATA, root, null);
    }

    public static void putDetailInfo(BigraphBuilder bb, Node prnt, List<String> userInfo, List<String> deviceInfo) {
        String uName = ZTSignature.UserInfo + "_" + NameGenerator.DEFAULT.generate();
        String dName = ZTSignature.DeviceInfo + "_" + NameGenerator.DEFAULT.generate();
        bb.addNode(uName, ZTSignature.UserInfo, prnt, null);
        bb.addNode(dName, ZTSignature.DeviceInfo, prnt, null);
        userInfo.add(uName);
        deviceInfo.add(dName);
    }

    static Node newDevice(BigraphBuilder bb, Root root, List<String> userInfo, List<String> deviceInfo) {
        String name = ZTSignature.Device + "_" + NameGenerator.DEFAULT.generate();
        Node device = bb.addNode(name, ZTSignature.Device, root, null);
        putDetailInfo(bb, device, userInfo, deviceInfo);
        return device;
    }

    public static Node newNode(BigraphBuilder bb, Parent prnt, String ctrlName) {
        String name = ctrlName + "_" + NameGenerator.DEFAULT.generate();
        return bb.addNode(name, ctrlName, prnt, null);
    }

    public static Node newNodeWithName(BigraphBuilder bb, Parent prnt, String ctrlName, String name) {
        return bb.addNode(name, ctrlName, prnt, null);
    }

    public static Node newPEP(BigraphBuilder bb, Root root) {
        String name = ZTSignature.PEP + "_" + NameGenerator.DEFAULT.generate();
        Node pep = bb.addNode(name, ZTSignature.PEP, root, null);
        bb.addNode(ZTSignature.ActiveZone + "_" + NameGenerator.DEFAULT.generate(), ZTSignature.ActiveZone, pep, null);
        bb.addNode(ZTSignature.StagingZone + "_" + NameGenerator.DEFAULT.generate(), ZTSignature.StagingZone, pep, null);
        return pep;
    }

    public static Node newPDP(BigraphBuilder bb, Root root, List<String> authedUsers, List<String> authedDevices) {
        String name = ZTSignature.PDP + "_" + NameGenerator.DEFAULT.generate();
        Node pdp = bb.addNode(name, ZTSignature.PDP, root, null);
        bb.addNode(ZTSignature.CDMHist + "_" + NameGenerator.DEFAULT.generate(), ZTSignature.CDMHist, pdp, null);
        Node authU = bb.addNode(ZTSignature.AuthUser + "_" + NameGenerator.DEFAULT.generate(), ZTSignature.AuthUser, pdp, null);
        Node authD = bb.addNode(ZTSignature.AuthDevice + "_" + NameGenerator.DEFAULT.generate(), ZTSignature.AuthDevice, pdp, null);
        // 授权过的用户、设备信息要放入对应的模块
        for (String uName : authedUsers) {
            bb.addNode(uName, ZTSignature.UserInfo, authU, null);
        }
        for (String dName : authedDevices) {
            bb.addNode(dName, ZTSignature.DeviceInfo, authD, null);
        }
        return pdp;
    }

    public static Node newDataTag(BigraphBuilder bb, Parent prnt, String name) {
        return newNodeWithName(bb, prnt, ZTSignature.DataTag, name);
    }

    public static Node newRequest(BigraphBuilder bb, Parent prnt) {
        return newNode(bb, prnt, ZTSignature.Request);
    }

    // 拷贝位置图，handle没有进行拷贝
    public static Node cloneNode(Node p, BigraphBuilder bb, Parent prnt) {
        Node res = p.replicate();
        res.setParent(prnt);
        class VState {
            Parent p;
            PlaceEntity cur;

            VState(Parent p, PlaceEntity cur) {
                this.p = p;
                this.cur = cur;
            }
        }
        Queue<VState> q = new LinkedList<>();
        for (Child c : p.getChildren()) {
            q.offer(new VState(res, c));
        }
        while (!q.isEmpty()) {
            VState state = q.poll();
            if (state.cur.isNode()) {
                Node ori = (Node) state.cur;
                Node copy = ori.replicate();
                copy.setParent(state.p);
                for (Child c : ori.getChildren()) {
                    q.offer(new VState(copy, c));
                }
            } else if (state.cur.isSite()) {
                bb.addSite(state.p);
            }
        }
        return res;
    }
}

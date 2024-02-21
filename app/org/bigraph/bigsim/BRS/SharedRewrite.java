package org.bigraph.bigsim.BRS;

import domain.vo.MenuVo;
import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.BigraphBuilder;
import org.bigraph.bigsim.model.SharedBigraph;
import org.bigraph.bigsim.model.component.InnerName;
import org.bigraph.bigsim.model.component.Root;
import org.bigraph.bigsim.model.component.Site;
import org.bigraph.bigsim.model.component.shared.SharedRoot;
import org.bigraph.bigsim.model.component.shared.SharedSite;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;

/**
 * @author huangningshuang
 * @date 2024/2/19
 */
public class SharedRewrite {
    private static final Logger logger = LoggerFactory.getLogger(SharedRewrite.class);

    public static Bigraph rewrite(SharedCSPMatch match, SharedBigraph redex, SharedBigraph reactum) {
        BigraphBuilder bb = new BigraphBuilder(redex.bigSignature(), true);
        bb.setBigraph(match.param);
        SharedBigraph lmb = match.lambda;

        // 修剪lmb的内接口，使得能够完成组合操作
        for (InnerName i : lmb.bigInner().values()) {
            if (!bb.containsOuterName(i.getName())) {
                lmb.bigInner().remove(i.getName());
                i.setHandle(null);
            }
        }
        //  lmb要删掉一部分site+root(lmb的位置图为id)
        for (int i = lmb.sharedSites().size() - bb.getSharedRoots().size(); i > 0; i--) {
            lmb.sharedRoots().remove(lmb.sharedRoots().size() - 1);
            lmb.sharedSites().remove(lmb.sharedSites().size() - 1);
        }
        // reactum sites的数量更多，lmb需要补充一部分
        for (int i = bb.getSharedRoots().size() - lmb.sharedSites().size(); i > 0; i--) {
            SharedRoot r = new SharedRoot();
            SharedSite s = new SharedSite(r);
            lmb.sharedSites().add(s);
            lmb.sharedRoots().add(r);
        }
        bb.sharedCompose(lmb);
        DebugPrinter.print(logger, "stage 1: ");
        bb.makeBigraph(false).print();

        SharedBigraph clone = reactum.clone();
        SharedBigraph react = SharedBigraph.juxtapose(clone, match.rdxId);
        bb.sharedCompose(react);
        DebugPrinter.print(logger, "stage 2: ");
        clone.print();
        bb.makeBigraph(false).print();

        bb.sharedCompose(match.context);

        Bigraph result = bb.makeBigraph(true);
        if (!result.isConsistent()) {
            throw new RuntimeException("Inconsistent shared bigraph");
        }
        DebugPrinter.print(logger, "shared rewrite ans: ");
        result.print();
        return result;
    }
}

package org.bigraph.bigsim.BRS;

import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.BigraphBuilder;
import org.bigraph.bigsim.model.component.InnerName;
import org.bigraph.bigsim.model.component.Root;
import org.bigraph.bigsim.model.component.Site;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author huangningshuang
 * @date 2023/12/17
 */
public class Rewrite {
    private static final Logger logger = LoggerFactory.getLogger(Rewrite.class);

    public static Bigraph rewrite(CSPMatch match, Bigraph redex, Bigraph reactum, InstantiationMap eta) {
        BigraphBuilder bb = new BigraphBuilder(redex.getSignature());
        for (int i = eta.getDomain() - 1; i >= 0; --i) {
            bb.leftJuxtapose(match.params.get(eta.getCod(i)));
        }
        Bigraph lmb = match.lambda;
        // 修剪lmb的内接口，使得能够完成组合操作
        for (InnerName i : lmb.bigInner().values()) {
            if (!bb.containsOuterName(i.getName())) {
                lmb.bigInner().remove(i.getName());
                i.setHandle(null);
            }
        }
        // redex sites数量更多，lmb要删掉一部分site+root(lmb的位置图为id)
        for (int i = lmb.bigSites().size() - bb.getRoots().size(); i > 0; i--) {
            lmb.bigRoots().remove(lmb.bigRoots().size() - 1);
            lmb.bigSites().remove(lmb.bigSites().size() - 1);
        }
        // reactum sites的数量更多，lmb需要补充一部分
        for (int i = bb.getRoots().size() - lmb.bigSites().size(); i > 0; i--) {
            Root r = new Root();
            Site s = new Site(r);
            lmb.bigSites().add(s);
            lmb.bigRoots().add(r);
        }
        // lmb /compose (p1 /otimes p2 ... /otimes pn)
        bb.compose(lmb);
        Bigraph reactumCopy = reactum.clone();
        // r' /otimes id
        reactumCopy = Bigraph.juxtapose(reactumCopy, match.rdxId);
        bb.compose(reactumCopy);
        bb.compose(match.context);
        Bigraph result = bb.makeBigraph(true);
        if (!result.isConsistent()) {
            throw new RuntimeException("Inconsistent bigraph");
        }
        return result;
    }
}

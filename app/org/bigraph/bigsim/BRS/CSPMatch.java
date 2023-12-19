package org.bigraph.bigsim.BRS;

import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.component.Node;
import org.bigraph.bigsim.utils.BidMap;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author huangningshuang
 * @date 2023/12/4
 */
public class CSPMatch {
    private static final Logger logger = LoggerFactory.getLogger(CSPMatch.class);

    Bigraph context, rdxImg, rdxId;
    final Bigraph lambda;
    final List<Bigraph> params;

    private Bigraph redex; // rdxImg /otimes rdxId

    private BidMap<Node, Node> embNodes; // 位置嵌入问题的解

    CSPMatch(Bigraph context, Bigraph redexImage, Bigraph redexId, Bigraph paramWiring, Bigraph[] params, BidMap<Node, Node> nodeEmbedding) {
        this.context = context;
        this.rdxImg = redexImage;
        this.rdxId = redexId;
        this.params = Collections.unmodifiableList(Arrays.asList(params));
        this.lambda = paramWiring;
        this.embNodes = nodeEmbedding;
    }

    public Bigraph getRedex() {
        if (redex == null) {
            redex = Bigraph.juxtapose(this.rdxImg, this.rdxId);
        }
        return redex;
    }

    public Bigraph getRedexImage() {
        return this.rdxImg;
    }

    public Bigraph getRedexId() {
        return this.rdxId;
    }

    public void print() {
        DebugPrinter.print(logger, "======================CSPMatch=====================");
        DebugPrinter.print(logger, "context: ");
        context.print();
        DebugPrinter.print(logger, "redex image:");
        rdxImg.print();
        DebugPrinter.print(logger, "redex id:");
        rdxId.print();
        DebugPrinter.print(logger, "paramWiring: ");
        lambda.print();
        int i = 0;
        for (Bigraph prm : params) {
            DebugPrinter.print(logger, "param " + i + ":");
            prm.print();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AgentMatch:\ncontext = ").append(context)
                .append("\nredexImage = ").append(rdxImg)
                .append("\nredexId = ").append(rdxId)
                .append("\nparamWiring = ").append(lambda);
        int i = 0;
        for (Bigraph prm : params) {
            builder.append("\nparam[").append(i++).append("] = ").append(prm);
        }
        return builder.toString();
    }
}

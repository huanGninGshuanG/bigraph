package org.bigraph.bigsim.BRS;

import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.SharedBigraph;
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
 * @date 2024/1/22
 */
public class SharedCSPMatch {
    private static final Logger logger = LoggerFactory.getLogger(SharedCSPMatch.class);

    SharedBigraph context, rdxImg, rdxId, param;
    final SharedBigraph lambda;

    private SharedBigraph redex; // rdxImg /otimes rdxId

    SharedCSPMatch(SharedBigraph context, SharedBigraph redexImage, SharedBigraph redexId, SharedBigraph paramWiring,
             SharedBigraph param) {
        this.context = context;
        this.rdxImg = redexImage;
        this.rdxId = redexId;
        this.param = param;
        this.lambda = paramWiring;
    }

    public SharedBigraph getRedex() {
        if (redex == null) {
            redex = SharedBigraph.juxtapose(this.rdxImg, this.rdxId);
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
        DebugPrinter.print(logger, "param:");
        param.print();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AgentMatch:\ncontext = ").append(context)
                .append("\nredexImage = ").append(rdxImg)
                .append("\nredexId = ").append(rdxId)
                .append("\nparamWiring = ").append(lambda)
                .append("\nparam = ").append(param);
        return builder.toString();
    }
}

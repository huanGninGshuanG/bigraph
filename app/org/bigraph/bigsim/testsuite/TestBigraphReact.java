package org.bigraph.bigsim.testsuite;

import org.bigraph.bigsim.BRS.CSPMatch;
import org.bigraph.bigsim.BRS.CSPMatcher;
import org.bigraph.bigsim.BRS.Rewrite;
import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.ReactionRule;
import org.bigraph.bigsim.model.Term;
import org.bigraph.bigsim.simulator.LTLSimulator;
import org.bigraph.bigsim.testsuite.ztrules.Rule;
import org.bigraph.bigsim.testsuite.ztrules.ZTRuleConnection;
import org.bigraph.bigsim.testsuite.ztrules.ZTRuleFinish;
import org.bigraph.bigsim.testsuite.ztrules.ZTRuleSendRequest;
import org.bigraph.bigsim.utils.BigraphToTerm;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author huangningshuang
 * @date 2024/1/1
 */
public class TestBigraphReact {
    private static final Logger logger = LoggerFactory.getLogger(TestBigraphReact.class);
    private static Bigraph agent;
    private static List<Rule> rules = new ArrayList<>();

    private static void init() {
        RandBigraphGenerator gen = new RandBigraphGenerator();
        agent = gen.makePrimeBigraph();
        ZTRuleSendRequest send = new ZTRuleSendRequest(gen);
        ZTRuleConnection conn = new ZTRuleConnection(gen, send);
        ZTRuleFinish finish = new ZTRuleFinish(gen, conn);
        for (int i = 0; i < send.getRedex().length; i++) {
            agent.rules().add(new ReactionRule("send_" + i, send.getRedex()[i], send.getReactum()[i]));
            rules.add(new Rule(send.getRedex()[i], send.getReactum()[i], send.getEta()[i]));
        }
        for (int i = 0; i < conn.getRedex().length; i++) {
            agent.rules().add(new ReactionRule("conn_" + i, conn.getRedex()[i], conn.getReactum()[i]));
            rules.add(new Rule(conn.getRedex()[i], conn.getReactum()[i], conn.getEta()[i]));
        }
        for (int i = 0; i < finish.getRedex().length; i++) {
            agent.rules().add(new ReactionRule("finish_" + i, finish.getRedex()[i], finish.getReactum()[i]));
            rules.add(new Rule(finish.getRedex()[i], finish.getReactum()[i], finish.getEta()[i]));
        }
        agent.addLTLSpec("true");
    }

    private static List<Bigraph> reacts = new ArrayList<>();

    private static void react(Bigraph agent) {
        reacts.clear();
        CSPMatcher matcher = new CSPMatcher();
        for (Rule r : rules) {
            Iterator<? extends CSPMatch> iterator = matcher.match(agent, r.getRedex()).iterator();
            DebugPrinter.print(logger, "match result: " + iterator.hasNext());
            while (iterator.hasNext()) {
                CSPMatch match = iterator.next();
                Bigraph nb = Rewrite.rewrite(match, r.getRedex(), r.getReactum(), r.getEta());
                DebugPrinter.print(logger, "---------new bigraph---------");
                nb.print();
                reacts.add(nb);
            }
        }
    }

    private static void baseTest() {
        react(agent);
        if (reacts.size() > 0) {
            DebugPrinter.print(logger, "-----------second try-----------");
            react(reacts.get(0));
        }
        if (reacts.size() > 0) {
            DebugPrinter.print(logger, "-----------third try-----------");
            react(reacts.get(0));
        }
        if (reacts.size() > 0) {
            DebugPrinter.print(logger, "-----------forth try-----------");
            react(reacts.get(0));
        }
    }

    public static void main(String[] args) {
        init();
        long start = System.currentTimeMillis();
        LTLSimulator simulator = new LTLSimulator(agent);
        simulator.simulate();
        String str = simulator.dumpDotForward("");
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        DebugPrinter.print(logger, "time spent: " + (double) timeElapsed / 1000);
        DebugPrinter.print(logger, "result str");
        DebugPrinter.print(logger, str);
    }
}

package org.bigraph.bigsim.model;

import org.bigraph.bigsim.model.component.Control;
import org.bigraph.bigsim.model.component.Node;
import org.bigraph.bigsim.model.component.*;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author huangningshuang
 * @date 2023/12/2
 */
public class BigraphBuilder implements BigraphHandler {
    private Bigraph bigraph;
    private boolean closed = false;
    private static final Logger logger = LoggerFactory.getLogger(BigraphBuilder.class);

    public BigraphBuilder(Signature signature) {
        bigraph = new Bigraph();
        bigraph.setSignature(signature);
    }

    private void assertOpen() {
        if (closed) {
            throw new UnsupportedOperationException("The operation is not supported by a closed BigraphBuilder");
        }
    }

    private void assertConsistency() {
        if (!bigraph.isConsistent())
            throw new RuntimeException("Inconsistent bigraph.");
    }

    public Bigraph getBigraph() {
        return bigraph;
    }

    @Override
    public boolean isEmpty() {
        assertOpen();
        return this.bigraph.isEmpty();
    }

    @Override
    public boolean isGround() {
        assertOpen();
        return this.bigraph.isGround();
    }

    @Override
    public List<? extends Root> getRoots() {
        assertOpen();
        return this.bigraph.getRoots();
    }

    @Override
    public List<? extends Site> getSites() {
        assertOpen();
        return this.bigraph.getSites();
    }

    @Override
    public Collection<? extends OuterName> getOuterNames() {
        assertOpen();
        return this.bigraph.getOuterNames();
    }

    @Override
    public Collection<? extends InnerName> getInnerNames() {
        assertOpen();
        return this.bigraph.getInnerNames();
    }

    @Override
    public Collection<? extends Node> getNodes() {
        assertOpen();
        return this.bigraph.getNodes();
    }

    @Override
    public Collection<? extends Edge> getEdges() {
        assertOpen();
        return this.bigraph.getEdges();
    }

    public boolean containsOuterName(String name) {
        assertOpen();
        return this.bigraph.bigOuter().containsKey(name);
    }

    public boolean containsInnerName(String name) {
        assertOpen();
        return this.bigraph.bigInner().containsKey(name);
    }

    public Root addRoot() {
        assertOpen();
        Root r = new Root(this);
        this.bigraph.bigRoots().add(r);
        assertConsistency();
        return r;
    }

    public Site addSite(String name, Parent parent) {
        if (parent == null)
            throw new IllegalArgumentException("Argument can not be null.");
        assertOpen();
        Site s = new Site(name, parent, this);
        this.bigraph.bigSites().add(s);
        assertConsistency();
        return s;
    }

    public Node addNode(String name, String ctrlName, Parent parent, List<Handle> handles) {
        if (ctrlName == null)
            throw new IllegalArgumentException("Control name can not be null.");
        if (parent == null)
            throw new IllegalArgumentException("Parent can not be null.");
        assertOpen();
        Control c = this.bigraph.bigSignature().getByName(ctrlName);
        if (c == null)
            throw new IllegalArgumentException("Control should be in the signature.");
        int ar = c.getArity();
        List<Handle> hs = new ArrayList<>(ar);
        Iterator<Handle> hi = (handles == null) ? null : handles.iterator();
        for (int i = 0; i < ar; i++) {
            Handle h = null;
            if (hi != null && hi.hasNext()) {
                h = hi.next();
            }
//            if (h == null) {
//                Edge e = new Edge();
//                bigraph.onEdgeAdded(e);
//                h = e;
//            }
            hs.add(h);
        }
        Node n = new Node(name, c, parent, hs);
        this.bigraph.onNodeAdded(n);
        assertConsistency();
        return n;
    }

    public OuterName addOuterName(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Argument can not be null.");
        return addOuterName(new OuterName(name));
    }

    private OuterName addOuterName(OuterName name) {
        assertOpen();
        if (bigraph.bigOuter().containsKey(name.getName())) {
            throw new IllegalArgumentException("Name '" + name.getName() + "' already present.");
        }
        this.bigraph.bigOuter().put(name.getName(), name);
        assertConsistency();
        return name;
    }

    public InnerName addInnerName(String name, Handle handle) {
        if (name == null)
            throw new IllegalArgumentException("Name can not be null.");
        if (handle.isEdge()) {
            this.bigraph.onEdgeAdded((Edge) handle);
        }
        return addInnerName(new InnerName(name), handle);
    }

    public InnerName addInnerName(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Name can not be null.");
        Edge e = new Edge();
        bigraph.onEdgeAdded(e);
        return addInnerName(name, e);
    }

    private InnerName addInnerName(InnerName n, Handle h) {
        assertOpen();
        if (bigraph.bigInner().containsKey(n.getName())) {
            throw new IllegalArgumentException("Name already present.");
        }
        n.setHandle(h);
        this.bigraph.bigInner().put(n.getName(), n);
        assertConsistency();
        return n;
    }

    /// 连接points到一个新的边
    public Edge relink(Collection<? extends Point> points) {
        if (points == null)
            throw new IllegalArgumentException("Argument can not be null.");
        return (Edge) relink(new Edge(), points.toArray(new Point[0]));
    }

    public Handle relink(Handle handle, Collection<? extends Point> points) {
        if (points == null)
            throw new IllegalArgumentException("Argument can not be null.");
        return relink(handle, points.toArray(new Point[0]));
    }

    public Handle relink(Handle handle, Point... points) {
        assertOpen();
        for (int i = 0; i < points.length; i++) {
            Handle old = points[i].getHandle();
            points[i].setHandle(handle);
            if (old != null && old.isEdge() && old.getPoints().isEmpty()) {
                bigraph.onEdgeRemoved((Edge) old);
            }
        }
        if (handle.isEdge()) {
            bigraph.onEdgeAdded((Edge) handle);
        }
        assertConsistency();
        return handle;
    }

    /// 在组合中使用，outername连接到一条边
    public Edge closeOuterName(OuterName name) {
        if (!bigraph.bigOuter().containsKey(name.getName())) {
            throw new IllegalArgumentException("Name '" + name.getName() + "' not present.");
        }
        Edge e = relink(name.getPoints());
        bigraph.bigOuter().remove(name.getName());
        return e;
    }

    public void closeInnerName(InnerName name) {
        if (!bigraph.bigInner().containsKey(name.getName())) {
            throw new IllegalArgumentException("Name '" + name.getName() + "' not present.");
        }
        Handle h = name.getHandle();
        name.setHandle(null);
        bigraph.bigInner().remove(name.getName());
        if (h.isEdge() && h.getPoints().isEmpty()) {
            bigraph.onEdgeRemoved((Edge) h);
        }
    }

    public void setBigraph(Bigraph big) {
        this.bigraph = big;
    }

    private void dfsTerm(Term term, Parent parent) {
        if (term.termType() == TermType.TPAR()) {
            Parent finalParent = parent;
            ((Paraller) term).getChildren().foreach(child -> {
                dfsTerm(child, finalParent);
                return true;
            });
        } else if (term.termType() == TermType.THOLE()) {
            addSite(term.toString(), parent);
        } else if (term.termType() == TermType.TPREF()) {
            Prefix pref = (Prefix) term;
            org.bigraph.bigsim.model.Node node = pref.node();
            List<Handle> hs = new ArrayList<>(node.ctrl().arity());
            node.ports().foreach(port -> {
                String handleName = port.name(), nameType = port.nameType();
                if (nameType.equals("outername")) {
                    OuterName outerName = null;
                    if (bigraph.bigOuter().containsKey(handleName)) outerName = bigraph.bigOuter().get(handleName);
                    else outerName = addOuterName(handleName);
                    if (outerName != null) hs.add(outerName);
                } else if (nameType.equals("edge")) {
                    Collection<Edge> edges = bigraph.edgesProxy().get();
                    boolean finded = false;
                    for (Edge edge : edges) {
                        if (edge.getName().equals(handleName)) {
                            hs.add(edge);
                            finded = true;
                            break;
                        }
                    }
                    if (!finded) {
                        Edge e = new Edge(handleName);
                        hs.add(e);
                        bigraph.onEdgeAdded(e);
                    }
                } else if (nameType.equals("idle")) {
                    hs.add(null);
                }
                return true;
            });
            parent = addNode(node.name(), node.ctrl().name(), parent, hs);
            dfsTerm(pref.suffix(), parent);
        }
    }

    public void parseTerm(Term term) {
//        DebugPrinter.print(logger, "all names: " + bigraph.root().getAllNames());
        Term p = term.next();
        if (p.termType() == TermType.TREGION()) {
            ((Regions) p).getChildren().foreach(child -> {
                Parent parent = addRoot();
                DebugPrinter.print(logger, "region child is: " + child);
                dfsTerm(child, parent);
                return true;
            });
        } else {
            DebugPrinter.print(logger, "region child is: " + term);
            Parent parent = addRoot();
            dfsTerm(term, parent);
        }
    }
}

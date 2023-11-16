package org.bigraph.bigsim.ctlimpl;

import org.bigraph.bigsim.transitionsystem.State;

import java.util.ArrayList;
import java.util.List;

public class CTLCheckResult {
    public enum PathType {
        WitnessType,
        CounterExample,
        NoNeed,
    }

    public boolean res;
    public List<State> path;
    public PathType type;

    public CTLCheckResult() {
        type = PathType.NoNeed;
        path = new ArrayList<>();
    }
}

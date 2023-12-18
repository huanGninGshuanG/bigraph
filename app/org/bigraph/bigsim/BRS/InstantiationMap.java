package org.bigraph.bigsim.BRS;

import java.util.List;

/**
 * @author huangningshuang
 * @date 2023/12/17
 * maps site from reactum to redex
 */

public class InstantiationMap {
    private int dom; // reactum sites
    private int cod; // redex sites
    private int[] map;

    public static InstantiationMap getIdMap(int size) {
        int[] map = new int[size];
        for (int i = 0; i < size; i++) map[i] = i;
        return new InstantiationMap(size, map);
    }

    public InstantiationMap(int codomain, int[] map) {
        this.cod = codomain;
        this.dom = map.length;
        this.map = new int[dom];
        for (int i = 0; i < map.length; i++) {
            int j = map[i];
            // 大于redex site数量
            if (j < 0 || j > this.cod) throw new IllegalArgumentException("Invalid instantiation map");
            this.map[i] = j;
        }
    }

    public int getDomain() {
        return dom;
    }

    public int getCodomain() {
        return cod;
    }

    public int getCod(int idx) {
        if (idx >= 0 && idx < this.dom) return map[idx];
        return -1;
    }
}

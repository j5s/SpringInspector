package org.sec.model;

import java.util.ArrayList;
import java.util.List;

public class Chain {
    private final List<CallGraph> chains;

    public Chain() {
        chains = new ArrayList<>();
    }

    public Chain(Chain chain) {
        this.chains = new ArrayList<>(chain.getChain());
    }

    public List<CallGraph> getChain() {
        return chains;
    }

    public void addChain(CallGraph callGraph) {
        chains.add(callGraph);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("---chain---");
        sb.append("\n");
        for (CallGraph callGraph : chains) {
            sb.append(callGraph.getCallerMethod().getClassReference().getName());
            sb.append(".");
            sb.append(callGraph.getCallerMethod().getName());
            sb.append("#");
            sb.append(callGraph.getCallerArgIndex());
            sb.append("--->");
            sb.append(callGraph.getTargetMethod().getClassReference().getName());
            sb.append(".");
            sb.append(callGraph.getTargetMethod().getName());
            sb.append("#");
            sb.append(callGraph.getTargetArgIndex());
            sb.append("\n");
        }
        sb.append("-----------");
        sb.append("\n");
        return sb.toString();
    }
}

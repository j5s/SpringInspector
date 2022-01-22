package org.sec.core.service;

import org.sec.log.SLF4J;
import org.sec.model.MethodReference;
import org.slf4j.Logger;

import java.util.*;

@SLF4J
public class SortService {
    @SuppressWarnings("all")
    private static Logger logger;

    public static List<MethodReference.Handle> start(
            Map<MethodReference.Handle, Set<MethodReference.Handle>> methodCalls) {
        logger.info("topological sort methods");
        Map<MethodReference.Handle, Set<MethodReference.Handle>> outgoingReferences = new HashMap<>();
        for (Map.Entry<MethodReference.Handle, Set<MethodReference.Handle>> entry : methodCalls.entrySet()) {
            MethodReference.Handle method = entry.getKey();
            outgoingReferences.put(method, new HashSet<>(entry.getValue()));
        }
        Set<MethodReference.Handle> dfsStack = new HashSet<>();
        Set<MethodReference.Handle> visitedNodes = new HashSet<>();
        List<MethodReference.Handle> sortedMethods = new ArrayList<>(outgoingReferences.size());
        for (MethodReference.Handle root : outgoingReferences.keySet()) {
            dfsSort(outgoingReferences, sortedMethods, visitedNodes, dfsStack, root);
        }
        return sortedMethods;
    }

    private static void dfsSort(Map<MethodReference.Handle, Set<MethodReference.Handle>> outgoingReferences,
                               List<MethodReference.Handle> sortedMethods, Set<MethodReference.Handle> visitedNodes,
                               Set<MethodReference.Handle> stack, MethodReference.Handle node) {
        if (stack.contains(node)) {
            return;
        }
        if (visitedNodes.contains(node)) {
            return;
        }
        Set<MethodReference.Handle> outgoingRefs = outgoingReferences.get(node);
        if (outgoingRefs == null) {
            return;
        }
        stack.add(node);
        for (MethodReference.Handle child : outgoingRefs) {
            dfsSort(outgoingReferences, sortedMethods, visitedNodes, stack, child);
        }
        stack.remove(node);
        visitedNodes.add(node);
        sortedMethods.add(node);
    }
}

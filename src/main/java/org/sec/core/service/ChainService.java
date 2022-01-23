package org.sec.core.service;

import org.sec.core.spring.SpringController;
import org.sec.core.spring.SpringMapping;
import org.sec.core.spring.SpringParam;
import org.sec.model.CallGraph;
import org.sec.model.Chain;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChainService {
    private static Logger logger;

    public static void start(List<Chain> chains, List<SpringController> controllers,
                             Set<CallGraph> discoveredCalls,
                             Map<MethodReference.Handle, Set<CallGraph>> graphCallMap,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             Map<MethodReference.Handle, MethodReference> methodMap) {
        controllers.forEach(controller -> controller.getMappings().forEach(mapping -> {
            Set<CallGraph> callGraphs = graphCallMap.get(mapping.getMethodName());
            mapping.getParamMap().forEach(param -> {
                int argIndex = param.getParamIndex() + 1;
                for (CallGraph callGraph : callGraphs) {
                    if (callGraph.getCallerArgIndex() == argIndex) {
                        Chain chain = new Chain();
                        chain.addChain(callGraph);
                        CallGraph last = callGraph;
                        boolean hasNext = true;
                        List<CallGraph> tempList = new ArrayList<>(discoveredCalls);
                        while (hasNext) {
                            for (int i = 0; i < tempList.size(); i++) {
                                boolean argEqual = tempList.get(i).getCallerArgIndex() ==
                                        last.getTargetArgIndex();
                                boolean methodEqual = methodMap.get(tempList.get(i).getCallerMethod()) ==
                                        methodMap.get(last.getTargetMethod());
                                if (argEqual && methodEqual) {
                                    chain.addChain(tempList.get(i));
                                    last = tempList.get(i);
                                    break;
                                }
                                if (i == tempList.size() - 1) {
                                    hasNext = false;
                                }
                            }
                        }
                        chains.add(chain);
                    }
                }
            });
        }));
        System.out.println(chains);
    }
}

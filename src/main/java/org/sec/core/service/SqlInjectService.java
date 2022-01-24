package org.sec.core.service;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.sec.core.asm.SqlInjectClassVisitor;
import org.sec.core.spring.SpringController;
import org.sec.core.spring.SpringMapping;
import org.sec.log.SLF4J;
import org.sec.model.CallGraph;
import org.sec.model.ClassFile;
import org.sec.model.MethodReference;
import org.sec.model.ResultInfo;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SLF4J
public class SqlInjectService {
    private static Logger logger;

    private static Map<MethodReference.Handle, Set<CallGraph>> allCalls;
    private static Map<String, ClassFile> classFileMap;
    private static final List<String> tempChain = new ArrayList<>();
    private static final List<ResultInfo> results = new ArrayList<>();

    private static boolean hasSqlInject;

    public static void start(Map<String, ClassFile> classFileByName,
                                   List<SpringController> controllers,
                                   Map<MethodReference.Handle, Set<CallGraph>> discoveredCalls) {
        results.clear();
        tempChain.clear();
        hasSqlInject = false;
        allCalls = discoveredCalls;
        classFileMap = classFileByName;
        logger.info("start analysis sql inject");
        for (SpringController controller : controllers) {
            for (SpringMapping mapping : controller.getMappings()) {
                MethodReference methodReference = mapping.getMethodReference();
                if (methodReference == null) {
                    continue;
                }
                Type[] argTypes = Type.getArgumentTypes(methodReference.getDesc());
                Type[] extendedArgTypes = new Type[argTypes.length + 1];
                System.arraycopy(argTypes, 0, extendedArgTypes, 1, argTypes.length);
                argTypes = extendedArgTypes;
                boolean[] vulnerableIndex = new boolean[argTypes.length];
                for (int i = 1; i < argTypes.length; i++) {
                    if (argTypes[i].getClassName().equals("java.lang.String")) {
                        vulnerableIndex[i] = true;
                    }
                }
                Set<CallGraph> calls = allCalls.get(methodReference.getHandle());
                if (calls == null || calls.size() == 0) {
                    continue;
                }
                tempChain.add(methodReference.getClassReference().getName() + "." + methodReference.getName());
                for (CallGraph callGraph : calls) {
                    int callerIndex = callGraph.getCallerArgIndex();
                    if (callerIndex == -1) {
                        continue;
                    }
                    if (vulnerableIndex[callerIndex]) {
                        tempChain.add(callGraph.getTargetMethod().getClassReference().getName() + "." +
                                callGraph.getTargetMethod().getName());
                        List<MethodReference.Handle> visited = new ArrayList<>();
                        doTask(callGraph.getTargetMethod(), callGraph.getTargetArgIndex(), visited);
                        if (hasSqlInject) {
                            ResultInfo resultInfo = new ResultInfo();
                            resultInfo.setRisk(ResultInfo.HIGH_RISK);
                            resultInfo.setVulName("SQL Inject");
                            resultInfo.getChains().addAll(tempChain);
                            results.add(resultInfo);
                            System.out.println(resultInfo);
                        }
                        hasSqlInject = false;
                    }
                }
                tempChain.clear();
            }
        }
    }

    public static List<ResultInfo> getResults() {
        return results;
    }

    private static void doTask(MethodReference.Handle targetMethod, int targetIndex,
                                     List<MethodReference.Handle> visited) {
        if (visited.contains(targetMethod)) {
            return;
        } else {
            visited.add(targetMethod);
        }
        ClassFile file = classFileMap.get(targetMethod.getClassReference().getName());
        try {
            if(file==null){
                return;
            }
            ClassReader cr = new ClassReader(file.getFile());
            SqlInjectClassVisitor cv = new SqlInjectClassVisitor(targetMethod, targetIndex);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            if (cv.getSave().contains(true)) {
                    hasSqlInject = true;
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Set<CallGraph> calls = allCalls.get(targetMethod);
        if (calls == null || calls.size() == 0) {
            return;
        }
        for (CallGraph callGraph : calls) {
            if (callGraph.getCallerArgIndex() == targetIndex && targetIndex != -1) {
                if (visited.contains(callGraph.getTargetMethod())) {
                    return;
                }
                tempChain.add(callGraph.getTargetMethod().getClassReference().getName() + "." +
                        callGraph.getTargetMethod().getName());
                doTask(callGraph.getTargetMethod(), callGraph.getTargetArgIndex(), visited);
            }
        }
    }
}

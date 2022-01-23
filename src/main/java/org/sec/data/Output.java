package org.sec.data;

import org.sec.core.spring.SpringController;
import org.sec.core.spring.SpringMapping;
import org.sec.core.spring.SpringParam;
import org.sec.log.SLF4J;
import org.sec.model.CallGraph;
import org.sec.model.MethodReference;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SLF4J
@SuppressWarnings("all")
public class Output {

    private static Logger logger;

    private static String getCallGraph(Map<MethodReference.Handle, Set<CallGraph>> graphCallMap,
                                       String packageName) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<MethodReference.Handle, Set<CallGraph>> item : graphCallMap.entrySet()) {
            if (item.getKey().getClassReference().getName().startsWith(packageName)) {
                for (CallGraph callGraph : item.getValue()) {
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
            }
        }
        return sb.toString();
    }

    public static void writeTargetCallGraphs(Map<MethodReference.Handle, Set<CallGraph>> graphCallMap,
                                             String packageName) {
        logger.info("write call graphs data");
        if (packageName == null || packageName.equals("")) {
            logger.error("need package name config");
            return;
        }
        packageName = packageName.replace(".", "/");
        try {
            Files.write(Paths.get("calls.txt"),
                    getCallGraph(graphCallMap, packageName).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeSortedMethod(List<MethodReference.Handle> sortedMethods) {
        logger.info("write sorted method data");
        StringBuilder sb = new StringBuilder();
        for (MethodReference.Handle method : sortedMethods) {
            sb.append(method.getClassReference().getName());
            sb.append(".");
            sb.append(method.getName());
            sb.append("\n");
        }
        try {
            Files.write(Paths.get("sorted.txt"), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeControllers(List<SpringController> controllers) {
        logger.info("write spring controllers");
        StringBuilder sb = new StringBuilder();
        for (SpringController controller : controllers) {
            sb.append(controller.getClassReference().getName());
            sb.append("\n");
            for (SpringMapping mapping : controller.getMappings()) {
                sb.append("\t");
                sb.append(mapping.getMethodName().getName());
                sb.append("\t");
                for (SpringParam param : mapping.getParamMap()) {
                    sb.append(param.getReqName());
                    sb.append("->");
                    sb.append(param.getParamName());
                    sb.append(" ");
                    sb.append(param.getParamType());
                    sb.append(" ");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        try {
            Files.write(Paths.get("controllers.txt"), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

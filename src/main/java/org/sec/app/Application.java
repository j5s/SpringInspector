package org.sec.app;

import com.beust.jcommander.JCommander;
import org.sec.core.inherit.InheritanceMap;
import org.sec.core.inherit.InheritanceUtil;
import org.sec.core.service.*;
import org.sec.core.spring.SpringController;
import org.sec.core.util.ClassUtil;
import org.sec.data.Output;
import org.sec.log.SLF4J;
import org.sec.model.*;
import org.slf4j.Logger;

import java.util.*;

@SLF4J
public class Application {
    /**
     * CLASS FILE LIST
     */
    private static final List<ClassFile> classFileList = new ArrayList<>();
    /**
     * ALL CLASS INFO
     */
    private static final List<ClassReference> discoveredClasses = new ArrayList<>();
    /**
     * ALL METHOD INFO
     */
    private static final List<MethodReference> discoveredMethods = new ArrayList<>();
    /**
     * CLASS NAME -> CLASS INFO
     */
    private static final Map<ClassReference.Handle, ClassReference> classMap = new HashMap<>();
    /**
     * METHOD NAME -> METHOD INFO
     */
    private static final Map<MethodReference.Handle, MethodReference> methodMap = new HashMap<>();
    /**
     * METHOD NAME -> CALL METHOD NAME
     */
    private static final Map<MethodReference.Handle, Set<MethodReference.Handle>> methodCalls = new HashMap<>();
    /**
     * CLASS NAME -> CLASS FILE
     */
    private static final Map<String, ClassFile> classFileByName = new HashMap<>();
    /**
     * METHOD NAME -> ALL METHOD IMPLS
     */
    private static final Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImpls = new HashMap<>();
    /**
     * SORTED METHODS
     */
    private static List<MethodReference.Handle> sortedMethods;
    /**
     * CALL GRAPHS
     */
    private static final Set<CallGraph> discoveredCalls = new HashSet<>();
    /**
     * METHOD NAME -> CALL GRAPHS
     */
    private static final Map<MethodReference.Handle, Set<CallGraph>> graphCallMap = new HashMap<>();
    /**
     * SPRING CONTROLLERS
     */
    private static final List<SpringController> controllers = new ArrayList<>();
    /**
     * RESULTS
     */
    private static final List<ResultInfo> resultInfos = new ArrayList<>();

    private static Logger logger;

    public static void run(String[] args) {
        Logo.PrintLogo();
        Command command = new Command();
        JCommander jc = JCommander.newBuilder().addObject(command).build();
        jc.parse(args);
        if (command.help) {
            jc.usage();
        }
        if (command.jars != null && command.jars.size() != 0) {
            printConfig(command);
            start(command);
        } else {
            logger.error("no jars input");
        }
    }

    private static void printConfig(Command command) {
        System.out.print("> Jar File: ");
        for (String jarFile : command.jars) {
            System.out.print(jarFile + " ");
        }
        System.out.println();
        if (command.springBoot) {
            System.out.println("> Use SpringBoot Jar");
        }
        if (command.packageName != null && !command.packageName.equals("")) {
            System.out.println("> Package Name: " + command.packageName);
        }
        if (command.jdk) {
            System.out.println("> Use rj.jar Lib");
        }
        if (command.lib) {
            System.out.println("> Use All Libs In SpringBoot Jar");
        }
        if (command.isDebug) {
            System.out.println("> Debug Mode");
        }
    }

    private static void start(Command command) {
        getClassFileList(command);
        discovery();
        inherit();
        methodCall();
        sort(command);
        buildCallGraphs(command);
        parseSpring(command);
        if (command.module == null || command.module.equals("")) {
            logger.warn("no module selected");
        } else {
            String module = command.module.toUpperCase(Locale.ROOT);
            if (module.contains("SSRF")) {
                SSRFService.start(classFileByName, controllers, graphCallMap);
                resultInfos.addAll(SSRFService.getResults());
            } else {
                logger.error("error module");
            }
        }
        System.out.println("total data: " + resultInfos.size());
        logger.info("delete temp dirs...");
    }

    private static void getClassFileList(Command command) {
        if (command.springBoot) {
            classFileList.addAll(ClassUtil.getAllClassesFromBoots(command.jars, command.jdk, command.lib));
        } else {
            classFileList.addAll(ClassUtil.getAllClassesFromJars(command.jars, command.jdk));
        }
    }

    private static void discovery() {
        DiscoveryService.start(classFileList, discoveredClasses, discoveredMethods,
                classMap, methodMap, classFileByName);
        logger.info("total classes: " + discoveredClasses.size());
        logger.info("total methods: " + discoveredMethods.size());
    }

    private static void inherit() {
        InheritanceMap inheritanceMap = InheritanceService.start(classMap);
        methodImpls.putAll(InheritanceUtil.getAllMethodImplementations(inheritanceMap, methodMap));
    }

    private static void methodCall() {
        MethodCallService.start(classFileList, methodCalls);
    }

    private static void sort(Command command) {
        sortedMethods = SortService.start(methodCalls);
        if (command.isDebug) {
            Output.writeSortedMethod(sortedMethods);
        }
    }

    private static void buildCallGraphs(Command command) {
        CallGraphService.start(discoveredCalls, sortedMethods, classFileByName,
                classMap, graphCallMap, methodMap, methodImpls);
        if (command.isDebug) {
            Output.writeTargetCallGraphs(graphCallMap, command.packageName);
        }
    }

    private static void parseSpring(Command command) {
        if (command.springBoot) {
            SpringService.start(classFileList, command.packageName, controllers, classMap, methodMap);
            if (command.isDebug) {
                Output.writeControllers(controllers);
            }
        }
    }
}

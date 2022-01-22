package org.sec.app;

import com.beust.jcommander.JCommander;
import org.sec.core.inherit.InheritanceMap;
import org.sec.core.inherit.InheritanceUtil;
import org.sec.core.service.DiscoveryService;
import org.sec.core.service.InheritanceService;
import org.sec.core.service.MethodCallService;
import org.sec.core.service.SortService;
import org.sec.core.util.ClassUtil;
import org.sec.log.SLF4J;
import org.sec.model.ClassFile;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.slf4j.Logger;

import java.util.*;

@SLF4J
public class Application {
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
     * CLASS NAME -> ALL METHODS IN CLASS
     */
    private static final Map<ClassReference.Handle, Set<MethodReference.Handle>> methodsByClass = new HashMap<>();
    /**
     * METHOD NAME -> ALL METHOD IMPLS
     */
    private static final Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImpls = new HashMap<>();
    /**
     * CLASS -> ALL SUPER CLASSES
     * CLASS -> ALL SUB CLASSES
     */
    private static InheritanceMap inheritanceMap;
    /**
     * SORTED METHODS
     */
    private static List<MethodReference.Handle> sortedMethods;

    @SuppressWarnings("all")
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
        List<ClassFile> classFileList = new ArrayList<>();
        getClassFileList(command, classFileList);
        discovery(classFileList);
        inherit();
        methodCall(classFileList);
        sort();
        if (command.module == null || command.module.equals("")) {
            logger.warn("no module selected");
        }
        logger.info("delete temp dirs...");
    }

    private static void getClassFileList(Command command, List<ClassFile> classFileList) {
        if (command.springBoot) {
            classFileList.addAll(ClassUtil.getAllClassesFromBoots(command.jars, command.jdk, command.lib));
        } else {
            classFileList.addAll(ClassUtil.getAllClassesFromJars(command.jars, command.jdk));
        }
    }

    private static void discovery(List<ClassFile> classFileList) {
        DiscoveryService.start(classFileList, discoveredClasses, discoveredMethods,
                classMap, methodMap, classFileByName);
        logger.info("total classes: " + discoveredClasses.size());
        logger.info("total methods: " + discoveredMethods.size());
    }

    private static void inherit() {
        inheritanceMap = InheritanceService.start(classMap);
        methodsByClass.putAll(InheritanceUtil.getMethodsByClass(methodMap));
        methodImpls.putAll(InheritanceUtil.getAllMethodImplementations(inheritanceMap, methodMap));
    }

    private static void methodCall(List<ClassFile> classFileList) {
        MethodCallService.start(classFileList, methodCalls);
    }

    private static void sort() {
        sortedMethods = SortService.start(methodCalls);
    }
}

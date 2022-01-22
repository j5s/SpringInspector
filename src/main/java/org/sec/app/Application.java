package org.sec.app;

import com.beust.jcommander.JCommander;
import org.sec.core.service.DiscoveryService;
import org.sec.core.util.ClassUtil;
import org.sec.log.SLF4J;
import org.sec.model.ClassFile;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<String> jars = command.jars;
        List<ClassFile> classFileList;
        if (command.springBoot) {
            classFileList = ClassUtil.getAllClassesFromBoots(jars, command.jdk, command.lib);
        } else {
            classFileList = ClassUtil.getAllClassesFromJars(jars, command.jdk);
        }
        DiscoveryService.start(classFileList, discoveredClasses, discoveredMethods, classMap, methodMap);
        logger.info("total classes: " + discoveredClasses.size());
        logger.info("total methods: " + discoveredMethods.size());
        if (command.module == null || command.module.equals("")) {
            logger.warn("no module selected");
        }
        logger.info("delete temp dirs...");
    }
}

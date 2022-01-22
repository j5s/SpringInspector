package org.sec.app;

import com.beust.jcommander.JCommander;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sec.core.service.DiscoveryService;
import org.sec.core.util.ClassUtil;
import org.sec.model.ClassFile;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void run(String[] args) {
        Logo.PrintLogo();
        logger.info("start spring inspector");
        Command command = new Command();
        JCommander jc = JCommander.newBuilder().addObject(command).build();
        jc.parse(args);
        if (command.help) {
            jc.usage();
        }
        if (command.jars != null && command.jars.size() != 0) {
            start(command);
        } else {
            logger.error("no jars input");
        }
    }

    private static void start(Command command) {
        List<String> jars = command.jars;
        List<ClassFile> classFileList = ClassUtil.getAllClassesFromJars(jars,false);
        DiscoveryService.start(classFileList, discoveredClasses, discoveredMethods, classMap, methodMap);
        logger.info("total classes: " + discoveredClasses.size());
        logger.info("total methods: " + discoveredMethods.size());

        if (command.module == null || command.module.equals("")) {
            return;
        }
        logger.info("delete temp files...");
    }
}

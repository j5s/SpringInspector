package org.sec.core.util;

import org.sec.log.SLF4J;
import org.sec.model.ClassFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SLF4J
public class ClassUtil {
    
    private static Logger logger;

    public static List<ClassFile> getAllClassesFromJars(List<String> jarPathList,
                                                        boolean runtime) {
        logger.info("get all classes");
        Set<ClassFile> classFileSet = new HashSet<>();
        if (runtime) {
            getRuntime(classFileSet);
        }
        for (String jarPath : jarPathList) {
            classFileSet.addAll(JarUtil.resolveNormalJarFile(jarPath));
        }
        return new ArrayList<>(classFileSet);
    }

    public static List<ClassFile> getAllClassesFromBoots(List<String> bootPathList,
                                                         boolean runtime,
                                                         boolean useAllLib) {
        logger.info("get all classes");
        Set<ClassFile> classFileSet = new HashSet<>();
        if (runtime) {
            getRuntime(classFileSet);
        }
        for (String jarPath : bootPathList) {
            classFileSet.addAll(JarUtil.resolveSpringBootJarFile(jarPath, useAllLib));
        }
        return new ArrayList<>(classFileSet);
    }

    private static void getRuntime(Set<ClassFile> classFileSet) {
        logger.info("get classes from rj.jar");
        String rtJarPath = System.getenv("JAVA_HOME") +
                File.separator + "jre" +
                File.separator + "lib" +
                File.separator + "rt.jar";
        Path rtPath = Paths.get(rtJarPath);
        if (!Files.exists(rtPath)) {
            logger.error("rt.jar not exists");
            throw new RuntimeException("rt.jar not exists");
        }
        classFileSet.addAll(JarUtil.resolveNormalJarFile(rtJarPath));
    }
}

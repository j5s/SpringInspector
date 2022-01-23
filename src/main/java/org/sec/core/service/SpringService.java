package org.sec.core.service;

import org.objectweb.asm.ClassReader;
import org.sec.core.spring.SpringController;
import org.sec.core.spring.asm.SpringClassVisitor;
import org.sec.log.SLF4J;
import org.sec.model.ClassFile;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

@SLF4J
public class SpringService {
    private static Logger logger;

    public static void start(List<ClassFile> classFileList, String packageName,
                             List<SpringController> controllers,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             Map<MethodReference.Handle, MethodReference> methodMap) {
        packageName = packageName.replace(".", "/");
        for (ClassFile file : classFileList) {
            try {
                SpringClassVisitor mcv = new SpringClassVisitor(packageName, controllers, classMap, methodMap);
                ClassReader cr = new ClassReader(file.getFile());
                cr.accept(mcv, ClassReader.EXPAND_FRAMES);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
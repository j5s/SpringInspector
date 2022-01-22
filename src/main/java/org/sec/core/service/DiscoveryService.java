package org.sec.core.service;

import org.objectweb.asm.ClassReader;
import org.sec.core.asm.DiscoveryClassVisitor;
import org.sec.log.SLF4J;
import org.sec.model.ClassFile;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;


@SLF4J
public class DiscoveryService {
    @SuppressWarnings("all")
    private static Logger logger;

    public static void start(List<ClassFile> classFileList,
                             List<ClassReference> discoveredClasses,
                             List<MethodReference> discoveredMethods,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             Map<MethodReference.Handle, MethodReference> methodMap,
                             Map<String, ClassFile> classFileByName) {
        logger.info("start discovery information");
        for (ClassFile file : classFileList) {
            try {
                DiscoveryClassVisitor dcv = new DiscoveryClassVisitor(discoveredClasses, discoveredMethods);
                ClassReader cr = new ClassReader(file.getFile());
                cr.accept(dcv, ClassReader.EXPAND_FRAMES);
                classFileByName.put(dcv.getName(), file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (ClassReference clazz : discoveredClasses) {
            classMap.put(clazz.getHandle(), clazz);
        }
        for (MethodReference method : discoveredMethods) {
            methodMap.put(method.getHandle(), method);
        }
    }
}

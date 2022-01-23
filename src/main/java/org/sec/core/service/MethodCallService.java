package org.sec.core.service;

import org.sec.core.asm.MethodCallClassVisitor;
import org.sec.log.SLF4J;
import org.sec.model.ClassFile;
import org.sec.model.MethodReference;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;


@SLF4J
public class MethodCallService {
    
    private static Logger logger;

    public static void start(List<ClassFile> classFileList,
                             Map<MethodReference.Handle, Set<MethodReference.Handle>> methodCalls) {
        logger.info("get method calls in method");
        for (ClassFile file : classFileList) {
            try {
                MethodCallClassVisitor mcv = new MethodCallClassVisitor(methodCalls);
                ClassReader cr = new ClassReader(file.getFile());
                cr.accept(mcv, ClassReader.EXPAND_FRAMES);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

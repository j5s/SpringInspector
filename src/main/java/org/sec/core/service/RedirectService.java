package org.sec.core.service;

import org.objectweb.asm.ClassReader;
import org.sec.core.asm.RedirectClassVisitor;
import org.sec.core.spring.SpringController;
import org.sec.core.spring.SpringMapping;
import org.sec.log.SLF4J;
import org.sec.model.ClassFile;
import org.sec.model.MethodReference;
import org.sec.model.ResultInfo;
import org.slf4j.Logger;

import java.util.*;


@SLF4J
public class RedirectService {
    private static Logger logger;

    private static final List<ResultInfo> results = new ArrayList<>();

    public static void start(Map<String, ClassFile> classFileByName,
                             List<SpringController> controllers) {

        logger.info("start analysis redirect");
        for (SpringController controller : controllers) {
            for (SpringMapping mapping : controller.getMappings()) {
                MethodReference methodReference = mapping.getMethodReference();
                if (methodReference == null) {
                    continue;
                }
                ClassFile file = classFileByName.get(mapping.getController().getClassReference().getName());
                try {
                    if (file == null) {
                        return;
                    }
                    ClassReader cr = new ClassReader(file.getFile());
                    RedirectClassVisitor cv = new RedirectClassVisitor(mapping.getMethodReference());
                    cr.accept(cv, ClassReader.EXPAND_FRAMES);
                    if (cv.getPass("SERVLET") != null && cv.getPass("SERVLET")) {
                        ResultInfo resultInfo = new ResultInfo();
                        resultInfo.setRisk(ResultInfo.MID_RISK);
                        resultInfo.setVulName("SERVLET REDIRECT");
                        resultInfo.getChains().add(mapping.getController().getClassReference().getName() + "."
                                + mapping.getMethodReference().getName());
                        results.add(resultInfo);
                        System.out.println(resultInfo);
                        logger.info("detect servlet redirect");
                    }
                    if (cv.getPass("SPRING") != null && cv.getPass("SPRING")) {
                        ResultInfo resultInfo = new ResultInfo();
                        resultInfo.setRisk(ResultInfo.MID_RISK);
                        resultInfo.setVulName("SPRING REDIRECT");
                        resultInfo.getChains().add(mapping.getController().getClassReference().getName() + "."
                                + mapping.getMethodReference().getName());
                        results.add(resultInfo);
                        System.out.println(resultInfo);
                        logger.info("detect spring redirect");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public static List<ResultInfo> getResults() {
        return new ArrayList<>(results);
    }
}

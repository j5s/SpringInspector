package org.sec.core.service;

import org.sec.core.inherit.InheritanceMap;
import org.sec.core.inherit.InheritanceUtil;
import org.sec.log.SLF4J;
import org.sec.model.ClassReference;
import org.slf4j.Logger;

import java.util.Map;

@SLF4J
public class InheritanceService {
    
    private static Logger logger;

    public static InheritanceMap start(Map<ClassReference.Handle, ClassReference> classMap) {
        logger.info("build inheritance");
        return InheritanceUtil.derive(classMap);
    }
}

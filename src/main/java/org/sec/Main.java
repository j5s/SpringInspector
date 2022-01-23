package org.sec;

import org.sec.app.Application;
import org.sec.log.SLF4J;
import org.sec.log.SLF4JProcessor;
import org.slf4j.Logger;

@SLF4J
public class Main {
    
    private static Logger logger;

    public static void main(String[] args) {
        SLF4JProcessor.process();
        logger.info("start spring inspector");
        Application.run(args);
    }
}

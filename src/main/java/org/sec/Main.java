package org.sec;

import org.sec.log.SLF4J;
import org.sec.log.SLF4JProcessor;
import org.sec.log.Log;

@SLF4J
public class Main implements Log {
    public static void main(String[] args) {
        SLF4JProcessor.process();
        logger.info("test");
    }
}

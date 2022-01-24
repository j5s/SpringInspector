package org.sec.log;

import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

public class SLF4JProcessor {
    private static final String PACKAGE_NAME = "org.sec";
    private static final String LOGGER_NAME = "logger";

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    public static void process() {
        try {
            Reflections reflections = new Reflections(PACKAGE_NAME);
            Set<Class<?>> allLogs = reflections.getTypesAnnotatedWith(SLF4J.class);
            for (Class<?> clazz : allLogs) {
                if (clazz.getAnnotation(SLF4J.class) != null) {
                    Field field = clazz.getDeclaredField(LOGGER_NAME);
                    setFinalStatic(field, LoggerFactory.getLogger(clazz));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

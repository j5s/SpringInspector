package org.sec.log;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class SLF4JProcessor {
    private static final String PACKAGE_NAME = "org.sec";
    private static final String LOGGER_NAME = "logger";
    private static final String LOG_CLASS = "org.sec.log.Log";

    @SuppressWarnings("all")
    private static List<Class<?>> getClassesInPackage(String packageName) throws Exception {
        String path = packageName.replace(".", "/");
        List<Class<?>> classes = new ArrayList<>();
        String[] classPathEntries = System.getProperty("java.class.path").split(
                System.getProperty("path.separator")
        );
        String name;
        for (String classpathEntry : classPathEntries) {
            if (classpathEntry.endsWith(".jar")) {
                File jar = new File(classpathEntry);
                JarInputStream is = new JarInputStream(new FileInputStream(jar));
                JarEntry entry;
                while ((entry = is.getNextJarEntry()) != null) {
                    name = entry.getName();
                    if (name.endsWith(".class")) {
                        if (name.contains(path) && name.endsWith(".class")) {
                            String classPath = name.substring(0, entry.getName().length() - 6);
                            classPath = classPath.replace("/", ".");
                            classes.add(Class.forName(classPath));
                        }
                    }
                }
            } else {
                File base = new File(classpathEntry + "/" + path);
                for (File file : Objects.requireNonNull(base.listFiles())) {
                    name = file.getName();
                    if (name.endsWith(".class")) {
                        name = name.substring(0, name.length() - 6);
                        classes.add(Class.forName(packageName + "." + name));
                    }
                }
            }
        }
        return classes;
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    public static void process() {
        try {
            List<Class<?>> classes = getClassesInPackage(PACKAGE_NAME);
            for (Class<?> clazz : classes) {
                if (clazz.getAnnotation(SLF4J.class) != null) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> _interface : interfaces) {
                        if (_interface.getName().equals(LOG_CLASS)) {
                            Field field = _interface.getField(LOGGER_NAME);
                            setFinalStatic(field, LoggerFactory.getLogger(clazz));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

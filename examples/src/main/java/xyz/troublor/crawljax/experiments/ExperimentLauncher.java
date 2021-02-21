package xyz.troublor.crawljax.experiments;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class is used to launch experiments via Java Reflection
 */
public class ExperimentLauncher {
    /**
     * @param args [status.log path, chrome browser debugger address, SubjectClass]
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        if (args.length < 3) {
            throw new IllegalArgumentException("Arguments expected: [coverageDir path, status.log path, chrome " +
                    "browser debugger address, SubjectClass]");
        }

        Class<?> subjectClass = Class.forName("xyz.troublor.crawljax.experiments." + args[3]);
        Constructor<?> constructor = subjectClass.getConstructor();
        Experiment experiment = (Experiment) constructor.newInstance();
        Method mainMethod = subjectClass.getMethod("start", String.class, String.class, String.class);
        mainMethod.invoke(experiment, args[0], args[1], args[2]);
    }
}

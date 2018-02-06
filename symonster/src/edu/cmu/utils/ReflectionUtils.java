package edu.cmu.utils;

import edu.cmu.codeformer.TestInterface;
import heros.ThreadSafe;

import java.util.List;

@ThreadSafe
public class ReflectionUtils {
    public static void loadLibraries(List<String> libs){

    }

    public static void runTest(String testPath) throws ClassNotFoundException {
        ClassLoader classLoader = TestInterface.class.getClassLoader();
        classLoader.loadClass("Test1.java");

    }
}

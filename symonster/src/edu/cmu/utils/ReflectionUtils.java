package edu.cmu.utils;

import edu.cmu.testfiles.TargetInterface;
import edu.cmu.testfiles.Testing;
import heros.ThreadSafe;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@ThreadSafe
public class ReflectionUtils {
    public static boolean runTest(String testName) {
        try {
            Class classToLoad = ClassLoader.getSystemClassLoader().loadClass("edu.cmu.testfiles."+testName);
            TargetInterface target = (TargetInterface)classToLoad.newInstance();
            if (Testing.pass(target)){
                return true;
            }
            else return false;
        } catch (InstantiationException e) {
            System.out.println("invalid 1");;
        } catch (IllegalAccessException e) {
            System.out.println("invalid 4");;
        } catch (ClassNotFoundException e) {
            System.out.println("invalid 5");;
        }
        return false;
    }
}

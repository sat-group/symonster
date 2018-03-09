package edu.cmu.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class TestUtils {
    /** where shall the compiled class be saved to (should exist already) */
    private static String classOutputFolder = "folder";

    public static class MyDiagnosticListener implements DiagnosticListener<JavaFileObject>
    {
        public void report(Diagnostic<? extends JavaFileObject> diagnostic)
        {

        }
    }

    /** java File Object represents an in-memory java source file <br>
     * so there is no need to put the source file on hard disk  **/
    public static class InMemoryJavaFileObject extends SimpleJavaFileObject
    {
        private String contents = null;

        public InMemoryJavaFileObject(String className, String contents) throws Exception
        {
            super(URI.create("string:///" + className.replace('.', '/')
                    + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors)
                throws IOException
        {
            return contents;
        }
    }

    /** Get a simple Java File Object ,<br>
     * It is just for demo, content of the source code is dynamic in real use case */
    private static JavaFileObject getJavaFileObject(String code)
    {
        JavaFileObject so = null;
        try
        {
            so = new InMemoryJavaFileObject("Target", code);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return so;
    }

    /** compile your files by JavaCompiler */
    public static void compile(Iterable<? extends JavaFileObject> files)
    {
        //get system compiler:
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // for compilation diagnostic message processing on compilation WARNING/ERROR
        MyDiagnosticListener c = new MyDiagnosticListener();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(c,
                Locale.ENGLISH,
                null);
        //specify classes output folder
        Iterable options = Arrays.asList("-d", classOutputFolder);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager,
                c, options, null,
                files);
        Boolean result = task.call();
    }

    /** run class from the compiled byte code file by URLClassloader */
    public static boolean runIt()
    {
        // Create a File object on the root of the directory
        // containing the class file
        File file = new File(classOutputFolder);

        try
        {
            // Convert File to a URL
            URL url = file.toURL();
            URL[] urls = new URL[] { url };

            // Create a new class loader with the directory
            ClassLoader loader = new URLClassLoader(urls);

            // Load in the class; Class.childclass should be located in
            // the directory file:/class/demo/
            Class thisClass = loader.loadClass("Target");

            Class params[] = {};
            Object paramsObj[] = {};
            Object instance = thisClass.newInstance();
            Method thisMethod = thisClass.getDeclaredMethod("test", params);

            // run the testAdd() method on the instance:
            return (boolean)thisMethod.invoke(instance, paramsObj);
        }
        catch (MalformedURLException e)
        {
            System.out.println("bug1");
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("bug2");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println("bug3");
        }
        return false;
    }

    public static boolean runTest(String code)
    {
        //1.Construct an in-memory java source file from your dynamic code
        JavaFileObject file = getJavaFileObject(code);
        Iterable<? extends JavaFileObject> files = Arrays.asList(file);

        //2.Compile your files by JavaCompiler
        compile(files);

        //3.Load your class by URLClassLoader, then instantiate the instance, and call method by reflection
        return runIt();
    }
}

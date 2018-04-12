import parser.JarParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class TrainErrorCalculator {

    public static void main(String[] args) throws IOException {
        List<Integer> a = Arrays.asList(1,2,3,4,5);
        Iterator<Integer> it = a.iterator();
        while(it.hasNext()){
            System.out.println(it.next());
            System.out.println(it.next());
        }
    }
}

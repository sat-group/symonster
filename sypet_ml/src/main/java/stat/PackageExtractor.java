package stat;

import stat.parser.PackageJarParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;

public class PackageExtractor {

    public static void main(String[] args) throws FileNotFoundException {

        File libFolder = new File(args[0]);
        File[] listFiles = libFolder.listFiles();
        StringBuilder builder = new StringBuilder();

        PrintWriter[] pws = new PrintWriter[args.length-1];
        for(int i=0; i<pws.length; i++) {
            pws[i] = new PrintWriter(new File("src/resources/" + args[i+1] + ".txt"));
        }

        for(File file : listFiles){
            String jar = args[0]+file.getName();
            PackageJarParser parser = new PackageJarParser();
            parser.parseJar(Collections.singletonList(jar));
            if (parser.getPackages().size() > 0) {
                for (String packageName : parser.getPackages()) {
                    for(int i=0; i<pws.length; i++){
                        if(packageName.equals(args[i+1])) {
                            pws[i].write(args[i+1]+file.getName()+"\n");
                            break;
                        }
                    }

                }
            }
        }

        for (PrintWriter pw : pws) {
            pw.close();
        }
    }
}

package stat;

import stat.common.DataSource;
import stat.ngram.BiGram;
import stat.parser.JarParser;
import stat.parser.LibraryJarParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class BiGramModelGenerator {

    private static String targetPackage;
    private static String corpusPath;
    private static String outputName;
    private static String libraryJarPath = "";

    public static void main(String[] args) throws FileNotFoundException {
        // corpus, name of result csv, package of concern, filepath of lib if not rt
        // comment this out if you want to input from command line
        args = new String[]{"lib/corpus/java.awt.geom/", "data_bigram_geom", "java.awt.geom"};

        targetPackage = args[2];
        corpusPath = args[0];
        outputName = args[1];

        if(args.length > 3) {
            libraryJarPath = args[3];
        }else{
            libraryJarPath = "";
        }

        // read all files from folder
        List<String> trainSet = new ArrayList<>();
        File libFolder = new File(corpusPath);
        int total = libFolder.listFiles().length;

        for(File fileEntry : libFolder.listFiles()){
            trainSet.add(corpusPath + fileEntry.getName());
        }

        // train
        LibraryJarParser.init(DataSource.generateLib(libraryJarPath), DataSource.generateCustomLib(targetPackage));
        List<LinkedHashSet<String>> trainingSets = new ArrayList<>();

        for(String s : trainSet){
            JarParser.parseJar(Collections.singletonList(s), DataSource.generateCustomLib(targetPackage));
            if(JarParser.getMethodToAppearancesMap().size() > 0){
                trainingSets.addAll(JarParser.getMethodToAppearancesMap().values());
            }
        }

        BiGram biGram = new BiGram(LibraryJarParser.getLabelSet(), trainingSets);


        // output table
        PrintWriter pw = new PrintWriter(new File("src/resources/"+outputName+".csv"));
        int n = biGram.getPTable().length;
        for(int i=0; i<n; i++){
            StringBuilder builder = new StringBuilder();
            for(int j=0; j<n; j++){
                builder.append(biGram.getPTable()[i][j]);
                if(j<n-1) {
                    builder.append(",");
                }else{
                    builder.append("\n");
                }
            }
            pw.write(builder.toString());
        }
        pw.close();
    }
}

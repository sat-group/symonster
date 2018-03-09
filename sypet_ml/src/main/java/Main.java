import knn.KNN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main {

    private static KNN knn;
    private static KNN experimentKnn;
    protected static List<String> packages = Collections.singletonList("java.awt.geom");

    /**
     * Sample main function
     * @param args no use at all
     */
    public static void main(String[] args) throws FileNotFoundException {
        // Libraries that we want to read from
        JarParserLib.init(generateLib(), packages, true);
    }

    public static void onParseLibComplete() throws FileNotFoundException {
        // add labels from library
        knn = new KNN(JarParserLib.getLabelSet());
        experimentKnn = new KNN(JarParserLib.getLabelSet());
        List<String> libs = generateTrain();
        //libs.add("lib/geometry.jar");
        PrintWriter pw = new PrintWriter(new File("src/resources/data.csv"));
        pw.write("name,parsed,rows,average\n");
        for(String s : libs) {
            JarParser.parseJar(Collections.singletonList(s), packages);
            pw.write(s);
            if(JarParser.getMethodToAppearancesMap().size() != 0){
                trainVarIndependent(libs, false);
                String resultString = experimentKnn.getTrainSparseString();
                pw.write(",true,");
                pw.write(resultString);

                // reload
                experimentKnn = new KNN(JarParserLib.getLabelSet());
                trainVarIndependent(libs, true);
                JarParser.refresh();
            }else{
                pw.write(",false\n");
            }
        }
        pw.write("final result:");
        pw.write(knn.getTrainDenseString());
        pw.write(knn.getSortedFreqString());
        pw.close();

        // add training data (var independent)
        /*if(JarParser.getMethodToAppearancesMap().size() == 0){
            System.out.println("empty, abort");
            return;
        }
        trainVarIndependent(libs);*/

        // add training data (var dependent)
        /*if(JarParser.getMethodToVarAppearancesMap().size() == 0){
            return;
        }*/
        //trainVarDependent(libs);

        //printTrainResult(false, true);

        // test prediction, var independent
        // pick a method from geometry
        /**
        libs = generateTest();
        JarParser.parseJar(libs, packages);
        Map<String, Set<String>> dataT = JarParser.getMethodToAppearancesMap();
        //Set<String> sampleRotate = data.get("<symonster.cmu.edu.Examples: java.awt.geom.Area rotate(java.awt.geom.Area,java.awt.geom.Point2D,double)>");
        Set<String> sampleRotate = new HashSet<>();
        sampleRotate.add("<java.awt.geom.Point2D: double getX()>");
        //sampleRotate.remove("<java.awt.geom.Area: java.awt.geom.Area createTransformedArea(java.awt.geom.AffineTransform)>");
        //System.out.println("prob: "+knn.getFreq(""));
        LinkedHashMap<String, Float> map = knn.predict(sampleRotate);
        System.out.println("===== Top 10 Prediction =====");
        int k = 0;
        for(String method : map.keySet()) {
            if(k<10) {
                System.out.println(method + " -> " + map.get(method));
                k++;
            }
        }**/
    }

    private static void trainVarDependent(List<String> libs, boolean flag){
        JarParser.parseJar(libs, packages);
        Map<String, Map<String, Set<String>>> varData = JarParser.getMethodToVarAppearancesMap();
        if(flag) {
            for (Map<String, Set<String>> s : varData.values()) {
                for (Set<String> t : s.values()) {
                    knn.addTrainVector(t);
                }
            }
            knn.preSort();
        }else{
            for (Map<String, Set<String>> s : varData.values()) {
                for (Set<String> t : s.values()) {
                    experimentKnn.addTrainVector(t);
                }
            }
            experimentKnn.preSort();
        }
    }

    private static void trainVarIndependent(List<String> libs, boolean flag){
        Map<String, Set<String>> data = JarParser.getMethodToAppearancesMap();

        if(flag) {
            for (Set<String> set : data.values()) {
                knn.addTrainVector(set);
            }

            knn.preSort();
        }else{
            for (Set<String> set : data.values()) {
                experimentKnn.addTrainVector(set);
            }

            experimentKnn.preSort();
        }
    }

    private static void printTrainResult(boolean dense, boolean sparse){
        System.out.println("===== Var Dependent Training Set Matrix =====");
        if(dense) {
            System.out.println("===== Dense =====");
            knn.showTrainSetDense();
        }
        if(sparse) {
            System.out.println("===== Sparse =====");
            knn.showTrainSetSparse();
        }
    }

    private static ArrayList<String> generateTrain(){
        // read all files from folder
        ArrayList<String> libs = new ArrayList<>();
        File libFolder = new File("lib/corpus/");
        for(final File fileEntry: libFolder.listFiles()){
            System.out.println(fileEntry.getName());
            libs.add("lib/corpus/"+fileEntry.getName());
        }

        // for manual testing
        //libs.add("../sypet_ml/lib/geometry.jar");
        /*
        ArrayList<String> libs = new ArrayList<>();
        libs.add("lib/corpus/n0Live-BrickGame.jar");
        libs.add("lib/corpus/2yangk23-GridGame.jar");
        libs.add("lib/corpus/AlanFoster-Java-Game-Engine.jar");
        libs.add("lib/corpus/AlexGreulich-MG2Game.jar");
        libs.add("../sypet_ml/lib/corpus/AlexLamson-BattleshipGame.jar");
        libs.add("../sypet_ml/lib/corpus/AnassTeemo-Jump_Game.jar");
        libs.add("../sypet_ml/lib/corpus/Anastron-Game.jar");
        libs.add("../sypet_ml/lib/corpus/AppleJuiceStudios-IH11-Game.jar");
        libs.add("../sypet_ml/lib/corpus/BenMcH-Game-Engine.jar");
        libs.add("../sypet_ml/lib/corpus/ChicoSystems-Space-Game.jar");
        libs.add("../sypet_ml/lib/corpus/CrazedTomato-Games-Assortment.jar");
        libs.add("../sypet_ml/lib/corpus/DCHSProgrammingClub-TanksGame.jar");
        libs.add("../sypet_ml/lib/corpus/DNNYVST-Interactive-Touch-Game.jar");
        libs.add("../sypet_ml/lib/corpus/EivindEE-gameoflife.jar");
        libs.add("../sypet_ml/lib/corpus/EvanDElia-PlatformGame.jar");
        libs.add("../sypet_ml/lib/corpus/Feinte75-PlatformerGame.jar");
        libs.add("../sypet_ml/lib/corpus/abom-snake-game.jar");
        libs.add("../sypet_ml/lib/corpus/agrant40-gamesTutorial.jar");
        libs.add("../sypet_ml/lib/corpus/ahuff44-game-maker.jar");
        libs.add("../sypet_ml/lib/corpus/alexcalibur3000-jbvag-game.jar");
        libs.add("../sypet_ml/lib/corpus/alycarter-gravityGame.jar");
        libs.add("../sypet_ml/lib/corpus/asbarber-gridworld-games.jar");
        libs.add("../sypet_ml/lib/corpus/bippity-GridworldGame.jar");
        libs.add("../sypet_ml/lib/corpus/bob0007-JavaGame.jar");
        libs.add("../sypet_ml/lib/corpus/chief-tyrol-301game.jar");
        libs.add("../sypet_ml/lib/corpus/chumich1-ThreadGame.jar");
        libs.add("../sypet_ml/lib/corpus/clockworked-gameoflife.jar");
        libs.add("../sypet_ml/lib/corpus/cxminer-java-game-spaceship.jar");
        libs.add("../sypet_ml/lib/corpus/davidsbuchan-BounceBallGame.jar");
        libs.add("../sypet_ml/lib/corpus/dphang-game-ai-projects.jar");
        libs.add("../sypet_ml/lib/corpus/dreamtitan-JavaGameEasyMode.jar");
        libs.add("../sypet_ml/lib/corpus/eandr127-Head-Game.jar");
        libs.add("../sypet_ml/lib/corpus/efferus20-CraneGame.jar");
        libs.add("../sypet_ml/lib/corpus/ellbosch-Helicopter_Game.jar");
        libs.add("../sypet_ml/lib/corpus/emlagowski-PangGame.jar");
        libs.add("../sypet_ml/lib/corpus/fab-jul-GameOfAnts.jar");
        libs.add("../sypet_ml/lib/corpus/fingerco-MazeGame.jar");*/
        return libs;
    }

    protected static ArrayList<String> generateLib(){
        ArrayList<String> libs = new ArrayList<>();
        libs.add("../sypet_ml/lib/rt.jar");

        return libs;
    }

    protected static ArrayList<String> generateTest(){
        ArrayList<String> libs = new ArrayList<>();
        libs.add("../sypet_ml/lib/geometry.jar");
        return libs;
    }

}

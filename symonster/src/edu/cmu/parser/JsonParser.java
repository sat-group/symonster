package edu.cmu.parser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

/**
 * Factory to parse symonster specific Json files.
 */
public class JsonParser {
    public static void main(String[] args) {
        parseJsonInput("/Users/liukaige/Desktop/testJson.json");
    }

    /**
     * Parse a json file as symonster input.
     * @param path file path.
     * @return input obj.
     */
    public static SyMonsterInput parseJsonInput(String path){
        try(Reader reader = new InputStreamReader(new FileInputStream(new File(path)))){
            Gson gson = new GsonBuilder().create();
            SyMonsterInput p = gson.fromJson(reader, SyMonsterInput.class);
            return p;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse a json file as symonster configuration settings.
     * @param path file path.
     * @return input config.
     */
    public static SymonsterConfig parseJsonConfig(String path){
        try(Reader reader = new InputStreamReader(new FileInputStream(new File(path)))){
            Gson gson = new GsonBuilder().create();
            SymonsterConfig p = gson.fromJson(reader, SymonsterConfig.class);
            return p;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

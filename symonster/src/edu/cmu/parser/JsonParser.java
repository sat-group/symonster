package edu.cmu.parser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class JsonParser {
    public static void main(String[] args) {
        parseJsonInput("/Users/liukaige/Desktop/testJson.json");
    }
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

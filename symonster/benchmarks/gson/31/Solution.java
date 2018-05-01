public class Solution {

    public Object parse(java.io.Reader reader, Class c){
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder().create();
        Object p = gson.fromJson(reader, c);
        return p;
    }

}

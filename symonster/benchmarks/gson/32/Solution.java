public class Solution {
    public String serialize(Object o){
        com.google.gson.GsonBuilder builder = new com.google.gson.GsonBuilder();
        builder.serializeNulls();
        com.google.gson.Gson gson = builder.create();
        String json = gson.toJson(o);
        return json;
    }

    public Object parse(java.io.Reader reader, Class c){
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder().create();
        Object p = gson.fromJson(reader, c);
        return p;
    }

}

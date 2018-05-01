public class bla{
    public String serialize(Object o){
        com.google.gson.GsonBuilder builder = new com.google.gson.GsonBuilder();
        builder.serializeNulls();
        com.google.gson.Gson gson = builder.create();
        String json = gson.toJson(o);
        return json;
    }
    public boolean test() {
        Person p = new Person();
        p.name = "abc";
        p.age = 31;
        p.school = null;
        return serialize(p).equals("{\"name\":\"abc\",\"age\":31,\"school\":null}");
    }

    public class Person{
        String name;
        int age;
        String school;
    }
}
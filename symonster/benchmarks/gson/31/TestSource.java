public boolean test() throws Throwable{
    java.io.Reader reader = null;
    try {
        reader = new java.io.InputStreamReader(new java.io.FileInputStream(new java.io.File("benchmarks/gson/31/test.json")));
    }
    catch (java.io.FileNotFoundException e) {
        return false;
    }
    Person sol = (Person) parse(reader,Person.class);
    return sol.age == 31 && sol.name.equals("abc");
}

public class Person{
    String name;
    int age;
}
public boolean test() throws Throwable{
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
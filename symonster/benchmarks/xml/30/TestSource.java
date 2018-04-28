public boolean test() throws Throwable {
    java.io.File file = new java.io.File("benchmarks/xml/30/doc.xml");
    java.lang.String id = "brand";
    
    java.lang.String n = getAttributeById(file,id);
    boolean flag = (n != null) && (n.equals("ut"));

    if(flag) 
        return true;
    else 
        return false;
}


# How to run SyPet using as an HTTP server?
Send a post request with the configuration .json file as the body. 

Example:
curl -X POST -d @test.json http://128.83.122.134:9092 --header "Content-Type:application/json"

Output:
```
	java.awt.Shape sypet_var801 = sypet_arg0;
	java.awt.geom.AffineTransform sypet_var802 = java.awt.geom.AffineTransform.getScaleInstance(sypet_arg1,sypet_arg2);
	java.awt.geom.Path2D.Float sypet_var803 = new java.awt.geom.Path2D.Float(sypet_var801,sypet_var802);
	java.awt.geom.Rectangle2D sypet_var804 = sypet_var803.getBounds2D();
	return sypet_var804;
```

Where test.json:

```
{
  "methodName": "scale",
  "paramNames": [
    "sypet_arg0",
    "sypet_arg1",
    "sypet_arg2"
  ],
  "srcTypes": [
    "java.awt.geom.Rectangle2D",
    "double",
    "double"
  ],
  "tgtType": "java.awt.geom.Rectangle2D",
  "packages": [
    "java.awt.geom"
  ],
  "testBody": "public static boolean test() throws Throwable { java.awt.geom.Rectangle2D rec = new java.awt.geom.Rectangle2D.Double(10, 20, 10, 2); java.awt.geom.Rectangle2D target = new java.awt.geom.Rectangle2D.Double(20, 60, 20, 6); java.awt.geom.Rectangle2D result = scale(rec, 2, 3); return (target.equals(result));}"
}
```

Examples for testing:
- curl -X POST -d @test.json http://128.83.122.134:9092 --header "Content-Type:application/json"
- curl -X POST -d @test2.json http://128.83.122.134:9092 --header "Content-Type:application/json"
- curl -X POST -d @test3.json http://128.83.122.134:9092 --header "Content-Type:application/json"
- curl -X POST -d @test4.json http://128.83.122.134:9092 --header "Content-Type:application/json"
- curl -X POST -d @test5.json http://128.83.122.134:9092 --header "Content-Type:application/json"
- curl -X POST -d @test6.json http://128.83.122.134:9092 --header "Content-Type:application/json"


# Limitations
- It currently only supports APIs from the JDK. 
- testBody MUST contain a method called test().
- Does not support multiple simultaneous requests.

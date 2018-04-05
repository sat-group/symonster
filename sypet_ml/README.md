# Running predictions:
run ```ant predict -Dargs="..."```
<br/>
where the args are
<br/>
jar to predict, name of result csv, package of concern, name pretrained data csv, name of output analysis csv, value of k, filepath of lib if not rt
<br/>
e.g.:
```ant predict -Dargs="lib/corpus/java.awt.geom/geometry.jar result_geom_k=3 java.awt.geom data_geom analysis_geom 3" ```
will generate a prediction named result_geom_k=3.csv under src/resources/ using the pretrained vectors in data_geom.csv and target package java.awt.geom from rt(no path specified)
<br/>
```ant predict -Dargs="lib/corpus/org.jsoup/yep.jar result_jsoup_k=1 org.jsoup data_jsoup analysis_jsoup 1 lib/jsoup-1.8.3.jar" ```
<br/>
will generate a prediction named result_jsoup_k=1.csv under src/resources/ using the pretrained vectors in data_jsoup.csv and target package org.jsoup from rt(no path specified)



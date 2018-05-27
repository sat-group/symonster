# Generating data files:
run either ```ant model_knn -Dargs="..."``` or ```ant model_bigram -Dargs="..."``` for the two models where the args are
<br/>
package of concern, corddpus location, output data csv file name, flag for dependent or not, library jar location(will use rt.jar as default if not given)
<br/>
e.g.:
```ant model_knn -Dargs="org.jsoup ../../corpus/org.jsoup/ jsoup 0 lib/jsoup-1.8.3.jar"```
will generate a data_jsoup.csv file under src/resources/ that contains plain information about the corpus as well as knn vectors that are to be used for prediction
<br/>
# Running predictions:
run either ```ant predict_knn -Dargs="..."``` or ```ant predict_bigram -Dargs="..."``` for the two models where the args are
<br/>
jar to predict, name of result csv, package of concern, name of pretrained data csv(generated using ant model), name of output analysis csv, filepath of lib if not rt, value of k
<br/>
e.g.:
```ant predict -Dargs="lib/corpus/java.awt.geom/geometry.jar result_geom_k=3 java.awt.geom data_geom analysis_geom 3" ```
will generate a prediction with k=3 named result_geom_k=3.csv under src/resources/ using the pretrained vectors in data_geom.csv and target package java.awt.geom from rt(no path specified)
<br/>
```ant predict_knn -Dargs="lib/corpus/org.jsoup/yep.jar result_jsoup_k=1 org.jsoup data_jsoup analysis_jsoup lib/jsoup-1.8.3.jar 1" ```
<br/>
will generate a prediction with k=1 named result_jsoup_k=1.csv under src/resources/ using the pretrained vectors in data_jsoup.csv and target package org.jsoup from jsoup-1.8.3.jar

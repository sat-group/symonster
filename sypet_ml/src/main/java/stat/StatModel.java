package stat;

import java.util.LinkedHashMap;
import java.util.Set;

public interface StatModel {

    LinkedHashMap<String, Double> predict(Set<String> method);
}

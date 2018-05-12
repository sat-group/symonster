package edu.cmu.equivprogram;

import edu.cmu.parser.MethodSignature;
import java.util.List;
public interface DependencyMap {
    List<List<MethodSignature>> findAllTopSorts(List<MethodSignature> signatures);
}

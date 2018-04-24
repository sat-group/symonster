package stat.knn;

import stat.StatModel;
import stat.common.Entry;
import stat.common.EntryComparator;
import soot.util.ArraySet;

import java.util.*;

/**
 * k-Nearest Neighbors, finds nearest neighbor through frequency as distance.
 */
public class KNN implements StatModel{
    private String[] labels;
    private Map<String, Integer> labelMap;
    private List<int[]> values;
    private int labelSize;
    private Map<Integer, Set<Integer>> idMap;

    /**
     * Constructor that starts with method names as columns
     *
     * @param labels one method name for each column
     */
    public KNN(LinkedHashSet<String> labels) {
        if (labels.size() <= 0) {
            throw new IllegalArgumentException();
        } else {
            this.labelSize = labels.size();
            this.labels = new String[labelSize];
            int j = 0;
            for (String s : labels) {
                this.labels[j] = s;
                j++;
            }
            this.labelMap = new HashMap<>();
            for (int i = 0; i < this.labels.length; i++) {
                labelMap.put(this.labels[i], i);
            }
            this.values = new ArrayList<>();
            for (String label : labelMap.keySet()) {
                System.out.println(label);
            }

            // Union lables that are actually the same
            unionDollarLabels();
        }
    }

    /**
     * Constructor if we already have all the trained data available
     *
     * @param labels     method names that serve as columns
     * @param preTrained pre-trained data
     */
    public KNN(Set<String> labels, List<int[]> preTrained) {
        this.labelSize = labels.size();
        this.labels = new String[labelSize];
        int j = 0;
        for (String s : labels) {
            this.labels[j] = s;
            j++;
        }
        this.labelMap = new HashMap<>();
        for (int i = 0; i < this.labels.length; i++) {
            labelMap.put(this.labels[i], i);
        }
        this.values = preTrained;

        // Union lables that are actually the same
        unionDollarLabels();
    }

    /**
     * Labels from Soot somehow have two representations... One with "$" sign, and one without.
     * I am going to just assume these two are the same, so I need to bind those labels into a union structure.
     */
    private void unionDollarLabels() {
        Map<Integer, Set<Integer>> sameLabelMap = new HashMap<>();
        for (int i = 0; i < labelSize - 1; i++) {
            for (int j = i + 1; j < labelSize; j++) {
                if (labels[i].contains("$")) {
                    String[] tmpArr = labels[i].split("\\$");
                    if (tmpArr[tmpArr.length - 1].contains(":")) {
                        String tmp1 = tmpArr[0] + ":" + tmpArr[tmpArr.length - 1].split(":")[1];
                        if (tmp1.contains(labels[j])) {
                            if (sameLabelMap.containsKey(i)) {
                                sameLabelMap.get(i).add(j);
                            } else {
                                Set<Integer> set = new ArraySet<>();
                                set.add(j);
                                sameLabelMap.put(i, set);
                            }

                            if (sameLabelMap.containsKey(j)) {
                                sameLabelMap.get(j).add(i);
                            } else {
                                Set<Integer> set = new ArraySet<>();
                                set.add(i);
                                sameLabelMap.put(j, set);
                            }
                        }
                    }
                } else if (labels[j].contains("$")) {
                    String[] tmpArr = labels[j].split("\\$");
                    if (tmpArr[tmpArr.length - 1].contains(":")) {
                        String tmp1 = tmpArr[0] + ":" + tmpArr[tmpArr.length - 1].split(":")[1];
                        if (tmp1.contains(labels[i])) {

                            if (sameLabelMap.containsKey(i)) {
                                sameLabelMap.get(i).add(j);
                            } else {
                                Set<Integer> set = new ArraySet<>();
                                set.add(j);
                                sameLabelMap.put(i, set);
                            }

                            if (sameLabelMap.containsKey(j)) {
                                sameLabelMap.get(j).add(i);
                            } else {
                                Set<Integer> set = new ArraySet<>();
                                set.add(i);
                                sameLabelMap.put(j, set);
                            }
                        }
                    }
                }
            }
        }
        this.idMap = sameLabelMap;
    }

    /**
     * Adds a training data into values as a vector
     *
     * @param appearances training data that consists of method names
     */
    public void addTrainVector(Set<String> appearances) {
        int[] vector = new int[labelSize];
        boolean hasOne = false;
        for (String s : appearances) {
            if (labelMap.containsKey(s)) {
                int i = labelMap.get(s);
                vector[i] = 1;
                hasOne = true;
            }
        }
        if (hasOne) {
            values.add(vector);
        }
    }

    /**
     * Prints out dense representaion of trained data
     */
    public void showTrainSetDense() {
        for (int[] vec : values) {
            StringBuilder vecString = new StringBuilder();
            vecString.append("<");
            for (int i = 0; i < vec.length; i++) {
                vecString.append(vec[i] + ", ");
            }
            vecString.deleteCharAt(vecString.length() - 1);
            vecString.append(">");
            System.out.println(vecString.toString());
        }
    }

    /**
     * Gives sparse representation of trained data
     *
     * @return string for sparse form of trained data
     */
    public String getTrainSparseString() {
        StringBuilder vecString = new StringBuilder();
        for (int[] vec : values) {
            vecString.append("[");
            for (int i = 0; i < vec.length; i++) {
                if (vec[i] == 1) {
                    vecString.append(labels[i] + "=1,");
                }
            }
            if (vecString.length() > 1)
                vecString.deleteCharAt(vecString.length() - 1);
            vecString.append("]\n");
        }
        return vecString.toString();
    }

    /**
     * Provides information about
     * - rows (total size of training set)
     * - average number of ones
     *
     * @return string representation of information
     */
    public String getTrainAnalysisInfoString() {
        StringBuilder vecString = new StringBuilder();
        vecString.append(values.size()).append(",");
        float total = 0;
        for (int[] vec : values) {
            for (int i = 0; i < vec.length; i++) {
                if (vec[i] == 1) {
                    total += 1;
                }
            }
        }
        float average = total / values.size();
        vecString.append(average).append('\n');
        return vecString.toString();
    }

    /**
     * Gives dense representation of trained data
     *
     * @return string for dense form of trained data
     */
    public String getTrainDenseString() {
        StringBuilder vecString = new StringBuilder();
        for (int[] vec : values) {
            vecString.append("<");
            for (int i = 0; i < vec.length; i++) {
                vecString.append(vec[i] + ", ");
            }
            vecString.deleteCharAt(vecString.length() - 1);
            vecString.append(">\n");
        }
        return vecString.toString();
    }

    /**
     * Prints out sparse representaion of trained data
     */
    public void showTrainSetSparse() {
        for (int[] vec : values) {
            StringBuilder vecString = new StringBuilder();
            vecString.append("[");
            for (int i = 0; i < vec.length; i++) {
                if (vec[i] == 1) {
                    vecString.append(labels[i].split(":")[1].split(">")[0] + "=1,");
                }
            }
            if (vecString.length() > 1)
                vecString.deleteCharAt(vecString.length() - 1);
            vecString.append("]");
            System.out.println(vecString.toString());
        }
    }

    /**
     * Gives a prediction based on given vector using KNN
     *
     * @param appearances given test data vector
     * @return a sorted mapping of possible methods with probabilities
     */
    @Override
    public LinkedHashMap<String, Double> predict(Set<String> appearances) {
        System.out.println(appearances);

        // Initialize
        List<Double> sumVector = new ArrayList<>(labelSize);
        for (int i = 0; i < labelSize; i++) {
            sumVector.add((double) 0);
        }
        int count = 0;

        List<Integer> indexList = new ArrayList<>();
        for (String s : appearances) {
            if (labelMap.containsKey(s)) {
                indexList.add(labelMap.get(s));
            }
        }

        // For every trained data
        for (int[] vec : values) {

            // Check to see if a row vector has 1 in all columns matching given test data
            boolean containsAll = true;
            for (int i : indexList) {
                boolean someUnitContains = false;
                if (vec[i] == 1) {
                    someUnitContains = true;
                }
                if (!someUnitContains) {
                    containsAll = false;
                    break;
                }
            }

            if (containsAll) {

                // Matches, update sum
                for (int i : indexList) {
                    sumVector.set(i, sumVector.get(i) - 1);
                }
                for (int i = 0; i < labelSize; i++) {
                    sumVector.set(i, sumVector.get(i) + vec[i]);
                }
                count++;
            }
        }

        // Does not exit, abort
        if (count == 0) {
            System.out.println("bad");
            return new LinkedHashMap<>();
        }
        System.out.println("good");

        // Add tuples to list for sorting
        List<Entry> entryList = new ArrayList<>(labelSize);

        for (int i = 0; i < labelSize; i++) {
            sumVector.set(i, sumVector.get(i) / count);
            entryList.add(new Entry(labels[i], sumVector.get(i)));
        }

        entryList.sort(new EntryComparator());

        // Generate map result
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        for (Entry e : entryList) {
            result.put(e.label, e.freq);
        }

        return result;
    }

    // Check if 2 labels are equivalent ($ and not $)
    private Set<Integer> equivLabels(int i) {
        if (idMap.containsKey(i)) {
            Set<Integer> l = idMap.get(i);
            l.add(i);
            return l;
        }
        return Collections.singleton(i);
    }
}

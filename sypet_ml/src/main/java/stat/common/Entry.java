package stat.common;


/**
 * Represents a method associated with its kNN distance value(frequency)
 */
public class Entry {
    public String label;
    public Double freq;

    public Entry(String label, Double freq) {
        this.label = label;
        this.freq = freq;
    }
}
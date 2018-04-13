package stat.common;

import java.util.Comparator;

/**
 * Comparator for sorting methods by freq
 */
public class EntryComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Entry) || !(o2 instanceof Entry)) {
            return 0;
        } else {
            Entry e1 = (Entry) o1;
            Entry e2 = (Entry) o2;
            return -e1.freq.compareTo(e2.freq);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
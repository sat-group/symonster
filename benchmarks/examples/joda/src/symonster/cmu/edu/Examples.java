package symonster.cmu.edu;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.DateTime;


public class Examples {
	
    public static int getAge(DateTime arg0) {
        DateTime v1 = DateTime.now();
        Years v2 = Years.yearsBetween(arg0, v1);
        int v3 = v2.getYears();
        return v3;
    }

    public static String dayOfWeek(String arg0, String arg1) {
        DateTimeFormatter v1 = DateTimeFormat.forPattern(arg1);
        DateTime v2 = DateTime.parse(arg0, v1);
        DateTime.Property v3 = v2.dayOfWeek();
        String v4 = v3.getAsText();
        return v4;
    }

    public static int daysOfMonth(String arg0, String arg1) {
        DateTimeFormatter v1 = DateTimeFormat.forPattern(arg1);
        DateTime v2 = DateTime.parse(arg0, v1);
        DateTime.Property v3 = v2.dayOfMonth();
        int v4 = v3.getMaximumValue();
        return v4;
    }

    public static int getDayFromString(String arg0, String arg1) {
        DateTimeFormatter v1 = DateTimeFormat.forPattern(arg1);
        LocalDate v2 = LocalDate.parse(arg0, v1);
        int v3 = v2.getDayOfMonth();
        return v3;
    }

    public static boolean isLeapYear(int arg0) {
        DateTime v1 = DateTime.now();
        DateTime v2 = v1.withWeekyear(arg0);
        DateTime.Property v3 = v2.year();
        boolean v4 = v3.isLeap();
        return v4;
    }
	
	public static int daysUtilNow(LocalDate arg0) {
	        LocalDate v1 = new LocalDate();
	        Days v2 = Days.daysBetween(arg0, v1);
	        int v3 = v2.getDays();
	        return v3;
	}
	
    public static int daysBetween(DateTime arg0, DateTime arg1) {
        LocalDate v1 = arg1.toLocalDate();
        LocalDate v2 = arg0.toLocalDate();
        Days v3 = Days.daysBetween(v2, v1);
        int v4 = v3.getDays();
        return v4;
    }
    
}

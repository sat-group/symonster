# My own analysis is anout analyzing equals and hashCode methods of each class. There are 2 major functionalities:
 * Check the presence of hashcode given equals method.
 * Check the return value of hashCode is not constant. Specifically, if the return value of the given class consists only of arithmetic expressions of constant values, the analysis will report a warning.
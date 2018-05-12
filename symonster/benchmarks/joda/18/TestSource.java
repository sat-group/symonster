public boolean test0() throws Throwable {
	return (isLeapYear(2000) == true);
}

public boolean test1() throws Throwable {
	return (isLeapYear(1900) == false);
}

public boolean test2() throws Throwable {
	return (isLeapYear(2011) == false);
}

public boolean test() throws Throwable {
	return test0() && test1() && test2();
}
public boolean test0() throws Throwable {
	return (daysOfMonth("2012/02", "yyyy/MM") == 29);	
}
	
public boolean test1() throws Throwable {
	return (daysOfMonth("2014/03", "yyyy/MM") == 31);					
}
	

public boolean test() throws Throwable {
		
    return test0() && test1();

} 

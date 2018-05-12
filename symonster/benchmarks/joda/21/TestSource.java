public boolean test0() throws Throwable {
	return (dayOfWeek("2015/11/10", "yyyy/MM/dd") == "Tuesday");	
}
	
public boolean test1() throws Throwable {
	return (dayOfWeek("2015/11/11", "yyyy/MM/dd") == "Wednesday");					
}
	

public boolean test() throws Throwable {
		
    return test0() && test1();

} 

public boolean test() throws Throwable {
	cmu.symonster.MyPoint mp = new cmu.symonster.MyPoint(1,3,5);
	mp.setName("foo");
	cmu.symonster.Point p = convert(mp);

	return (p.getRoll() == 3 && p.getName().equals("foo"));
}

public boolean test() throws Throwable {
	cmu.symonster.MyPoint mp = new cmu.symonster.MyPoint(1,3,5);
	cmu.symonster.Point p = convert(mp);

	return (p.getRoll() == 3 && p.getYaw() == 5);
}

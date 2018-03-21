public boolean test() throws Throwable {
	cmu.symonster.MyPoint mp = new cmu.symonster.MyPoint(2,3);
	cmu.symonster.Point p = convert(mp);
	return (p.getX() == 2 && p.getY() == 3);
}
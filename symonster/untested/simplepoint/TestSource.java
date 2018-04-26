public boolean test() throws Throwable {
    point.MyPoint mp = new point.MyPoint(2,3);
    point.Point p = convert(mp);
    return (p.getX() == 2 && p.getY() == 3);
}
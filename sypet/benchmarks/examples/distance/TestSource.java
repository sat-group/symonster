public static boolean test() throws Throwable {
	java.awt.geom.Point2D p1 = new java.awt.geom.Point2D.Double(0,1);
	java.awt.geom.Point2D p2 = new java.awt.geom.Point2D.Double(0,4);
	return (Math.abs(distance(p1,p2)-3)<0.000005);
}
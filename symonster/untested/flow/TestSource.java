public boolean test() throws Throwable {
	symonster.cmu.edu.Shape shape = new symonster.cmu.edu.Shape();
	shape.setY(2);
	symonster.cmu.edu.Shape res = setXZ(shape, 1, 3);
	return (res.getX() == 1 && res.getY() == 2 && res.getZ() == 3);
}

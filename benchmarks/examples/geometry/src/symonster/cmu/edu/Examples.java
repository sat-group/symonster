package symonster.cmu.edu;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

public class Examples {
	
	    public Rectangle2D scale(Rectangle2D arg0, double arg1, double arg2) {
	        Area v1 = new Area(arg0);
	        AffineTransform v2 = AffineTransform.getScaleInstance(arg1, arg2);
	        Area v3 = v1.createTransformedArea(v2);
	        Rectangle2D v4 = v3.getBounds2D();
	        return v4;
	    }
	    
	    public Rectangle2D shear(Rectangle2D arg0, double arg1, double arg2) {
	        Area v1 = new Area(arg0);
	        AffineTransform v2 = AffineTransform.getShearInstance(arg1, arg2);
	        Area v3 = v1.createTransformedArea(v2);
	        Rectangle2D v4 = v3.getBounds2D();
	        return v4;
	    }
	    
	    public Rectangle2D rotateQuadrant(Rectangle2D arg0, int arg1) {
	        Area v1 = new Area(arg0);
	        AffineTransform v2 = AffineTransform.getQuadrantRotateInstance(arg1);
	        Area v3 = v1.createTransformedArea(v2);
	        Rectangle2D v4 = v3.getBounds2D();
	        return v4;
	    }
	    
	    public Area rotate(Area arg0, Point2D arg1, double arg2) {
	        double v1 = arg1.getX();
	        double v2 = arg1.getY();
	        AffineTransform v3 = AffineTransform.getRotateInstance(arg2, v1, v2);
	        Area v4 = arg0.createTransformedArea(v3);
	        return v4;
	    }
	    
	    public Rectangle2D translate(Rectangle2D arg0, double arg1, double arg2) {
	        Area v1 = new Area(arg0);
	        AffineTransform v2 = AffineTransform.getTranslateInstance(arg1, arg2);
	        Area v3 = v1.createTransformedArea(v2);
	        Rectangle2D v4 = v3.getBounds2D();
	        return v4;
	    }
	    
	    public Rectangle2D getIntersection(Rectangle2D arg0, Ellipse2D arg1) {
	        Area v1 = new Area(arg1);
	        Rectangle2D v2 = v1.getBounds2D();
	        Rectangle2D v3 = v2.createIntersection(arg0);
	        return v3;
	    }
	   

}

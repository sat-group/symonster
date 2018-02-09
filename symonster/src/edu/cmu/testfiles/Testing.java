package edu.cmu.testfiles;

import cmu.symonster.MyPoint;

public class Testing {
    public static boolean pass(TargetInterface target){
        if (target.conv(10,20).getX()==10)
            return true;
        else return false;
    }
}

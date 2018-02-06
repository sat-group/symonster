package edu.cmu.tests;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;

public class PointPetriNet {
	
	private PetriNet pnet;
	
	public PointPetriNet(){
		this.pnet = new PetriNet();
	}
	
	public PetriNet getPetriNet(){
		return pnet;
	}
	
	private void createPlace(PetriNet pn, String name, int max){
		Place p = pn.createPlace(name);
		p.setMaxToken(max);
	}
		
	public void buildPointPetriNet(){
		
		// Create places for the petri-net
		createPlace(pnet, "int", 3);
		createPlace(pnet, "Point", 2);
		createPlace(pnet, "MyPoint", 2);
		createPlace(pnet, "void", 2);
		
		// Create transitions for the petri-net
		pnet.createTransition("int<-getX(Point)");
		pnet.createTransition("int<-getY(Point)");
		pnet.createTransition("void<-setX(Point,int)");
		pnet.createTransition("void<-setY(Point,int)");
		pnet.createTransition("Point<-Point(void)");
		
		pnet.createTransition("int<-getX(MyPoint)");
		pnet.createTransition("int<-getY(MyPoint)");
		pnet.createTransition("MyPoint<-MyPoint(int,int)");
		
		// Create clone transitions for the petri-net
		pnet.createTransition("int<-clone(int)");
		pnet.createTransition("Point<-clone(Point)");
		pnet.createTransition("MyPoint<-clone(MyPoint)");
		pnet.createTransition("void<-clone(void)");
		
		// Create flows for the petri-net
		pnet.createFlow("Point","int<-getX(Point)",1);
		pnet.createFlow("int<-getX(Point)","int",1);
		pnet.createFlow("Point","int<-getY(Point)",1);
		pnet.createFlow("int<-getY(Point)","int",1);
		pnet.createFlow("Point","void<-setX(Point,int)",1);
		pnet.createFlow("int","void<-setX(Point,int)",1);
		pnet.createFlow("void<-setX(Point,int)","void",1);
		pnet.createFlow("Point","void<-setY(Point,int)",1);
		pnet.createFlow("int","void<-setY(Point,int)",1);
		pnet.createFlow("void<-setY(Point,int)","void",1);
		pnet.createFlow("void","Point<-Point(void)",1);
		pnet.createFlow("Point<-Point(void)","Point",1);
		
		pnet.createFlow("MyPoint","int<-getX(MyPoint)",1);
		pnet.createFlow("int<-getX(MyPoint)","int",1);
		pnet.createFlow("MyPoint","int<-getY(MyPoint)",1);
		pnet.createFlow("int<-getY(MyPoint)","int",1);
		pnet.createFlow("int","MyPoint<-MyPoint(int,int)",2);
		pnet.createFlow("MyPoint<-MyPoint(int,int)","MyPoint",1);
		
		// Create flows for the clone edges
		pnet.createFlow("int","int<-clone(int)",1);
		pnet.createFlow("int<-clone(int)","int",2);
		pnet.createFlow("Point","Point<-clone(Point)",1);
		pnet.createFlow("Point<-clone(Point)","Point",2);
		pnet.createFlow("MyPoint","MyPoint<-clone(MyPoint)",1);
		pnet.createFlow("MyPoint<-clone(MyPoint)","MyPoint",2);
		pnet.createFlow("void","void<-clone(void)",1);
		pnet.createFlow("void<-clone(void)","void",2);
		
	}

}

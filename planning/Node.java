package group26.planning;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.unimelb.swen30006.partc.roads.Intersection;

/**
 * A node in the graph that represents an intersection in the simulation
 * @author Group 26 - Stewart Collins 326206, Raja Ramkumar 725564, Thomas Nielsen 357491
 *
 */
public class Node {
	//The roads connected to the intersection
	private ArrayList<Edge> roadsStarting;
	//Indicates whether the node has been visited in this search already
	private boolean visited;
	//The position of the intersection
	private Point2D.Double position;
	//The width and length of the intersection
	private float width, length;
	
	/**
	 * A constructor for the node
	 * @param intersection The intersection the node represents
	 */
	public Node(Intersection intersection) {
		this.position = intersection.pos;
		this.visited = false;
		this.roadsStarting = new ArrayList<Edge>();
		this.width = intersection.width;
		this.length = intersection.length;
	}
	
	/**
	 * Returns the length of the intersection
	 * @return The length of the intersection
	 */
	public float getLength(){
		return this.length;
	}
	
	/**
	 * Returns the width of the intersection
	 * @return The width of the intersection
	 */
	public float getWidth(){
		return this.width;
	}
	
	/**
	 * Returns the position of the intersection
	 * @return The position of the intersection
	 */
	public Point2D.Double getPosition(){
		return this.position;
	}
	
	/**
	 * Returns the roads connected to the intersection
	 * @return The roads connected to the intersection
	 */
	public ArrayList<Edge> getRoads(){
		return roadsStarting;
	}
	
	/**
	 * Returns whether the node has been visited this search or not
	 * @return Boolean indicating if the node has been visited
	 */
	public boolean getVisited(){
		return visited;
	}
	
	/**
	 * Sets visited to true
	 */
	public void setVisited(){
		this.visited = true;
	}
	
	/**
	 * Sets visited to false
	 */
	public void setUnvisited(){
		this.visited = false;
	}
	
	/**
	 * Calculates the straight line distance from the node to the destination
	 * @param destination The destination the distance is calculated from
	 * @return The distance to the destination
	 */
	public double straightLineDistanceToDestination(Point2D.Double destination){
		double xDif, yDif;
		double straightLineDistance;
		
		xDif = destination.getX() - this.position.getX();
		yDif = destination.getY() - this.position.getY();		
		straightLineDistance = Math.sqrt((xDif*xDif)+(yDif*yDif));
		
		return straightLineDistance;		
	}
	
	public void addRoad(Edge edge){
		roadsStarting.add(edge);
	}
	
	
	
}

package group26.planning;

import java.awt.geom.Point2D;

import com.unimelb.swen30006.partc.roads.Road;

/**
 * Represents an edge in the graph, a road in the simulation
 * @author Group 26 - Stewart Collins 326206, Raja Ramkumar 725564, Thomas Nielsen 357491
 *
 */
public class Edge {
	//The allowable distance a road may be from the destination to consider having reached it
	private final int ALLOWEDDISTANCEFROMDESTINATION = 50;
	//The node the edge starts at
	private Node startNode;
	//The node the edge ends at if it has a final intersection
	private Node endNode;
	//The position the edge ends at if it has no final intersection
	private Point2D.Double endPosition;
	
	/**
	 * Constructor for the edge class
	 * @param startNode The node the edge starts from
	 */
	Edge(Node startNode){
		this.startNode = startNode;
		endNode = null;
	}
	
	/**
	 * Sets the end node
	 * @param endNode The node the road ends at
	 */
	public void setEndNode(Node endNode){
		this.endNode = endNode;
	}
	
	/**
	 * Returns the node at the opposite end of the road to the given node
	 * @param startNode The node that is already
	 * @return
	 */
	public Node getEndOfRoad(Node startNode){
		if(this.startNode == startNode){
			return endNode;
		}else if(this.endNode == startNode){
			return startNode;
		}else{
			System.out.println("Start node not recognised");
			return null;
		}
	}
	
	/**
	 * Returns the end node of the edge
	 * @return The end node
	 */
	public Node getEndNode(){
		return endNode;
	}
	
	/**
	 * Returns the start node of the edge
	 * @return The start node
	 */
	public Node getStartNode(){
		return startNode;
	}
	
	/**
	 * Returns whether the edge has an end node
	 * @return Boolean indicating the existence of an end node
	 */
	public boolean hasEndNode(){
		if(endNode == null){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * Sets the end position of the edge if it does not have an end node based on the road it represents
	 * @param road The road the edge represents
	 */
	public void setEndPosition(Road road){
		//Checks which of the road positions is the one already connected to start node
		if(connectedToIntersection(road.getEndPos())){
			//Sets the end position to the unconnected end
			this.endPosition = road.getStartPos();
		}else if(connectedToIntersection(road.getStartPos())){
			this.endPosition = road.getStartPos();
		}else{
			System.out.println("Fatal error, map invalid");
			System.out.println("Road with start position:("+road.getStartPos().x+","+road.getStartPos().getX()+") end position:("+road.getEndPos().x+","+road.getEndPos().getX()+")");
			System.out.println("Does not connect with intersection with position:(" + this.startNode.getPosition().getX()+","+ this.startNode.getPosition().getY()+") width:"+startNode.getWidth()+" length:"+startNode.getLength());
			System.exit(0);
		}
	}
	
	/**
	 * Returns whether there is a point on the road within the given range
	 * for reaching the destination
	 * @param destination The destination to be reached
	 * @return Boolean indicating if the destination is on the road
	 */
	public boolean destinationOnRoad(Point2D.Double destination){
		double distance;		
		distance = closestPointToDestination(destination);
		if(distance < ALLOWEDDISTANCEFROMDESTINATION){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Calculates the closest point to the destination on the road and the distance
	 * from that point to the destination
	 * @param destination The destination to be reached
	 * @return The distance from the destination at the closest point on the road
	 */
	private double closestPointToDestination(Point2D.Double destination){
		double xRangeStart, xRangeEnd;
		double yRangeStart, yRangeEnd;
		double straightLineDistance;
		double xDif, yDif;
		double closestY, closestX;
		
		//If road has an end node then calculate the start and end of the road based on that
		if(endNode == null){
			//Determine which is the lowest and which is the highest position of the road for each axis
			if(this.startNode.getPosition().getX() > this.endPosition.getX()){
				xRangeEnd = this.startNode.getPosition().getX();
				xRangeStart = this.endPosition.getX();
			}else{
				xRangeStart = this.startNode.getPosition().getX();
				xRangeEnd = this.endPosition.getX();
			}if(this.startNode.getPosition().getY() > this.endPosition.getY()){
				yRangeEnd = this.startNode.getPosition().getY();
				yRangeStart = this.endPosition.getY();
			}else{
				yRangeStart = this.startNode.getPosition().getY();
				yRangeEnd = this.endPosition.getY();
			}
		}else{
			//Otherwise use the final position of the road
			if(this.startNode.getPosition().getX() > this.endNode.getPosition().getX()){
				xRangeEnd = this.startNode.getPosition().getX();
				xRangeStart = this.endNode.getPosition().getX();
			}else{
				xRangeStart = this.startNode.getPosition().getX();
				xRangeEnd = this.endNode.getPosition().getX();
			}if(this.startNode.getPosition().getY() > this.endNode.getPosition().getY()){
				yRangeEnd = this.startNode.getPosition().getY();
				yRangeStart = this.endNode.getPosition().getY();
			}else{
				yRangeStart = this.startNode.getPosition().getY();
				yRangeEnd = this.endNode.getPosition().getY();
			}
		}
		
		//Determine the closest x position on the road
		if(destination.getX()>xRangeStart){
			if(destination.getX()<xRangeEnd){
				closestX = destination.getX();
			}else{
				closestX = xRangeEnd;
			}
		}else{
			closestX = xRangeStart;
		}
		
		//Determine the closest y position on the road
		if(destination.getY()>yRangeStart){
			if(destination.getY()<yRangeEnd){
				closestY = destination.getY();
			}else{
				closestY = yRangeEnd;
			}
		}else{
			closestY = yRangeStart;
		}
		
		//Determine the distance from the closest position to the destination for each axis
		xDif = destination.getX() - closestX;
		yDif = destination.getY() - closestY;
		
		//Calculate the straight line distance to the destination
		straightLineDistance = Math.sqrt((xDif*xDif)+(yDif*yDif));
		return straightLineDistance;
		
	}
	
	/**
	 * Indicates whether the given position is connected to the start node intersection
	 * @param position The position of the end of the road
	 * @return True if connected to the start intersection, false if not
	 */
	private boolean connectedToIntersection(Point2D.Double position){
		Double xDif, yDif;
		
		xDif = this.startNode.getPosition().getX() - position.getX();
		xDif = Math.abs(xDif);
		yDif = this.startNode.getPosition().getY() - position.getY();
		yDif = Math.abs(yDif);
		
		if(xDif <= this.startNode.getWidth()){
			if(yDif <= this.startNode.getLength()){
				return true;
			}
		}return false;
	}

}

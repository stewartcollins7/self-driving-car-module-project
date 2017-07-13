package group26.planning;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.unimelb.swen30006.partc.core.World;
import com.unimelb.swen30006.partc.roads.Road;

/**
 * This class stores the graph and the route to a given destination and interacts with the planning control class
 * @author Group 26 - Stewart Collins 326206, Raja Ramkumar 725564, Thomas Nielsen 357491
 *
 */
public class Route {
	//A graph of all the roads and intersections
	private Graph roadMap;
	//A list of all the intersections the car needs to travel between to reach the destination
	private ArrayList<Node> currentRoute;
	//The destination to be reached
	private Point2D.Double destination;
	//The position of the node most recently given to the planning system
	private Point2D.Double previousNodePosition;
	
	/**
	 * Constructor for the route class, creates the graph from the world object
	 * @param world The world that contains the intersections and roads the graph is constructed from
	 */
	public Route(World world){
		roadMap = new Graph(world);
	}
	
	/**
	 * Plans a route to the given destination originating from the given road
	 * @param startingRoad The road the car is starting from
	 * @param destination The location of the given destination
	 * @return Returns true if a valid route exists, false if not
	 */
	public boolean planRoute(Road startingRoad, Point2D.Double destination){
		currentRoute = this.roadMap.planRoute(startingRoad, destination);
		if(currentRoute == null){
			return false;
		}else{
			this.destination = destination;
			return true;
		}
	}
	
	/**
	 * Calculates the distance to the destination from the remaining nodes on the route
	 * @return The distance to the destination
	 */
	public double distanceToDestination(){
		double total = 0;
		double intersectionDistance;
		double distanceFromCurrentNode, distanceFromEndNode;
		Point2D.Double finalNodePosition;
		double xDif, yDif;
		Node node1, node2;
		
		//Calculate the distance between all the nodes on the route
		for(int i=0;i<(currentRoute.size()-1);i++){
			node1 = currentRoute.get(i);
			node2 = currentRoute.get(i+1);
			if(node1.getPosition().getX()==node2.getPosition().getX()){
				intersectionDistance = node1.getPosition().getY()-node2.getPosition().getY();
				intersectionDistance = Math.abs(intersectionDistance);
			}else{
				intersectionDistance = node1.getPosition().getX()-node2.getPosition().getX();
				intersectionDistance = Math.abs(intersectionDistance);
			}total += intersectionDistance;
		}
		
		if(currentRoute.size()>0){
			//Calculate the distance from the node the car is currently travelling towards to next node on route
			node1 = currentRoute.get(0);
			if(node1.getPosition().getX()==previousNodePosition.getX()){
				intersectionDistance = node1.getPosition().getY()-previousNodePosition.getY();
				intersectionDistance = Math.abs(intersectionDistance);
			}else{
				intersectionDistance = node1.getPosition().getX()-previousNodePosition.getX();
				intersectionDistance = Math.abs(intersectionDistance);
			}total += intersectionDistance;
			
			//Get the position of the final node
			finalNodePosition = currentRoute.get(currentRoute.size()-1).getPosition();
		}else{
			//If the route is empty the final node is the last given node
			finalNodePosition = previousNodePosition;
		}
		//Calculate distance from final node to destination
		yDif = finalNodePosition.getY() - destination.getY();
		yDif = Math.abs(yDif);
		xDif = finalNodePosition.getX() - destination.getX();
		xDif = Math.abs(xDif);
		if(xDif > yDif){
			distanceFromEndNode = xDif;
		}else{
			distanceFromEndNode = yDif;
		}total += distanceFromEndNode;
		
		return total;
	}
	
	/**
	 * Returns the position of the next intersection on the route and removes that intersection from the route
	 * @return The position of the next intersection on the route
	 */
	public Point2D.Double nextIntersectionPosition(){
		Node nextNode;
		
		if(currentRoute.size()>0){
			nextNode = currentRoute.get(0);
			this.previousNodePosition = nextNode.getPosition();
			currentRoute.remove(0);		
			return previousNodePosition;
		}else{
			return null;
		}
	}
}

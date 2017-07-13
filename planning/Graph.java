package group26.planning;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import com.unimelb.swen30006.partc.core.World;
import com.unimelb.swen30006.partc.roads.Intersection;
import com.unimelb.swen30006.partc.roads.Intersection.Direction;
import com.unimelb.swen30006.partc.roads.Road;

/**
 * The graph class the contains a graph of the road structure based on nodes and edges
 * Performs searches to determine the best route to destination
 * @author Group 26 - Stewart Collins 326206, Raja Ramkumar 725564, Thomas Nielsen 357491
 *
 */
public class Graph {
	//The intersections on the graph
	HashMap<Intersection, Node> intersections;
	//The roads on the graph
	HashMap<Road, Edge> roads;
	
	/**
	 * Constructor for the graph class, builds the graph based on the intersections
	 * obtained from the world object
	 * @param world The world object that contains the intersections and roads
	 */
	public Graph(World world){
		Node tempNode;
		Edge tempEdge;
		HashMap<Direction, Road> tempRoads;
		Intersection[] tempIntersections;	
		
		this.intersections = new HashMap<Intersection, Node>();
		this.roads = new HashMap<Road, Edge>();
		
		tempIntersections = world.getIntersections();
		//Add each intersection to the graph
		for(Intersection intersection : tempIntersections){
			tempNode = new Node(intersection);
			this.intersections.put(intersection, tempNode);
			tempRoads = intersection.roads;
			
			for(Road tempRoad : tempRoads.values()){
				//For each road connected to the intersection check if already added to graph
				if(this.roads.containsKey(tempRoad)){
					//If already exists then set it's end node to the current intersection
					tempEdge = roads.get(tempRoad);
					tempEdge.setEndNode(tempNode);
				}else{
					//Otherwise add it to the graph
					tempEdge= new Edge(tempNode);
					this.roads.put(tempRoad,  tempEdge);
				}tempNode.addRoad(tempEdge);
			}
		}
		
		//Check all roads to update the end position of those that are not connected to an end node
		for(Road tempRoad : this.roads.keySet()){
			tempEdge = roads.get(tempRoad);
			if(!tempEdge.hasEndNode()){
				tempEdge.setEndPosition(tempRoad);
			}
		}
	}
	
	/**
	 * Returns whether a valid route exists to the destination
	 * @param currentRoad The road the car is currently on
	 * @param destination The destination the car wants to reach
	 * @return Boolean indicating whether a valid route exists to the destination
	 */
	public boolean isValidRoute(Road currentRoad, Point2D.Double destination){
		Edge currentEdge;
		boolean destinationFound;	
		currentEdge = roads.get(currentRoad);
		destinationFound = depthFirstSearch(currentEdge, destination);
		setNodesToUnvisited();
		return destinationFound;		
	}
	
	/**
	 * Plans a route from the given road to the destination
	 * @param currentRoad The road the car is starting on
	 * @param destination The destination the car wants to reach
	 * @return Returns the route if a valid route exists, null otherwise
	 */
	public ArrayList<Node> planRoute(Road currentRoad, Point2D.Double destination){
		Node startingNode;
		ArrayList<Node> route = null;
		Edge currentEdge = roads.get(currentRoad);
		
		//Check if there is a valid route
		if(isValidRoute(currentRoad, destination)){
			route = new ArrayList<Node>();
			if(!currentEdge.hasEndNode()){
				startingNode = currentEdge.getStartNode();
			}else{
				//Choose the node closest to the destination as the starting node
				if(currentEdge.getStartNode().straightLineDistanceToDestination(destination) > currentEdge.getEndNode().straightLineDistanceToDestination(destination)){
					startingNode = currentEdge.getEndNode();
				}else{
					startingNode = currentEdge.getStartNode();
				}
			}
			//Find the route to the destination
			route = pathFindingSearch(startingNode, destination);
			this.setNodesToUnvisited();
		}
		
		return route;
	}
	
	/**
	 * Uses A* search to find an efficient route to the given destination
	 * @param currentNode The node that is currently being searched
	 * @param destination The destination to be reached
	 * @return Null if no path found or the path from the current node to the destination
	 */
	private ArrayList<Node> pathFindingSearch(Node currentNode, Point2D.Double destination){
		ArrayList<Node> route = null;
		Node nextNode;
		ArrayList<Edge> edges = currentNode.getRoads();
		
		//First check if any of the roads can reach the destination
		for(Edge edge : edges){
			if(edge.destinationOnRoad(destination)){
				route = new ArrayList<Node>();
				route.add(currentNode);
				return route;
			}
		}//Sort the edges based on how close they are to the destination
		edges = this.sortEdgesByClosestIntersectionToDestination(currentNode, destination);
		
		//For each edge recursively search the node at the end of the road until route is found
		for(Edge edge : edges){
			if(edge.hasEndNode()){
				nextNode = edge.getEndOfRoad(currentNode);
				if(!nextNode.getVisited()){
					nextNode.setVisited();
					route = pathFindingSearch(nextNode, destination);
					if(route != null){
						route.add(0, currentNode);
						return route;
					}
				}
			}
		}return route;
	}
	
	/**
	 * Returns a list of the edges of the node sorted by the distance from the destination of their end node
	 * @param currentNode The node that contains the edges
	 * @param destination The destination to be reached
	 * @return An array of the edges contained within the node sorted by the distance from the destination of their end node
	 */
	private ArrayList<Edge> sortEdgesByClosestIntersectionToDestination(Node currentNode, Point2D.Double destination){
		ArrayList<Edge> edges = currentNode.getRoads();
		ArrayList<Edge> sortedEdges = new ArrayList<Edge>();
		Node edge1EndNode, edge2EndNode;
		Edge edge2;
		boolean placed = false;
		int i;
		
		for(Edge edge1 : edges){
			edge1EndNode = edge1.getEndOfRoad(currentNode);
			//If an edge does not have an end node then place at end of list
			if(edge1EndNode == null){
				sortedEdges.add(sortedEdges.size(), edge1);
			}else{
				//Otherwise find it's position in the list
				for(i=0;i<sortedEdges.size();i++){
					edge2 = sortedEdges.get(i);
					edge2EndNode = edge2.getEndOfRoad(currentNode);
					if(edge2EndNode == null){
						sortedEdges.add(i, edge1);
						placed = true;
						break;
					}else if(edge1EndNode.straightLineDistanceToDestination(destination) < edge2EndNode.straightLineDistanceToDestination(destination)){
						sortedEdges.add(i, edge1);
						placed = true;
						break;
					}
				}if(!placed){
					sortedEdges.add(i, edge1);
				}else{
					placed = false;
				}
			}
		}return sortedEdges;
	}
	
	/**
	 * Sets all nodes in the graph to unvisited
	 * Must be done after valid route or path finding search
	 */
	private void setNodesToUnvisited(){
		for(Node node : intersections.values()){
			node.setUnvisited();
		}
	}
	
	
	
	/**
	 * Uses DFS to determine whether a valid route exists to the destination
	 * @param currentEdge The edge the car is currently on
	 * @param destination The destination the car wants to reach
	 * @return Boolean indicating whether a valid route exists to the destination
	 */
	private boolean depthFirstSearch(Edge currentEdge, Point2D.Double destination){
		boolean destinationFound;
		Node currentNode;
		ArrayList<Edge> nodeEdges;
	
		//Check whether the given edge is close enough to the destination
		destinationFound = currentEdge.destinationOnRoad(destination);
		if(destinationFound){
			return true;
		}else{
			//Search all the edges of the start node if not yet visited
			currentNode = currentEdge.getStartNode();
			if(!currentNode.getVisited()){
				currentNode.setVisited();
				nodeEdges = currentNode.getRoads();
				for(Edge edge : nodeEdges){
					if(edge != currentEdge){
						destinationFound = depthFirstSearch(edge, destination);
						if(destinationFound){
							return true;
						}
					}
				}
			}//Search all the edges of the end node if not yet visited
			if(currentEdge.hasEndNode()){
				currentNode = currentEdge.getEndNode();
				if(!currentNode.getVisited()){
					currentNode.setVisited();
					nodeEdges = currentNode.getRoads();
					for(Edge edge : nodeEdges){
						if(edge != currentEdge){
							destinationFound = depthFirstSearch(edge, destination);
							if(destinationFound){
								return true;
							}
						}
					}
				}
			}
		}return false;
		
	}
	
}

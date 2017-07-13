package group26.planning;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;

import com.unimelb.swen30006.partc.ai.interfaces.IPlanning;
import com.unimelb.swen30006.partc.ai.interfaces.PerceptionResponse;
import com.unimelb.swen30006.partc.ai.interfaces.PerceptionResponse.Classification;
import com.unimelb.swen30006.partc.core.World;
import com.unimelb.swen30006.partc.core.objects.Car;
import com.unimelb.swen30006.partc.roads.Road;

/**
 * The main planning class that coordinates all of the different elements of the planning subsytem
 * Implements the IPlanning interface
 * @author Group 26 - Stewart Collins 326206, Raja Ramkumar 725564, Thomas Nielsen 357491
 *
 */
public class PlanningControl implements IPlanning{
	//Constants used in the update method
	private final int ACCELERATE = 1;
	private final int BRAKE = -1;
	private final int MAXINTERSECTIONSIZE = 30;
	private final int AVERAGESPEED = 40;
	
	//Objects from the simulation used in planning
	private World world;
	private Car car;
	
	//Stores a graph of the roads and calculates the route
	private Route route;
	//Handles input to the car
	private CarController controller;
	//Handles collision scenarios for the car
    private CollisionHandler collisionHandler;
    //Handles only traffic light responses currently
    private RoadRuleHandler roadRuleHandler;

	//The current position of the car
	private Point2D.Double currentPosition;
	//The current direction the car is traveling in
	private Direction currentDirection;
	//The direction of the next intersection
	private Direction nextIntersectionDirection;
	//The destination position
	private Point2D.Double destination;
	//Indicates whether the car is turning
	private boolean turning;
	//The position of the next and previous intersections on the route
	private Point2D.Double positionOfnextIntersection;
	private Point2D.Double prevIntersection;
	//Indicates whether the destination has been reached
	private boolean destinationReached;
	
	//Specifies the directions the car can travel in
	private enum Direction {
		North, South, East, West
	}
	
	/**
	 * The constructor for the planning control subsystem
	 * @param world The world object
	 * @param car The car that is being controlled
	 */
	public PlanningControl(World world, Car car){
		this.world = world;
		this.car = car;
		this.controller = new CarController(car);
		this.currentPosition = car.getPosition();
		this.route = new Route(world);
		this.collisionHandler = new CollisionHandler();
		this.roadRuleHandler = new RoadRuleHandler();
		this.currentDirection = Direction.East;
		this.destination = null;
	}
	
	/**
	 * Plans a route to the given destination
	 * @param destination The destination the car wants to travel to
	 * @return Boolean indicating whether there is a valid route to the given destination
	 */
	public boolean planRoute(Double destination) {
		boolean validRoute;
		Road startingRoad = world.roadAtPoint(car.getPosition());
		validRoute = route.planRoute(startingRoad, destination);
		
		if(validRoute){
			this.destination = destination;
			positionOfnextIntersection = route.nextIntersectionPosition();
			setNextIntersectionDirection();
			destinationReached = false;
		}else{
			System.out.println("No valid route found to destination");
		}
		return validRoute;
	}
	
	/**
	 * Calculates an ETA for the remaining journey based on the distance remaining
	 * @return The time to destination in seconds
	 */
	public float eta() {
		float routeDistance=0;
		float distanceToNextIntersection=0;
		float totalDistance;
		
		if(positionOfnextIntersection != null){
			routeDistance = (float)route.distanceToDestination();
			distanceToNextIntersection = distanceToNextIntersection();
		}totalDistance = routeDistance + distanceToNextIntersection;
		
		return totalDistance/AVERAGESPEED;
	}

	/**
	 * Updates the planning system with the latest perception results
	 * Uses the perception results to determine whether a stop in needed to avoid
	 * collision or respond to traffic lights
	 * If no stop is needed then steers the car based on the current route
	 * @param results The latest perception results from the perception subsystem
	 * @param delta The amount of time passed since the last update
	 */
	public void update(PerceptionResponse[] results, float delta) {
		//Update the current position
		this.currentPosition = car.getPosition();
		
		//Used for calculating how far into an intersection the car is
		double axisDif;
		
		//If no route has been selected then do nothing
		if(destination == null){
			return;
		}
		
		if(results==null){
			if(!destinationReached){
				controller.adjustCar(getDirectionAngle(currentDirection), ACCELERATE);
		    }else{
			    controller.adjustCar(getDirectionAngle(currentDirection), BRAKE);
			}
		}else{
			//Sorting the perception response to handle the highest priority
			PerceptionResponse[] sortedResponse = prioritisePerceptionResponse(results); 
			
			//Updating the collision handler
			collisionHandler.update(sortedResponse);
			
			//Getting traffic lights for road rule handler
			ArrayList<PerceptionResponse> trafficLights = new ArrayList<PerceptionResponse>();
			for(PerceptionResponse worldObject : results){
				if(worldObject.objectType == Classification.TrafficLight){
					trafficLights.add(worldObject);
				}
			}//Updating road rule handler
			roadRuleHandler.update(trafficLights);
			

			if(!destinationReached && !collisionHandler.collisionImminent() && !roadRuleHandler.checkForStop()){
				controller.adjustCar(getDirectionAngle(currentDirection), ACCELERATE);
		    }else{
			    controller.adjustCar(getDirectionAngle(currentDirection), BRAKE);
			}
		}
		
				
		
		//If there are more intersections on the current route
		if(positionOfnextIntersection != null){
			//Check if turning
			if(this.turning){
				//Check if need to drive halfway through the intersection to reach the right lane
				if(hookTurnRequired()){
					//Drive halfway through the intersection
					if(this.currentDirection == Direction.East || this.currentDirection == Direction.West){
						axisDif = car.getPosition().getX() - prevIntersection.getX();
						axisDif = Math.abs(axisDif);
					}else{
						axisDif = car.getPosition().getY() - prevIntersection.getY();
						axisDif = Math.abs(axisDif);
					}if(axisDif > ((MAXINTERSECTIONSIZE/2)-5)){
						this.currentDirection = this.nextIntersectionDirection;
						this.turning = false;
					}
				}else{
					//Otherwise turn immediately
					this.currentDirection = this.nextIntersectionDirection;
					this.turning = false;
				}
			}else{
				//If reached intersection then get next intersection
				if(reachedIntersection()){
					this.prevIntersection = this.positionOfnextIntersection;
					this.positionOfnextIntersection = route.nextIntersectionPosition();
					if(positionOfnextIntersection != null){
						this.setNextIntersectionDirection();
						this.turning = true;
					}else{
						this.setDestinationDirection();
						this.turning = true;
					}
				}
			}
		}else{
			//If no more intersections then check if reached destination
			if(this.currentDirection == Direction.East || this.currentDirection == Direction.West){
				axisDif = car.getPosition().getX() - destination.getX();
				axisDif = Math.abs(axisDif);
			}else{
				axisDif = car.getPosition().getY() - destination.getY();
				axisDif = Math.abs(axisDif);
			}if(axisDif < 3){
				destinationReached = true;
			}
		}
		
		//Update the car
		car.update(delta);
	}
	
	/**
	 * Indicates whether the car has reached the intersection it was traveling towards
	 * @return True if it has reached the intersection, false if not
	 */
	private boolean reachedIntersection(){
		if(this.currentDirection == Direction.East){
			if(car.getPosition().getX() >= positionOfnextIntersection.getX()){
				return true;
			}else{
				return false;
			}
		}else if(this.currentDirection == Direction.West){
			if(car.getPosition().getX() <= positionOfnextIntersection.getX()){
				return true;
			}else{
				return false;
			}
		}else if(this.currentDirection == Direction.North){
			if(car.getPosition().getY() >= positionOfnextIntersection.getY()){
				return true;
			}else{
				return false;
			}
		}else if(this.currentDirection == Direction.South){
			if(car.getPosition().getY() <= positionOfnextIntersection.getY()){
				return true;
			}else{
				return false;
			}
		}else{
			System.out.println("Direction not recognised");
			return false;
		}
	}
	/**
	 * Updates the direction of the next intersection
	 */
	private void setNextIntersectionDirection(){
		double axisDifference;
		axisDifference = currentPosition.getX() - positionOfnextIntersection.getX();
		axisDifference = Math.abs(axisDifference);
		if(axisDifference > MAXINTERSECTIONSIZE){
			if(currentPosition.getX()>positionOfnextIntersection.getX()){
				nextIntersectionDirection = Direction.West;
			}else{
				nextIntersectionDirection = Direction.East;
			}
		}else{
			axisDifference = currentPosition.getY() - positionOfnextIntersection.getY();
			axisDifference = Math.abs(axisDifference);
			if(axisDifference > MAXINTERSECTIONSIZE){
				if(currentPosition.getY()>positionOfnextIntersection.getY()){
					nextIntersectionDirection = Direction.South;
				}else{
					nextIntersectionDirection = Direction.North;
				}
			}else{
				System.out.println("Error, direction for next intersection not recognised");
			}
		}
	}
	
	/**
	 * Sets the direction of the destination when the final intersection has been reached
	 */
	private void setDestinationDirection(){
		double xAxisDifference, yAxisDifference;
		
		xAxisDifference = currentPosition.getX() - destination.getX();
		xAxisDifference = Math.abs(xAxisDifference);
		yAxisDifference = currentPosition.getY() - destination.getY();
		yAxisDifference = Math.abs(yAxisDifference);
		
		if(xAxisDifference > yAxisDifference){
			if(currentPosition.getX()>destination.getX()){
				nextIntersectionDirection = Direction.West;
			}else{
				nextIntersectionDirection = Direction.East;
			}
		}else{
			if(currentPosition.getY()>destination.getY()){
				nextIntersectionDirection = Direction.South;
			}else{
				nextIntersectionDirection = Direction.North;
			}
		}
	}

	/**
	 * Indicates whether the car needs to drive halfway through the intersection before turning
	 * in order to reach the right lane
	 * @return True if a hook turn is required, false if not
	 */
	private boolean hookTurnRequired(){
		boolean hookTurnRequired;
		if(this.currentDirection == Direction.East){
			if(this.nextIntersectionDirection == Direction.South){
				hookTurnRequired = true;
			}else{
				hookTurnRequired = false;
			}
		}if(this.currentDirection == Direction.West){
			if(this.nextIntersectionDirection == Direction.North){
				hookTurnRequired = true;
			}else{
				hookTurnRequired = false;
			}
		}if(this.currentDirection == Direction.South){
			if(this.nextIntersectionDirection == Direction.West){
				hookTurnRequired = true;
			}else{
				hookTurnRequired = false;
			}
		}if(this.currentDirection == Direction.North){
			hookTurnRequired = true;
		}else{
			hookTurnRequired = false;
		}return hookTurnRequired;
	}
	
	/**
	 * Returns the angle the car needs to face to drive in the given direction
	 * @param direction The direction the car needs to face
	 * @return The angle of the given direction
	 */
	private int getDirectionAngle(Direction direction){
		if(direction == Direction.North){
			return 90;
		}else if(direction == Direction.South){
			return 270;
		}else if(direction == Direction.East){
			return 0;
		}else if(direction == Direction.West){
			return 180;
		}else{
			return -1;
		}
	}
	
	/**
	 * Returns the distance to the next intersection, used for calculating eta
	 * @return The distance to the next intersection
	 */
	private float distanceToNextIntersection(){
		double distanceToNextIntersection = 0;
		if(this.currentDirection == Direction.East){
			distanceToNextIntersection = positionOfnextIntersection.getX() - car.getPosition().getX();
		}else if(this.currentDirection == Direction.West){
			distanceToNextIntersection = car.getPosition().getX() - positionOfnextIntersection.getX();
		}else if(this.currentDirection == Direction.North){
			distanceToNextIntersection = positionOfnextIntersection.getY() - car.getPosition().getY();
		}else if(this.currentDirection == Direction.South){
			distanceToNextIntersection = car.getPosition().getY() - positionOfnextIntersection.getY();
		}return (float)distanceToNextIntersection;
	}
	
	/**
	 * This function sorts the perception response with respect to timetoCollision
	 * @param current perception response of the world
	 * @return sorted perception response
	 */
	private PerceptionResponse[] prioritisePerceptionResponse(PerceptionResponse[] results){
		
		for(int i=0; i<results.length - 1; i++)
		{
			int index = i;
            for (int j = i + 1; j < results.length; j++)
                if (results[j].timeToCollision < results[index].timeToCollision)
                    index = j;
      
            PerceptionResponse smallerCollisionTime = results[index]; 
            results[index] = results[i];
            results[i] = smallerCollisionTime;
		}
		
		return results;
	}
	

}

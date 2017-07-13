package group26.planning;

import java.util.ArrayList;
import java.util.HashMap;

import com.unimelb.swen30006.partc.ai.interfaces.PerceptionResponse;
import com.unimelb.swen30006.partc.core.infrastructure.TrafficLight;

/**
 * RoadRuleHandler currently only checks for traffic lights to see if the car needs to stop for a close traffic light
 * @author Group 26 - Stewart Collins 326206, Raja Ramkumar 725564, Thomas Nielsen 357491
 *
 */
public class RoadRuleHandler {
	private ArrayList<PerceptionResponse> trafficLights;
	
	public RoadRuleHandler(){
		trafficLights = null;
	}
	
	
	/**
	 * Updates and sorts the traffic lights with the new location of the car
	 * @param trafficLights The traffic light objects close to the car
	 */
	public void update(ArrayList<PerceptionResponse> trafficLights){
		PerceptionResponse currentClosestTrafficLight;
		int i;
		ArrayList<PerceptionResponse> sortedTrafficLights = new ArrayList<PerceptionResponse>();
		//Only need to find the closest traffic light that the car is facing as that will be the only one that affects the car
		for(PerceptionResponse trafficLight : trafficLights){
			if(sortedTrafficLights.size()<1){
				sortedTrafficLights.add(trafficLight);
			}else{
				for(i=0;i<sortedTrafficLights.size();i++){
					currentClosestTrafficLight = sortedTrafficLights.get(0);
					if(trafficLight.distance < currentClosestTrafficLight.distance){
						sortedTrafficLights.add(0, trafficLight);
						//This line is supposed to check if the car is facing the traffic light but it may be incorrect, will find out in testing
						if(trafficLight.direction.angle()<180){
							sortedTrafficLights.add(0, trafficLight);
						}else{
							sortedTrafficLights.add(sortedTrafficLights.size(), trafficLight);
							
						}break;
					}
				}if(i==sortedTrafficLights.size()){
					sortedTrafficLights.add(sortedTrafficLights.size(), trafficLight);
				}
			}		
		}this.trafficLights = sortedTrafficLights;
	}
	
	/**
	 * Returns whether the car needs to stop for a traffic light
	 * @return True if the car needs to stop for a close red or orange light
	 */
	public boolean checkForStop(){
		if(trafficLights.size() < 1){
			return false;
		}
		PerceptionResponse closestTrafficLight = trafficLights.get(0);
		HashMap<String,Object> trafficLightInformation;
		if(trafficLights.size()<1){
			return false;
		}else{
			//Check if closest traffic light is facing the car (not behind the car)
			if(closestTrafficLight.direction.angle()<180){
				if(closestTrafficLight.distance < 10){
					trafficLightInformation = closestTrafficLight.information;
					TrafficLight.State state = (TrafficLight.State)trafficLightInformation.get("State");
					if(state == null){
						System.out.println("State of traffic light not recognised");
						return false;
					}
					if(state == TrafficLight.State.Red || state == TrafficLight.State.Amber){
						return true;
					}
				}
			}return false;
		}
	}
	
}

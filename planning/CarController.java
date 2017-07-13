package group26.planning;

import com.unimelb.swen30006.partc.core.objects.Car;

/**
 * The class that handles input to the car to adjust acceleration and turning angle
 * @author Group 26 - Stewart Collins 326206, Raja Ramkumar 725564, Thomas Nielsen 357491
 *
 */
public class CarController {
	private Car car;
	private final int TURNSPEED = 20;
	
	/**
	 * Constructor for the car controller
	 * @param car The car that is being controlled
	 */
	public CarController(Car car){
		this.car = car;
	}
	
	/**
	 * Adjusts the car based on the given angle and acceleration inputs
	 * @param turnAngle The desired angle of the car
	 * @param accelerationValue Indicates whether acceleration is required, positive is accelerate, negative is brake
	 */
	public void adjustCar(int turnAngle, float accelerationValue){
		accelerate(accelerationValue);
		//System.out.println("Turn angle:" + turnAngle + " AccelerationValue:" + accelerationValue  + " currentAngle: " + car.getVelocity().angle());
		turn(turnAngle);
	}
	
	/**
	 * Applies acceleration or brake to the car
	 * @param accelerationValue Indicates whether acceleration is required, positive is accelerate, negative is brake
	 */
	private void accelerate(float accelerationValue){
		if(accelerationValue > 0){
			car.accelerate();
		}else if(accelerationValue < 0){
			car.brake();
		}
	}
	
	/**
	 * Adjusts the angle of the car to turn it to the given angle
	 * @param turnAngle The desired angle of the car
	 */
	private void turn(int turnAngle){
		//Check if car already at the desired angle
		
		if(car.getVelocity().angle() != turnAngle){
			//Get how much the car needs to turn
			float angleDifference = turnAngle - car.getVelocity().angle();
			//If angle is over 180 degrees then turn in the other direction
			if(angleDifference > 180){
				angleDifference -= 360;
			}else if(angleDifference < -180){
				angleDifference += 360;
			}//If the desired angle is greater than the max turn speed then turn the max turnspeed
			if(Math.abs(angleDifference) > TURNSPEED){
				if(angleDifference > 0){
					car.turn(TURNSPEED);
				}else{
					car.turn(-TURNSPEED);
				}
			}else{
				//Otherwise turn only the remaining difference
				car.turn(angleDifference);
			}
		}
	}
	
	
}

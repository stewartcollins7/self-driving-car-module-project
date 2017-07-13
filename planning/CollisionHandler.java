package group26.planning;


import com.unimelb.swen30006.partc.ai.interfaces.PerceptionResponse;


/**
 * This class stores the perception response and calculate if there is a collision imminent corresponds 
 * to current perception response
 * @author Group 26 - Stewart Collins 326206, Raja Ramkumar 725564, Thomas Nielsen 357491
 *
 */

public class CollisionHandler {
	
	private PerceptionResponse[] collisionObjectList;
	//private float decelerationRequired;
	
	
	/**
	 * Constructor for the CollisionHandler class, initializes the collisionObject list
	 */
	public CollisionHandler(){
		collisionObjectList = null;
	}
	
	/**
	 * Updates the collisionObject list with the current perception response
	 * @param current perception result
	 */
	public void update(PerceptionResponse[] result){
		collisionObjectList = result;
	}
	
	/**
	 * Checks whether there is a collision imminent in the current vicinity
	 * @return Returns true if collision is imminent, false if not
	 */
	public boolean collisionImminent()
	{
		if(collisionObjectList.length>0)
		{
			for(int i=0; i<collisionObjectList.length; i++)
			{
				if(collisionObjectList[i].timeToCollision<2 && collisionObjectList[i].timeToCollision>-1)
				{
					return true;
				}
			}
		}
		//decelerationRequired = 20;
		return false;
	}

}

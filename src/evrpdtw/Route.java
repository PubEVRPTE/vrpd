package evrpdtw;

import java.util.*;

public class Route{

	public double cost;
	public double v_distance;
	public double d_distance;
	public double time;
	
	public boolean depart;//whether depart;
	
	public ArrayList<Boolean> takeoff_land;
	public ArrayList<Integer> vehicleRoute;
	public ArrayList<ArrayList<Integer>> droneRoute;
	
	public Route() {
		takeoff_land = new ArrayList<Boolean>();
		vehicleRoute = new ArrayList<Integer>();
		droneRoute = new ArrayList<ArrayList<Integer>>();
	}
	
	public boolean check() {
		
		return true;
	}
	
	public String toString() {
		
		return "";
	}
}

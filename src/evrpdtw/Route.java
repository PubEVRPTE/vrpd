package evrpdtw;

import java.util.*;

public class Route{

	public double cost;
	public double weight;
	public double time;
	public boolean depart;//whether depart;
	
	public ArrayList<Integer> vehicleRoute;
	
	public HashMap<Integer, Integer> droneNext;
	public HashMap<Integer, Integer> dronePrev;
	
	public Route() {
		cost = 0;
		weight = 0;
		time = 0;
		depart = false;
		vehicleRoute = new ArrayList<Integer>();
		droneNext = new HashMap<Integer, Integer>();
		dronePrev = new HashMap<Integer, Integer>();
	}
	
	public Route(Route r) {
		cost = r.cost;
		weight = r.weight;
		time = r.time;
		depart = r.depart;
		vehicleRoute = new ArrayList<Integer>(r.vehicleRoute);
		droneNext = new HashMap<Integer, Integer>(r.droneNext);
		dronePrev = new HashMap<Integer, Integer>(r.dronePrev);
	}

	public void calculate_cost(Problem inst) {
		cost = 0;
		weight = 0;
		ArrayList<Double> cumulatedTime = new ArrayList<Double>(inst.c_n);
		int id = vehicleRoute.get(0);
		int prevId;
		for (int i = 1; i < vehicleRoute.size(); i++) {
			prevId = id;
			id = vehicleRoute.get(i);

			Integer drone = dronePrev.get(id); // 接收无人机
			double vehicleTime = 0;
			double operationTime = 0;
			if (drone != null) {
				Integer droneDepart = dronePrev.get(drone);
				double droneTime = (inst.distance[id][drone] + inst.distance[drone][droneDepart]) / inst.d_speed + inst.d_serviceTime + inst.l_t;
				cost += inst.d_cost * droneTime;
				operationTime = inst.r_t;
				vehicleTime = inst.l_t;
				cumulatedTime.set(id, cumulatedTime.get(droneDepart) + droneTime + operationTime);
				weight += inst.vec_poi.get(drone).pack_weight;
			}
			vehicleTime += inst.distance[prevId][id] / inst.v_speed + inst.v_serviceTime;
			cost += inst.v_cost * vehicleTime;
			cumulatedTime.set(id, Math.max(cumulatedTime.get(prevId) + vehicleTime + operationTime, cumulatedTime.get(id)));
			weight += inst.vec_poi.get(id).pack_weight;
		}
		time = cumulatedTime.get(0);
	}

	public String toString() {
		
		return " ";
	}
}

package evrpdtw;

import java.util.*;

public class Route{

	public Problem inst;
	
	public double cost;
	public double weight;
	public double time;
	public boolean depart;//whether depart;
	public boolean change;
	
	public ArrayList<Integer> vehicleRoute;
	
	public HashMap<Integer, Integer> droneNext;
	public HashMap<Integer, Integer> dronePrev;

	ArrayList<Double> cumulatedTime;
	ArrayList<Double> cumulatedWeight;
	
	public Route(Problem inst) {
		cost = 0;
		weight = 0;
		time = 0;
		depart = false;
		change = false;
		this.inst = inst;
		vehicleRoute = new ArrayList<Integer>();
		droneNext = new HashMap<Integer, Integer>();
		dronePrev = new HashMap<Integer, Integer>();
		cumulatedTime = new ArrayList<Double>(Collections.nCopies(inst.c_n + 1, 0.0));
		cumulatedWeight = new ArrayList<Double>(Collections.nCopies(inst.c_n + 1, 0.0));
	}
	
	public Route(Route r) {
		cost = r.cost;
		weight = r.weight;
		time = r.time;
		depart = r.depart;
		change = r.change;
		inst = r.inst;
		vehicleRoute = new ArrayList<Integer>(r.vehicleRoute);
		droneNext = new HashMap<Integer, Integer>(r.droneNext);
		dronePrev = new HashMap<Integer, Integer>(r.dronePrev);
		cumulatedTime = new ArrayList<Double>(Collections.nCopies(inst.c_n + 1, 0.0));
		cumulatedWeight = new ArrayList<Double>(Collections.nCopies(inst.c_n + 1, 0.0));
	}

	public void calculate_cost(Problem inst) {
		cost = 0;
		weight = 0;
		cumulatedTime = new ArrayList<Double>(Collections.nCopies(inst.c_n + 1, 0.0));
		cumulatedWeight = new ArrayList<Double>(Collections.nCopies(inst.c_n + 1, 0.0));
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
				cost += inst.d_cost * (inst.distance[id][drone] + inst.distance[drone][droneDepart]);
				operationTime = inst.r_t;
				vehicleTime = inst.l_t;
				cumulatedTime.set(id, cumulatedTime.get(droneDepart) + droneTime + operationTime);
				weight += inst.vec_poi.get(drone).pack_weight;
			}
			vehicleTime += inst.distance[prevId][id] / inst.v_speed;
			if (i < vehicleRoute.size() - 1) {
				vehicleTime += inst.v_serviceTime;
			}
			cost += inst.v_cost * inst.distance[prevId][id];
			cumulatedTime.set(id, Math.max(cumulatedTime.get(prevId) + vehicleTime + operationTime, cumulatedTime.get(id)));
			weight += inst.vec_poi.get(id).pack_weight;
			cumulatedWeight.set(id, weight);
		}
		time = cumulatedTime.get(0);
		change = false;
	}
	
	public void removeElement(int c) {
		for (int i = 0; i < vehicleRoute.size(); i++) {
			if (vehicleRoute.get(i) == c) {
				vehicleRoute.remove(i);
				break;
			}
		}
		calculate_cost(inst);
	}
	
	public void insertDrone(Sortie q) {
		int l_id = q.launch_position;
		int r_id = q.recovery_position;
		int d_id = q.delivery_position;
		droneNext.put(l_id, d_id);
		droneNext.put(d_id, r_id);
		dronePrev.put(r_id, d_id);
		dronePrev.put(d_id, l_id);
		calculate_cost(inst);
	}
	
	public void insertTruck(int index, int c) {
		
	}
	
	public String toString() {
		
		return " ";
	}
}

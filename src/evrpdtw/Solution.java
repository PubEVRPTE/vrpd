package evrpdtw;

import java.util.*;

public class Solution {

	public Problem inst;
	public double t_weight;
	public double time;
	public double t_cost;
	public ArrayList<Route> route_list;
	public ArrayList<Integer> belongTo; // 存储每个点分别属于哪条路径, 避免逐路径寻找点; 在Problem中手动维护
	
	public Solution(Problem inst) {
		this.inst = inst;
		t_weight = 0;
		time = 0;
		t_cost = 0;
		route_list = new ArrayList<Route>();
		belongTo = new ArrayList<Integer>();
		for (int i = 0; i < inst.c_n + 1; i++) {
			belongTo.add(-1);
		}
	}
	
	public Solution(Solution obj) {
		this.inst = obj.inst;
		route_list = new ArrayList<Route>();
		for (int i = 0; i < obj.route_list.size(); i++) {
			route_list.add(new Route(obj.route_list.get(i)));
		}
		belongTo = new ArrayList<Integer>(obj.belongTo);
		t_weight = obj.t_weight;
		time = obj.time;
		t_cost = obj.t_cost;
	}
	
	public void calculate_cost(Problem inst) {
		t_cost = 0;
		time = 0;
		for (Route route: route_list) {
			route.cost = 0;
			route.weight = 0;
			ArrayList<Double> cumulatedTime = new ArrayList<Double>(inst.c_n);
			ArrayList<Integer> vehicleRoute = route.vehicleRoute;
			int id = vehicleRoute.get(0);
			int prevId;
			for (int i = 1; i < vehicleRoute.size(); i++) {
				prevId = id;
				id = vehicleRoute.get(i);

				Integer drone = route.dronePrev.get(id); // 接收无人机
				double vehicleTime = 0;
				double operationTime = 0;
				if (drone != null) {
					Integer droneDepart = route.dronePrev.get(drone);
					double droneTime = (inst.distance[id][drone] + inst.distance[drone][droneDepart]) / inst.d_speed + inst.d_serviceTime + inst.l_t;
					route.cost += inst.d_cost * droneTime;
					operationTime = inst.r_t;
					vehicleTime = inst.l_t;
					cumulatedTime.set(id, cumulatedTime.get(droneDepart) + droneTime + operationTime);
					route.weight += inst.vec_poi.get(drone).pack_weight;
				}
				vehicleTime += inst.distance[prevId][id] / inst.v_speed + inst.v_serviceTime;
				route.cost += inst.v_cost * vehicleTime;
				cumulatedTime.set(id, Math.max(cumulatedTime.get(prevId) + vehicleTime + operationTime, cumulatedTime.get(id)));
				route.weight += inst.vec_poi.get(id).pack_weight;
			}
			route.time = cumulatedTime.get(0);
			t_cost += route.cost;
			t_weight += route.weight;
			time = Math.max(time, route.time);
		}
	}
	
	// 检查解是否有效
	public void check(Problem inst) {
		calculate_cost(inst);
		ArrayList<Boolean> visited = new ArrayList<Boolean>(inst.c_n);
		for (Route route: route_list) {
			double totalWeight = 0;
			ArrayList<Integer> vehicleRoute = route.vehicleRoute;
			boolean droneAvailable = true;
			for (int i = 0; i < vehicleRoute.size(); i++) {
				// 无人机是否合法
				Integer id = vehicleRoute.get(i);
				if (route.dronePrev.get(id) != null) { // 接收无人机
					if (droneAvailable == true) {
						throw new RuntimeException("Invalid solution: Duplicate drone landing.");
					}
					droneAvailable = true;
				}
				// 发送无人机
				Integer drone = route.droneNext.get(id);
				if (drone != null) {
					if (droneAvailable == false) {
						throw new RuntimeException("Invalid solution: Drone departs while there's actually no drone available.");
					}
					Integer droneLanding = route.droneNext.get(drone);
					if (droneLanding == null) {
						throw new RuntimeException("Invalid solution: Drone route broken - it never lands.");
					} else if ((inst.distance[i][drone] + inst.distance[drone][droneLanding]) / inst.d_speed + inst.d_serviceTime + inst.l_t > inst.d_time) {
						throw new RuntimeException("Invalid solution: Drone departs for infeasible place.");
					}
					if (route.dronePrev.get(drone) != id || route.dronePrev.get(droneLanding) != drone) {
						throw new RuntimeException("Invalid solution: Drone route does not consist.");
					}
					double weight = inst.vec_poi.get(drone).pack_weight;
					if (weight > inst.d_weight) {
						throw new RuntimeException("Invalid solution: Drone overweight.");
					}
					totalWeight += weight;
					droneAvailable = false;
					visited.set(drone, true);
				}
				totalWeight += inst.vec_poi.get(id).pack_weight;
				visited.set(id, true);
			}
			if (droneAvailable == false) {
				throw new RuntimeException("Invalid solution: Vehicle returns without drone.");
			}
			if (totalWeight > inst.v_weight_drone) {
				throw new RuntimeException("Invalid solution: Vehicle overweight");
			}
		}
		for (boolean e : visited) {
			if (e == false) {
				throw new RuntimeException("Invalid solution: Customer(s) not visited.");
			}
		}
		for (Route route: route_list) {
			if (route.time > inst.v_time) {
				throw new RuntimeException("Invalid solution: Vehicle following a too-far route.");
			}
		}
	}
	
	public String toString() {
		
		return "";
	}
	
}

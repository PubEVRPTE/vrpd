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
		// TODO: t_weight, launch_time, launch_cost & recovery_time
		t_cost = 0;
		time = 0;
		for (ArrayList<Integer> route: vehicleRoute) {
			ArrayList<Double> cumulatedTime = new ArrayList<Double>(inst.c_n);

			int id = route.get(0);
			int prevId;
			for (int i = 1; i < route.size(); i++) {
				prevId = id;
				id = route.get(i);

				Integer drone = dronePrev.get(id); // 接收无人机
				if (drone != null) {
					Integer droneDepart = dronePrev.get(drone);
					double droneTime = (inst.distance[id][drone] + inst.distance[drone][droneDepart]) / inst.d_speed;
					t_cost += inst.d_cost * droneTime;
					cumulatedTime.set(id, cumulatedTime.get(droneDepart) + droneTime);
				}
				double vehicleTime = inst.distance[prevId][id] / inst.v_speed;
				t_cost += inst.v_cost * vehicleTime;
				cumulatedTime.set(id, Math.max(cumulatedTime.get(prevId) + vehicleTime, cumulatedTime.get(id)));
			}

			time = Math.max(time, cumulatedTime.get(0));
		}
	}
	
	// 检查解是否有效
	public void check(Problem inst) {
		ArrayList<Boolean> visited = new ArrayList<Boolean>(inst.c_n);
		for (ArrayList<Integer> route: vehicleRoute) {
			boolean droneAvailable = true;
			double vehicleDistance = 0;
			for (int i = 0; i < route.size(); i++) {
				// 无人机是否合法
				Integer id = route.get(i);
				if (dronePrev.get(id) != null) { // 接收无人机
					if (droneAvailable == true) {
						throw new RuntimeException("Invalid solution: Duplicate drone landing.");
					}
					droneAvailable = true;
				}
				// 发送无人机
				Integer drone = droneNext.get(id);
				if (drone != null) {
					if (droneAvailable == false) {
						throw new RuntimeException("Invalid solution: Drone departs while there's actually no drone available.");
					}
					Integer droneLanding = droneNext.get(drone);
					if (droneLanding == null) {
						throw new RuntimeException("Invalid solution: Drone route broken - it never lands.");
					} else if (inst.distance[i][drone] + inst.distance[drone][droneLanding] > inst.d_maxDistance) {
						throw new RuntimeException("Invalid solution: Drone departs for infeasible place.");
					}
					if (dronePrev.get(drone) != id || dronePrev.get(droneLanding) != drone) {
						throw new RuntimeException("Invalid solution: Drone route does not consist.");
					}
					droneAvailable = false;
					visited.set(drone, true);
				}
				if (i > 0) {
					vehicleDistance += inst.distance[route.get(i-1)][i];
				}
				visited.set(id, true);
			}
			if (droneAvailable == false) {
				throw new RuntimeException("Invalid solution: Vehicle returns without drone.");
			}
			if (vehicleDistance > inst.v_maxDistance) {
				throw new RuntimeException("Invalid solution: Vehicle following a too-far route.");
			}
		}
		for (boolean e : visited) {
			if (e == false) {
				throw new RuntimeException("Invalid solution: Customers not visited.");
			}
		}
	}
	
	public String toString() {
		
		return "";
	}
	
}

package evrpdtw;

import java.util.ArrayList;
import java.util.Random;

class Sortie {

	public int launch_position; // launch_index in route_index
	public int delivery_position; // drone delivery customer id
	public int recovery_position; // recovery_index in route_index
	
	public Sortie() {
		
	}
	
	public Sortie(int i, int j, int k) {
		launch_position = i;
		delivery_position = j;
		recovery_position = k;
	}
	
}

public class Heuristics {

	public Problem inst;
	
	// solve方法中的参数
	public static final double timeLimit = 300;
	public static final double maxTemperatureRatio = 1e-10;
	public static final int maxNoImpv = 50;

	public long timeStart;
	
	public ArrayList<Integer> vec_poi_id;//not cover depot
	public Neighborhood neighborhood;
	public Solution sol;
	public Solution bestSolution;
	
	public static final Random random = new Random();
	
	public Heuristics(Problem inst) {
		this.inst = inst;
		vec_poi_id = new ArrayList<Integer>();
		for (poi p:inst.vec_poi) {
			if (p.type == 2) {
				vec_poi_id.add(p.id);
			}
		}
		neighborhood = new Neighborhood(inst);
		sol = new Solution(inst);
	}
	
	public void initial() {
		nearest_neighbor();
		drone_addition();
	}
	
	public void solve() {
		// 模拟退火
		initial();
		sol.check(inst);
		bestSolution = sol;
		System.out.println("Initial: " + bestSolution.t_cost);
		timerOn();
		int iter = 1;
		int noImpv = 0;

		double maxTemperature = maxTemperatureRatio * sol.t_cost;

		while (timeout() == false) {

			Solution newSolution = new Solution(sol);
			ArrayList<Integer> to_insert = neighborhood.destroy(newSolution);
			
			neighborhood.repair(newSolution, to_insert);
			neighborhood.localSearch(newSolution);
			newSolution.check(inst);

			double temperature = maxTemperature * (time() / timeLimit);
			
			if (random.nextDouble() < Math.exp((sol.t_cost - newSolution.t_cost) / temperature)) {
				sol = newSolution;
			}

			if (sol.t_cost < bestSolution.t_cost) {
				bestSolution = new Solution(sol); // 深拷贝
				System.out.println("Iter " + iter + ": " + bestSolution.t_cost + ", time: " + time());
				noImpv = 0;
			} else {
				noImpv++;
				if (noImpv > maxNoImpv) {
					sol = new Solution(bestSolution);
					noImpv = 0;
				}
			}

			iter++;
		}
		System.out.println("Best: " + bestSolution.t_cost + ", time: " + time());
	}
	
	public void nearest_neighbor() {
		// int route_index = 0;
		// while (!vec_poi_id.isEmpty()) {
		// 	Route r = new Route(inst);
		// 	r.vehicleRoute.add(inst.vec_poi.get(0).id);
		// 	int lastId = inst.vec_poi.get(0).id;
		// 	while (!vec_poi_id.isEmpty()) {
		// 		double mindist = Double.MAX_VALUE;
		// 		int mini = -1;
		// 		int minid = -1;
		// 		double add_weight = 0;
		// 		double add_time = 0;
		// 		for (int i = 0; i < vec_poi_id.size(); i++) {
		// 			int id = vec_poi_id.get(i);
		// 			if (inst.distance[lastId][id] < mindist) {
		// 				add_weight = inst.vec_poi.get(id).pack_weight;
		// 				add_time = inst.vec_poi.get(id).v_serviceTime
		// 						+ inst.distance[lastId][id] / inst.v_speed;
		// 				if (r.weight + add_weight <= inst.v_weight_drone && r.time + add_time <= inst.v_time) {
		// 					mindist = inst.distance[lastId][id];
		// 					minid = id;
		// 					mini = i;
		// 				}
		// 			}
		// 		}
				
		// 		if (minid != -1 && r.weight + add_weight <= inst.v_weight_drone && r.time + add_time + inst.distance[minid][inst.vec_poi.get(0).id] / inst.v_speed <= inst.v_time) {
		// 			r.vehicleRoute.add(minid);
		// 			r.weight += add_weight;
		// 			r.time += add_time;
		// 			r.cost += mindist * inst.v_cost;
		// 			vec_poi_id.remove(mini);
		// 			sol.belongTo.set(minid, route_index);
		// 			lastId = minid;
		// 		}
		// 		else {
		// 			break;
		// 		}

		// 	}
			
		// 	double dist = inst.distance[r.vehicleRoute.get(r.vehicleRoute.size() - 1)][inst.vec_poi.get(0).id];
		// 	r.vehicleRoute.add(inst.vec_poi.get(0).id);
		// 	r.time += dist / inst.v_speed;
		// 	r.cost += dist * inst.v_cost;

		// 	sol.route_list.add(r);
		// 	sol.time += r.time;
		// 	sol.t_cost += r.cost;
		// 	sol.t_weight += r.weight;
		// 	route_index++;
		// }

		//greedy
		
		int route_index = 0;
		while (!vec_poi_id.isEmpty()) {
			Route r = new Route(inst);
			r.vehicleRoute.add(inst.vec_poi.get(0).id);
			r.vehicleRoute.add(inst.vec_poi.get(0).id);
			while (!vec_poi_id.isEmpty()) {
				int nearnext_id = vec_poi_id.get(0);
				int nearnext_index = 0;
				int insert_index = 0;
				double min_add_distance = Integer.MAX_VALUE;
				for (int i = 0; i < vec_poi_id.size(); i++) {
					int tmp_id = vec_poi_id.get(i);
					for (int j = 0; j < r.vehicleRoute.size()-1; j++) {
						double add_distance = inst.distance[r.vehicleRoute.get(j)][tmp_id] 
											+ inst.distance[tmp_id][r.vehicleRoute.get(j+1)]
											- inst.distance[r.vehicleRoute.get(j)][r.vehicleRoute.get(j+1)];
						if (add_distance < min_add_distance) {
							nearnext_id = tmp_id;
							nearnext_index = i;
							insert_index = j;
							min_add_distance = add_distance;
						}
					}
				}
				double add_weight = inst.vec_poi.get(nearnext_id).pack_weight;
				double add_time = inst.vec_poi.get(nearnext_id).v_serviceTime
								+ min_add_distance/inst.v_speed;
				if (r.weight + add_weight <= inst.v_weight_drone && r.time + add_time <= inst.v_time) {
					r.vehicleRoute.add(insert_index+1, nearnext_id);
					r.weight += add_weight;
					r.time += add_time;
					r.cost += min_add_distance * inst.v_cost;
					vec_poi_id.remove(nearnext_index);
					sol.belongTo.set(nearnext_id, route_index);
				}
				else {
					break;
				}
				
			}
			
			sol.route_list.add(r);
			sol.time += r.time;
			sol.t_cost += r.cost;
			sol.t_weight += r.weight;
			route_index++;
		}
		
		
	}
	
	public void drone_addition() {
		while (true) {
			boolean improved = false;
			for (Integer c: inst.vec_droneable_poi_id) {
				//remove
				Solution sol_x = new Solution(sol);
				Route to_lookfor = sol_x.route_list.get(sol_x.belongTo.get(c));
				if (to_lookfor.droneNext.get(c) == null && to_lookfor.dronePrev.get(c) == null) {
					to_lookfor.removeElement(c);
					sol_x.belongTo.set(c, -1);
					//find
					Sortie bestSortie = neighborhood.FindSortie(c, sol_x, sol.t_cost);
					if (bestSortie != null) {
						//update
						System.out.println("Find sortie: " + bestSortie.launch_position + ", " + bestSortie.delivery_position + ", " + bestSortie.recovery_position);
						int routeId = sol_x.belongTo.get(bestSortie.launch_position);
						sol_x.route_list.get(routeId).insertDrone(bestSortie);
						sol_x.belongTo.set(c, routeId);
						sol = sol_x;
						improved = true;
					}
				}
			}
			if (improved == false) {
				break;
			}
		}
	}
	
	public void timerOn() {
		timeStart = System.nanoTime();
	}

	public double time() {
		return (double)(System.nanoTime() - timeStart) / 1e9;
	}

	public boolean timeout() {
		return time() > timeLimit;
	}
}

package evrpdtw;

import java.util.ArrayList;
import java.util.Random;

class Sortie {

	public int launch_position;//launch_index in route_index
	public int delivery_position;//drone delivery customer id
	public int recovery_position;//recovery_index in route_index
	
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
	public static final double maxTemperature = 3000;
	public static final int maxNoImpv = 10;
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
		
		timerOn();
		int iter = 0;
		int noImpv = 0;

		while (timeout() == false) {
			Solution newSolution = new Solution(sol);
			ArrayList<Integer> to_insert = neighborhood.destroy(newSolution);
			neighborhood.repair(newSolution, to_insert);

			double temperature = maxTemperature * (time() / timeLimit);
			if (random.nextDouble() < Math.exp((sol.t_cost - newSolution.t_cost) / temperature)) {
				sol = newSolution;
			}

			if (sol.t_cost < bestSolution.t_cost) {
				bestSolution = new Solution(sol); // 深拷贝
				System.out.println("Iter " + iter + ": " + bestSolution.t_cost);
				noImpv = 0;
			} else {
				noImpv++;
				if (noImpv > maxNoImpv) {
					sol = new Solution(bestSolution);
					noImpv = 0;
				}
			}

			// TODO: 自适应(写完四个repair再说)

			iter++;
		}
	}
	
	public void nearest_neighbor() {
		//greedy
		
		int route_index = 0;
		while (!vec_poi_id.isEmpty()) {
			Route r = new Route();
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
					r.d_distance +=min_add_distance;
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
		for (Integer c:inst.vec_droneable_poi_id) {
			//remove
			Solution sol_x = new Solution(sol);
			ArrayList<Integer> to_lookfor = sol_x.route_list.get(sol_x.belongTo.get(c)).vehicleRoute;
			for (int i = 0; i < to_lookfor.size(); i++) {
				if (to_lookfor.get(i) == c) {
					to_lookfor.remove(i);
					//update sol_x !!!
					break;
				}
			}
			//find
			Sortie bestSortie = FindSortie(c, sol_x, Integer.MAX_VALUE);
			if (bestSortie != null) {
				//update
				sol_add_drone(sol_x, bestSortie);
				sol = sol_x;
			}
			
		}
	}
	
	public Sortie FindSortie(int c, Solution sol_x, double thresholdCost) {
		Sortie BestSortie = null;
		for (int r_index = 0; r_index < sol_x.route_list.size(); r_index++) {
			Route r = sol_x.route_list.get(r_index);
			if (r.weight + inst.vec_poi.get(c).pack_weight < inst.v_weight_drone) {
				int r_n = r.vehicleRoute.size();
				for (int i = 0; i < r_n-1; i++ ) {
					for (int k = i+1; k < r_n; k++) {
						if (r.droneNext.get(i) == null && r.dronePrev.get(k) == null) {
							int launch_id = r.vehicleRoute.get(i);
							int recovery_id = r.vehicleRoute.get(k);
							Sortie p = new Sortie(launch_id, c, recovery_id);
							double drone_distance = inst.distance[launch_id][c] + inst.distance[c][recovery_id];
							double vehicle_time = inst.distance[launch_id][recovery_id]/inst.v_speed;
							double drone_time = inst.d_serviceTime + drone_distance/inst.d_speed;
							if (r.time + inst.l_t + inst.r_t + Math.max(vehicle_time, drone_time) - vehicle_time < inst.v_time && r.cost + drone_distance*inst.d_cost < thresholdCost) {
								BestSortie = p;
								thresholdCost = r.cost + drone_distance*inst.d_cost;
							}
						}
						
						
					}
				}
			}
		}
		return BestSortie;
	}
	
	public void sol_add_drone(Solution sol_x, Sortie q) {
		int l_id = q.launch_position;
		int r_id = q.recovery_position;
		int d_id = q.delivery_position;
		sol_x.route_list.get(sol_x.belongTo.get(l_id)).droneNext.put(l_id, d_id);
		sol_x.route_list.get(sol_x.belongTo.get(l_id)).droneNext.put(d_id, r_id);
		sol_x.route_list.get(sol_x.belongTo.get(l_id)).dronePrev.put(r_id, d_id);
		sol_x.route_list.get(sol_x.belongTo.get(l_id)).dronePrev.put(d_id, l_id);
		sol_x.t_weight += inst.vec_poi.get(d_id).pack_weight;
		double drone_distance = inst.distance[l_id][d_id] + inst.distance[d_id][r_id];
		double vehicle_time = inst.distance[l_id][r_id]/inst.v_speed;
		double drone_time = inst.d_serviceTime + drone_distance/inst.d_speed;
		sol_x.time += inst.l_t + inst.r_t + Math.max(drone_time, vehicle_time) - vehicle_time;
		sol_x.t_cost += drone_distance*inst.d_cost;
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

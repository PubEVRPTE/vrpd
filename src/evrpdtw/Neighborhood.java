package evrpdtw;

import java.util.*;

public class Neighborhood {

	// destroy方法中确定beta的参数
	public static final int c_low_lb = 1;
	public static final int c_low_ub = 3;
	public static final int c_high = 40;
	public static final double delta = 0.15;
	public static final Random random = new Random();

	public static final double overflow_cent = 0.1;

	public Problem inst;

	public Neighborhood(Problem inst) {
		this.inst = inst;
	}

	public ArrayList<Integer> destroy(Solution sol) {
		if (random.nextBoolean()) {
			return destroy1(sol, getBeta());
		} else {
			return destroy2(sol, getBeta());
		}
	}

	public void repair(Solution sol, ArrayList<Integer> to_insert) {
		sol.calculate_cost(inst); // 效率低，待改进
		double r = random.nextDouble();
		if (r < 0.25) {
			repair1(sol, to_insert);
		} else if (r < 0.5) {
			repair2(sol, to_insert);
		} else if (r < 0.75) {
			repair3(sol, to_insert);
		} else {
			repair4(sol, to_insert);
		}
		
		sol.check(inst); // 调试检查正确性用，没问题就注释掉
	}

	public int getBeta() {
		return Math.min(Math.max(random.nextInt(c_low_ub) + c_low_lb, (int) Math.round(delta * inst.c_n)), Math.min(inst.c_n - 1, c_high));
	}

	public ArrayList<Integer> destroy1(Solution sol, int beta) {
		ArrayList<Integer> remove_list = new ArrayList<Integer>();
		ArrayList<Boolean> removed = new ArrayList<Boolean>(Collections.nCopies(inst.c_n + 1, false));
		while (beta > 0) {
			Integer id = random.nextInt(inst.c_n) + 1;
			while (removed.get(id)) {
				id = random.nextInt(inst.c_n) + 1;
			}

			int routeId = sol.belongTo.get(id);
			Route route = sol.route_list.get(routeId);

			for (int i = 0; i < route.vehicleRoute.size(); i++) {
				int current = route.vehicleRoute.get(i);
				boolean found = false;
				if (current == id) { // case 1: 由卡车配送
					// 删掉接收的无人机
					Integer drone = route.dronePrev.get(id);
					if (drone != null) {
						int droneDepart = route.dronePrev.get(drone);
						route.droneNext.remove(droneDepart);
						route.droneNext.remove(drone);
						route.dronePrev.remove(drone);
						route.dronePrev.remove(id);
						sol.belongTo.set(drone, -1);
						remove_list.add(drone);
						removed.set(drone, true);
						beta--;
					}
					// 删掉发送的无人机
					drone = route.droneNext.get(id);
					if (drone != null) {
						int droneLanding = route.droneNext.get(drone);
						route.droneNext.remove(id);
						route.droneNext.remove(drone);
						route.dronePrev.remove(drone);
						route.dronePrev.remove(droneLanding);
						sol.belongTo.set(drone, -1);
						remove_list.add(drone);
						removed.set(drone, true);
						beta--;
					}

					route.vehicleRoute.remove(i);
					sol.belongTo.set(id, -1);
					remove_list.add(id);
					removed.set(id, true);
					beta--;
					found = true;
				} else if (id.equals(route.droneNext.get(current))) { // case 2: 由无人机配送
					int droneLanding = route.droneNext.get(id);
					route.droneNext.remove(current);
					route.droneNext.remove(id);
					route.dronePrev.remove(id);
					route.dronePrev.remove(droneLanding);
					sol.belongTo.set(id, -1);
					remove_list.add(id);
					removed.set(id, true);
					beta--;
					found = true;
				}
				if (found) {
					break;
				}
			}
		}
		return remove_list;
	}

	public ArrayList<Integer> destroy2(Solution sol, int beta) {
		ArrayList<Integer> remove_list = new ArrayList<Integer>();
		ArrayList<Boolean> removed = new ArrayList<Boolean>(Collections.nCopies(inst.c_n + 1, false));

		Integer id = random.nextInt(inst.c_n) + 1;
		while (beta > 0) {
			int routeId = sol.belongTo.get(id);
			Route route = sol.route_list.get(routeId);

			for (int i = 0; i < route.vehicleRoute.size(); i++) {
				int current = route.vehicleRoute.get(i);
				boolean found = false;
				if (current == id) { // case 1: 由卡车配送
					// 删掉接收的无人机
					Integer drone = route.dronePrev.get(id);
					if (drone != null) {
						int droneDepart = route.dronePrev.get(drone);
						route.droneNext.remove(droneDepart);
						route.droneNext.remove(drone);
						route.dronePrev.remove(drone);
						route.dronePrev.remove(id);
						sol.belongTo.set(drone, -1);
						remove_list.add(drone);
						removed.set(drone, true);
						beta--;
					}
					// 删掉发送的无人机
					drone = route.droneNext.get(id);
					if (drone != null) {
						int droneLanding = route.droneNext.get(drone);
						route.droneNext.remove(id);
						route.droneNext.remove(drone);
						route.dronePrev.remove(drone);
						route.dronePrev.remove(droneLanding);
						sol.belongTo.set(drone, -1);
						remove_list.add(drone);
						removed.set(drone, true);
						beta--;
					}

					route.vehicleRoute.remove(i);
					sol.belongTo.set(id, -1);
					remove_list.add(id);
					removed.set(id, true);
					beta--;
					found = true;
				} else if (id.equals(route.droneNext.get(current))) { // case 2: 由无人机配送
					int droneLanding = route.droneNext.get(id);
					route.droneNext.remove(current);
					route.droneNext.remove(id);
					route.dronePrev.remove(id);
					route.dronePrev.remove(droneLanding);
					sol.belongTo.set(id, -1);
					remove_list.add(id);
					removed.set(id, true);
					beta--;
					found = true;
				}
				if (found) {
					break;
				}
			}
			// 找最近的两个点暂时直接搜索dist
			// 可以事先按dist排个序，降低搜索时间
			double mindist1 = Double.MAX_VALUE, mindist2 = Double.MAX_VALUE;
			int minidx1 = -1, minidx2 = -1;
			for (int i = 1; i <= inst.c_n; i++) {
				if (removed.get(i)) {
					continue;
				}
				if (inst.distance[id][i] < mindist1) {
					mindist2 = mindist1;
					minidx2 = minidx1;
					mindist1 = inst.distance[id][i];
					minidx1 = i;
				} else if (inst.distance[id][i] < mindist2) {
					mindist2 = inst.distance[id][i];
					minidx2 = i;
				}
			}
			if (random.nextBoolean()) {
				minidx1 = minidx2;
			}
			id = minidx1;
		}
		return remove_list;
	}

	public void repair1(Solution sol, ArrayList<Integer> to_insert) {
		// 随机插入卡车
		while (to_insert.size() > 0) {
			int c = to_insert.remove(random.nextInt(to_insert.size()));
			TruckBestInsertion(c, sol);
		}
		sol.calculate_cost(inst);

		// 部分改为无人机
		ArrayList<poi> copyCus = new ArrayList<poi>(inst.vec_poi);
		while (copyCus.size() > 1) {
			Solution newSolution = new Solution(sol);
			int id = random.nextInt(copyCus.size()-1) + 1;
			poi current = copyCus.remove(id);
			id = current.id;
			Route route = newSolution.route_list.get(newSolution.belongTo.get(id));
			if (current.pack_weight < inst.d_weight && route.droneNext.get(id) == null && route.dronePrev.get(id) == null) {
				double threshold = newSolution.t_cost;
				route.removeElement(id);
				newSolution.belongTo.set(id, -1);
				Sortie bestSortie = FindSortie(id, newSolution, threshold);
				if (bestSortie != null) {
					int routeId = newSolution.belongTo.get(bestSortie.launch_position);
					newSolution.route_list.get(routeId).insertDrone(bestSortie);
					newSolution.belongTo.set(id, routeId);
					sol = newSolution;
				}
			}
		}
	}

	public void repair2(Solution sol, ArrayList<Integer> to_insert) {
		// 随机插入卡车
		while (to_insert.size() > 0) {
			int c = to_insert.remove(random.nextInt(to_insert.size()));
			TruckRandomInsertion(c, sol);
		}
		sol.calculate_cost(inst);

		// 部分改为无人机
		ArrayList<poi> copyCus = new ArrayList<poi>(inst.vec_poi);
		while (copyCus.size() > 1) {
			Solution newSolution = new Solution(sol);
			int id = random.nextInt(copyCus.size()-1) + 1;
			poi current = copyCus.remove(id);
			id = current.id;
			Route route = newSolution.route_list.get(newSolution.belongTo.get(id));
			if (current.pack_weight < inst.d_weight && route.droneNext.get(id) == null && route.dronePrev.get(id) == null) {
				double threshold = newSolution.t_cost;
				route.removeElement(id);
				newSolution.belongTo.set(id, -1);
				Sortie bestSortie = FindSortie_w(id, newSolution, threshold);
				if (bestSortie != null) {
					int routeId = newSolution.belongTo.get(bestSortie.launch_position);
					newSolution.route_list.get(routeId).insertDrone(bestSortie);
					newSolution.belongTo.set(id, routeId);
					sol = newSolution;
				}
			}
		}
	}

	public void repair3(Solution sol, ArrayList<Integer> to_insert) {
		ArrayList<Boolean> notInserted = new ArrayList<Boolean>(Collections.nCopies(inst.c_n + 1, false));
		for (int e : to_insert) {
			notInserted.set(e, true);
		}
		ArrayList<Integer> uninserted = new ArrayList<Integer>(); // 不能成功插入的点
		while (to_insert.size() > 0) {
			int c = to_insert.remove(random.nextInt(to_insert.size()));

			// 找到当前解离它最近的点
			int minidx = -1;
			double mindist = Double.MAX_VALUE;
			for (int id = 1; id <= inst.c_n; id++) {
				if (notInserted.get(id)) {
					continue;
				}
				if (inst.distance[c][id] < mindist) {
					mindist = inst.distance[c][id];
					minidx = id;
				}
			}

			// 尝试插入
			int routeId = sol.belongTo.get(minidx);
			if (AttemptBestInsertion(c, sol.route_list.get(routeId))) {
				notInserted.set(c, true);
				sol.belongTo.set(c, routeId);
			} else {
				uninserted.add(c);
			}
		}
		if (uninserted.size() > 0) {
			repair1(sol, uninserted);
		}
	}

	public void repair4(Solution sol, ArrayList<Integer> to_insert) {
		ArrayList<Integer> truck_insert = new ArrayList<Integer>();
		// 去除超重的
		for (int i = to_insert.size() - 1; i >= 0 ; i--) {
			int id = to_insert.get(i);
			if (inst.vec_poi.get(id).pack_weight > inst.d_weight) {
				truck_insert.add(id);
				to_insert.remove(i);
			}
		}

		// 随机插入卡车
		while (truck_insert.size() > 0) {
			int c = truck_insert.remove(random.nextInt(truck_insert.size()));
			TruckBestInsertion(c, sol);
		}
		sol.calculate_cost(inst);

		if (to_insert.size() > 0) {
			repair3(sol, to_insert);
		}
	}

	public void TruckBestInsertion(int c, Solution s) {
		double add_cost = Double.MAX_VALUE;
		Route r_toInsert = null;
		int route_index = -1;
		for (int i = 0; i < s.route_list.size(); i++) {
			Route r = s.route_list.get(i);
			if (r.weight + inst.vec_poi.get(c).pack_weight <= inst.v_weight_drone) {
				for (int j = 0; j < r.vehicleRoute.size() - 1; j++) {
					Route r_t = new Route(r);
					r_t.vehicleRoute.add(j + 1, c);
					r_t.calculate_cost(inst);
					if (r_t.time <= inst.v_time && r_t.cost - r.cost < add_cost) {
						route_index = i;
						add_cost = r_t.cost - r.cost;
						r_toInsert = r_t;
					}
				}
			}
		}
		if (route_index == -1) {
			r_toInsert = new Route(inst);
			r_toInsert.vehicleRoute.add(0);
			r_toInsert.vehicleRoute.add(c);
			r_toInsert.vehicleRoute.add(0);
			s.route_list.add(r_toInsert);
			s.belongTo.set(c, s.route_list.size()-1);
		} else {
			s.route_list.set(route_index, r_toInsert);
			s.belongTo.set(c, route_index);
		}
	}

	public Sortie FindSortie(int c, Solution s, double thresholdCost) {
		Sortie BestSortie = null;
		for (int r_index = 0; r_index < s.route_list.size(); r_index++) {
			Route r = s.route_list.get(r_index);
			if (r.weight + inst.vec_poi.get(c).pack_weight < inst.v_weight_drone) {
				int r_n = r.vehicleRoute.size();
				boolean depart = false;
				for (int i = 0; i < r_n - 1; i++) {
					int launch_id = r.vehicleRoute.get(i);
					if (i > 0 && r.dronePrev.get(launch_id) != null) {
						depart = false;
					}
					if (r.droneNext.get(launch_id) != null) {
						depart = true;
					}
					if (!depart) {
						// 从这个点开始到下一次起飞中间所有的点都可以作为降落点
						for (int k = i + 1; k < r_n; k++) {
							int recovery_id = r.vehicleRoute.get(k);
							if (r.droneNext.get(recovery_id) != null) {
								break;
							}
							// 检查时间合法
							if ((inst.distance[launch_id][c] + inst.distance[c][recovery_id]) / inst.d_speed + inst.d_serviceTime
									+ inst.l_t <= inst.d_time) {
								Route newRoute = new Route(r);
								newRoute.droneNext.put(launch_id, c);
								newRoute.droneNext.put(c, recovery_id);
								newRoute.dronePrev.put(c, launch_id);
								newRoute.dronePrev.put(recovery_id, c);
								newRoute.calculate_cost(inst);
								if (newRoute.time < inst.v_time && s.t_cost + (inst.distance[launch_id][c] + inst.distance[c][recovery_id]) * inst.d_cost < thresholdCost) {
									BestSortie = new Sortie(launch_id, c, recovery_id);
									thresholdCost = s.t_cost + (inst.distance[launch_id][c] + inst.distance[c][recovery_id]) * inst.d_cost;
								}
							}
						}
					}
					
				}
			}
		}
		return BestSortie;
	}

	public void TruckRandomInsertion(int c, Solution s) {
		final double near_distance = 5;
		ArrayList<Integer> c_toInsert = new ArrayList<Integer>();
		ArrayList<Integer> c_toInsertIndex = new ArrayList<Integer>();
		for (int i = 0; i < s.route_list.size(); i++) {
			Route route = s.route_list.get(i);
			for (int j = 0; j < route.vehicleRoute.size() - 1; j++) {
				int id = route.vehicleRoute.get(j);
				int nextId = route.vehicleRoute.get(j + 1);
				if (inst.distance[c][id] < near_distance && inst.distance[c][nextId] < near_distance
					&& inst.vec_poi.get(id).pack_weight + route.weight <= inst.v_weight_drone
					&& route.droneNext.get(id) == null && route.dronePrev.get(id) == null) {
					c_toInsert.add(id);
					c_toInsertIndex.add(j);
				}
			}
		}

		Route r_toInsert = null;
		int route_index = -1;
		while (c_toInsert.size() > 0) {
			int ran = random.nextInt(c_toInsert.size());
			int c_insert = c_toInsert.get(ran);
			int c_insert_index = c_toInsertIndex.get(ran);
			route_index = s.belongTo.get(c_insert);
			Route route = s.route_list.get(route_index);

			Route newRoute = new Route(route);
			newRoute.vehicleRoute.add(c_insert_index + 1, c);
			newRoute.calculate_cost(inst);
			if (newRoute.time < inst.v_time) {
				r_toInsert = newRoute;
				break;
			} else {
				c_toInsert.remove(ran);
				c_toInsertIndex.remove(ran);
			}
		}
		
		if (r_toInsert == null) {
			r_toInsert = new Route(inst);
			r_toInsert.vehicleRoute.add(0);
			r_toInsert.vehicleRoute.add(c);
			r_toInsert.vehicleRoute.add(0);
			s.route_list.add(r_toInsert);
			s.belongTo.set(c, s.route_list.size() - 1);
		} else {
			s.route_list.set(route_index, r_toInsert);
			s.belongTo.set(c, route_index);
		}
	}

	// 弱化版的FindSortie
	public Sortie FindSortie_w(int c, Solution s, double thresholdCost) {
		thresholdCost *= (1 + overflow_cent);
		Sortie BestSortie = null;
		for (int r_index = 0; r_index < s.route_list.size(); r_index++) {
			Route r = s.route_list.get(r_index);
			if (r.weight + inst.vec_poi.get(c).pack_weight < inst.v_weight_drone) {
				int r_n = r.vehicleRoute.size();
				boolean depart = false;
				for (int i = 0; i < r_n - 1; i++) {
					int launch_id = r.vehicleRoute.get(i);
					if (i > 0 && r.dronePrev.get(launch_id) != null) {
						depart = false;
					}
					if (r.droneNext.get(launch_id) != null) {
						depart = true;
					}
					if (!depart) {
						// 从这个点开始到下一次起飞中间所有的点都可以作为降落点
						for (int k = i + 1; k < r_n; k++) {
							int recovery_id = r.vehicleRoute.get(k);
							if (r.droneNext.get(recovery_id) != null) {
								break;
							}
							// 检查时间合法
							if ((inst.distance[launch_id][c] + inst.distance[c][recovery_id]) / inst.d_speed + inst.d_serviceTime
									+ inst.l_t <= inst.d_time) {
								Route newRoute = new Route(r);
								newRoute.droneNext.put(launch_id, c);
								newRoute.droneNext.put(c, recovery_id);
								newRoute.dronePrev.put(c, launch_id);
								newRoute.dronePrev.put(recovery_id, c);
								newRoute.calculate_cost(inst);
								if (newRoute.time < inst.v_time && s.t_cost + (inst.distance[launch_id][c] + inst.distance[c][recovery_id]) * inst.d_cost < thresholdCost) {
									BestSortie = new Sortie(launch_id, c, recovery_id);
									thresholdCost = s.t_cost + (inst.distance[launch_id][c] + inst.distance[c][recovery_id]) * inst.d_cost;
								}
							}
						}
					}
					
				}
			}
		}
		return BestSortie;
	}

	public boolean AttemptBestInsertion(int c, Route route) {
		boolean success = false;
		int idx = -1, land = -1; // 若isDrone为true, 则idx和land记录ID; 否则记录index(方便两种数据结构的插入)
		boolean isDrone = false;
		double cost = Double.MAX_VALUE;
		// 合法性检查: 卡车超重
		double weight = inst.vec_poi.get(c).pack_weight;
		if (weight + route.weight < inst.v_weight_drone) {
			boolean droneAvailable = true;
			for (int i = 0; i < route.vehicleRoute.size(); i++) {
				int id = route.vehicleRoute.get(i);
				if (id > 0) {
					// 卡车插入
					Route newRoute = new Route(route);
					newRoute.vehicleRoute.add(i, c);
					newRoute.calculate_cost(inst);
					if (newRoute.time < inst.v_time && newRoute.cost < cost) {
						idx = i;
						isDrone = false;
						cost = newRoute.cost;
						success = true;
					}
				}

				if (i > 0 && route.dronePrev.get(id) != null) {
					droneAvailable = true;
				}
				if (i < route.vehicleRoute.size() - 1 && route.droneNext.get(id) != null) {
					droneAvailable = false;
				}
				
				// 无人机插入
				// 合法性检查: 超重
				if (weight < inst.d_weight && droneAvailable) {
					// 枚举所有从此出发的无人机路径
					for (int landIdx = i + 1; landIdx < route.vehicleRoute.size(); landIdx++) {
						int landId = route.vehicleRoute.get(landIdx);
						if (route.droneNext.get(landId) != null) {
							break;
						}
						// 合法性检查: 时间
						if ((inst.distance[id][c] + inst.distance[c][landId]) / inst.d_speed + inst.d_serviceTime
								+ inst.l_t <= inst.d_time) {
							Route newRoute = new Route(route);
							newRoute.droneNext.put(id, c);
							newRoute.droneNext.put(c, landId);
							newRoute.dronePrev.put(c, id);
							newRoute.dronePrev.put(landId, c);
							newRoute.calculate_cost(inst);
							if (newRoute.time < inst.v_time && newRoute.cost < cost) {
								idx = id;
								land = landId;
								isDrone = true;
								cost = newRoute.cost;
								success = true;
							}
						}
					}
				}

			}
		}
		if (success) {
			if (isDrone) {
				route.droneNext.put(idx, c);
				route.droneNext.put(c, land);
				route.dronePrev.put(c, idx);
				route.dronePrev.put(land, c);
			} else {
				route.vehicleRoute.add(idx, c);
			}
			route.calculate_cost(inst);
		}
		return success;
	}
}

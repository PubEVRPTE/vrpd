package evrpdtw;

import java.util.*;

public class Neighborhood {

	// destroy方法中确定beta的参数
	public static final int c_low_lb = 1;
	public static final int c_low_ub = 3;
	public static final int c_high = 40;
	public static final double delta = 0.2;
	public static final Random random = new Random();

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
		repair1(sol, to_insert);
		sol.check(inst); // 调试检查正确性用，没问题就注释掉
	}

	public int getBeta() {
		return Math.max(Math.max(random.nextInt(c_low_ub) + c_low_lb, (int) Math.round(delta * inst.c_n)), c_high);
	}

	public ArrayList<Integer> destroy1(Solution sol, int beta) {
		ArrayList<Integer> remove_list = new ArrayList<Integer>();
		ArrayList<Boolean> removed = new ArrayList<Boolean>(inst.c_n + 1);
		while (beta > 0) {
			int id = random.nextInt(inst.c_n) + 1;
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
						sol.belongTo.remove(drone);
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
						sol.belongTo.remove(drone);
						remove_list.add(drone);
						removed.set(drone, true);
						beta--;
					}

					route.vehicleRoute.remove(i);
					sol.belongTo.remove(id);
					remove_list.add(id);
					removed.set(id, true);
					beta--;
					found = true;
				} else if (route.droneNext.get(current) == id) { // case 2: 由无人机配送
					int droneLanding = route.droneNext.get(id);
					route.droneNext.remove(current);
					route.droneNext.remove(id);
					route.dronePrev.remove(id);
					route.dronePrev.remove(droneLanding);
					sol.belongTo.remove(id);
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
		ArrayList<Boolean> removed = new ArrayList<Boolean>(inst.c_n + 1);

		int id = random.nextInt(inst.c_n) + 1;
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
						sol.belongTo.remove(drone);
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
						sol.belongTo.remove(drone);
						remove_list.add(drone);
						removed.set(drone, true);
						beta--;
					}

					route.vehicleRoute.remove(i);
					sol.belongTo.remove(id);
					remove_list.add(id);
					removed.set(id, true);
					beta--;
					found = true;
				} else if (route.droneNext.get(current) == id) { // case 2: 由无人机配送
					int droneLanding = route.droneNext.get(id);
					route.droneNext.remove(current);
					route.droneNext.remove(id);
					route.dronePrev.remove(id);
					route.dronePrev.remove(droneLanding);
					sol.belongTo.remove(id);
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

	public Sortie FindSortie(int c, Solution s, double threshold) {
		Sortie bestSortie = null;

		return bestSortie;
	}

	public void TruckRandomInsertion(int c, Solution s) {

	}

	// 弱化版的FindSortie
	public Sortie FindSortie_w(int c, Solution s, double threshold) {
		Sortie bestSortie = null;

		return bestSortie;
	}

	public boolean AttempBestInsertion(int c, Route route) {
		boolean success = false;
		int idx = -1, land = -1; // 若isDrone为true, 则idx和land记录ID; 否则记录index(方便两种数据结构的插入)
		boolean isDrone = false;
		double cost = Double.MAX_VALUE;
		// 合法性检查: 卡车超重
		double weight = inst.vec_poi.get(c).pack_weight;
		if (weight + route.weight < inst.v_weight_drone) {
			int id = route.vehicleRoute.get(0);
			boolean droneAvailable = (route.droneNext.get(0) == null);
			for (int i = 1; i < route.vehicleRoute.size(); i++) {
				id = route.vehicleRoute.get(i);
				// 卡车插入
				Route newRoute = new Route(route);
				newRoute.vehicleRoute.add(i, c);
				newRoute.calculate_cost(inst);
				if (newRoute.cost < cost) {
					idx = i;
					isDrone = false;
					cost = newRoute.cost;
					success = true;
				}
				if (route.dronePrev.get(id) != null) {
					droneAvailable = true;
				}
				if (route.droneNext.get(id) != null) {
					droneAvailable = false;
				}
				// 无人机插入
				// 合法性检查: 超重
				if (weight < inst.d_weight && droneAvailable) {
					// 枚举所有从此出发的无人机路径
					int landIdx = i + 1;
					while (route.droneNext.get(landIdx) == null) {
						// 合法性检查: 时间
						int landId = route.vehicleRoute.get(landIdx);
						if ((inst.distance[id][c] + inst.distance[c][landId]) / inst.d_speed + inst.d_serviceTime + inst.l_t < inst.d_time) {
							newRoute = new Route(route);
							newRoute.droneNext.put(id, c);
							newRoute.droneNext.put(c, landId);
							newRoute.dronePrev.put(c, id);
							newRoute.dronePrev.put(landId, c);
							newRoute.calculate_cost(inst);
							if (newRoute.cost < cost) {
								idx = id;
								land = landId;
								isDrone = true;
								cost = newRoute.cost;
								success = true;
							}
						}
						landIdx++;
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

	public void repair1(Solution sol, ArrayList<Integer> to_insert) {
		// TODO
	}

	public void repair2(Solution sol, ArrayList<Integer> to_insert) {

	}

	public void repair3(Solution sol, ArrayList<Integer> to_insert) {
		ArrayList<Boolean> notInserted = new ArrayList<Boolean>(inst.c_n + 1);
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
			if (AttempBestInsertion(minidx, sol.route_list.get(sol.belongTo.get(minidx))) == false) {
				uninserted.add(minidx);
			}
		}
		if (uninserted.size() > 0) {
			repair1(sol, uninserted);
		}
	}

	public void repair4(Solution sol, ArrayList<Integer> to_insert) {

	}
}

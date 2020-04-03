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
    
    public void nearest_neighbor(Solution sol) {
		// greedy

	}

	public void relocate(Solution sol) {

	}

	public void drone_addition(Solution sol) {

    }
    
    public ArrayList<Integer> destroy(Solution sol) {
		if (random.nextBoolean()) {
			return destroy1(sol, getBeta());
		} else {
			return destroy2(sol, getBeta());
		}
	}

	public void repair(Solution sol, ArrayList<Integer> to_insert) {
		repair1(sol, to_insert);
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
			ArrayList<Integer> route = sol.vehicleRoute.get(routeId);

			for (int i = 0; i < route.size(); i++) {
				int current = route.get(i);
				boolean found = false;
				if (current == id) { // case 1: 由卡车配送
					// 删掉接收的无人机
					Integer drone = sol.dronePrev.get(id);
					if (drone != null) {
						int droneDepart = sol.dronePrev.get(drone);
						sol.droneNext.remove(droneDepart);
						sol.droneNext.remove(drone);
						sol.dronePrev.remove(drone);
						sol.dronePrev.remove(id);
						sol.belongTo.remove(drone);
						remove_list.add(drone);
						removed.set(drone, true);
						beta--;
					}
					// 删掉发送的无人机
					drone = sol.droneNext.get(id);
					if (drone != null) {
						int droneLanding = sol.droneNext.get(drone);
						sol.droneNext.remove(id);
						sol.droneNext.remove(drone);
						sol.dronePrev.remove(drone);
						sol.dronePrev.remove(droneLanding);
						sol.belongTo.remove(drone);
						remove_list.add(drone);
						removed.set(drone, true);
						beta--;
					}

					route.remove(i);
					sol.belongTo.remove(id);
					remove_list.add(id);
					removed.set(id, true);
					beta--;
					found = true;
				} else if (sol.droneNext.get(current) == id) { // case 2: 由无人机配送
					int droneLanding = sol.droneNext.get(id);
					sol.droneNext.remove(current);
					sol.droneNext.remove(id);
					sol.dronePrev.remove(id);
					sol.dronePrev.remove(droneLanding);
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
			ArrayList<Integer> route = sol.vehicleRoute.get(routeId);

			for (int i = 0; i < route.size(); i++) {
				int current = route.get(i);
				boolean found = false;
				if (current == id) { // case 1: 由卡车配送
					// 删掉接收的无人机
					Integer drone = sol.dronePrev.get(id);
					if (drone != null) {
						int droneDepart = sol.dronePrev.get(drone);
						sol.droneNext.remove(droneDepart);
						sol.droneNext.remove(drone);
						sol.dronePrev.remove(drone);
						sol.dronePrev.remove(id);
						sol.belongTo.remove(drone);
						remove_list.add(drone);
						removed.set(drone, true);
						beta--;
					}
					// 删掉发送的无人机
					drone = sol.droneNext.get(id);
					if (drone != null) {
						int droneLanding = sol.droneNext.get(drone);
						sol.droneNext.remove(id);
						sol.droneNext.remove(drone);
						sol.dronePrev.remove(drone);
						sol.dronePrev.remove(droneLanding);
						sol.belongTo.remove(drone);
						remove_list.add(drone);
						removed.set(drone, true);
						beta--;
					}

					route.remove(i);
					sol.belongTo.remove(id);
					remove_list.add(id);
					removed.set(id, true);
					beta--;
					found = true;
				} else if (sol.droneNext.get(current) == id) { // case 2: 由无人机配送
					int droneLanding = sol.droneNext.get(id);
					sol.droneNext.remove(current);
					sol.droneNext.remove(id);
					sol.dronePrev.remove(id);
					sol.dronePrev.remove(droneLanding);
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
			for (int i = 0; i < inst.c_n; i++) {
				if (removed.get(i)) {
					continue;
				}
				if (inst.distance[id][i] < mindist1) {
					mindist2 = mindist1; minidx2 = minidx1;
					mindist1 = inst.distance[id][i]; minidx1 = i;
				} else if (inst.distance[id][i] < mindist2) {
					mindist2 = inst.distance[id][i]; minidx2 = i;
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
        // TODO
        sol.check(inst);
        sol.calculate_cost(inst);
	}

	public void repair2(Solution sol, ArrayList<Integer> to_insert) {

	}

	public void repair3(Solution sol, ArrayList<Integer> to_insert) {

	}

	public void repair4(Solution sol, ArrayList<Integer> to_insert) {

	}
}
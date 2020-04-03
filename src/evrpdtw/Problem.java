package evrpdtw;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.*;

class poi {

	public int id;
	public int type;// 1 depot 2 customer 3 charge
	public double x;
	public double y;
	public double pack_weight;
	public int v_serviceTime;
	public int d_seviceTime;

	public poi() {

	}

	public poi(int id, int type, double x, double y, double p_w, int v_s_t, int d_s_t) {
		this.id = id;
		this.type = type;
		this.x = x;
		this.y = y;
		this.pack_weight = p_w;
		this.v_serviceTime = v_s_t;
		this.d_seviceTime = d_s_t;
	}
}

public class Problem {

	public double v_weight;
	public double v_weight_drone;
	public double v_time;
	public double v_speed;
	public int v_serviceTime;
	public double v_maxDistance;
	public double v_cost;

	public double d_weight;
	public double d_time;
	public double d_speed;
	public int d_serviceTime;
	public double d_maxDistance;
	public double d_cost;
	public double launch_cost;// vehicle launch cost

	public int l_t;// drone launch time
	public int r_t;// drone recovery time

	public int c_n;// costumer point number
	public int a_n;// all point number

	public ArrayList<poi> vec_poi;// cover depot
	public ArrayList<Integer> vec_poi_id;// not cover depot

	public ArrayList<Integer> vec_droneable_poi_id;
	// public ArrayList<ArrayList<ArrayList<Integer>>>
	// chargeable_List;//两个顾客点的之间的充电站集合

	public double[][] location;
	public double[][] distance;
	public int[][] infeasible_edge;// relate to charge

	public Solution sol;

	// destroy方法中确定beta的参数
	public static final int c_low_lb = 1;
	public static final int c_low_ub = 3;
	public static final int c_high = 40;
	public static final double delta = 0.2;
	public static final Random random = new Random();

	public Problem() {

		vec_poi = new ArrayList<poi>();
		vec_poi_id = new ArrayList<Integer>();
		vec_droneable_poi_id = new ArrayList<Integer>();

		v_maxDistance = v_speed * v_serviceTime;
		d_maxDistance = d_speed * d_serviceTime;

		// chargeable_List = new ArrayList<ArrayList<ArrayList<Integer>>>();
		sol = new Solution(this);
	}

	public void prepare() {
		distance = new double[a_n][a_n];
		infeasible_edge = new int[a_n][a_n];

		// 数据处理：1、去除不可行边；2、获得可供无人机服务的顾客点；3、筛选充电站
		// 1
		for (int i = 0; i < a_n; i++) {
			for (int j = 0; j < a_n; j++) {
				distance[i][j] = Math.sqrt((location[i][0] - location[j][0]) * (location[i][0] - location[j][0])
						+ (location[i][1] - location[j][1]) * (location[i][1] - location[j][1]));
				infeasible_edge[i][j] = 1;
			}
		} // 计算distance并将infeasible_edge数组全部置为1；

		// for (int i = 0; i < CustomerPointNum; i++) {
		// for (int j = 0; j < CustomerPointNum; j++) {
		// if (i == j) {
		// continue;
		// }
		// Customer c1 = customer_list.get(customer_id_list.get(i));
		// Customer c2 = customer_list.get(customer_id_list.get(j));
		// //
		// if (c1.volume + c2.volume > vVolume || c1.weight + c2.weight > vWeight) {
		// infeasible_edge[i][j] = 0;
		// }
		// //
		// if (c1.earliestTime + c1.serviceTime + distance[i][j]/dSpeed >
		// c2.lastestTime) {
		// infeasible_edge[i][j] = 0;
		// }
		// //
		// double mini_charge_distance = Integer.MAX_VALUE;
		// double minj_charge_distance = Integer.MAX_VALUE;
		// for (int s = 0; s < charge_id_list.size(); s++) {
		// int charge_id = charge_id_list.get(s);
		// if (distance[i][charge_id] < mini_charge_distance) {
		// mini_charge_distance = distance[i][charge_id];
		// }
		// if (distance[j][charge_id] < minj_charge_distance) {
		// minj_charge_distance = distance[j][charge_id];
		// }
		// }
		// if (mini_charge_distance + distance[i][j] + minj_charge_distance > vDistance)
		// {
		// infeasible_edge[i][j] = 0;
		// }
		// }
		// }//只服务i，j就超载；服务完i后无法再服务j；只服务i，j电力不够

		for (int i = 0; i < c_n; i++) {
			poi c = vec_poi.get(i);
			if (c.pack_weight < d_weight) {
				vec_droneable_poi_id.add(c.id);
			}
		} // 可以被无人机服务的顾客点
	}

	public void load_instance(String r_f_n, String p_f_n) throws FileNotFoundException {
		// read global variable
		File f1 = new File(r_f_n);
		File f2 = new File(p_f_n);
		Scanner scan1 = new Scanner(f1);
		scan1.next();
		v_speed = scan1.nextDouble();
		scan1.next();
		d_speed = scan1.nextDouble();
		scan1.next();
		v_weight_drone = scan1.nextDouble();
		scan1.next();
		v_weight = scan1.nextDouble();
		scan1.next();
		d_weight = scan1.nextDouble();
		scan1.next();
		v_serviceTime = scan1.nextInt();
		scan1.next();
		d_serviceTime = scan1.nextInt();
		scan1.next();
		d_time = scan1.nextDouble();
		scan1.next();
		v_time = scan1.nextDouble();
		scan1.next();
		l_t = scan1.nextInt();
		scan1.next();
		r_t = scan1.nextInt();
		scan1.next();
		v_cost = scan1.nextDouble();
		scan1.next();
		d_cost = scan1.nextDouble();
		scan1.close();

		// read node
		Scanner scan2 = new Scanner(f2);
		poi depot = new poi(0, 1, 0, 0, 0, 0, 0);
		vec_poi.add(depot);
		scan2.next();
		c_n = scan2.nextInt();
		a_n = c_n + 1;
		location = new double[a_n][2];
		location[0][0] = 0;
		location[0][1] = 0;
		scan2.next();
		scan2.next();
		scan2.next();
		scan2.next();
		scan2.next();
		for (int i = 0; i < c_n; i++) {
			double xx = scan2.nextDouble();
			double yy = scan2.nextDouble();
			double dd = scan2.nextDouble();
			poi r = new poi(i + 1, 2, xx, yy, dd, v_serviceTime, d_serviceTime);
			location[i + 1][0] = xx;
			location[i + 1][1] = yy;
			vec_poi.add(r);
			vec_poi_id.add(r.id);
		}
		scan2.close();

	}

	public void initial() {
		nearest_neighbor();
		relocate();
		drone_addition();
	}

	public void solve() {
		// 模拟退火
		// 暂时不模拟退火了
		int iter = 0;

		while (iter < 1000) {
			destroy();

			iter++;
		}
	}

	public void nearest_neighbor() {
		// greedy

	}

	public void relocate() {

	}

	public void drone_addition() {

	}

	public void destroy() {
		if (random.nextBoolean()) {
			destroy1(getBeta());
		} else {
			destroy2(getBeta());
		}
	}

	public int getBeta() {
		return Math.max(Math.max(random.nextInt(c_low_ub) + c_low_lb, (int) Math.round(delta * c_n)), c_high);
	}

	public ArrayList<Integer> destroy1(int beta) {
		ArrayList<Integer> remove_list = new ArrayList<Integer>();
		ArrayList<Boolean> removed = new ArrayList<Boolean>(c_n + 1);
		while (beta > 0) {
			int id = random.nextInt(c_n) + 1;
			while (removed.get(id)) {
				id = random.nextInt(c_n) + 1;
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

	public ArrayList<Integer> destroy2(int beta) {
		ArrayList<Integer> remove_list = new ArrayList<Integer>();
		ArrayList<Boolean> removed = new ArrayList<Boolean>(c_n + 1);

		int id = random.nextInt(c_n) + 1;
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
			for (int i = 0; i < c_n; i++) {
				if (removed.get(i)) {
					continue;
				}
				if (distance[id][i] < mindist1) {
					mindist2 = mindist1; minidx2 = minidx1;
					mindist1 = distance[id][i]; minidx1 = i;
				} else if (distance[id][i] < mindist2) {
					mindist2 = distance[id][i]; minidx2 = i;
				}
			}
			if (random.nextBoolean()) {
				minidx1 = minidx2;
			}
			id = minidx1;
		}
		return remove_list;
	}

	public void repair1() {

	}

	public void repair2() {

	}

	public void repair3() {

	}

	public void repair4() {

	}
}

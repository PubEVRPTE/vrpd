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
	public double v_cost;

	public double d_weight;
	public double d_time;
	public double d_speed;
	public int d_serviceTime;
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

		// chargeable_List = new ArrayList<ArrayList<ArrayList<Integer>>>();
		sol = new Solution();
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

	public void destroy1(int beta) {
		int removed = 0;
		ArrayList<Boolean> removed_list = new ArrayList<Boolean>(c_n);

		while (removed < beta) {
			int toRemove = random.nextInt(c_n);
			while (removed_list.get(toRemove)) {
				toRemove = random.nextInt(c_n);
			}

			int route_id = 0;
			Route route;
			while (route_id < sol.route_list.size()) {
				route = sol.route_list.get(route_id);
				boolean found = false;
				int find = route.vehicleRoute.indexOf(toRemove);
				if (find != -1) { // 该点由车辆配送
					if (route.vehicleRoute.size() == 1) { // 路径上只有这一个点，直接删除路径
						// TODO: 更新solution的时间、距离等参数
						sol.route_list.remove(route_id);
						route = null;
					} else { // 常规删除

						// 处理该点出发的无人机
						if (route.droneNext.get(find) != -1) {
							// 无人机路径固定只有三个点，硬编码
							int cursor = route.droneNext.get(find), prev = find;
							route.droneNext.set(prev, -1);
							route.dronePrev.set(cursor, -1);
							prev = cursor;
							cursor = route.droneNext.get(cursor);
							route.droneNext.set(prev, -1);
							route.dronePrev.set(cursor, -1);
						}

						// 处理该点接收的无人机
						if (route.dronePrev.get(find) != -1) {
							// 无人机路径固定只有三个点，硬编码
							int cursor = route.dronePrev.get(find), prev = find;
							route.dronePrev.set(prev, -1);
							route.droneNext.set(cursor, -1);
							prev = cursor;
							cursor = route.dronePrev.get(cursor);
							route.dronePrev.set(prev, -1);
							route.droneNext.set(cursor, -1);
						}

						route.droneNext.remove(find);
						route.dronePrev.remove(find);
						route.vehicleRoute.remove(find);
					}
					found = true;
				} else if (route.droneNext.get(toRemove) != -1) { // 该点由无人机配送
					int next = route.droneNext.get(toRemove);
					int prev = route.dronePrev.get(toRemove);
					route.droneNext.set(toRemove, -1);
					route.droneNext.set(prev, -1);
					route.dronePrev.set(toRemove, -1);
					route.dronePrev.set(next, -1);
					found = true;
				}

				if (found) {
					removed++;
					removed_list.set(toRemove, true);
					break;
				}
			}
		}
	}

	public void destroy2(int beta) {

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

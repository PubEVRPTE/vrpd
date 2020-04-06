package evrpdtw;

import java.util.*;

public class Neighborhood {

	// destroy������ȷ��beta�Ĳ���
		public static final int c_low_lb = 1;
		public static final int c_low_ub = 3;
		public static final int c_high = 40;
		public static final double delta = 0.2;
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
			sol.calculate_cost(inst); // Ч�ʵͣ����Ľ�
			repair1(sol, to_insert);
			sol.check(inst); // ���Լ����ȷ���ã�û�����ע�͵�
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
					if (current == id) { // case 1: �ɿ�������
						// ɾ�����յ����˻�
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
						// ɾ�����͵����˻�
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
					} else if (route.droneNext.get(current) == id) { // case 2: �����˻�����
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
					if (current == id) { // case 1: �ɿ�������
						// ɾ�����յ����˻�
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
						// ɾ�����͵����˻�
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
					} else if (route.droneNext.get(current) == id) { // case 2: �����˻�����
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
				// ���������������ʱֱ������dist
				// �������Ȱ�dist�Ÿ��򣬽�������ʱ��
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
			Solution copySol = new Solution(sol);
			int free_c = to_insert.size();
			// ������뿨��
			while(free_c > 0){
				int id = random.nextInt(free_c) + 1;
				TruckBestInsertion(id, copySol);
				to_insert.remove(id);
				free_c -= 1;
			}

			// ���ָ�Ϊ���˻�
			ArrayList<poi> copyCus = new ArrayList<poi>(inst.vec_poi);
			int c_n = copyCus.size();
			while(c_n > 0){
				int id = random.nextInt(c_n) + 1;
				poi current = copyCus.get(id);
				copyCus.remove(id);
				int routeId = copySol.belongTo.get(id);
				Route route = copySol.route_list.get(routeId);
				Integer droneN = route.droneNext.get(id);
				Integer droneP = route.dronePrev.get(id);
				if(current.pack_weight < inst.d_weight && droneN == null && droneP == null){
					double threshold = copySol.t_cost;
					copySol.belongTo.remove(id);
					copyCus.remove(id);
					FindSortie(id, copySol, threshold);
				}

			}
		}

		public void repair2(Solution sol, ArrayList<Integer> to_insert) {
			Solution copySol = new Solution(sol);
			int free_c = to_insert.size();
			// ������뿨��
			while(free_c > 0){
				int id = random.nextInt(free_c) + 1;
				TruckRandomInsertion(id, copySol);
				to_insert.remove(id);
				free_c -= 1;
			}
			
			// ���ָ�Ϊ���˻�
			ArrayList<poi> copyCus = new ArrayList<poi>(inst.vec_poi);
			int c_n = copyCus.size();
			while(c_n > 0){
				int id = random.nextInt(c_n) + 1;
				poi current = copyCus.get(id);
				copyCus.remove(id);
				int routeId = copySol.belongTo.get(id);
				Route route = copySol.route_list.get(routeId);
				Integer droneN = route.droneNext.get(id);
				Integer droneP = route.dronePrev.get(id);
				if(current.pack_weight < inst.d_weight && droneN == null && droneP == null){
					double threshold = copySol.t_cost;
					copySol.belongTo.remove(id);
					copyCus.remove(id);
					FindSortie_w(id, copySol, threshold);
				}

			}
		}

		public void repair3(Solution sol, ArrayList<Integer> to_insert) {
			ArrayList<Boolean> notInserted = new ArrayList<Boolean>(inst.c_n + 1);
			for (int e : to_insert) {
				notInserted.set(e, true);
			}
			ArrayList<Integer> uninserted = new ArrayList<Integer>(); // ���ܳɹ�����ĵ�
			while (to_insert.size() > 0) {
				int c = to_insert.remove(random.nextInt(to_insert.size()));

				// �ҵ���ǰ����������ĵ�
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

				// ���Բ���
				if (AttempBestInsertion(minidx, sol.route_list.get(sol.belongTo.get(minidx))) == false) {
					uninserted.add(minidx);
				}
			}
			if (uninserted.size() > 0) {
				repair1(sol, uninserted);
			}
		}

		public void repair4(Solution sol, ArrayList<Integer> to_insert) {
			ArrayList<poi> copyCus = new ArrayList<poi>(inst.vec_poi);
			ArrayList<Integer> oversize = new ArrayList<>(); 
			poi a = new poi();
			int id = 0;
			double w = 0;
			// ȥ�����ص�
			for(int i = 0; i < copyCus.size(); i++){
				a = copyCus.get(i);
				w = a.pack_weight;
				if(w > inst.d_weight){
					oversize.add(a.id);
					copyCus.remove(a);
				}
			}
			Solution copySol = new Solution(sol);
			for(int i = 0; i < oversize.size(); i++){
				id = oversize.get(i);
				TruckBestInsertion(id, copySol);
			}
			repair1(copySol, to_insert);
		}
		
		public void TruckBestInsertion(int c, Solution s) {
			double add_cost = Double.MAX_VALUE;
			Route r_toInsert = null;
			int route_index = -1;
			for (int i = 0; i < s.route_list.size(); i++) {
				Route r = s.route_list.get(i);
				if (r.weight + inst.vec_poi.get(c).pack_weight <= inst.v_weight_drone) {
					for (int j = 0; j < r.vehicleRoute.size()-1; j++) {
						Route r_t = new Route(r);
						r_t.vehicleRoute.add(j+1, c);
						r_t.calculate_cost(inst);
						if (r_t.time <= inst.v_time && (r_t.cost - r.cost) < add_cost) {
							route_index = i;
							add_cost = (r_t.cost - r.cost);
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
			}
			else {
				s.route_list.set(route_index,r_toInsert);
			}
		}
		
		public Sortie FindSortie(int c, Solution s, double thresholdCost) {
			Sortie BestSortie = null;
			for (int r_index = 0; r_index < s.route_list.size(); r_index++) {
				Route r = s.route_list.get(r_index);
				if (r.weight + inst.vec_poi.get(c).pack_weight < inst.v_weight_drone) {
					int r_n = r.vehicleRoute.size();
					for (int i = 0; i < r_n-1; i++ ) {
						for (int k = i+1; k < r_n; k++) {
							int launch_id = r.vehicleRoute.get(i);
							int recovery_id = r.vehicleRoute.get(k);
							Sortie p = new Sortie(launch_id, c, recovery_id);
							double drone_distance = inst.distance[launch_id][c] + inst.distance[c][recovery_id];
							double drone_time = inst.d_serviceTime + drone_distance/inst.d_speed;
							double new_cost = r.cost + drone_distance*inst.d_cost;
							if (new_cost < thresholdCost && drone_time <= inst.d_time) {
								boolean depart = false;
								for (int ii = i; ii >= 0; ii--) {
									if (r.droneNext.get(r.vehicleRoute.get(ii)) != null) {
										depart = true;
										break;
									}
									else if (r.droneNext.get(r.vehicleRoute.get(ii)) == null && r.dronePrev.get(r.vehicleRoute.get(ii)) != null) {
										break;
									}
								}
								for (int kk = k; kk < r_n; kk++) {
									if (r.droneNext.get(r.vehicleRoute.get(kk)) == null) {
										depart = true;
										break;
									}
									else if (r.droneNext.get(r.vehicleRoute.get(kk)) != null && r.dronePrev.get(r.vehicleRoute.get(kk)) == null) {
										break;
									}
								}
								if (!depart) {
									double vehicle_time = inst.distance[i][i+1]/inst.v_speed;
									int tmp = i+1;
									while (tmp < k) {
										vehicle_time += inst.v_serviceTime + inst.distance[tmp][tmp+1]/inst.v_speed;
										tmp++;
									}
									if (r.time + inst.l_t + inst.r_t + Math.max(vehicle_time, drone_time) - vehicle_time <= inst.v_time && r.cost + drone_distance*inst.d_cost < thresholdCost) {
										BestSortie = p;
										thresholdCost = r.cost + drone_distance*inst.d_cost;
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
			//TODO
			double near_distance = 5;
			ArrayList<Integer> c_toInsert = new ArrayList<Integer>();
			for (int i = 0; i < s.route_list.size(); i++) {
				Route r = s.route_list.get(i);
				for (int j = 0; j < r.vehicleRoute.size()-1; j++) {
					int id = r.vehicleRoute.get(j);
					if (inst.distance[c][id] < near_distance && inst.distance[c][id+1] < near_distance) {
						c_toInsert.add(id);
					}
				}
			}
			Route r_toInsert = null;
			int route_index = -1;
			while (true) {
				int ran = random.nextInt(c_toInsert.size());
				int c_insert = c_toInsert.get(ran);
				int c_insert_index = -1;
				route_index = s.belongTo.get(c_insert);
				Route r = s.route_list.get(route_index);
				if (inst.vec_poi.get(c_insert).pack_weight + r.weight <= inst.v_weight_drone) {
					for (int i = 0; i < r.vehicleRoute.size(); i++) {
						if (r.vehicleRoute.get(i) == c_insert) {
							c_insert_index = i;
							break;
						}
					}
					if (r.droneNext.get(c_insert) == null || r.dronePrev.get(c_insert) == null) {
						Route r_t = new Route(r);
						r_t.vehicleRoute.add(c_insert_index+1, c);
						r_t.calculate_cost(inst);
						r_toInsert = r_t;
					}
					else {
						c_toInsert.remove(ran);
					}
					if (r_toInsert != null || c_toInsert.isEmpty()) {
						break;
					}
				}
				
			}
			
			if (r_toInsert == null) {
				r_toInsert = new Route(inst);
				r_toInsert.vehicleRoute.add(0);
				r_toInsert.vehicleRoute.add(c);
				r_toInsert.vehicleRoute.add(0);
				s.route_list.add(r_toInsert);
			}
			else {
				s.route_list.set(route_index,r_toInsert);
			}
		}

		//�������FindSortie
		public Sortie FindSortie_w(int c, Solution s, double thresholdCost) { 
			Sortie BestSortie = null;
			for (int r_index = 0; r_index < s.route_list.size(); r_index++) {
				Route r = s.route_list.get(r_index);
				if (r.weight + inst.vec_poi.get(c).pack_weight < inst.v_weight_drone) {
					int r_n = r.vehicleRoute.size();
					for (int i = 0; i < r_n-1; i++ ) {
						for (int k = i+1; k < r_n; k++) {
							int launch_id = r.vehicleRoute.get(i);
							int recovery_id = r.vehicleRoute.get(k);
							Sortie p = new Sortie(launch_id, c, recovery_id);
							double drone_distance = inst.distance[launch_id][c] + inst.distance[c][recovery_id];
							double drone_time = inst.d_serviceTime + drone_distance/inst.d_speed;
							double new_cost = r.cost + drone_distance*inst.d_cost;
							if (new_cost < thresholdCost*(1+overflow_cent) && drone_time <= inst.d_time) {
								boolean depart = false;
								for (int ii = i; ii >= 0; ii--) {
									if (r.droneNext.get(r.vehicleRoute.get(ii)) != null && r.dronePrev.get(r.vehicleRoute.get(ii)) == null) {
										depart = true;
										break;
									}
									else if (r.droneNext.get(r.vehicleRoute.get(ii)) == null && r.dronePrev.get(r.vehicleRoute.get(ii)) != null) {
										break;
									}
								}
								for (int kk = k; kk < r_n; kk++) {
									if (r.droneNext.get(r.vehicleRoute.get(kk)) == null && r.dronePrev.get(r.vehicleRoute.get(kk)) != null) {
										depart = true;
										break;
									}
									else if (r.droneNext.get(r.vehicleRoute.get(kk)) != null && r.dronePrev.get(r.vehicleRoute.get(kk)) == null) {
										break;
									}
								}
								if (!depart) {
									double vehicle_time = inst.distance[i][i+1]/inst.v_speed;
									int tmp = i+1;
									while (tmp < k) {
										vehicle_time += inst.v_serviceTime + inst.distance[tmp][tmp+1]/inst.v_speed;
										tmp++;
									}
									if (r.time + inst.l_t + inst.r_t + Math.max(vehicle_time, drone_time) - vehicle_time <= inst.v_time && r.cost + drone_distance*inst.d_cost < thresholdCost*(1+overflow_cent)) {
										BestSortie = p;
										thresholdCost = r.cost + drone_distance*inst.d_cost;
									}
								}
							}
							
							
							
						}
					}
				}
			}
			return BestSortie;
		}
		
		public int NearestCustomer(int c, Solution s) {
			int c_nearest = -1;
			
			return c_nearest;
		}
		
		public boolean AttempBestInsertion(int c, Route route) {
			boolean success = false;
			int idx = -1, land = -1; // ��isDroneΪtrue, ��idx��land��¼ID; �����¼index(�����������ݽṹ�Ĳ���)
			boolean isDrone = false;
			double cost = Double.MAX_VALUE;
			// �Ϸ��Լ��: ��������
			double weight = inst.vec_poi.get(c).pack_weight;
			if (weight + route.weight < inst.v_weight_drone) {
				int id = route.vehicleRoute.get(0);
				boolean droneAvailable = (route.droneNext.get(0) == null);
				for (int i = 1; i < route.vehicleRoute.size(); i++) {
					id = route.vehicleRoute.get(i);
					// ��������
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
					// ���˻�����
					// �Ϸ��Լ��: ����
					if (weight < inst.d_weight && droneAvailable) {
						// ö�����дӴ˳��������˻�·��
						int landIdx = i + 1;
						while (route.droneNext.get(landIdx) == null) {
							// �Ϸ��Լ��: ʱ��
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
}

package evrpdtw;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.*;

class poi {
	
	public int id;
	public int type;//1 depot 2 customer 3 charge
	public double x;
	public double y;
	public double pack_weight;
	public double v_serviceTime;
	public double d_serviceTime;
	
	public poi() {
		
	}
	
	public poi(int id, int type, double x, double y, double p_w, double v_s_t, double d_s_t) {
		this.id = id;
		this.type = type;
		this.x = x;
		this.y = y;
		this.pack_weight = p_w;
		this.v_serviceTime = v_s_t;
		this.d_serviceTime  = d_s_t;
	}
}

public class Problem {
	
	public double v_weight;
	public double v_weight_drone;
	public double v_time;
	public double v_speed;
	public double v_serviceTime;
	public double v_cost;

	public double d_weight;
	public double d_time;
	public double d_speed;
	public double d_serviceTime;
	public double d_cost;
	public double launch_cost;//vehicle launch cost
	
	public double l_t;//drone launch time
	public double r_t;//drone recovery time
	
	public int c_n;//costumer point number
	public int a_n;//all point number
	
	public ArrayList<poi> vec_poi;//cover depot
	public ArrayList<Integer> vec_droneable_poi_id;
	
	public double[][] location;
	public double[][] distance;
	
	public Problem() {
		vec_poi = new ArrayList<poi>();
		vec_droneable_poi_id = new ArrayList<Integer>();
	}
	
	public void prepare() {
		distance = new double[a_n][a_n];
		
		for (int i = 0; i < a_n; i++) {
			for (int j = 0; j < a_n; j++) {
				distance[i][j] = Math.sqrt((location[i][0] - location[j][0]) * (location[i][0] - location[j][0])
						+ (location[i][1] - location[j][1]) * (location[i][1] - location[j][1]));
			}
		}
		
		for (int i = 0; i < c_n; i++) {
			poi c = vec_poi.get(i);
			if (c.type == 2 && c.pack_weight < d_weight) {
				vec_droneable_poi_id.add(c.id);
			}
		}//���Ա����˻�����Ĺ˿͵�
	}
	
	public void load_instance(String r_f_n, String p_f_n) throws FileNotFoundException {
		//read global variable
		File f1 = new File(r_f_n);
		File f2 = new File(p_f_n);
		Scanner scan1 = new Scanner(f1);
		scan1.next();v_speed = scan1.nextDouble();
		scan1.next();d_speed = scan1.nextDouble();
		scan1.next();v_weight_drone = scan1.nextDouble();
		scan1.next();v_weight = scan1.nextDouble();
		scan1.next();d_weight = scan1.nextDouble();
		scan1.next();v_serviceTime = scan1.nextInt();
		scan1.next();d_serviceTime = scan1.nextInt();
		scan1.next();d_time = scan1.nextDouble();
		scan1.next();v_time = scan1.nextDouble();
		scan1.next();l_t = scan1.nextInt();
		scan1.next();r_t = scan1.nextInt();
		scan1.next();v_cost = scan1.nextDouble();
		scan1.next();d_cost = scan1.nextDouble();
		scan1.close();
		
		
		//read node
		Scanner scan2 = new Scanner(f2);
		poi depot = new poi(0, 1, 0, 0, 0, 0, 0);
		vec_poi.add(depot);
		scan2.next();
		c_n = scan2.nextInt();
		a_n = c_n + 1;
		location = new double[a_n][2];
		location[0][0] = 0;
		location[0][1] = 0;
		scan2.next();scan2.next();scan2.next();scan2.next();scan2.next();
		for (int i = 0; i < c_n; i++) {
			double xx = scan2.nextDouble();
			double yy = scan2.nextDouble();
			double dd = scan2.nextDouble();
			poi r = new poi(i+1, 2, xx, yy, dd, v_serviceTime, d_serviceTime);
			location[i+1][0] = xx;
			location[i+1][1] = yy;
			vec_poi.add(r);
		}
		scan2.close();
		
	}
}

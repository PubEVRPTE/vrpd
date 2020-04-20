package vrpd;

import evrpdtw.*;

import java.io.FileNotFoundException;

public class Test {

	public static void main(String[] args) throws FileNotFoundException {
		// Currently just solve it multiple times
		// Serves to find solution inconsistenciess
		String r_f_n = "RouteData.txt";
		String p_f_n = "instances/Instances/150.30.01.txt";
		for (int i = 0; i < 20; i++) {
			Problem inst = new Problem();
			inst.load_instance(r_f_n, p_f_n);
			inst.prepare();
			Heuristics heur = new Heuristics(inst);
			heur.solve();
		}
	}
}

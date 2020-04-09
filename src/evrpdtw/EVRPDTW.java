package evrpdtw;

import java.io.FileNotFoundException;

public class EVRPDTW {

	public static void main(String[] args) throws FileNotFoundException {
		String r_f_n = "RouteData.txt";
		String p_f_n = "150.30.c.01.txt";
		Problem inst = new Problem();
		inst.load_instance(r_f_n, p_f_n);
		inst.prepare();
		Heuristics heur = new Heuristics(inst);
		heur.solve();
		
	}
}

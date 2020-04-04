package evrpdtw;

import java.io.FileNotFoundException;

public class EVRPDTW {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		String r_f_n = "RouteData.txt";
		String p_f_n = "250.30.s.25.txt";
		Problem inst = new Problem();
		inst.load_instance(r_f_n, p_f_n);
		inst.prepare();
		Heuristics heur = new Heuristics(inst);
		heur.solve();
		
		System.out.println(heur.bestSolution.toString());
		
	}
}

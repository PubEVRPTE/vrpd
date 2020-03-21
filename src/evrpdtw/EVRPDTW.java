package evrpdtw;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class EVRPDTW {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		String r_f_n = "RouteData.txt";
		String p_f_n = "150.30.c.01.txt";
		Problem inst = new Problem();
		inst.load_instance(r_f_n, p_f_n);
		inst.prepare();
		inst.initial();
		inst.solve();
		
		System.out.println(inst.sol.toString());
		
	}
}

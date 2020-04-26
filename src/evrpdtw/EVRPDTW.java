package evrpdtw;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.io.FileNotFoundException;

public class EVRPDTW {

	public static void main(String[] args) throws FileNotFoundException {
		String r_f_n = "RouteData.txt";
		String p_f_n = "instances/Instances/150.30.1.txt";
		Problem inst = new Problem();
		inst.load_instance(r_f_n, p_f_n);
		inst.prepare();
		Heuristics heur = new Heuristics(inst);
		heur.solve();
		try {
			writetxt(heur);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void writetxt(Heuristics heur) throws IOException{
		String path = System.getProperty("user.dir") + "/vehicleRoute.txt";
		createFile(path);
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path,true)));
        
		for(int i = 0; i < heur.sol.route_list.size(); i++) {
			for(int j = 0; j < heur.sol.route_list.get(i).vehicleRoute.size(); j++) {
				out.write(heur.sol.route_list.get(i).vehicleRoute.get(j) + " ");
			}
			out.write("\r\n");
		}
		out.close();
		
		path = System.getProperty("user.dir") + "/dronesRoute.txt";
		createFile(path);
		BufferedWriter out1 = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path,true)));
		
		for(int i = 0; i < heur.sol.route_list.size(); i++) {
			for(int j = 0; j < heur.sol.route_list.get(i).droneNext.size(); j++) {
				if(heur.sol.route_list.get(i).droneNext.get(j) != null && heur.sol.route_list.get(i).dronePrev.get(j) != null)
					out1.write(heur.sol.route_list.get(i).droneNext.get(j)+" "+j+" "+heur.sol.route_list.get(i).dronePrev.get(j)+"\r\n");
			}
		}
		out1.close();
        
	}

	static void createFile(String path){
		File file = new File(path);
		boolean blnCreated = false;
		try
		{
		blnCreated = file.createNewFile();
		}
		catch(IOException ioe)
		{
		System.out.println("创建文档出现异常 :" + ioe);
		}
		
		System.out.println("文本文档" + file.getPath() + "是否被创建 : " + blnCreated);
	}
}

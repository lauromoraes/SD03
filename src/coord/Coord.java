package coord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class Coord {
	
	public Socket socket;
	public LinkedList<String> hostsAddress;
	public ArrayList<Integer> allSamples;
	public HashMap<String, Integer> mapProcess;
	public Integer nProcess = 0;

	public Coord() {
		hostsAddress = new LinkedList<String>();
		allSamples = new ArrayList<Integer>();
		mapProcess = new HashMap<String, Integer>();
	}
	
	public ArrayList<Integer> defineLimits(ArrayList<Integer> list, Integer n_process) {
		ArrayList<Integer> inter_limits = new ArrayList<Integer>(n_process-1);
		double s = (double)list.size() / n_process;
		for(int i=1; i < (n_process); i++) {
			inter_limits.add(list.get((int)(i*s)));
		}
		return inter_limits;
	}
	
	public void setProcess() {
		for(String s : hostsAddress) {
			String[] fields = s.split(":");
			String ip = fields[0];
			Integer port = Integer.parseInt(fields[1]);
			try {
				socket = new Socket(ip, port);
				ObjectInputStream input = new ObjectInputStream( socket.getInputStream() );
				int n = (Integer) input.readObject();
				
				mapProcess.put(ip, n);
				nProcess += n;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void defineBuckets() {
		for(String s : hostsAddress) {
			String[] fields = s.split(":");
			String ip = fields[0];
			Integer port = Integer.parseInt(fields[1]);
			try {
				socket = new Socket(ip, port);
				ObjectInputStream input = new ObjectInputStream( socket.getInputStream() );
				
				ArrayList<Integer> samples = (ArrayList<Integer>) input.readObject();
				allSamples.addAll(samples);
				
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		Collections.sort(allSamples);
		ArrayList<Integer> limits = defineLimits(allSamples, nProcess);
		
		
	}

}

package coord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import op.OpCode;
import pipe.SocketPipe;

public class Coord extends Thread {
	
	public Socket socket;
	public ServerSocket ss;
	public boolean isFinished;
	public HashMap<String, Integer> mapProcess;
	public HashMap<Integer, String> mapBuckets;
	public HashMap<String, Boolean> recieved_samples;
	public ArrayList<Integer> allSamples;
	public Integer nProcess = 0;
	public SocketPipe socketPipe;
	public Listener listener;
	public Integer state;
	public Integer n_samples_recieved;
	public String address;

	public Coord() {
		state = 0;
		n_samples_recieved = 0;
	}
	
	public void setupCoord() {
		address = getIp();
		mapBuckets = new HashMap<>();
		allSamples = new ArrayList<Integer>();
		isFinished = false;
		socketPipe = new SocketPipe();
		mapProcess = new HashMap<String, Integer>();
		recieved_samples = new HashMap<String, Boolean>();
		try {
			ss = new ServerSocket(6969);
		} catch (IOException e) {
			e.printStackTrace();
		}
		listener = new Listener(ss, socketPipe);
	}
	
	public String getIp() {
		try {
			Enumeration<NetworkInterface> i = NetworkInterface.getNetworkInterfaces();
			while(i.hasMoreElements()) {
				NetworkInterface n = i.nextElement();
				Enumeration<InetAddress> e = n.getInetAddresses();
				while(e.hasMoreElements()) {
					InetAddress a = e.nextElement();
					if(a instanceof Inet4Address && !a.getHostAddress().startsWith("127")) {
						return a.getHostAddress();
						
					}
				}
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	public void start_coord() {
		listener.start();
	}
	
	public ArrayList<Integer> defineLimits(ArrayList<Integer> list, Integer n_process) {
		ArrayList<Integer> inter_limits = new ArrayList<Integer>(n_process-1);
		double s = (double)list.size() / n_process;
		for(int i=1; i < (n_process); i++) {
			inter_limits.add(list.get((int)(i*s)));
		}
		return inter_limits;
	}
	
	@SuppressWarnings("unchecked")
	public void defineBuckets() {
		for(String s : mapProcess.keySet()) {
			String[] fields = s.split(":");
			String ip = fields[0];
			Integer port = Integer.parseInt(fields[1]);
			try {
				socket = new Socket(ip, port);
				ObjectInputStream input = new ObjectInputStream( socket.getInputStream() );
				
				nProcess += (Integer) input.readObject();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		for(String s : mapProcess.keySet()) {
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

	public void action(Socket s) {
		try {
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			try {
				Object[] o = (Object[])in.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void mainLoop() {
		Scanner scanner = new Scanner(System.in);
		while(!isFinished) {
			String line = scanner.nextLine();

			if(line.matches("(end|quit|exit)")) {
				System.out.println("Saindo...");
				listener.setFinished();
				isFinished = true;
				continue;
			} else if(line.matches("((\\d){1,3}\\.){3}(\\d){1,3}:(\\d){4,5}")) {
				System.out.println("connectando a " + line);
				continue;
			} else if(line.matches("list")){
				System.out.println("hosts");
				for(String s : mapProcess.keySet()) {
					System.out.println("\t"+s+" >> " + mapProcess.get(s));
				}
			} else if(line.matches("pipe")){
				System.out.println("pipes");
			} else if(line.matches("state")){
				System.out.println("stado: " + state);
			} else if(line.matches("start")){
				state = 1;
				System.out.println("stado mudando para: " + state);
				start_samples();
			} else {
				System.err.println("Invalid Command.");
			}
		}
		scanner.close();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
//		System.out.println("state: " + state);
//		System.out.println("isFinished: " + isFinished);

		while(!isFinished) {
//			System.out.println("loop");
			Socket s = socketPipe.read_buffer();
//			System.out.println(s + " - " + state);
			switch (state) {
			case 0:
				try {
					ObjectInputStream in = new ObjectInputStream(s.getInputStream());
					Object[] o = (Object[])in.readObject();
					in.close();
//					System.out.println("register");
					if((OpCode)o[0] == OpCode.REGISTER) {
						String host_address = (String)o[1];
						Integer process = (Integer)o[2];
						System.out.println("host register: " + host_address + " - " + process);
						mapProcess.put(host_address, process);
						recieved_samples.put(host_address, false);
					} else {
						System.out.println("Invalid OpCode: " + o[0]);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				break;
			
			case 1:
				try {
					ObjectInputStream in = new ObjectInputStream(s.getInputStream());
					Object[] o = (Object[])in.readObject();
					in.close();
					if((OpCode)o[0] == OpCode.SEND_SAMPLE) {
						System.out.println("recebendo amostras de: " + (String)o[1]);
						allSamples.addAll((ArrayList<Integer>)o[2]);
						recieved_samples.put((String)o[1], true);
						n_samples_recieved++;
						if(n_samples_recieved == recieved_samples.size()) {
							System.out.println(allSamples);
						}
						System.out.println("samples: " + allSamples);
						System.out.println("amostras recebidas de: " + (String)o[1]);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				break;

			default:
				System.err.println("Invalid Code.");
				break;
			}
		}
	}
	
	public void start_samples() {
		for(String host_address : mapProcess.keySet()) {
			String[] fields = host_address.split(":");
			String ip = fields[0];
			Integer port = Integer.parseInt(fields[1]);
			try {
				System.out.println("enviando requisicao para: " + host_address);
				Socket s = new Socket(ip, port);
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				Object[] o = {OpCode.START_SAMPLE, this.address, null};
				out.writeObject(o);
				out.close();
				s.close();
				System.out.println("requisicao enviada para: " + host_address);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}

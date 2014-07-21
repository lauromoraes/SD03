package host;

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
import java.util.Enumeration;
import java.util.Scanner;

import op.OpCode;
import pipe.SocketPipe;
import coord.Listener;
import freader.FReader;

public class Host extends Thread {
	
	public Socket socket;
	public ServerSocket ss;
	public boolean isFinished;
	public Integer state;
	public FReader reader;
	public double sampleRate;
	public String coord_address;
	public Listener listener;
	public SocketPipe socketPipe;
	public String address;

	public Host() {
		socket = null;
		ss = null;
		isFinished = false;
		state = 0;
		sampleRate = 0.2;
		socketPipe = new SocketPipe();
		address = getIp() + ":" + 6969;
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
	
	public void setupHost(String address, String fPath) {
		reader = new FReader(fPath);
		try {
			ss = new ServerSocket(6969);
		} catch (IOException e) {
			e.printStackTrace();
		}
		coord_address = address;
		listener = new Listener(ss, socketPipe);
	}
	
	public void start_host() {
		first_communication();
		listener.start();
	}
	
	public void mainLoop() {
		while(!isFinished) {
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
				} else {
					System.err.println("Invalid Command.");
				}
			}
			scanner.close();
		}
	}
	
	public Integer getProcessors() {
		Integer n = null;
		n = Runtime.getRuntime().availableProcessors();
		return n;
	}
	
	public ArrayList<Integer> samples() {
		reader.sampling(sampleRate);
		ArrayList<Integer> s =  reader.getSamples();
		System.out.println("Samples " + s);
		return s;
	}
	
	@Override
	public void run() {
		while(!isFinished) {
			Socket s = socketPipe.read_buffer();
			switch (state) {
			case 0:
				try {
					ObjectInputStream in = new ObjectInputStream(s.getInputStream());
					Object[] o = (Object[])in.readObject();
					in.close();
					if((OpCode)o[0] == OpCode.START_SAMPLE) {
						String coord_address = (String)o[1];
						
						Socket s2 = new Socket(coord_address, 6969);
						ObjectOutputStream out = new ObjectOutputStream(s2.getOutputStream());
						Object[] response = {OpCode.SEND_SAMPLE, address, samples()};
						out.writeObject(response);
						s2.close();
						state = 1;
						System.out.println("Sended");
					} else {
						System.out.println("Invalid OpCode: " + o[0]);
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

	public void first_communication() {
		String[] fields = coord_address.split(":");
		String ip = fields[0];
		Integer port = Integer.parseInt(fields[1]);
		System.out.println("Connect to coord: " + coord_address);
		try {
			socket = new Socket(ip, port);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			Integer n = getProcessors();
			System.out.println("N " + n);
			Object[] o = {OpCode.REGISTER, address, n};
			out.writeObject(o);
			out.close();
			out = null;
			socket.close();
			socket = null;
			System.out.println("Connection done;");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void send_samples() {

		String[] fields = coord_address.split(":");
		String ip = fields[0];
		Integer port = Integer.parseInt(fields[1]);
		try {
			socket = new Socket(ip, port);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ArrayList<Integer> n = samples();
			Object[] o = {OpCode.SEND_SAMPLE, address, n};
			out.writeObject(o);
			out.close();
			out = null;
			socket.close();
			socket = null;
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

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
			Socket s = socketPipe.read_buffer();
			try {
				ObjectInputStream in = new ObjectInputStream(s.getInputStream());
				Object[] o = (Object[])in.readObject();
				if((OpCode)o[0] == OpCode.START_SAMPLE) {
					send_samples();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if(ss != null) {
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		return s;
	}
	
	@Override
	public void run() {
		mainLoop();
	}

	public void first_communication() {
		String[] fields = coord_address.split(":");
		String ip = fields[0];
		Integer port = Integer.parseInt(fields[1]);
		try {
			socket = new Socket(ip, port);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			Integer n = getProcessors();
			Object[] o = {OpCode.REGISTER, address, n};
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
	
	public void send_samples() {

		String[] fields = coord_address.split(":");
		String ip = fields[0];
		Integer port = Integer.parseInt(fields[1]);
		try {
			socket = new Socket(ip, port);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ArrayList<Integer> n = samples();
			Object[] o = {OpCode.SEND_SAMPLE, address, n};
			out.writeObject(n);
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

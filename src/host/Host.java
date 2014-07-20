package host;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import freader.FReader;

public class Host extends Thread {
	
	public Socket socket;
	public ServerSocket listener;
	public boolean isFinished;
	public Integer state;
	public FReader reader;
	public double sampleRate;

	public Host() {
		socket = null;
		listener = null;
		isFinished = false;
		state = 0;
		sampleRate = 0.2;
	}
	
	public void setupHost(String fPath) {
		reader = new FReader(fPath);
		try {
			listener = new ServerSocket(6969);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void action(Socket s) {
		switch (state) {
		case 0:
			try {
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				Integer n = getProcessors();
				out.writeInt(n);
				state = 1;
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		
		case 1:
			try {
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				out.writeObject( samples() );
				this.state = 2;
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;

		default:
			System.err.println("Invalid Code.");
			break;
		}
	}
	
	public void mainLoop() {
		while(!isFinished) {
			try {
				listener.setSoTimeout(2000);
				Socket s = listener.accept();
				action(s);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(listener != null) {
			try {
				listener.close();
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
	
	public Integer[] samples() {
		reader.sampling(sampleRate);
		Integer[] s =  reader.getSamples();
		return s;
	}
	
	@Override
	public void run() {
		
	}

}

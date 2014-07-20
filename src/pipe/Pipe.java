package pipe;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Pipe {
	
	public boolean isFinished;
	public Integer blockSize;
	public ConcurrentLinkedQueue<Integer> buffer;

	public Pipe(Integer blockSize) {
		this.isFinished = false;
		this.buffer = new ConcurrentLinkedQueue<Integer>();
		this.blockSize = blockSize;
	}
	
	public synchronized void setBlockSize(int size) {
		synchronized (blockSize) {
			this.blockSize = size;
		}
	}
	
	public synchronized void write(Integer d) {
		if(d != null) {
			buffer.offer(d);
			if(buffer.size() >= blockSize) {
				notify();
			}
		} else {
			System.err.println("Null data: fail to insert on buffer.");
		}
	}
	
	public synchronized void writeBlock(Integer[] d) {
		if(d != null) {
			for(Integer v : d) {
				buffer.offer(v);
			}
			if(buffer.size() >= blockSize) {
				notify();
			}
		} else {
			System.err.println("Null data: fail to insert on buffer.");
		}
	}
	
	public synchronized Integer[] readBlock() {
		Integer[] data = null;
		while(!is_finished()) {
			if(buffer.size() >= blockSize) {
				data = new Integer[blockSize];
				for(int i=0; i < blockSize; i++) {
					data[i] = buffer.poll();
				}
				break;
			} else {
				if(!is_finished()) {
					try {
						wait(2000);
						//System.out.println("checking...");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return data;
	}
	
	public synchronized int bufferSize() {
		int t = buffer.size();
		return t;
	}
	
	public synchronized boolean is_empty() {
		return buffer.isEmpty();
	}

	public synchronized boolean is_finished() {
		return isFinished;
	}

	public synchronized void set_finished() {
		isFinished = true;
		notifyAll();
	}
	
	public void printBuffer() {
		System.out.println("\nprintBuffer");
		System.out.println("-----------");
		for(Integer i : buffer) {
			System.out.println(i);
		}
	}

}

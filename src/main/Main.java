package main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import freader.FReader;

public class Main {
	
	public boolean isFinished = false;
	public FReader fReader;
	
	public Main() {
	}
	
	public void setupFile(String path) {
		fReader = new FReader(path);
	}
	
	public ArrayList<Integer> make(ArrayList<Integer> list, Integer n_process) {
		ArrayList<Integer> inter_limits = new ArrayList<Integer>(n_process-1);
		double s = (double)list.size() / n_process;
		for(int i =1; i < (n_process); i++) {
			inter_limits.add(list.get((int)(i*s)));
		}
		return inter_limits;
	}
	
	public void genBinFile(int nIntWords) {
		try {
			Random r = new Random(547789);
			File f = new File("input.bin");
			f.delete();
			f.createNewFile();
			RandomAccessFile rf = new RandomAccessFile(f, "rw");
			
			System.out.println("Generetated");
			System.out.println("-----------");
			for(int i = 0; i < nIntWords; i++) {
				int val = r.nextInt();
				rf.writeInt( val );
				System.out.println(val);
			}
			rf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Integer[] getSample(String path, double sampleRate) {
		boolean deb = true;
		Integer[] sample = null;
		
		fReader.sampling(sampleRate);
		sample = fReader.getSamples();
		
		if(deb) {
			System.out.println("\nAMOSTRAS\n--------------");
			for(Integer i : sample) {
				//System.out.println(i);
			}
		}
		
		return sample;
	}
	
	public Integer[] genIntVet(int size) {
		int seed = 123554;
		Integer[] vet = new Integer[size];
		Random r = new Random(seed);
		for(int i=0; i < size; i++) {
			vet[i] = r.nextInt();
		}
		return vet;
	}
	
	public void main_loop() {
		Scanner scanner = new Scanner(System.in);
		while(!isFinished) {
			String line = scanner.nextLine();
			if(line.matches("end") || line.matches("exit")) {
				//this.finish_app();
				continue;
			} else if(line.matches("((\\d){1,3}\\.){3}(\\d){1,3}:(\\d){4,5}")) {
				//this.register();
				continue;
			} else if(line.matches("list")){
				System.out.println("listing all hosts:");
					
			} else {
				System.err.println("Invalid Command.");
			}
		}
		scanner.close();
	}

	public static void main(String[] args) {
		String path = "D:\\workspace3\\SD3-00\\input.bin";
		
		
		System.out.println("Main");
		for(String a : args) {
			System.out.println("MAIN> " + a);
		}
		System.out.println("-------------");
		
		Main m = new Main();
		m.setupFile(path);
		m.fReader.setFilePath(path);
		m.fReader.setupBuff(4, 7);
		
		m.genBinFile(20);	// create bin file
		
		System.out.println("Blocks");
		System.out.println("-----------");
		m.fReader.readBlocksFromFile();
		
		m.getSample(path, 0.4);
		
		
//		Integer[] v1 = m.genIntVet(3);
//		Pipe p = new Pipe(5);
//		p.printBuffer();
//		p.writeBlock(v1);
//		p.printBuffer();
//		p.readBlock();
//		p.printBuffer();
		
		
		System.exit(0);
	}

}

package freader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class FReader extends Thread {
	
	public byte[] buff = null;
	public RandomAccessFile rFile = null;
	public String fPath;
	public double sampleRate = 0.01;
	public ArrayList<Integer> samples;

	public FReader(String fPath) {
		this.fPath = fPath;
		try {
			rFile = new RandomAccessFile(fPath, "rwd");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Integer> getSamples() {
		return this.samples;
	}
	
	public void setFilePath(String fPath) {
		this.fPath = fPath;
	}
	
	public void setupBuff(int sWord, int nWords) {
		this.buff = new byte[(sWord * nWords)];
	}
	
	public void sampling(double sampleRate) {
		try {
			this.sampleRate = sampleRate;
			long fSize = this.rFile.length();
			fSize = fSize / 4;
			int nSamples = Math.max(1, (int) (fSize * sampleRate) );
			samples = new ArrayList<Integer>(nSamples);
			
			double sFile;
			sFile = (double) fSize / nSamples;	// get range size
			//System.out.printf("fsize: %d\nnsamples: %d\nnsamples: %f\n", fSize, nSamples, sFile);
			
			for(int i=0; i < (nSamples); i++) {
				long fIndex = (long) (i * sFile);
				rFile.seek(fIndex * 4);
				int val = rFile.readInt();
				//System.out.printf("i: %d - val: %d\n", i , val);
				samples.add(val);
			}
			rFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readBlocksFromFile() {
		try {
			long pos = 0;
			long sBuff = this.buff.length;
			rFile.seek(0);
			while( rFile.read(buff) == sBuff ) {
				pos += sBuff;
				rFile.seek(pos);
				IntBuffer bf = ByteBuffer.wrap(buff).asIntBuffer();
				int[] v = new int[bf.remaining()];
				bf.get(v);
//				System.out.println(">>>");
//				for(int i : v) {
//					System.out.println(i);
//				}
				// TODO insert
			}
			if(pos < rFile.length()) {
				IntBuffer bf = ByteBuffer.wrap(buff).asIntBuffer();
				int index = (int)((rFile.length()-pos)/4);
				int[] v = new int[index];
				bf.get(v);
//				System.out.println(">>>");
//				for(int i : v) {
//					System.out.println(i);
//				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
	}

}

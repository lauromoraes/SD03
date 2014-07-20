package bucket;

import java.util.ArrayList;
import java.util.HashMap;

public class Bucket {
	
	HashMap<Integer, Object> buckets = null;
	ArrayList<Integer> infs;

	public Bucket() {
		this.buckets = new HashMap<Integer, Object>();
		this.infs = null;
	}
	
	public void defineIntervals() {
		
	}
	
	public void addToBucket(Integer val) {
		
	}
	
	public Integer findBucket(Integer val) {
		Integer index = null;
		int i = 0;
		while(val <= infs.get(i)) {
			i++;
		}
		index = infs.get(i-1);
		return index;
	}

}

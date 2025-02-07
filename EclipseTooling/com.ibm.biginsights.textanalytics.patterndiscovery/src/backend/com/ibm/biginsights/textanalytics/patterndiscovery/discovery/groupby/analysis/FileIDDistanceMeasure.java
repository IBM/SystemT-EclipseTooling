/*******************************************************************************
* Copyright IBM
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IntPair;

/**
 * 
 * 
 * Contains and ranks distance scores for sequence pairs.. Used as data object during correlation-measure computation and filtering.
 * 
 * Loads distance measures from a file into memory. Assumes a file with a id1,id2,distance
 * The constructor allows specifying a default value for unmentioned entries. If 
 * desired, the values are inverted by substracting them from the maximum value. 
 */
public class FileIDDistanceMeasure implements DistanceMeasure {


	
	private Map<IntPair, Double>distances  = new HashMap<IntPair, Double>();
	double defaultValue;

	public FileIDDistanceMeasure(InputStream in, double defaultValue, boolean invert) throws NumberFormatException, IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(in,GroupByNewProcessor.ENCODING)); 

		while(br.ready()){
			try {
				String[] line = br.readLine().split(",");
				int d1v = Integer.parseInt(line[0]);
				int d2v = Integer.parseInt(line[1]);
				int small = Math.min(d1v, d2v);
				int big = Math.max(d1v, d2v);
				distances.put(new IntPair(small,big),new Double(Double.parseDouble(line[2])));
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		if(invert){
			double maxValue = maximumDistance();
			for (IntPair key : distances.keySet()) {
				distances.put(key, maxValue-distances.get(key));
			}
		}
		br.close();
	}
	/* (non-Javadoc)
	 * @see com.ibm.avatar.discovery.groupby.analysis.DistanceMeasure#distance(com.ibm.avatar.discovery.groupby.analysis.Datapoint, com.ibm.avatar.discovery.groupby.analysis.Datapoint)
	 */
	//@Override
	public double distance(Datapoint d1, Datapoint d2) {
		int d1v = ((Integer)d1.getContent()).intValue();
		int d2v = ((Integer)d2.getContent()).intValue();
		return distance(d1v, d2v);
	}
	public double distance(int d1v,int d2v){
		int small = Math.min(d1v, d2v);
		int big = Math.max(d1v, d2v);
		Double delta = distances.get(new IntPair(small,big));
		if(delta==null) return defaultValue;
		return delta;
		
	}
	


	/* (non-Javadoc)
	 * For simplicity, the median is estimated by the average of the non-default values
	 * @see com.ibm.avatar.discovery.groupby.analysis.DistanceMeasure#estimatedMedian()
	 */
	//@Override
	public double estimatedMedian() {
		double sum = 0;
		int count = 0; 
		for (Double v : distances.values()) {
			if(!Double.isNaN(v)&&!Double.isInfinite(v)){
				sum += v;
				count++;
			}
		}
		return sum/count;
	}
	//@Override
	public double maximumDistance() {
		double maxValue = defaultValue;
		for (double d : distances.values()) {
			if(!Double.isNaN(d)&&!Double.isInfinite(d)){
				maxValue = Math.max(d, maxValue);
			}
		}
		return maxValue;
	}
	//@Override
	public double minimumDistance() {
		double minValue = defaultValue;
		for (double d : distances.values()) {
			if(!Double.isNaN(d)&&!Double.isInfinite(d)){
				minValue = Math.min(d, minValue);
			}
		}
		return minValue;
	}
	@SuppressWarnings("unchecked")
	public List<IntPair> firstK(int k, boolean desc) {
			SortedSet<IntPair> sorted = new TreeSet<IntPair>(new DistanceComparator(desc));
			for (IntPair intPair2 : distances.keySet()) {
				if(distances.get(intPair2).isNaN()||distances.get(intPair2).isInfinite())
					continue;
				sorted.add(intPair2);
			}
			ArrayList<IntPair> result = new ArrayList<IntPair>();
			int added = 0;
			for (IntPair intPair : sorted) {
				result.add(intPair);
				if(added==k) 
					break;
				added++;
			}
			return result;
	}
	@SuppressWarnings("unchecked")
	private class DistanceComparator implements Comparator{
		int factor = 1;
		public DistanceComparator(boolean desc){
			if(desc) factor = -1; else factor = 1;
		}
		//@Override
		public int compare(Object o1, Object o2) {
			IntPair p1 = (IntPair) o1;
			double v1 = distances.get(p1).doubleValue();
			if(Double.isNaN(v1)||Double.isInfinite(v1))return 1;
			IntPair p2 = (IntPair) o2;
			double v2 = distances.get(p2).doubleValue();
			if(Double.isNaN(v2)||Double.isInfinite(v2))return -1;
			int result = factor*(int)Math.round(Math.signum(v1-v2));
			if(result==0){
				//note: this will never return 0 because otherwise the results will be excluded from the set.
				//System.out.println("apparently equal: "+o1+" = "+o2);
				result = 1;
			}
			return result;
		}
		
	}

}

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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby;

public class OutputObject {



	private int ID;
	private String originalExtract;
	private String signature;
	private int count;
	private int newCount;
	private int base;
	private String uniqueID;
	
	public OutputObject(){
		uniqueID = "";
		count = 0;
		ID = 0;
		base = 1;
		originalExtract = null;
		signature = null;
	}
	
	public OutputObject(int count, int newCount, 
			int ID, String originalExtract, 
			String signature, String uniqueID){
		this.ID = ID;
		this.newCount = newCount;
		this.count = count;
		this.originalExtract = originalExtract;
		this.signature = signature;
		this.base = 1;		//default set to "base" identity
		this.uniqueID = uniqueID;
	}
	
	public String[] printLine(){
		String[] line = {Integer.toString(this.count),
				Integer.toString(this.ID),
				Integer.toString(this.newCount),
				this.originalExtract,
				this.uniqueID,
				this.signature
		};
		return line;
	}
	
	public String[] printLineAdd(String add){
		String[] line = {Integer.toString(this.count),
				Integer.toString(this.ID),
				Integer.toString(this.newCount),
				this.originalExtract,
				this.uniqueID,
				add,
				this.signature
		};
		return line;
	}
	
	public int getCount(){
		return this.count;
	}
	
	public int getNewCount(){
		return this.newCount;
	}
	
	public int getID(){
		return this.ID;
	}
	
	public String getUniqueID(){
		return this.uniqueID;
	}
	
	public int getBase(){
		return this.base;
	}
	
	public String getOriginalExtract(){
		return this.originalExtract;
	}
	
	public String getSignature(){
		return this.signature;
	}
	
	public void setCount(int count){
		this.count = count;
	}
	
	public void setNewCount(int count){
		this.newCount = count;
	}
	
	public void setID(int ID){
		this.ID = ID;
	}
	
	public void setUniqueID(String ID){
		this.uniqueID = ID;
	}
	
	public void setBase(int base){
		this.base = base;
	}
	
	public void setOriginalExtract(String extract){
		this.originalExtract = extract;
	}
	
	public void setSignature(String sig){
		this.signature = sig;
	}
}

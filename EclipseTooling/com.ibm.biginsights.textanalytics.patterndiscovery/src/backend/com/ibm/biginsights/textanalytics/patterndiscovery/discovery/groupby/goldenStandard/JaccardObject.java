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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.goldenStandard;

public class JaccardObject {



	private int ID;
	private String originalExtract;
	private String signature;
	private int count;
	private String[] entities;
	private String entity1;
	private String entity2;
	private String uniqueID;
	private String isRelated;
	private String snippet;
	
	public JaccardObject(){
		uniqueID = "";
		count = 0;
		ID = 0;
		entity1 = null;
		entity2 = null;
		originalExtract = null;
		signature = null;
	}
	
	public JaccardObject(int count, 
			int ID, String originalExtract, 
			String uniqueID,String signature, String[] entities, 
			String related, String snippet){
		this.ID = ID;
		this.count = count;
		this.originalExtract = originalExtract;
		this.signature = signature;
		this.uniqueID = uniqueID;
		this.isRelated = related;
		this.snippet = snippet;
		this.entities = entities;
	}
	
	public String[] printLine(){
		String[] line = {Integer.toString(this.count),
				Integer.toString(this.ID),
				this.originalExtract,
				this.uniqueID,
				this.signature,
				this.entity1
		};
		return line;
	}
	
	public String[] printLineAdd(String add){
		String[] line = {Integer.toString(this.count),
				Integer.toString(this.ID),
				this.originalExtract,
				this.uniqueID,
				add,
				this.signature,
				this.entity1
		};
		return line;
	}
	
	public String[] getEntities(){
		return this.entities;
	}
	
	public void setEntities(String[] ent){
		this.entities = ent;
	}
	
	public int getCount(){
		return this.count;
	}
	
	public int getID(){
		return this.ID;
	}
	
	public String getUniqueID(){
		return this.uniqueID;
	}
	
	public String getEntity1(){
		return this.entity1;
	}
	
	public String getEntity2(){
		return this.entity2;
	}
	
	public String getRelated(){
		return this.isRelated;
	}
	
	public String getSnippet(){
		return this.snippet;
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
	
	public void setID(int ID){
		this.ID = ID;
	}
	
	public void setUniqueID(String ID){
		this.uniqueID = ID;
	}
	
	public void setEntity1(String entity1){
		this.entity1 = entity1;
	}
	
	public void setEntity2(String entity2){
		this.entity2 = entity2;
	}
	
	public void setRelated(String related){
		this.isRelated = related;
	}
	
	public void setSnippet(String snippet){
		this.snippet = snippet;
	}
	
	public void setOriginalExtract(String extract){
		this.originalExtract = extract;
	}
	
	public void setSignature(String sig){
		this.signature = sig;
	}
}

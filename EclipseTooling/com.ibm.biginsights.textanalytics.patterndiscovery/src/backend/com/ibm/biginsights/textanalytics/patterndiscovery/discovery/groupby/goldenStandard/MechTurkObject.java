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

import java.util.HashMap;

public class MechTurkObject {



	private String uniqueID;
	private String entity1;
	private String text;
	private int docid;
	private String entity2;
	private HashMap<String, Double[]> answerKey;
	private String isRelated;
	
	public MechTurkObject(){
		uniqueID = "";
		docid = 0;
		entity1 = "";
		entity2 = "";
		text = "";
		isRelated = "";
		answerKey = new HashMap<String, Double[]>();
	}
	
	public MechTurkObject(String uniqueID, int docid, String entity2, 
			String entity1, String text, HashMap<String, Double[]> ans){
		this.entity1 = entity1;
		this.entity2 = entity2;
		this.docid = docid;
		this.text = text;
		this.uniqueID = uniqueID;
		this.answerKey = ans;
	}
	
	public String[] printLine(){
		String[] line = {
				this.uniqueID,
				Integer.toString(this.docid),
				this.entity1,
				this.entity2,
				this.text,
		};
		return line;
	}
	
	public String[] printLineAdd(String add){
		String[] line = {
				this.uniqueID,
				Integer.toString(this.docid),
				this.entity1,
				this.entity2,
				this.text,
				add,
		};
		return line;
	}
	
	public int getDocID(){
		return this.docid;
	}
	
	public String getEntity2(){
		return this.entity2;
	}
	
	public String getEntity1(){
		return this.entity1;
	}
	
	public String getUniqueID(){
		return this.uniqueID;
	}
	
	public String getText(){
		return this.text;
	}
	
	public String getRelated(){
		return this.isRelated;
	}
		
	public HashMap<String, Double[]> getAnswers(){
		return answerKey;
	}
	
	public void setDocID(int docid){
		this.docid = docid;
	}
	
	public void setEntity2(String entity2){
		this.entity2 = entity2;
	}
	
	public void setEntity1(String entity1){
		this.entity1 = entity1;
	}
	
	public void setUniqueID(String ID){
		this.uniqueID = ID;
	}
	
	public void setText(String extract){
		this.text = extract;
	}
	
	public void setAnswer(HashMap<String, Double[]> ans){
		this.answerKey = ans;
	}
	
	public void setRelated(String related){
		this.isRelated = related;
	}
}

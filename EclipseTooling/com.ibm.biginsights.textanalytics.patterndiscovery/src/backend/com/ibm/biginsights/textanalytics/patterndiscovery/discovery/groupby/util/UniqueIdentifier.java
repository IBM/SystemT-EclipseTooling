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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util;

import java.util.ArrayList;

import com.ibm.avatar.algebra.datamodel.Span;

public class UniqueIdentifier {


	
	Span GroupingSpan = null;
	ArrayList<Span> entitiesSpan = null;
	int counter = 0;
	String documentID = null;

	public UniqueIdentifier(String docID, int count){
		
		this.counter = count;
		this.documentID = docID;
		
	}
	
	public void setGroupingSpan(Span groupSpan){
		this.GroupingSpan = groupSpan;
	}
	
	public void addEntitiesSpan(Span entitySpan){
		if (entitiesSpan == null){
			entitiesSpan = new ArrayList<Span>();
		}
		entitiesSpan.add(entitySpan);
	}
	
	// Get Span for ID - either from entities if exists or from
	// context String if not
	// if only one entity - use the context as the unique ID - not
	// the entity

	public String getUniqueID(){
		String ID = this.documentID+"-";
		if (entitiesSpan == null){
			ID = ID + this.GroupingSpan.getBegin() + ":" + this.GroupingSpan.getEnd()+ "#"+ this.counter;
		} else {
			for (Span entity: entitiesSpan){
				ID = entity.getBegin() + ":" + entity.getEnd()+":";
			}
			ID = ID.substring(0,ID.length()-1) + "#"+this.counter;
		}
		ID = "\"" + ID + "\"";
		return ID;
	}
	
	
}

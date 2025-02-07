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
package com.ibm.biginsights.textanalytics.goldstandard.model;

import java.io.Serializable;

import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 * AnnotationType represents a category of annotations. The user creates them through the Gold Standard configuration dialog.
 * Portions of text in an input document are labeled with an annotation type.
 * 
 * 
 *
 */
public class AnnotationType implements Cloneable, Serializable{



	private static final long serialVersionUID = 7080468081286025680L;

	private String fieldName;
	private String viewName;
	private boolean enabled;
	private String shortcutKey;
//	private RGB color;
//	private static final RGB black = new RGB(0, 0, 0);
	
	
	private AnnotationType() {
		super();
	}


	public AnnotationType(String viewName, String fieldName, boolean enabled, String shortcutKey) {
		super();
		this.fieldName = fieldName;
		this.viewName = viewName;
		this.enabled = enabled;
		setShortcutKey(shortcutKey);
		//this.color = color;
	}


	public String getFieldName() {
		return fieldName;
	}

	
	public String getViewName() {
		return viewName;
	}


	public boolean isEnabled() {
		return enabled;
	}

	public String getShortcutKey() {
		return shortcutKey;
	}

//	public RGB getColor() {
//		return color;
//	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append(viewName);
		builder.append(",");
		builder.append(fieldName);
		builder.append(",");
		builder.append(enabled);
		builder.append(",");
		builder.append(shortcutKey);
		//builder.append(",");
		//builder.append(color.red + "-"+color.green+"-"+color.blue);
		builder.append("}");
		return builder.toString();
	}
	
	public static AnnotationType toObject(String annotationType){
		String[] attributes = annotationType.split(",");
		if(attributes == null || attributes.length != 4){
			return null;
		}
		
		AnnotationType object = new AnnotationType();
		object.viewName = attributes[0].trim();
		object.fieldName = attributes[1].trim();
		String enabled = attributes[2].trim();
		String key = attributes[3].trim();
		//String color = attributes[4].trim();
		
		if("true".equals(enabled)){
			object.enabled = true;
		}else{
			object.enabled = false;
		}
		
		object.setShortcutKey(key);

//		try{
//			String[] rgb = color.split("-");
//			if(rgb == null || rgb.length != 3){
//				object.color = black;
//			}else{
//				object.color = new RGB(Integer.parseInt(rgb[0]),
//										Integer.parseInt(rgb[1]),
//										Integer.parseInt(rgb[2]));
//			}
//		}catch(Exception e){
//			object.color = black;
//		}
		
		return object;
	}


	public void setFieldName(String name) {
		this.fieldName = name;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}


	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	public void setShortcutKey(String shortcutKey) {
		if(StringUtils.isEmpty(shortcutKey.trim())){
			this.shortcutKey = " ";
		}else{
			this.shortcutKey = shortcutKey.trim();
		}
	}


//	public void setColor(RGB color) {
//		this.color = color;
//	}


	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AnnotationType(this.getViewName(),
				this.getFieldName(),				
				this.isEnabled(), 
				this.getShortcutKey()); 
				//new RGB(this.getColor().red, this.getColor().green, this.getColor().blue));
	}
}

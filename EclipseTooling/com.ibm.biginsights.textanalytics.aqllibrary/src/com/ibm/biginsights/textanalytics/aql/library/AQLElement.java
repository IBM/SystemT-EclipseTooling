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
package com.ibm.biginsights.textanalytics.aql.library;

import java.util.ArrayList;


/**
 *  Babbar
 * 
 */
public class AQLElement {


	String type;
	String name; // qualified..
	String unQualifiedName; // unqualified name..
	public ArrayList<String> dependsOnElement;
	int beginOffset;
	int endOffset;
	int beginLineNumber;
	int endLineNumber;
	String filePath;
	String comment;
	String moduleName; // to hold the module it belongs to...
	String aliasName;  // to hold the alias names for views functions etc that are imported..
	String fromModuleName; // to hold the name of the module where it is accessed from..
	
	public ArrayList<String> getDependentElements()
	{
		return this.dependsOnElement;
	}
	public String getName()
	{
		return this.name;
	}
	public String getType()
	{
		return this.type;
	}
	public int getBeginOffset()
	{
		return this.beginOffset;
	}
	public int getEndOffset()
	{
		return this.endOffset;
	}
	public int getBeginLine()
	{
		return this.beginLineNumber;
	}
	public int getEndLine()
	{
		return this.endLineNumber;
	}
	
	public String getFilePath()
	{
		return this.filePath;
	}
	public String getComment()
	{
		return this.comment;
	}
  public String getModuleName ()
  {
    return moduleName;
  }
  public void setModuleName (String moduleName)
  {
    this.moduleName = moduleName;
  }
  public String getAliasName ()
  {
    return aliasName;
  }
  public void setAliasName (String aliasName)
  {
    this.aliasName = aliasName;
  }
  public String getUnQualifiedName ()
  {
    return unQualifiedName;
  }
  public void setUnQualifiedName (String unQualifiedName)
  {
    this.unQualifiedName = unQualifiedName;
  }
  public String getFromModuleName ()
  {
    return fromModuleName;
  }
  public void setFromModuleName (String fromModuleName)
  {
    this.fromModuleName = fromModuleName;
  }
}

class SubElement
{
	String type;
	int beginOffset;
	int endOffset;
	int beginLineNumber;
	int endLineNumber;
	
	public String getType()
	{
		return this.type;
	}
	public int getBeginOffset()
	{
		return this.beginOffset;
	}
	public int getEndOffset()
	{
		return this.endOffset;
	}
	public int getBeginLine()
	{
		return this.beginLineNumber;
	}
	public int getEndLine()
	{
		return this.endLineNumber;
	}
}

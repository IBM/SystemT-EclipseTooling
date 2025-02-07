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
package com.ibm.biginsights.textanalytics.workflow.plan.serialize;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "example")
public class ExampleModel {



	String text;
	String filePath;
	String fileLabel;
	int offset;
	int length;

	public ExampleModel() {
		this("", "", "", 0, 0);
	}

	public ExampleModel(String text, String filePath, String fileLabel, int offset, int length) {
		super();
		this.text = text;
		this.filePath = filePath;
		this.fileLabel = fileLabel;
		this.offset = offset;
		this.length = length;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFilePath() {
	  return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileLabel() {
	  // Defect 26090: Sometimes "/" is part of the fileLabel (filename) and causes problem in Windows env.
    if (System.getProperty ("os.name").contains ("Windows") && fileLabel.contains ("/"))
      return fileLabel.replace ("/", "\\");
    else
      return fileLabel;
	}

	public void setFileLabel(String fileLabel) {
		this.fileLabel = fileLabel;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

}

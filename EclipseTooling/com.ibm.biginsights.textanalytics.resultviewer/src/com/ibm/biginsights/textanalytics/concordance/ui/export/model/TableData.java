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

package com.ibm.biginsights.textanalytics.concordance.ui.export.model;

/**
 * Represents a Span from Annotation Explorer Result
 */
public class TableData {



	private int start;
	private int end;
	private String text;

	public TableData(int start, int end, String text) {
		super();
		this.start = start;
		this.end = end;
		this.text = text;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		if (this.start == 0 && this.end == 0)
			return this.text;
		return this.text + " [" + this.start + " - " + this.end + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TableData) {
			TableData span = (TableData) o;
			return (this.start == span.start) && (this.end == span.end)
					&& this.text == span.text;
		}
		return false;
	}

}

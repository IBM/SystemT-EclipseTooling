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
package com.ibm.biginsights.textanalytics.patterndiscovery.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

public class StyleRanges {



	int length;
	String text;
	Map<Integer, StyleRange> ranges;
	boolean[] used;

	/**
	 * 
	 * @param text
	 *            the text that the style will be applied to. this is used only
	 *            predict the maximum length of the styles
	 */
	public StyleRanges(String text) {
		this.text = text;
		length = text.length();
		ranges = new HashMap<Integer, StyleRange>();
		used = new boolean[text.length()];
		for (int i = 0; i < length; i++) {
			used[i] = false;
		}
	}
	
	public static String backslash(String str){
		String ret = "";
		
		String specialCharacters = "[\\^$.|?*+()]{} ";
		
		for(Character c : str.toCharArray()){
			ret += (!specialCharacters.contains(c.toString())) ? c : "[\\W]+";
		}
		
		//System.err.println(String.format("Original String: %s  -- After backslash: %s", str, ret));
		
		return "\\b" + ret + "\\b";
	}

	public static Collection<StyleRange> getStyles(String text, String pattern,
			Color color, int fontStyle) {

		Pattern pat = Pattern.compile(pattern);
		Matcher mat = pat.matcher(text);

		ArrayList<StyleRange> styles = new ArrayList<StyleRange>();
		String str = "";

		while (mat.find()) {
			str = mat.group();
			StyleRange style = new StyleRange();

			style.start = mat.start(0);
			style.length = str.length();

			if (color != null)
				style.background = color;

			style.fontStyle = fontStyle;

			styles.add(style);
		}

		return styles;
	}

	/**
	 * 
	 * @return returns an array of the styles ordered by their position in the
	 *         original text
	 */
	public StyleRange[] getStyles() {
		StyleRange[] styles = new StyleRange[ranges.keySet().size()];
		int counter = 0;
		for (int i = 0; i < length; i++) {
			if (ranges.containsKey(i)) {
				styles[counter] = ranges.get(i);
				counter++;
			}
		}

		return styles;
	}

	/**
	 * return a string representation of this object. basically it print the
	 * start and end position over the text for all the styles
	 */
	public String toString() {
		String ret = "";

		for (int i = 0; i < length; i++) {
			if (ranges.containsKey(i)) {
				StyleRange range = ranges.get(i);
				ret += String.format("Range start:%d -- end:%d \n",
						range.start, range.start + range.length);
			}
		}

		return ret;
	}

	/**
	 * method to add a new Style Range to the list of styles. it will check for
	 * the range of this new style and will apply it to the areas that are not
	 * already being used. So, if there is another style for this position, then
	 * it will be ignored.
	 * 
	 * @param range
	 *            the StyleRange to be added
	 */
	public void addStyleRange(StyleRange range) {
		for (TwoInteger r : getRangesAvailable(range.start, range.start
				+ range.length)) {
			StyleRange _r = new StyleRange(r.start, r.length, range.foreground,
					range.background, range.fontStyle);
			ranges.put(_r.start, _r);
			for (int i = r.start; i < r.end; i++) {
				used[i] = true;
			}
		}
	}

	/**
	 * adds a Collection of StyleRange objects. see @addStyleRange for more
	 * details of how each StyleRange is added.
	 * 
	 * @param styles
	 */
	public void addAll(Collection<StyleRange> styles) {
		for (StyleRange style : styles) {
			addStyleRange(style);
		}
	}

	/**
	 * checks if a given position in the text has a style applied to it.
	 * 
	 * @param start
	 * @return
	 */
	private boolean isPositionUsed(int start) {
		if (start >= length)
			return false;
		return used[start];
	}

	/**
	 * given a range, this method will calculate the positions in this range
	 * that are available to be used for a new StyleRange.
	 * 
	 * @param start
	 *            first position of the rage
	 * @param end
	 *            last position used by the range
	 * @return a list of ranges that are available inside the provided range.
	 */
	private ArrayList<TwoInteger> getRangesAvailable(int start, int end) {
		ArrayList<TwoInteger> ret = new ArrayList<StyleRanges.TwoInteger>();

		if (start > end || start < 0 || end > length) {
			return ret;
		}

		for (int i = start; i <= end; i++) {
			if (!isPositionUsed(i)) {
				int _start = i;
				int _end = i;
				int j;
				for (j = i + 1; j <= end; j++) {
					if (!isPositionUsed(j)) {
						_end++;
						if (j == end) {
							ret.add(new TwoInteger(_start, _end));
							break;
						}
					} else {
						ret.add(new TwoInteger(_start, _end));
						break;
					}
				}
				i = j;
			}
		}

		return ret;
	}

	/**
	 * class that allows to store the start, end, and length of a range into
	 * this object
	 * 
	 * 
	 * 
	 */
	class TwoInteger {
		int start;
		int end;
		int length;

		/**
		 * Constructor that initialize the object from a given start and end
		 * positions
		 * 
		 * @param start
		 * @param end
		 */
		public TwoInteger(int start, int end) {
			this.start = start;
			this.end = end;
			length = end - start;
		}
	}

}

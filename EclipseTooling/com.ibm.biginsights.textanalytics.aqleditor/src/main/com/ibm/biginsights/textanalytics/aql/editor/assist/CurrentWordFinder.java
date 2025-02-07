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
package com.ibm.biginsights.textanalytics.aql.editor.assist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;

import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLPartitionScanner;

/**
 *  
 *  Babbar
 * 
 */
public class CurrentWordFinder {



  /**
   * @param document
   * @param offset
   * @return
   */
	public static IRegion findWord(IDocument document, int offset) {

		int start= -2;
		int end= -1;

		try {
			int pos = offset-1;
			char c;
			
			while (pos >= 0) {
				c= document.getChar(pos);
				if (!Character.isLetterOrDigit(c))
				{
					if((c == '/') || (c == '.') || (c == '_') || (c == '-'))
					{
						--pos;
						continue;
					}						
					else
						break;
				}
				--pos;
			}
			start= pos;

			pos= offset;
			int length= document.getLength();

			while (pos < length) {
				c= document.getChar(pos);
				if (!Character.isLetterOrDigit(c))
				{
					if((c == '/') || (c == '.') || (c == '_') || (c == '-'))
					{
						++pos;
						continue;
					}
					else
						break;
				}
				++pos;
			}
			end= pos;

		} catch (BadLocationException x) {
		}

		if (start >= -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return null;
	}

	 /**
	 * Returns the IRegion for the offset ignoring / _ - as delimiters
	 * @param document
	 * @param offset
	 * @return
	 */
	public static IRegion findCurrWord(IDocument document, int offset) {

	    int start= -2;
	    int end= -1;

	    try {
	      int pos = offset-1;
	      char c;
	      
	      while (pos >= 0) {
	        c= document.getChar(pos);
	        if (!Character.isLetterOrDigit(c))
	        {
	          if((c == '/') || (c == '_') || (c == '-'))
	          {
	            --pos;
	            continue;
	          }           
	          else
	            break;
	        }
	        --pos;
	      }
	      start= pos;

	      pos= offset;
	      int length= document.getLength();

	      while (pos < length) {
	        c= document.getChar(pos);
	        if (!Character.isLetterOrDigit(c))
	        {
	          if((c == '/') || (c == '_') || (c == '-'))
	          {
	            ++pos;
	            continue;
	          }
	          else
	            break;
	        }
	        ++pos;
	      }
	      end= pos;

	    } catch (BadLocationException x) {
	    }

	    if (start >= -1 && end > -1) {
	      if (start == offset && end == offset)
	        return new Region(offset, 0);
	      else if (start == offset)
	        return new Region(start, end - start);
	      else
	        return new Region(start + 1, end - start - 1);
	    }

	    return null;
	  }
	public static IRegion findLastWord(IDocument document, int offset) {

		int start= -2;
		int end= -1;
		boolean wordstart = false;
		boolean colon = false;
		try {
			int pos = offset-1;
			char c;
			
			while (pos >= 0) {
				c= document.getChar(pos);
				
				ITypedRegion tRegion= document.getPartition(pos);
				//skipping comments to get the last word
				if (tRegion != null && (tRegion.getType() == AQLPartitionScanner.AQL_COMMENT)) 
				{
					if(!wordstart)
					{
						--pos;
						continue;
					}
					else
					{
						//end = pos + 1;
						break;
					}
				}
				
				
				if(!wordstart)
				{
				if(((c == '\n') || (c == '\t') || (c == '\r') || (c == ' ')) && (!wordstart))
				{
					--pos;
					continue;
				}
				else if(c == ';')
				{
					colon = true;
					wordstart = true;
					end = pos + 1;
				}
				else if(Character.isLetterOrDigit(c))
				{
					wordstart = true;
					end = pos + 1;
				}
				}
				
				if((wordstart) && (!Character.isLetterOrDigit(c)))
				{
					if((c == '/') || (c == '.') || (c == '_') || (c == '-'))
					{
						--pos;
						continue;
					}						
					else
					{
						if(colon)
						--pos;
						break;
					}
				}
				--pos;
			}
			start= pos;
		} catch (BadLocationException x) {
			x.printStackTrace();
		}

		if (start >= -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return null;
	}
}




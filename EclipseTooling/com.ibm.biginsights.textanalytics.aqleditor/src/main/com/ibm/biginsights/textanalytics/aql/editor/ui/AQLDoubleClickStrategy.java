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
package com.ibm.biginsights.textanalytics.aql.editor.ui;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * Determines AQL Editor's behaviour when a double click is registered
 * on text in the editor.
 */
public class AQLDoubleClickStrategy implements ITextDoubleClickStrategy {


  
  protected ITextViewer fText;

  @Override
  public void doubleClicked(ITextViewer part) {
    int pos = part.getSelectedRange().x;

    if (pos < 0) {
      return;
    }

    this.fText = part;

    if (!selectWordInDoubleQuotes(pos)) {
      selectWord(pos);
    }
  }

  /**
   * If double click is within a word enclosed in double quotes, it selects
   * and highlights the whole word (even if it contains spaces).
   * @param caretPos Cursor position
   * @return true if a word within double quotes was detected and highlighted, else false
   */
  protected boolean selectWordInDoubleQuotes(int caretPos) {
    IDocument doc = this.fText.getDocument();
    int startPos, endPos;

    try {
      int pos = caretPos;
      char c = ' ';

      //Step: 1 - Go backwards from cursor position looking for double quote character in same line. Note position
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (c == '\\') {
          pos -= 2;
          continue;
        }
        /**
         * Do not use Character.LINE_SEPARATOR for identifying new line. It is not meant to denote new line but rather
         * the character category for line separator in unicode. Instead look for matches to any possible newline
         * character - \r\n (windows) or \n (linux).
         */
        if (c == '\r' || c == '\n' || c == '\"') { 
          break;
        }
        --pos;
      }

      if (c != '\"') {
        return false; //if no preceding double quote character is found, do nothing and return false.
      }

      startPos = pos;

      pos = caretPos;
      int length = doc.getLength();
      c = ' ';

      //Step 2 - If preceding double quote character was found, go forward from cursor position, looking for ending double quote.
      //Note position.
      while (pos < length) {
        c = doc.getChar(pos);
        if (c == '\r' || c == '\n' || c == '\"') {
          break;
        }
        ++pos;
      }
      if (c != '\"') {
        return false; //if ending double quote character is not found, do nothing and return false.
      }

      endPos = pos;

      int offset = startPos + 1;
      int len = endPos - offset;
      this.fText.setSelectedRange(offset, len); //Step 3 - after range is identified, select and highlight it.
      return true;
    } catch (BadLocationException x) {
      //TODO: add error handling here.
    }

    return false;
  }

  protected boolean selectWord(int caretPos) {

    IDocument doc = this.fText.getDocument();
    int startPos, endPos;

    try {

      int pos = caretPos;
      char c;

      while (pos >= 0) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        --pos;
      }

      startPos = pos;

      pos = caretPos;
      int length = doc.getLength();

      while (pos < length) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        ++pos;
      }

      endPos = pos;
      selectRange(startPos, endPos);
      return true;

    } catch (BadLocationException x) {
      //TODO: add error handling here
    }

    return false;
  }

  private void selectRange(int startPos, int stopPos) {
    int offset = startPos + 1;
    int length = stopPos - offset;
    this.fText.setSelectedRange(offset, length);
  }
}

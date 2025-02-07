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

import static com.ibm.biginsights.textanalytics.aql.editor.ui.AQLStatementScanner.unwind;
import static org.eclipse.jface.text.rules.ICharacterScanner.EOF;
import static org.eclipse.jface.text.rules.Token.UNDEFINED;

import java.util.Arrays;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

public class IdentifierRule implements IRule {


  
  private static final int[] punctuationChars = {'(', ')', ','};
  
  static {
    Arrays.sort(punctuationChars);
  }
  
  private final IToken success;
  
  public IdentifierRule(IToken successToken) {
    super();
    this.success = successToken;
  }

  @Override
  public IToken evaluate(ICharacterScanner scanner) {
    int readCount = 0;
    int c = scanner.read();
    ++readCount;
    if (c == EOF) {
      unwind(scanner, readCount);
      return UNDEFINED;
    }
    if (Character.isLetter(c)) {
      while (Character.isLetter(c) || Character.isDigit(c) || c == '_') {
        c = scanner.read();
        ++readCount;
      }
      return this.success;
    }
    if (Arrays.binarySearch(punctuationChars, c) >= 0) {
      return this.success;
    }
    unwind(scanner, readCount);
    return UNDEFINED;
  }

}

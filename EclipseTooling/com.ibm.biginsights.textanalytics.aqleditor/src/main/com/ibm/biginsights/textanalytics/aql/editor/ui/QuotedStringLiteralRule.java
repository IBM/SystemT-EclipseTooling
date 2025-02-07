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

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

/**
 * Rule for recognizing string literals.
 */
public class QuotedStringLiteralRule implements IRule {



  // The quotation marks for this rule. We use the rule for single quoted and double quoted
  // literals. Other quotation characters would also work.
  private final char quotMark;

  // The success token of this rule.
  private final IToken token;

  /**
   * 
   * @param quotMark
   *          Quotation mark character.
   * @param token
   *          Success token of rule.
   */
  public QuotedStringLiteralRule(char quotMark, IToken token) {
    super();
    this.quotMark = quotMark;
    this.token = token;
  }

  @Override
  public IToken evaluate(ICharacterScanner scanner) {
    // Read count is needed when we unwind the scanner in case of failure.
    int readCount = 0;
    char c = (char) scanner.read();
    ++readCount;
    // Must start with quotation mark
    if (c != this.quotMark) {
      // In the failure case, unwind the scanner before returning.
      unwind(scanner, readCount);
      return UNDEFINED;
    }
    // Check for escape sequences.  Backslash is hard-coded escape character.
    boolean escape = false;
    while (true) {
      c = (char) scanner.read();
      ++readCount;
      // If we're in an escape sequence (i.e., we've just seen a backslash).
      if (escape) {
        // EOF in the middle of a string means failure.
        if (c == EOF) {
          unwind(scanner, readCount);
          return UNDEFINED;
        }
        // For any character, we just continue.
        escape = false;
      } else {
        // We're not in an escape sequence.
        // If we see another quotation mark, we're done.
        if (c == this.quotMark) {
          return this.token;
        }
        // Distinguish special cases of backslash (escape character) and EOF.
        switch (c) {
        case '\\': {
          escape = true;
          break;
        }
        case (char) EOF: {
          unwind(scanner, readCount);
          return UNDEFINED;
        }
        default: {
          // do nothing
          break;
        }
        }
      }
    }
  }

}

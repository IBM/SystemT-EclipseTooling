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

import static org.eclipse.jface.text.rules.ICharacterScanner.EOF;
import static org.eclipse.jface.text.rules.Token.UNDEFINED;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;

public class AQLStatementPartitionRule implements IPredicateRule {


  
  private final IToken successToken;
  
  public AQLStatementPartitionRule(IToken token) {
    super();
    this.successToken = token;
  }

  @Override
  public IToken evaluate(ICharacterScanner scanner) {
    int c = scanner.read();
    if (c == EOF) {
      return UNDEFINED;
    }
    if (Character.isLowerCase(c)) {
      c = scanner.read();
      while (c != EOF) {
        if (c == ';') {
          return this.successToken;
        }
        c = scanner.read();
      }
    }
    return UNDEFINED;
  }

  @Override
  public IToken getSuccessToken() {
    return this.successToken;
  }

  @Override
  public IToken evaluate(ICharacterScanner scanner, boolean resume) {
    if (resume) {
      int c = scanner.read();
      while (c != EOF) {
        if (c == ';') {
          return this.successToken;
        }
        c = scanner.read();
      }
      return UNDEFINED;
    }
    return evaluate(scanner);
  }

}

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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;

import com.ibm.biginsights.textanalytics.aql.editor.syntax.AQLSyntaxElements;

public class AQLStatementScanner extends RuleBasedScanner {



  public AQLStatementScanner(ColorManager manager) {

    IToken defaultToken = new Token(new TextAttribute(manager.getColor(IAQLColorConstants.DEFAULT)));

    IToken stringToken = new Token(new TextAttribute(manager.getColor(IAQLColorConstants.STRING)));

    IToken regexToken = new Token(new TextAttribute(manager.getColor(IAQLColorConstants.REGEX)));

    IToken commentToken = new Token(new TextAttribute(
        manager.getColor(IAQLColorConstants.AQL_COMMENT)));

    IToken keywordToken = new Token(new TextAttribute(manager.getColor(IAQLColorConstants.KEYWORD),
        null, SWT.BOLD));

    IToken typeToken = new Token(new TextAttribute(manager.getColor(IAQLColorConstants.TYPE), null,
        SWT.BOLD));

    List<IRule> rl = new ArrayList<IRule>(10);
    // Keyword rule
    WordRule keywordRule = new WordRule(new KeywordWordDetector());
    for (String keyword : AQLSyntaxElements.KEYWORDS) {
      keywordRule.addWord(keyword, keywordToken);
    }
    rl.add(keywordRule);
    // Type rule
    WordRule typeRule = new WordRule(new TypeNameDetector());
    for (String type : AQLSyntaxElements.TYPES) {
      typeRule.addWord(type, typeToken);
    }
    rl.add(typeRule);
    // Add rule for regexes
    // rl.add(new SingleLineRule("\"", "\"", stringToken, '\\'));
    rl.add(new QuotedStringLiteralRule('/', regexToken));
    // Add rule for double quotes
    // rl.add(new SingleLineRule("\"", "\"", stringToken, '\\'));
    rl.add(new QuotedStringLiteralRule('"', stringToken));
    // Add a rule for single quotes
    // rl.add(new SingleLineRule("'", "'", stringToken, '\\'));
    rl.add(new QuotedStringLiteralRule('\'', stringToken));
    // Add single line comment rule (the escape character doesn't matter
    rl.add(new SingleLineRule("--", null, commentToken, '\\', true)); //$NON-NLS-1$
    // Multi-line comment rule
    rl.add(new MultiLineRule("/*", "*/", commentToken)); //$NON-NLS-1$ //$NON-NLS-2$
    // Add generic whitespace rule.
    rl.add(new WhitespaceRule(new AQLWhitespaceDetector()));
    // Default text rule
    rl.add(new IdentifierRule(defaultToken));

    IRule[] rules = new IRule[rl.size()];
    rl.toArray(rules);

    setRules(rules);
  }

  // Must unwind the scanner in failure case, otherwise passages are swallowed. Why doesn't the
  // scanner have an unread(int) method?  This is slightly absurd.
  public static void unwind(ICharacterScanner scanner, int readCount) {
    while (readCount > 0) {
      scanner.unread();
      --readCount;
    }
  }

}

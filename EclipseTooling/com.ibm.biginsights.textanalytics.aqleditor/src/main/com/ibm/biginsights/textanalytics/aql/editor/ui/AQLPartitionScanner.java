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

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class AQLPartitionScanner extends RuleBasedPartitionScanner {



  public final static String AQL_COMMENT = "__aql_comment"; //$NON-NLS-1$
  
  public static final String AQL_REGEX = "__aql_regex"; //$NON-NLS-1$
  
  public static final String AQL_VIEW_NAME = "__aql_view_name"; //$NON-NLS-1$

  public static final String AQL_STRING= "__aql_string"; //$NON-NLS-1$

  public AQLPartitionScanner() {

    IToken aqlComment = new Token(AQL_COMMENT);
    IToken aqlRegex = new Token(AQL_REGEX);
    IToken aqlString = new Token(AQL_STRING);

    List<IPredicateRule> rl = new ArrayList<IPredicateRule>(10);

    rl.add(new SingleLineRule("--", null, aqlComment, '\\', true)); //$NON-NLS-1$
    rl.add(new MultiLineRule("/*", "*/", aqlComment)); //$NON-NLS-1$ //$NON-NLS-2$
    rl.add(new SingleLineRule("/", "/", aqlRegex, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
    rl.add(new SingleLineRule("\"", "\"", aqlString, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
    rl.add(new SingleLineRule("'", "'", aqlString, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
  
    IPredicateRule[] rules = new IPredicateRule[rl.size()];
    rl.toArray(rules);
    setPredicateRules(rules);
  }
}

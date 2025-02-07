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
package com.ibm.biginsights.textanalytics.regex.learner.utils;

import java.util.ArrayList;

import com.ibm.biginsights.textanalytics.regex.learner.expression.AlternationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionLeaf;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionNode;
import com.ibm.biginsights.textanalytics.regex.learner.learner.utils.Converter;

/**
 * This class creates a regular expression with many parentheses, i.e. that allows to retrieve the
 * part of strings that are matched by any subgroups. The class is used to retrieve the parts of the
 * sample strings that are actually matched by a certain subgroup (when updating the regex derived
 * by RIGAAlignFirst). It also used by the GUI code (highlighting of a particular subexpression).
 * 
 * 
 * 
 */

public class GroupRegex {

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  // a "linear" traversal of all leafs of the regular expression
  private final Traversal traversal;

  // a regular expression representing the expression in traversal,
  // with many groups
  private String groupRegex;

  // the index of the current group --> to be able to retrieve a certain
  // subgroup
  private int currentGroupIndex;

  // information for each subgroup of the groupRegex --> is the group of this
  // index a leaf or a combination of multiple groups?
  private ArrayList<Boolean> isLeaf;

  // CONSTRUCTOR
  public GroupRegex(Traversal traversal) {
    this.traversal = traversal;
    this.isLeaf = new ArrayList<Boolean>();
    this.currentGroupIndex = 0;
    this.groupRegex = ""; //$NON-NLS-1$
  }

  /**
   * create a regular expression with parentheses around each group (for highlighting)
   * 
   * @param expression
   * @return String representing the regular expression
   */
  private String getGroupRegex2 (Expression expression)
  {
    final Expression current = this.traversal.getCurrentLeaf ();

    if (expression instanceof ExpressionLeaf) {
      this.isLeaf.add (true);
      int innerParentheses = 0;
      if (((ExpressionLeaf) expression).isSamplesAlternation ()) {
        if ((expression.getMinimum () == 1) && (expression.getMaximum () == 1)) {
          this.groupRegex += Converter.toRegex (expression);
        }
        else {
          final String regexPart = "(" + Converter.toRegex (expression) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          String regexCopy = new String (regexPart);
          regexCopy = regexCopy.replaceAll ("[^(]", ""); //$NON-NLS-1$ //$NON-NLS-2$
          innerParentheses = regexCopy.length () - 1;
          this.groupRegex += regexPart;
        }
      }
      else if (((ExpressionLeaf) expression).isDigitRange ()) {
        String regexPart;
        if (((ExpressionLeaf) expression).isAllowLeadingZeros ()) {
          regexPart = "(" + Converter.toRegex (expression) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
          regexPart = Converter.toRegex (expression);
        }
        String regexCopy = new String (regexPart);
        regexCopy = regexCopy.replaceAll ("[^(]", ""); //$NON-NLS-1$ //$NON-NLS-2$
        innerParentheses = regexCopy.length () - 1;
        this.groupRegex += regexPart;
      }
      else if (((expression.getMaximum () != 0) || (expression.getMinimum () != 0)) || (expression.isOptional ())) {
        final String regexPart = "(" + Converter.toRegex (expression) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        String regexCopy = new String (regexPart);
        regexCopy = regexCopy.replaceAll ("[^(]", ""); //$NON-NLS-1$ //$NON-NLS-2$
        innerParentheses = regexCopy.length () - 1;
        this.groupRegex += regexPart;
      }
      else {
        this.groupRegex += "(" + Converter.toRegex (expression) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
      }
      for (int i = 0; i < innerParentheses; i++) {
        this.isLeaf.add (false);
      }
      if (expression == current) {
        String regexCopy = new String (this.groupRegex);
        regexCopy = regexCopy.replaceAll ("\\\\\\(", ""); // Defect 64710: When the expression contains the character "(", it is
                                                          // not replaced by the command below, so manually remove it first.
                                                          // "(" in regexCopy is "\(", whose regex to be used in replaceAll() is
                                                          // "\\\("; hence the long string "\\\\\\(".
        regexCopy = regexCopy.replaceAll ("[^(]", ""); 
        this.currentGroupIndex = regexCopy.length () - innerParentheses;
      }
    }
    else {
      if (expression instanceof AlternationExpression) {
        if (((AlternationExpression) expression).isSamplesAlternation ()) {
          this.isLeaf.add (true);
        }
        else {
          this.isLeaf.add (false);
        }
        this.groupRegex += "("; //$NON-NLS-1$
        String bar = ""; //$NON-NLS-1$
        for (final Expression subexpression : ((ExpressionNode) expression).getSubexpressions ()) {
          this.groupRegex += bar;
          bar = "|"; //$NON-NLS-1$
          getGroupRegex2 (subexpression);
        }
        this.groupRegex += ")"; //$NON-NLS-1$
        if (expression.isOptional ()) {
          this.groupRegex += "?"; //$NON-NLS-1$
        }
      }
      else {
        // expression instanceof ConcatenationExpression
        this.isLeaf.add (false);
        this.groupRegex += "("; //$NON-NLS-1$
        for (final Expression subexpression : ((ExpressionNode) expression).getSubexpressions ()) {
          getGroupRegex2 (subexpression);
        }
        this.groupRegex += ")"; //$NON-NLS-1$
        if (expression.isOptional ()) {
          this.groupRegex += "?"; //$NON-NLS-1$
        }
      }
    }
    return this.groupRegex;
  }

  /**
   * create a regular expression with parentheses around each group (for highlighting)
   * 
   * @param expression
   * @return String representing the regular expression
   */
  public String getGroupRegex(Expression expression) {
    this.groupRegex = ""; //$NON-NLS-1$
    if (this.isLeaf == null) {
      this.isLeaf = new ArrayList<Boolean>();
    } else {
      this.isLeaf.clear();
    }

    this.isLeaf.add(false);

    getGroupRegex2(expression);

    return this.groupRegex;
  }

  // GETTERS

  public int getCurrentGroupIndex() {
    return this.currentGroupIndex;
  }

  public ArrayList<Boolean> getIsLeaf() {
    return this.isLeaf;
  }

}

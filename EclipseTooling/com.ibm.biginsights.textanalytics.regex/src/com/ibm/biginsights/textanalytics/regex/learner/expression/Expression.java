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
package com.ibm.biginsights.textanalytics.regex.learner.expression;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterHierarchy;
import com.ibm.biginsights.textanalytics.regex.learner.learner.Bias;
import com.ibm.biginsights.textanalytics.regex.learner.learner.utils.Converter;
import com.ibm.biginsights.textanalytics.regex.learner.utils.GroupRegex;
import com.ibm.biginsights.textanalytics.regex.learner.utils.Traversal;

/**
 * 
 * 
 *         The abstract class Expression is the superclass of all Expression nodes that are used to
 *         represent the tree structure of a regular expression. For example, the regular expression
 *         'a(b|c)d' can be represented as a ConcatenationExpression with the subexpressions 'a',
 *         '(b|c)' and 'd'. 'a' and 'd' are ExpressionLeafs, while '(b|d)' is an
 *         AlternationExpression of the ExpressionLeafs 'b' and 'd'. ConcatenationExpressions and
 *         AlternationExpressions are subclasses of ExpressionNode. Each ExpressionNode can have
 *         subexpressions of any Expression type (nodes or leafs).
 * 
 */

public abstract class Expression implements Cloneable {



  protected String type;

  /* minimum number of occurrences */
  protected int minimum = 1;

  /* maximum number of occurrences */
  protected int maximum = 1;

  /* boolean indicating if the whole expression is optional or not */
  protected boolean optional = false;

  // abstract methods
  public abstract boolean contains(Expression expr);

  public abstract Expression invert();

  @Override
  public abstract boolean equals(Object expr);

  public abstract boolean insert(Expression expr);

  @Override
  public abstract String toString();

  public abstract String toStringWithSamples();

  /**
   * This method updates a certain expression
   * 
   * @param expression
   * @param setSamplesAlternation
   */
  public static void updateExpression(Expression expression, boolean setSamplesAlternation,
      Bias bias) {
    if (expression instanceof ExpressionLeaf) {
      // update type
      String allSamples = ""; //$NON-NLS-1$
      for (final String sample : ((ExpressionLeaf) expression).getSamples()) {
        allSamples += sample;
      }
      try {    	
		expression.setType(CharacterHierarchy.getCharacterClass(allSamples).getName());
	} catch (NullPointerException e) {
		//during parsing of special characters, allSamples is null and therefore doesnt require
		//any update of expression. 
		//System.out.println("allSamples is null at this point");
	}
      if (setSamplesAlternation) {
        if ((((ExpressionLeaf) expression).getSamples().size() == 1)
            || expression.getType().equals(CharacterHierarchy.OTHER)
            || expression.getType().equals(CharacterHierarchy.WHITESPACE)
            || ((expression.getType().equals(CharacterHierarchy.LETTER)
                || expression.getType().equals(CharacterHierarchy.LOWER) || expression.getType()
                .equals(CharacterHierarchy.UPPER)) && (((ExpressionLeaf) expression).getSamples()
                .size() <= bias.getSummarizer_minNoOfSamples()))) {
          ((ExpressionLeaf) expression).setSamplesAlternation(true);
        }
        // else {
        // ((ExpressionLeaf) expression).setSamplesAlternation(false);
        // }
        if (expression.getType().equals(CharacterHierarchy.DIGIT)
            && !bias.isDigitsNeverAlternation()) {
          ((ExpressionLeaf) expression).setSamplesAlternation(false);
        }
      }
      if (bias.isConvertToCharClassOnly()) {
        ((ExpressionLeaf) expression).setSamplesAlternation(false);
      }
      // sort samples by length --> longest first --> Java Regex Engine
      // NFA problem... :-(
      ((ExpressionLeaf) expression).sortSamples();
    } else {
      for (final Expression subexpr : ((ExpressionNode) expression).getSubexpressions()) {
        updateExpression(subexpr, setSamplesAlternation, bias);
      }
    }
  }

  /**
   * This method updates the samples of each leaf. It retrieves the part of the sample strings that
   * are matched by the group representing the leaf and adds them to the samples list of the
   * respective leaf.
   * 
   * @param expression
   *          - Expression to be updated
   *          - list of sample strings (whole samples provided to the learning algorithms)
   */
  public static void updateLeafSamples(Expression expression, ArrayList<String> samples) {
    final Traversal traversal = new Traversal(expression);
    while (traversal.hasNext()) {
      final ExpressionLeaf leaf = traversal.getNextLeaf();
      final GroupRegex gr = new GroupRegex(traversal);
      final String regex = gr.getGroupRegex(expression);
      int currentGroupIndex = gr.getCurrentGroupIndex();
      final Pattern pattern = Pattern.compile(regex);
      final ArrayList<String> newSamples = new ArrayList<String>();
      for (final String sample : samples) {
        final Matcher matcher = pattern.matcher(sample);
        matcher.find();
        String groupString = null;
        try
        {	
        	groupString = matcher.group(currentGroupIndex);
        }
        catch(IllegalStateException e)
        {
        	//e.printStackTrace();
        	
        }
        catch(IndexOutOfBoundsException e)
        {
        	//e.printStackTrace();
        }
        if (groupString != null) {
          if (!groupString.equals("")) { //$NON-NLS-1$
            newSamples.add(Converter.escape(groupString));
          }
        }
      }
      leaf.getSamples().clear();
      leaf.addSamples(newSamples);
    }
  }

  // for debugging purposes
  public abstract String getType();

  // GETTER + SETTER METHODS

  public int getMinimum() {
    return this.minimum;
  }

  public void setMinimum(int minimum) {
    this.minimum = minimum;
  }

  public int getMaximum() {
    return this.maximum;
  }

  public void setMaximum(int maximum) {
    this.maximum = maximum;
  }

  public boolean isOptional() {
    return this.optional;
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  @Override
  public Expression clone() {
    Expression clone = null;
    try {
      clone = (Expression) super.clone();
    } catch (final CloneNotSupportedException e) {
      // can't happen
      throw new AssertionError("Cloning of " + this + Messages.Expression_FAILED); //$NON-NLS-1$
    }
    return clone;
  }

  public void setType(String name) {
    this.type = name;
  }

}

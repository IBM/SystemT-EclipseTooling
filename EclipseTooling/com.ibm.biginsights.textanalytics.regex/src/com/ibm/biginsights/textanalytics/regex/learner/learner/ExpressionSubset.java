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
package com.ibm.biginsights.textanalytics.regex.learner.learner;

import java.util.ArrayList;

import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterHierarchy;
import com.ibm.biginsights.textanalytics.regex.learner.expression.AlternationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ConcatenationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionLeaf;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionNode;

/**
 * This class is used by the RIGACommonSubstrings to represent the expression subsets when
 * partitioning.
 * 
 * 
 * 
 */
public class ExpressionSubset {



  // set of expressions to be partitioned
  ArrayList<ConcatenationExpression> expressions = new ArrayList<ConcatenationExpression>();

  // partition of expressions that contain the sample nextCSS
  ExpressionSubset nextWith = null;

  // partition of expressions that do not contain the sample nextCSS
  ExpressionSubset nextWithout = null;

  // after partitioning, this field contains the alignedExpression
  ExpressionNode alignedExpression = null;

  /**
   * This method defines the partitions nextWith and nextWithout. Make sure that the interchangeable
   * strings have been added to all leaf's samples list before calling this method on the
   * expressions containing those leafs.
   * 
   * @param nextCSS
   *          the next common substrings that is to be used for partitioning
   */
  public void defineNext(String nextCSS) {
    if (this.expressions != null) {
      if (!this.expressions.isEmpty()) {
        this.nextWith = new ExpressionSubset();
        this.nextWithout = new ExpressionSubset();
        for (final ExpressionNode node : this.expressions) {
          boolean withString = false;
          for (final Expression leaf : node.getSubexpressions()) {
            if (((ExpressionLeaf) leaf).getSamples().contains(nextCSS)) {
              withString = true;
            }
          }
          if (withString) {
            this.nextWith.addExpression((ConcatenationExpression) node);
          } else {
            this.nextWithout.addExpression((ConcatenationExpression) node);
          }
        }
      }
    }
  }

  /**
   * This method builds the aligned expression. Must not be called before the partitioning is
   * complete.
   * 
   * @param interchangeableStrings
   */
  public void buildAlignedExpression(ArrayList<ArrayList<String>> interchangeableStrings) {
    if (((this.nextWith == null) && (this.nextWithout == null))) {
      // leaf ExpressionSubset
      if (this.expressions.isEmpty()) {
        this.alignedExpression = null;
      } else {
        ExpressionNode aligned = this.expressions.get(0);
        aligned = changeLeafTypesToCommonSubstring(aligned, interchangeableStrings);
        for (int i = 1; i < this.expressions.size(); i++) {
          final ExpressionNode toAlign = changeLeafTypesToCommonSubstring(this.expressions.get(i),
              interchangeableStrings);
          aligned = Aligner.align(aligned, toAlign);
        }
        aligned = (ExpressionNode) changeLeafTypesToCharClass(aligned);
        this.alignedExpression = aligned;
      }
    } else {
      if (this.nextWith != null) {
        this.nextWith.buildAlignedExpression(interchangeableStrings);
      }
      if (this.nextWithout != null) {
        this.nextWithout.buildAlignedExpression(interchangeableStrings);
      }
      // not a leaf ExpressionSubset
      if ((this.nextWith == null) && (this.nextWithout != null)) {
        this.alignedExpression = this.nextWithout.alignedExpression;
      } else if ((this.nextWith != null) && (this.nextWithout == null)) {
        this.alignedExpression = this.nextWith.alignedExpression;
      } else if ((this.nextWith != null) && (this.nextWithout != null)) {
        if ((this.nextWith.alignedExpression != null)
            && (this.nextWithout.alignedExpression == null)) {
          this.alignedExpression = this.nextWith.alignedExpression;
        } else if ((this.nextWith.alignedExpression == null)
            && (this.nextWithout.alignedExpression != null)) {
          this.alignedExpression = this.nextWithout.alignedExpression;
        } else { // nextWith and nextWithout alignedExpressions are != null
          this.alignedExpression = new AlternationExpression();
          if (this.nextWith.alignedExpression instanceof AlternationExpression) {
            for (final Expression subexpr : this.nextWith.alignedExpression.getSubexpressions()) {
              this.alignedExpression.addSubexpression(subexpr);
            }
          } else {
            this.alignedExpression.addSubexpression(this.nextWith.alignedExpression);
          }
          if (this.nextWithout.alignedExpression instanceof AlternationExpression) {
            for (final Expression subexpr : this.nextWithout.alignedExpression.getSubexpressions()) {
              this.alignedExpression.addSubexpression(subexpr);
            }
          } else {
            this.alignedExpression.addSubexpression(this.nextWithout.alignedExpression);
          }
        }
      }
    }
  }

  /**
   * if a leaf's samples are interchangeable strings, the type of that leaf is changed to the first
   * item in the interchangeables list. this improves alignment.
   * 
   * @param ExpressionNode
   *          expression --> expression whose subexpression-leafs are to be renamed
   * @param ArrayList
   *          <ArrayList<String>> interchangeables
   * @return expression with renamed expression leafs
   */
  private ExpressionNode changeLeafTypesToCommonSubstring(ExpressionNode expression,
      ArrayList<ArrayList<String>> interchangeables) {
    for (final Expression subexpression : expression.getSubexpressions()) {
      final ArrayList<String> inters = RIGACommonSubstrings.getInterchangeables(
          ((ExpressionLeaf) subexpression).getSample(0), interchangeables);
      if (inters != null) {
        // this leaf is has interchangeable sample strings
        ((ExpressionLeaf) subexpression).setType(inters.get(0));
      }
    }
    return expression;
  }

  /**
   * if a leaf's samples are interchangeable strings, the type of that leaf is changed to the first
   * item in the interchangeables list. this improves alignment.
   * 
   * @param ExpressionNode
   *          expression --> expression whose subexpression-leafs are to be renamed
   * @param ArrayList
   *          <ArrayList<String>> interchangeables
   * @return expression with renamed expression leafs
   */
  private Expression changeLeafTypesToCharClass(Expression expression) {
    if (expression instanceof ExpressionLeaf) {
      final ExpressionLeaf leaf = (ExpressionLeaf) expression;
      final String type = CharacterHierarchy.getCharacterClass(leaf.getSample(0)).getName();
      if (type.equals(CharacterHierarchy.LOWER) || type.equals(CharacterHierarchy.UPPER)) {
        boolean lowercase = false;
        boolean uppercase = false;
        for (final String sample : leaf.getSamples()) {
          if (CharacterHierarchy.getCharacterClass(sample).getName()
              .equals(CharacterHierarchy.LOWER)) {
            lowercase = true;
          }
          if (CharacterHierarchy.getCharacterClass(sample).getName()
              .equals(CharacterHierarchy.UPPER)) {
            uppercase = true;
          }
        }
        if (lowercase && uppercase) {
          leaf.setType(CharacterHierarchy.LETTER);
        } else {
          leaf.setType(type);
        }
      } else {
        leaf.setType(type);
      }
    } else {
      for (final Expression subsubexpression : ((ExpressionNode) expression).getSubexpressions()) {
        changeLeafTypesToCharClass(subsubexpression);
      }
    }
    return expression;
  }

  // GETTER + SETTER METHODS

  public ExpressionSubset getNextWith() {
    return this.nextWith;
  }

  public void setNextWith(ExpressionSubset nextWith) {
    this.nextWith = nextWith;
  }

  public ExpressionSubset getNextWithout() {
    return this.nextWithout;
  }

  public void setNextWithout(ExpressionSubset nextWithout) {
    this.nextWithout = nextWithout;
  }

  public void addExpression(ConcatenationExpression expr) {
    this.expressions.add(expr);
  }

  public ArrayList<ConcatenationExpression> getExpressions() {
    return this.expressions;
  }

  public ExpressionNode getAlignedExpression() {
    return this.alignedExpression;
  }

  @Override
  public String toString() {
    String string = "["; //$NON-NLS-1$
    for (final ConcatenationExpression expr : this.expressions) {
      string += expr.toStringWithSamples() + " "; //$NON-NLS-1$
    }
    string += "]\n"; //$NON-NLS-1$
    if (this.nextWith != null) {
      string += "\twith: " + this.nextWith.toString() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    if (this.nextWithout != null) {
      string += "\twithout: " + this.nextWithout.toString() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    return string;
  }

  public String getText() {
    String string = "expressions ["; //$NON-NLS-1$
    for (final ConcatenationExpression expr : this.expressions) {
      string += expr.toStringWithSamples() + " "; //$NON-NLS-1$
    }
    string += "]"; //$NON-NLS-1$
    return string;
  }

}

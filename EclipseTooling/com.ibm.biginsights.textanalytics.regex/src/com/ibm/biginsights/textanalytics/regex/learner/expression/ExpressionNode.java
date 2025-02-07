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


/**
 * 
 * 
 *         Any inner nodes of the tree representing the regular expression (see documentation of
 *         'Expression') must be subclasses of ExpressionNode. ExpressionNode summarizes the
 *         features of AlternationExpression and ConcatenationExpression such as the subexpressions
 *         list.
 * 
 */

public abstract class ExpressionNode extends Expression {



  /* list of subexpressions of this expression */
  protected ArrayList<Expression> subexpressions = new ArrayList<Expression>();

  // METHODS

  protected abstract boolean insertLeaf(ExpressionLeaf expr);

  protected abstract boolean insertAlternation(AlternationExpression expr);

  protected abstract boolean insertConcatenation(ConcatenationExpression expr);

  /**
   * @return this expression with its subexpressions in an inverted order
   */
  @Override
  public Expression invert() {
    final ArrayList<Expression> subexpressionsInverted = new ArrayList<Expression>();
    for (int i = this.subexpressions.size() - 1; i >= 0; i--) {
      subexpressionsInverted.add(this.subexpressions.get(i).invert());
    }
    final ExpressionNode node = clone();
    node.subexpressions = subexpressionsInverted;
    return node;
  }

  /**
   * inserts an expression into this expression (if expr is compatible)
   * 
   * @return true if expr is compatible and can be inserted (adjustment of samples, min & max) false
   *         if expr is incompatible
   */
  @Override
  public boolean insert(Expression expr) {
    if (expr instanceof ExpressionLeaf) {
      return insertLeaf((ExpressionLeaf) expr);
    } else if (expr instanceof AlternationExpression) {
      return insertAlternation((AlternationExpression) expr);
    } else if (expr instanceof ConcatenationExpression) {
      return insertConcatenation((ConcatenationExpression) expr);
    } else {
      System.out.println("What type should this be?"); //$NON-NLS-1$
      return false;
    }
  }

  /**
   * @return new object with same characteristics like this object
   */
  @Override
  public ExpressionNode clone() {
    final ExpressionNode clone = (ExpressionNode) super.clone();
    // deep copy of subexpressions
    clone.subexpressions = new ArrayList<Expression>();
    for (final Expression subexpression : this.subexpressions) {
      clone.subexpressions.add(subexpression.clone());
    }
    return clone;
  }

  /**
   * not used in implementation so far...
   * 
   * @return hash code of this object
   */
  @Override
  public int hashCode() {
    int result = 17;
    // the only significant field is subexpressions
    for (final Expression subexpression : this.subexpressions) {
      result = 31 * result + subexpression.hashCode();
    }
    return result;
  }

  // GETTER + SETTER METHODS
  public ArrayList<Expression> getSubexpressions() {
    return this.subexpressions;
  }

  public Expression getSubexpression(int index) {
    return this.subexpressions.get(index);
  }

  public void addSubexpression(Expression expr) {
    this.subexpressions.add(expr);
  }

  public void addSubexpressions(ArrayList<Expression> expressions) {
    this.subexpressions.addAll(expressions);
  }

}

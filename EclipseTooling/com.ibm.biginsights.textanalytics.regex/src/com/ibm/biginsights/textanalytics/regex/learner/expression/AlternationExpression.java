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

import java.util.Iterator;

import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterHierarchy;

/**
 * The objects of this class represent alternations. The order of the subexpressions does not matter
 * for alternations.
 */

public class AlternationExpression extends ExpressionNode {



  // METHODS

  /**
   * This method returns true if this alternation contains a subexpression of the same type(s) as
   * the Expression given as a parameter.
   * 
   * @param expr
   *          check if this alternation contains an expression with the same type(s)
   * @return true if this is the case, false otherwise
   */
  @Override
  public boolean contains(Expression expr) {
    if (equals(expr)) {
      return true;
    }
    for (final Expression subexpression : this.subexpressions) {
      if (subexpression.contains(expr)) {
        return true;
      }
    }
    return false;
  }

  /**
   * checks if two expressions are equivalent
   * 
   * @param expr
   *          expression to compare with this object
   * @return true if equivalent, false otherwise
   */
  @Override
  public boolean equals(Object expr) {
    // identity check
    if (this == expr) {
      return true;
    }
    // check for correct type
    if (!(expr instanceof AlternationExpression)) {
      return false;
    }
    // compare significant fields (subexpressions)
    for (final Expression subexpression : this.subexpressions) {
      if (!((AlternationExpression) expr).contains(subexpression)) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return a new object with the same characteristics as this object
   */
  @Override
  public AlternationExpression clone() {
    return (AlternationExpression) super.clone();
  }

  /**
   * insert a certain leaf into an alternation (if contained in this object or one of the
   * subexpressions, adjust min, max,...),
   * 
   * @param leaf
   *          to insert
   * @return true if insertion successful, false otherwise
   */
  @Override
  protected boolean insertLeaf(ExpressionLeaf leaf) {
    if (contains(leaf)) {
      final Iterator<Expression> iterator = this.subexpressions.iterator();
      Expression subexpression;
      boolean inserted = false;
      while (iterator.hasNext() && !inserted) {
        subexpression = iterator.next();
        inserted = subexpression.insert(leaf);
      }
      return inserted;
    }
    return false;
  }

  /**
   * this method tries to insert a concatenation expression into an alternation -> insertion into
   * alternation is only possible if there is a compatible subexpression -> otherwise, alternation
   * must be build "from calling part of code"
   * 
   * @param ConcatenationExpression
   *          to be inserted
   * @return true if insertion was successful, false otherwise
   */
  @Override
  protected boolean insertConcatenation(ConcatenationExpression conc) {
//    if (!(conc instanceof ConcatenationExpression)) {
//      return false;
//    }
    for (final Expression subexpression : this.subexpressions) {
      if (subexpression instanceof ExpressionNode) {
        if (((ExpressionNode) subexpression).insertConcatenation(conc)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * if there is no compatible subexpression, return false if this is an alternation, try to insert
   * into this object or its subexpressions, if no compatible one, return false. new alternation
   * must be build from calling part of code.
   * 
   * @param AlternationExpression
   *          alt to be inserted
   * @return true if insertion was successful, false otherwise
   */
  @Override
  protected boolean insertAlternation(AlternationExpression alt) {
//    if (!(alt instanceof AlternationExpression)) {
//      return false;
//    }
    if (equals(alt)) {
      for (final Expression subexpression : ((ExpressionNode) alt).getSubexpressions()) {
        for (final Expression thisSubexpression : this.subexpressions) {
          if (subexpression.equals(thisSubexpression)) {
            thisSubexpression.insert(subexpression);
            break;
          }
        }
      }
      return true;
    }
    // if alternations are not equal, try to insert into one of the
    // subexpression,
    // if not possible, build up new alternation from calling code
    for (final Expression subexpression : this.subexpressions) {
      if (subexpression.insert(alt)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return true if this alternations' subexpressions are ALL of type ExpressionLeaf false
   *         otherwise
   */
  public boolean isLeafAlternation() {
    for (final Expression subexpr : this.subexpressions) {
      if (!(subexpr instanceof ExpressionLeaf)) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return true if this alternations' subexpressions are ALL of type ExpressionLeaf and the types
   *         are NOT general types of the character hierarchy but specific samples false otherwise
   */
  public boolean isSamplesAlternation() {
    for (final Expression subexpr : this.subexpressions) {
      if (!(subexpr instanceof ExpressionLeaf)) {
        return false;
      }
      if (CharacterHierarchy.isCharacterClass(subexpr.getType())) {
        return false;
      }
    }
    return true;
  }

  // TO STRING - METHODS
  /**
   * @return a string containing the leaf informationLabel
   */
  @Override
  public String toString() {
    String string = "("; //$NON-NLS-1$
    String bar = ""; //$NON-NLS-1$
    // System.out.println("subexpressions: " + subexpressions);
    for (final Expression subexpression : this.subexpressions) {
      string += bar + subexpression.toString();
      bar = "|"; //$NON-NLS-1$
    }
    string += ")"; //$NON-NLS-1$
    if (this.optional) {
      string += "{opt}"; //$NON-NLS-1$
    }
    return string;
  }

  /**
   * @return a string containing the leaf informationLabel including samples
   */
  @Override
  public String toStringWithSamples() {
    String string = "("; //$NON-NLS-1$
    String bar = ""; //$NON-NLS-1$
    for (final Expression subexpression : this.subexpressions) {
      if (subexpression != null) {
        string += bar + subexpression.toStringWithSamples();
      }
      bar = "|"; //$NON-NLS-1$
    }
    string += ")"; //$NON-NLS-1$
    if (this.optional) {
      string += "{opt}"; //$NON-NLS-1$
    }
    return string;
  }

  @Override
  public String getType() {
    return "ALT"; //$NON-NLS-1$
  }
}

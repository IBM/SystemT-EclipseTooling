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

import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterHierarchy;

/**
 * 
 * 
 *         The objects of this class represent concatenations and contain a list of subexpressions.
 *         The order of the subexpressions matters for concatenations.
 */

public class ConcatenationExpression extends ExpressionNode {



  // CONSTRUCTOR - when building a new Sample --> build expression
  public ConcatenationExpression(String sample) {
    final char sampleChars[] = sample.toCharArray();
    for (int i = 0; i < sampleChars.length; i++) {
      final ExpressionLeaf leaf = new ExpressionLeaf(CharacterHierarchy.getCharacterClass(
          sampleChars[i]).getName());
      leaf.addSample(sampleChars[i] + ""); //$NON-NLS-1$
      this.subexpressions.add(leaf);
    }
  }

  // Default Constructor
  public ConcatenationExpression() {
    super();
  }

  // METHODS

  /**
   * this method checks if this ConcatenationExpression object contains the Expression expression.
   * 
   * @param Expression
   *          expression --> is an equivalent expression contained in this object?
   * @return true if this ConcatenationExpression object contains an equivalent expression, false
   *         otherwise
   */
  @Override
  public boolean contains(Expression expression) {
    if (equals(expression)) {
      return true;
    }
    for (final Expression subexpression : this.subexpressions) {
      if (subexpression.contains(expression)) {
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
    if (!(expr instanceof ConcatenationExpression)) {
      return false;
    }
    // cast argument to correct type and compare significant fields
    // (subexpressions)
    if (!(this.subexpressions.size() == ((ExpressionNode) expr).subexpressions.size())) {
      return false;
    }
    boolean equal = true;
    for (int i = 0; i < this.subexpressions.size(); i++) {
      if (!this.subexpressions.get(i).equals(((ExpressionNode) expr).getSubexpression(i))) {
        equal = false;
        break;
      }
    }
    return equal;
  }

  /**
   * @return a new object with the same characteristics as this object
   */
  @Override
  public ConcatenationExpression clone() {
    return (ConcatenationExpression) super.clone();
  }

  /**
   * insert a certain leaf into a concatenation: (if leaf equals the ONLY element of concatenation,
   * insert, otherwise no insertion possible, return false) In oder to keep a lot of
   * informationLabel about the concept, the leaf cannot be inserted into a subexpression, even if
   * there is a matching one. All other subexpressions would then have to be declared optional.
   * Instead, create a new alternation (this Concatenation | leaf) from "calling part of code".
   * 
   * @param leaf
   *          to insert
   * @return true if insertion successful, false otherwise
   */
  @Override
  protected boolean insertLeaf(ExpressionLeaf leaf) {
    if ((this.subexpressions.size() == 1) && this.subexpressions.get(0).equals(leaf)) {
      if (this.subexpressions.get(0).insert(leaf)) {
        return true;
      }
    }
    // cannot insert leaf into conc --> if this failed, must be ALTERNATION
    // "from calling part of code"
    return false;
  }

  /**
   * a concatenation expression is inserted into a concatenation -> insertion into concatenation is
   * only possible if they are compatible -> otherwise, try to insert the concatenation into one of
   * the subexpressions. -> if that isn't successful either, alternation must be build
   * "from calling part of code"
   * 
   * @param ConcatenationExpression
   *          conc to be inserted
   * @return true if insertion was successful, false otherwise
   */
  @Override
  protected boolean insertConcatenation(ConcatenationExpression conc) {
//    if (!(conc instanceof ConcatenationExpression)) {
//      return false;
//    }
    // this object is a CONCATENATION --> conc can only insert if equivalent
    // (same types and same order of subexpressions)
    // (else build alternation "from calling part of code")
    if (equals(conc)) {
      for (int i = 0; i < this.subexpressions.size(); i++) {
        this.subexpressions.get(i).insert(((ExpressionNode) conc).subexpressions.get(i));
      }
      return true;
    }
    for (final Expression subexpression : this.subexpressions) {
      if (subexpression.insert(conc)) {
        return true;
      }
    }
    return false;
  }

  /**
   * try to insert into one of the subexpressions (if there is a compatible one). if there is no
   * compatible subexpression, return false. A new alternation must be build from calling part of
   * code.
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
    for (final Expression subexpression : this.subexpressions) {
      if (subexpression.insert(alt)) {
        return true;
      }
    }
    return false;
  }

  // TO STRING - METHODS
  /**
   * @return a string containing the leaf informationLabel
   */
  @Override
  public String toString() {
    String string = "("; //$NON-NLS-1$
    for (final Expression subexpression : this.subexpressions) {
      string += subexpression.toString() + " "; //$NON-NLS-1$
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
    for (final Expression subexpression : this.subexpressions) {
      string += subexpression.toStringWithSamples() + " "; //$NON-NLS-1$
    }
    string += ")"; //$NON-NLS-1$
    if (this.optional) {
      string += "{opt}"; //$NON-NLS-1$
    }
    return string;
  }

  @Override
  public String getType() {
    return "CONC"; //$NON-NLS-1$
  }

}

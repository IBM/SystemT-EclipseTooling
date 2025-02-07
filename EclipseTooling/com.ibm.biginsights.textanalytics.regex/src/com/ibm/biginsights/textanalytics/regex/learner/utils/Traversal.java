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

import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionLeaf;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionNode;

/**
 * Traversal represents a "linear" version of the regular expression tree, i.e. the expressionLeaf
 * list contains all expression leafs in a certain order. It can be used as a kind of Iterator, as
 * index points to a certain subexpression. Going forward and back is possible.
 * 
 * 
 * 
 */

public class Traversal {



  // ordered list of ExpressionLeafs
  private final ArrayList<ExpressionLeaf> expressionsList;

  // index pointing to the current subexpression --> default: -1.
  // getNextLeaf() must be called to get the first leaf.
  private int index;

  // CONSTRUCTOR
  public Traversal(Expression expression) {
    this.expressionsList = new ArrayList<ExpressionLeaf>();
    fillList(expression);
    this.index = -1;
  }

  /**
   * This methods creates the ExpressionLeaf list for a given Expression (that is still in tree
   * representation).
   * 
   * @param expression
   */
  private void fillList(Expression expression) {
    if (expression instanceof ExpressionLeaf) {
      this.expressionsList.add((ExpressionLeaf) expression);
    } else {
      for (final Expression subexpr : ((ExpressionNode) expression).getSubexpressions()) {
        fillList(subexpr);
      }
    }
  }

  /**
   * This method returns the next list in the leaf (first call of this method: returns first leaf in
   * the list). If the current leaf is the last leaf in the list, it returns null and sets the index
   * pointer back to before the list.
   * 
   * @return returns the next leaf in the list (depending on index) returns null if the current leaf
   *         is the last leaf in the list and resets the index to -1
   */
  public ExpressionLeaf getNextLeaf() {
    if (hasNext()) {
      this.index++;
      return this.expressionsList.get(this.index);
    }
    this.index = -1;
    return null;
  }

  /**
   * This method returns the previous list in the leaf. If the current leaf is the first leaf in the
   * list, it returns null.
   * 
   * @return returns the previous leaf in the list (depending on index) returns null if the current
   *         leaf is the first leaf in the list.
   */
  public ExpressionLeaf getPreviousLeaf() {
    if (hasPrevious()) {
      this.index--;
      return this.expressionsList.get(this.index);
    }
    return null;
  }

  /**
   * This method returns the subexpression index is currently pointing to.
   * 
   * @return the current subexpression
   */
  public ExpressionLeaf getCurrentLeaf() {
    if ((this.index >= 0) && (this.index < this.expressionsList.size())) {
      return this.expressionsList.get(this.index);
    }
    return null;
  }

  /**
   * true if the current subexpression is the last one in the list, false otherwise
   * 
   * @return true if the current subexpression is the last one in the list, false otherwise
   */
  public boolean hasNext() {
    if (this.index < this.expressionsList.size() - 1) {
      return true;
    }
    return false;
  }

  /**
   * Returns false if the current subexpression is the first one in the list, false otherwise.
   * 
   * @return true if there is at least one subexpression BEFORE the current subexpression in the
   *         list false otherwise
   */
  public boolean hasPrevious() {
    if ((this.index == -1) || (this.index == 0)) {
      return false;
    }
    return true;
  }

}

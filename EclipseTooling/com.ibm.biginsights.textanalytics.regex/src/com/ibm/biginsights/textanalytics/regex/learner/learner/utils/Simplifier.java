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
package com.ibm.biginsights.textanalytics.regex.learner.learner.utils;

import java.util.ArrayList;

import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterHierarchy;
import com.ibm.biginsights.textanalytics.regex.learner.expression.AlternationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ConcatenationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionLeaf;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionNode;

/**
 * 
 * This class simplifies nested alternations which may occur during the alignment processes.
 * 
 * 
 * 
 */

public class Simplifier {



  private Simplifier() {
    // prevent instantiation
  }

  /**
   * resolves nested alternations, such as (A|(B|C)) to (A|B|C), (A|(B|C)) to (A|B|C), (A|(B|A)) to
   * (A|B)
   * 
   * @param expression
   *          - Expression to be simplified
   * @return expression with simplified alternations
   */
  public static Expression generalizeAlternations(Expression expression) {
    if (expression instanceof ExpressionLeaf) {
      return expression;
    } else if (expression instanceof ConcatenationExpression) {
      final ExpressionNode simplified = new ConcatenationExpression();
      for (final Expression subexpr : ((ExpressionNode) expression).getSubexpressions()) {
        simplified.addSubexpression(generalizeAlternations(subexpr));
      }
      return simplified;
    } else {
      // expression instance of AlternationExpression
      if (!((AlternationExpression) expression).isLeafAlternation()) {
        final ExpressionNode simplified = new AlternationExpression();
        for (final Expression subexpr : ((ExpressionNode) expression).getSubexpressions()) {
          simplified.addSubexpression(generalizeAlternations(subexpr));
        }
        return simplified;
      }
      String allSamples = ""; //$NON-NLS-1$
      boolean optional = false;
      final ArrayList<String> samples = new ArrayList<String>();
      for (final Expression subexpression : ((ExpressionNode) expression).getSubexpressions()) {
        for (int i = 0; i < ((ExpressionLeaf) subexpression).getSamples().size(); i++) {
          allSamples += ((ExpressionLeaf) subexpression).getSample(i);
          samples.add(((ExpressionLeaf) subexpression).getSample(i));
        }
        if (subexpression.isOptional()) {
          optional = true;
        }
      }
      final ExpressionLeaf leaf = new ExpressionLeaf(CharacterHierarchy.getCharacterClass(
          allSamples).getName());
      leaf.setSamplesAlternation(true);
      leaf.addSamples(samples);
      if (expression.isOptional()) {
        optional = true;
      }
      leaf.setOptional(optional);
      return leaf;

    }
  }

  /**
   * resolves nested alternations, such as (A|(B|C)) to (A|B|C), (A|(B|C)) to (A|B|C), (A|(B|A)) to
   * (A|B)
   * 
   * @param expression
   *          - Expression to be simplified
   * @return expression with simplified alternations
   */
  public static Expression removeNestedAlternations(Expression expression) {
    if (expression instanceof ExpressionLeaf) {
      return expression;
    } else if (expression instanceof ConcatenationExpression) {
      final ConcatenationExpression conc = new ConcatenationExpression();
      for (final Expression subexpr : ((ConcatenationExpression) expression).getSubexpressions()) {
        conc.addSubexpression(removeNestedAlternations(subexpr));
      }
      return conc;
    } else {
      // expr is alternation expression
      final AlternationExpression alt = new AlternationExpression();
      for (final Expression subexpr : ((AlternationExpression) expression).getSubexpressions()) {
        if (subexpr instanceof AlternationExpression) {
          final Expression subexpr2 = removeNestedAlternations(subexpr);
          for (final Expression subsubexpr : ((AlternationExpression) subexpr2).getSubexpressions()) {
            alt.addSubexpression(removeNestedAlternations(subsubexpr));
          }
        } else {
          alt.addSubexpression(removeNestedAlternations(subexpr));
        }
      }
      return alt;
    }
  }

}

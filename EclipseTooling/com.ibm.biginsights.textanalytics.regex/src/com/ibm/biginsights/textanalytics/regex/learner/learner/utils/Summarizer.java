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

import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterHierarchy;
import com.ibm.biginsights.textanalytics.regex.learner.expression.AlternationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ConcatenationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionLeaf;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionNode;

/**
 * 
 * 
 * 
 *         This class provides methods to summarize an expression, i.e. to make one leaf out of
 *         adjacent leafs of the same character class.
 * 
 */

public class Summarizer {



  private Summarizer() {
    // prevent instantiation
  }

  /**
   * summarizes expressions in the following way: e.g. if MIN_NUMBER_OF_SAMPLES = 1 or 2
   * Digit{1,2}[1 2] Digit{2,5}[3 5] --> Digit{3,7}[1 2 3 5], Digit{1,2}{opt}[1 2] Digit{2,5}[3 5]
   * --> Digit{2,7}[1 2 3 5] Digit{1,2}[1 2] Digit{2,5}{opt}[3 5] --> Digit{1,7}[1 2 3 5]
   * Digit{1,2}[1 2] Digit{2,5}{opt}[3 5] --> Digit{1,7}{opt}[1 2 3 5]
   * 
   * @param Expression
   *          expr to be summarized
   * @param MIN_NUMBER_OF_SAMPLES
   *          --> units will only be summarized if number of samples >= MIN_NUMBER_OF_SAMPLES
   * @return summarized expression
   */
  public static Expression summarize(Expression expr, int MIN_NUMBER_OF_SAMPLES) {
    // no need to summarize a simple ExpressionLeaf
    if (expr instanceof ExpressionLeaf) {
      if (((ExpressionLeaf) expr).getSamples().size() < MIN_NUMBER_OF_SAMPLES) {
        ((ExpressionLeaf) expr).setSamplesAlternation(true);
      }
      return expr;
    }
    // AlternationExpression: summarize each subexpression
    else if (expr instanceof AlternationExpression) {
      final ExpressionNode summarized = new AlternationExpression();
      for (final Expression subexpr : ((ExpressionNode) expr).getSubexpressions()) {
        summarized.addSubexpression(summarize(subexpr, MIN_NUMBER_OF_SAMPLES));
      }
      if (expr.isOptional()) {
        summarized.setOptional(true);
      }
      return summarized;
    }
    // ConcatenationExpression: summarize each subexpression
    else {
      final ConcatenationExpression summarized = new ConcatenationExpression();
      ExpressionLeaf summary = null;
      for (final Expression subexpression : ((ConcatenationExpression) expr).getSubexpressions()) {
        if (subexpression instanceof ExpressionNode) {
          if (summary != null) {
            summarized.addSubexpression(summary.clone());
            summary = null;
          }
          summarized.addSubexpression(summarize(subexpression, MIN_NUMBER_OF_SAMPLES));
        } else {
          final ExpressionLeaf leaf = (ExpressionLeaf) subexpression;
          if (summary == null) {
            summary = leaf;
            if (summary.getSamples().size() < MIN_NUMBER_OF_SAMPLES) {
              summary.setSamplesAlternation(true);
              summarized.addSubexpression(summary.clone());
              summary = null;
            }
          } else {
            if (!leaf.getType().equals(summary.getType())) {
              summarized.addSubexpression(summary.clone());
              summary = leaf;
              if (summary.getSamples().size() < MIN_NUMBER_OF_SAMPLES) {
                summary.setSamplesAlternation(true);
                summarized.addSubexpression(summary.clone());
                summary = null;
              }
            } else {
              if (!(leaf.getSamples().size() < MIN_NUMBER_OF_SAMPLES)) {
                summary.addSamples(leaf.getSamples());
                summary.setMaximum(summary.getMaximum() + leaf.getMaximum());
                if (!summary.isOptional() && !leaf.isOptional()) {
                  summary.setMinimum(summary.getMinimum() + leaf.getMinimum());
                } else if (!summary.isOptional() && leaf.isOptional()) {
                  // no need to adjust minimum
                } else if (summary.isOptional() && !leaf.isOptional()) {
                  summary.setMinimum(leaf.getMinimum());
                  summary.setOptional(false);
                }
                // both optional
                else {
                  summary.setMinimum(Math.min(summary.getMinimum(), leaf.getMinimum()));
                  // predecessor stays optional
                }
              } else {
                summarized.addSubexpression(summary.clone());
                summary = leaf;
                if (summary.getSamples().size() < MIN_NUMBER_OF_SAMPLES) {
                  summary.setSamplesAlternation(true);
                  summarized.addSubexpression(summary.clone());
                  summary = null;
                }
              }
            }
          }
        }
      }
      if (summary != null) {
        summarized.addSubexpression(summary.clone());
      }
      return summarized;
    }
  }

  /**
   * summarizes congruences (case sensitive)
   * 
   * @param Expression
   *          expr to be summarized
   * @return summarized expr
   */
  public static Expression summarizeCongruencesCaseSensitive(Expression expr) {
    if (expr instanceof ExpressionLeaf) {
      return expr;
    } else if (expr instanceof ConcatenationExpression) {
      final ExpressionNode summarized = new ConcatenationExpression();
      ExpressionLeaf summary = null;
      for (final Expression subexpr : ((ExpressionNode) expr).getSubexpressions()) {
        if (subexpr instanceof ExpressionLeaf) {
          if (summary == null) {
            summary = (ExpressionLeaf) subexpr.clone();
          } else {
            if (summary.equals(subexpr)) {
              summary.setSample(0, summary.getSample(0) + ((ExpressionLeaf) subexpr).getSample(0));
              summary.setMinimum(summary.getMinimum() + 1);
              summary.setMaximum(summary.getMaximum() + 1);
            } else {
              summarized.addSubexpression(summary);
              summary = (ExpressionLeaf) subexpr.clone();
            }
          }
        } else {
          if (summary != null) {
            summarized.addSubexpression(summary);
            summary = null;
          }
          summarized.addSubexpression(summarizeCongruencesCaseSensitive(subexpr));
        }
      }
      summarized.addSubexpression(summary);
      return summarized;
    } else if (expr instanceof AlternationExpression) {
      final ExpressionNode summarized = new AlternationExpression();
      for (final Expression subexpr : ((ExpressionNode) expr).getSubexpressions()) {
        summarized.addSubexpression(summarizeCongruencesCaseSensitive(subexpr));
      }
      return summarized;

    } else {
      System.out.println("What type should this be?"); //$NON-NLS-1$
      return null;
    }
  }

  /**
   * summarizes LETTER congruences (case insensitive)
   * 
   * @param Expression
   *          expr to be summarized
   * @return summarized expr
   */
  public static Expression summarizeCongruencesCaseInsensitive(Expression expr) {
    if (expr instanceof ExpressionLeaf) {
      return expr;
    } else if (expr instanceof AlternationExpression) {
      final ExpressionNode summarized = new AlternationExpression();
      for (final Expression subexpr : ((ExpressionNode) expr).getSubexpressions()) {
        summarized.addSubexpression(summarizeCongruencesCaseInsensitive(subexpr));
      }
      return summarized;
    } else /* if (expr instanceof ConcatenationExpression) */{
      final ExpressionNode summarized = new ConcatenationExpression();
      ExpressionLeaf summary = null;
      for (final Expression subexpr : ((ExpressionNode) expr).getSubexpressions()) {
        if (subexpr instanceof ExpressionLeaf) {
          if (summary == null) {
            summary = (ExpressionLeaf) subexpr.clone();
          } else {
            if (summary.equals(subexpr) || isLetterSequence(summary, (ExpressionLeaf) subexpr)) {
              summary.setSample(0, summary.getSample(0) + ((ExpressionLeaf) subexpr).getSample(0));
              summary.setMinimum(summary.getMinimum() + 1);
              summary.setMaximum(summary.getMaximum() + 1);
              if (isLetterSequence(summary, (ExpressionLeaf) subexpr)
                  && !summary.getType().equals(subexpr.getType())) {
                summary.setType(CharacterHierarchy.LETTER);
              }
            } else {
              summarized.addSubexpression(summary);
              summary = (ExpressionLeaf) subexpr.clone();
            }
          }
        } else {
          if (summary != null) {
            summarized.addSubexpression(summary);
            summary = null;
          }
          summarized.addSubexpression(summarizeCongruencesCaseInsensitive(subexpr));
        }
      }
      summarized.addSubexpression(summary);
      return summarized;
    }
  }

  /**
   * Checks if leaf1 and leaf2 are both of type letter
   * 
   * @param ExpressionLeaf
   *          leaf1 and ExpressionLeaf leaf2 --> leafs to be compared if they are a letter sequence
   * @return if both
   */
  private static boolean isLetterSequence(ExpressionLeaf leaf1, ExpressionLeaf leaf2) {
    String allSamples = ""; //$NON-NLS-1$
    for (final String sample : leaf1.getSamples()) {
      allSamples += sample;
    }
    final boolean leaf1SamplesAreLetters = CharacterHierarchy.getCharClassByName(
        CharacterHierarchy.LETTER).belongsTo(allSamples);
    if (leaf1.getType().equals(CharacterHierarchy.LOWER)
        || leaf1.getType().equals(CharacterHierarchy.UPPER)
        || leaf1.getType().equals(CharacterHierarchy.LETTER) || leaf1SamplesAreLetters) {
      // leaf 1 is of type letter
      allSamples = ""; //$NON-NLS-1$
      for (final String sample : leaf2.getSamples()) {
        allSamples += sample;
      }
      final boolean leaf2SamplesAreLetters = CharacterHierarchy.getCharClassByName(
          CharacterHierarchy.LETTER).belongsTo(allSamples);
      if (leaf2.getType().equals(CharacterHierarchy.LOWER)
          || leaf2.getType().equals(CharacterHierarchy.UPPER)
          || leaf2.getType().equals(CharacterHierarchy.LETTER) || leaf2SamplesAreLetters) {
        // leaf2 is of type letter
        return true;
      }
    }
    // either leaf1 or leaf2 or both is / are not letter(s)
    return false;
  }

}

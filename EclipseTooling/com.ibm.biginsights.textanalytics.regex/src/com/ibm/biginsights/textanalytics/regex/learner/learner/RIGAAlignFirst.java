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

import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionNode;
import com.ibm.biginsights.textanalytics.regex.learner.learner.utils.Simplifier;
import com.ibm.biginsights.textanalytics.regex.learner.learner.utils.Summarizer;

/**
 * This class implements the RIGAAlignFirst algorithm. 1) Aligns sample expression using Aligner 2)
 * summarizes aligned expression using Summarizer
 */

public class RIGAAlignFirst extends RIGALearner {



  // constructor
  public RIGAAlignFirst(ArrayList<Sample> samples, Bias bias) {
    super(samples, bias);
  }

  // methods
  /**
   * learns a regular expression from the samples provided to the learner
   */
  @Override
  public Expression learn() {

    // Unit Alignment
    final ArrayList<Expression> expressions = getSampleExpressions();
    ExpressionNode alignedExpression = (ExpressionNode) expressions.get(0);
    for (int i = 1; i < expressions.size(); i++) {
      alignedExpression = Aligner.align(alignedExpression, (ExpressionNode) expressions.get(i));
    }

    // Summarize, update and generalize aligned expression
    alignedExpression = (ExpressionNode) Summarizer.summarize(alignedExpression,
        this.bias.getSummarizer_minNoOfSamples());
    Expression.updateExpression(alignedExpression, true, this.bias);
    alignedExpression = (ExpressionNode) Simplifier.generalizeAlternations(alignedExpression);

    return alignedExpression;
  }

}

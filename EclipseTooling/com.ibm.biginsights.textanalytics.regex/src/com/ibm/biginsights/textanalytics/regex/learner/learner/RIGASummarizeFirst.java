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
import com.ibm.biginsights.textanalytics.regex.learner.learner.utils.Summarizer;

/**
 * This class implements the RIGASummarizeFirst algorithm. 1) summarize the sample expressions 2)
 * align the sample expressions
 * 
 * 
 * 
 */
public class RIGASummarizeFirst extends RIGALearner {


 
	// CONSTRUCTOR
  public RIGASummarizeFirst(ArrayList<Sample> samples, Bias bias) {
    super(samples, bias);
  }

  // METHODS
  /**
   * learns a regular expression from a sample set
   */
  @Override
  public Expression learn() {
    final ArrayList<Expression> expressions = getSampleExpressions();
    // summarize samples
    // System.out.println("SUMMARIZED:");
    for (int j = 0; j < expressions.size(); j++) {
      final Expression expression = expressions.get(j);
      if (this.bias.isCaseSensitiveSummarizing()) {
        expressions.set(j, Summarizer.summarizeCongruencesCaseSensitive(expression));
      } else {
        // CASE_INSENSITIVE_SUMMARIZING
        expressions.set(j, Summarizer.summarizeCongruencesCaseInsensitive(expression));
      }
      // System.out.println(expressions.get(j).toStringWithSamples());
    }
    // Unit Alignment
    ExpressionNode alignedExpression = (ExpressionNode) expressions.get(0);
    for (int i = 1; i < expressions.size(); i++) {
      alignedExpression = Aligner.align(alignedExpression, (ExpressionNode) expressions.get(i));
    }
    // update the learned expression
    Expression.updateExpression(alignedExpression, true, this.bias);
    return alignedExpression;
  }
}

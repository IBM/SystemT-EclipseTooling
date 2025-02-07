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

import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;

/**
 * This class is intended to be subclassed by concrete RIGALearner classes.
 * 
 * 
 * 
 */

public abstract class RIGALearner {



  // Bias object containing bias information
  Bias bias;

  // complete sample list
  protected ArrayList<Sample> samples;

  // constructor
  protected RIGALearner(ArrayList<Sample> samples, Bias bias) {
    this.samples = samples;
    this.bias = bias;
  }

  // METHODS
  /*
   * learns an expression from the samples
   */
  public abstract Expression learn();

  // PRINT, TOSTRING... METHODS

  /**
   * prints all expressions of the learner to the console for debugging purposes
   */
  public void printExpressions() {
    System.out.println(Messages.RIGALearner_EXPRESSION_OF_LEARNER);
    for (final Sample sample : this.samples) {
      System.out.println(sample.getSample() + "\n" + sample.getExpression().toStringWithSamples()); //$NON-NLS-1$
    }
  }

  // GETTER + SETTER METHODS

  public ArrayList<Sample> getSamples() {
    return this.samples;
  }

  /**
   * returns a list of the sample expressions
   * 
   * @return a list of the sample expressions
   */
  public ArrayList<Expression> getSampleExpressions() {
    final ArrayList<Expression> expressions = new ArrayList<Expression>();
    for (final Sample sample : this.samples) {
      expressions.add(sample.getExpression());
    }
    return expressions;
  }

  /**
   * returns a list of the sample strings of the learner
   * 
   * @return a list of the sample strings
   */
  public ArrayList<String> getSampleStrings() {
    final ArrayList<String> sampleStrings = new ArrayList<String>();
    for (final Sample s : this.samples) {
      sampleStrings.add(s.getSample());
    }
    return sampleStrings;
  }

}

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
package com.ibm.biginsights.textanalytics.regex.learner.suggest;

import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.learner.utils.Converter;

/**
 * This class encapsulates the information about a regular expression suggested by a RegExLearner
 * algorithm. - tree representation of the regex - string representation of the regex (a first
 * suggestion) - an hint / hint when this regex might be useful
 * 
 * 
 * 
 */

public class SuggestedRegex {



  private Expression regularExpression;

  private final String regexString;

  private String hint;

  // CONSTRUCTOR
  public SuggestedRegex(Expression regularExpression, String hint) {
    this.regularExpression = regularExpression;
    this.regexString = Converter.toRegex(regularExpression);
    this.hint = hint;
  }

  public Expression getRegularExpression() {
    return this.regularExpression;
  }

  public void setRegularExpression(Expression regularExpression) {
    this.regularExpression = regularExpression;
  }

  public String getRegexString() {
    return this.regexString;
  }

  public String getHint() {
    return this.hint;
  }

  public void setHint(String hint) {
    this.hint = hint;
  }

}

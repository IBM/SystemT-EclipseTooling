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
package com.ibm.biginsights.textanalytics.concordance.ui.filter;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.IInputValidator;

/**
 * Regex syntax check using Java's regex compiler.
 */
public class RegexPatternInputValidator implements IInputValidator {


  
  private boolean isRegex;
  
  public RegexPatternInputValidator(final boolean isRegex) {
    super();
    this.isRegex = isRegex;
  }

  public String isValid(String newText) {
    if (this.isRegex) {
      try {
        Pattern.compile(newText);
      } catch (PatternSyntaxException e) {
        return "Regex syntax error: " + e.getMessage();
      }
    }
    // null means no error
    return null;
  }
  
  public void toggleRegex() {
    if (this.isRegex) {
      this.isRegex = false;
    } else {
      this.isRegex = true;
    }
  }

}

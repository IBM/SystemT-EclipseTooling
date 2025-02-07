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
package com.ibm.biginsights.textanalytics.workflow.plan.validators;

import org.eclipse.jface.dialogs.IInputValidator;

import com.ibm.biginsights.textanalytics.workflow.messages.Messages;

/**
 * validator to check that a user enter a non empty value
 * 
 * 
 */
public class NoEmptyComponentInputValidator implements IInputValidator
{



  @Override
  public String isValid (String newText)
  {
    if (newText.isEmpty ()) { return Messages.field_empty_error_message; }
    return null;
  }

}

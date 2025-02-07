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
package com.ibm.biginsights.textanalytics.aql.editor.ui;

import org.eclipse.jface.text.rules.IWordDetector;

public class TypeNameDetector implements IWordDetector {



  @Override
  public boolean isWordStart(char c) {
    switch (c) {
    case 'B': return true;
    case 'F': return true;
    case 'S': return true;
    case 'T': return true;
    case 'I': return true;
    default: return false;
    }
  }

  @Override
  public boolean isWordPart(char c) {
    return Character.isLetter(c);
  }

}

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

import static com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel.StringFilterType.LEFT_CONTEXT;

public class LeftContextFilterHandler extends TextPatternFilterHandler {



  public LeftContextFilterHandler() {
    super(LEFT_CONTEXT, "Left Context Text Filter",
        "Specify a text pattern that the left context must match.\nUse '*' as a wildcard character, e.g., \"*foo*bar*\".");
  }
  
}

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

package com.ibm.biginsights.textanalytics.refactor.ui.wizards;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameInfo;
import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameRefactoring;



public class AQLElementRenameWizard extends RefactoringWizard {



  private final AQLElementRenameInfo info;


  public AQLElementRenameWizard( final AQLElementRenameRefactoring refactoring,
                               final AQLElementRenameInfo info ) {
    super( refactoring, DIALOG_BASED_USER_INTERFACE );
    this.info = info;
  }

  // Call the customized user wizard page..
  protected void addUserInputPages() {
    setDefaultPageTitle( getRefactoring().getName() );
    addPage( new AQLElementRenameInputPage( info ) );
  }
}

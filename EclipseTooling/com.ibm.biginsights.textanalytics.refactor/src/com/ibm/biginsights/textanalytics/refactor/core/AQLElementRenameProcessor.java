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

package com.ibm.biginsights.textanalytics.refactor.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;;

public class AQLElementRenameProcessor extends RefactoringProcessor {



  private final AQLElementRenameInfo info;
  private final AQLElementRenameDelegate delegate;

  public AQLElementRenameProcessor( final AQLElementRenameInfo info ) {
    this.info = info;
    delegate = new AQLElementRenameDelegate( info );
  }

  public Object[] getElements() {
    return new Object[] { info.getOldName() };
  }

  public String getIdentifier() {
    return getClass().getName();
  }

  public String getProcessorName() {
    return AQLElementRenameCoreTexts.AQLElementRenameProcessor_name;
  }

  public boolean isApplicable() throws CoreException {
    return true;
  }

  public RefactoringStatus checkInitialConditions( final IProgressMonitor pm ) {
    return delegate.checkInitialConditions();
  }

  public RefactoringStatus checkFinalConditions( 
            final IProgressMonitor pm,  final CheckConditionsContext context ) {
    return delegate.checkFinalConditions( pm, context );
  }

  public Change createChange( final IProgressMonitor pm ) {
    CompositeChange result = new CompositeChange( getProcessorName() ); 
    delegate.createChange( pm, result );
    return result;
  }

  public RefactoringParticipant[] loadParticipants( 
                               final RefactoringStatus status, 
                               final SharableParticipants sharedParticipants ) {
    // This would be the place to load the participants via the 
    // ParticipantManager and decide which of them are allowed to participate. 
    // Useful when we consider extraction plan and launch config participants
    return new RefactoringParticipant[ 0 ];
  }
}

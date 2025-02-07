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
package com.ibm.biginsights.textanalytics.workflow.plan.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.regex.command.RegexContainer;
import com.ibm.biginsights.textanalytics.regex.command.RegexHandlerUtil;
import com.ibm.biginsights.textanalytics.regex.learner.ui.RegexLearnerWizard;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomWizardDialog;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;

/**
 * Action to open Regular Expression Generator for a selection of examples.
 */
public class OpenRegexGenAction extends Action
{


  
	List<String> clues;

  public OpenRegexGenAction (IStructuredSelection selection)
  {
    setImageDescriptor (Activator.getImageDescriptor ("regexGenerator.gif"));   // $NON-NLS-1$
    setText (Messages.open_regex_generator_text);
    setClues(selection);
  }

  /**
   * Open the regular expression generator and load the selected examples (clues) into the Samples area.
   */
  @Override
  public void run ()
  {
    Shell shell = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getShell ();
    RegexContainer regexContainer = new RegexContainer ();
    regexContainer.setRegex ("");   // $NON-NLS-1$
    RegexLearnerWizard regexGenWizard = new RegexLearnerWizard (regexContainer);
    regexGenWizard.setSamples(clues);
    WizardDialog dialog = new CustomWizardDialog (shell, regexGenWizard);

    final int rc = dialog.open ();
    if (rc == Window.OK) {
      String resultRegex = regexContainer.getRegex();
      final Clipboard cb = new Clipboard(shell.getDisplay());

      if ( ! StringUtils.isEmpty (resultRegex) ) {
        resultRegex = RegexHandlerUtil.wrapStringAsRegex (resultRegex);

        // Copy regex to clipboard
        TextTransfer textTransfer = TextTransfer.getInstance ();
        cb.setContents (new Object[] { resultRegex }, new Transfer[] { textTransfer });

        // Tell user about the regex in clipboard
        CustomMessageBox msgBox = CustomMessageBox.createInfoMessageBox (shell,
              com.ibm.biginsights.textanalytics.regex.Messages.RegexLearnerWizard_REGEX_GENERATOR,
              resultRegex + "\n\n" + com.ibm.biginsights.textanalytics.regex.Messages.ExprBuilderWizard_INFO_COPY_CLIPBOARD); //$NON-NLS-1$

        msgBox.open ();
      }

    }
  }

  private void setClues (IStructuredSelection selection)
  {
    clues = new ArrayList<String> ();
    if ( selection != null && !selection.isEmpty () )
    {
      for ( Object selTO : selection.toList () ) {
        clues.add (((TreeObject)selTO).getLabel ());
      }
    }
  }

}

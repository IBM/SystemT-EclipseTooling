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
package com.ibm.biginsights.textanalytics.regex.learner.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.regex.Activator;
import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.command.RegexContainer;
import com.ibm.biginsights.textanalytics.regex.learner.suggest.SuggestedRegex;

/**
 * 
 * 
 *         This wizard allows to import a text file with samples of a regular concepts and then
 *         suggests various regular expressions for the concept. The concept can then be refined by
 *         direct interaction for each expression subgroup. When finishing, the regex field of the
 *         rule section is assigned the "learned" regular expression.
 */

public class RegexLearnerWizard extends Wizard implements INewWizard {



  public static String WIZARD_ID = "com.ibm.biginsights.textanalytics.regex.learner.ui"; //$NON-NLS-1$

  public static int TABLE_ITEM_COUNT = 50;

  // FIXME: I guess this is where the result goes?
  private final RegexContainer regexContainer;

//  private ISelection selection;

  private RegexLearnerWizardView1 page1;

  protected RegexLearnerWizardView2 page2;

  protected ArrayList<String> samples = new ArrayList<String>();

  protected ArrayList<SuggestedRegex> suggestedRegexes = new ArrayList<SuggestedRegex>();

  protected int selectedRegexIndex = 0;

  boolean canFinish = false;

  public RegexLearnerWizard(RegexContainer regexContainer) {
    this.regexContainer = regexContainer;
    setWindowTitle(Messages.RegexLearnerWizard_REGEX_GENERATOR);
  }

  @Override
  public void addPages() {
    this.page1 = new RegexLearnerWizardView1(Messages.RegexLearnerWizard_REGEX_GEN_PAGE1, this);
    addPage(this.page1);
    this.page2 = new RegexLearnerWizardView2(Messages.RegexLearnerWizard_REGEX_GEN_PAGE2, this);
    addPage(this.page2);
  }

  @Override
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);
    // enable contextual help
    for (int i = 0; i < getPages().length; i++) {
      final Control pageControl = (getPages()[i]).getControl().getShell();
      PlatformUI.getWorkbench().getHelpSystem()
          .setHelp(pageControl, Activator.TA_REGEX_LEARNER_WIZARD_CONTEXTID);
    }
  }

  @Override
  public boolean performFinish() {
    // update regex
	    //System.out.println("performing finish");
	    
    // set current samples to list of table
	if(getContainer().getCurrentPage().getClass().getSimpleName().equals("RegexLearnerWizardView2"))
	{
	try
	{
    final boolean error = this.page2.controller.checkInputData(false);
    if (!error) {
      this.page2.controller.changeLeaf();
      this.page2.updateFinalRegex();
    } else {
      final Status status = new Status(
          IStatus.ERROR,
          RegexLearnerWizard.WIZARD_ID,
          0,
          Messages.RegexLearnerWizard_REGEX_GEN_ERROR,
          null);
      ErrorDialog.openError(Display.getCurrent().getActiveShell(), Messages.RegexLearnerWizard_CHANGES_ERROR,
          Messages.RegexLearnerWizard_CHANGES_ERROR_INCONSISTANT, status);
    }
	}
	catch(NullPointerException ne)
	{
		//TODO here
	}
		//System.out.println("replacing text from textbox: " + this.page2.finalRegex);
		this.regexContainer.setRegex(this.page2.finalRegex);
	}
	else
	{
		try
		{
			//System.out.println("replacing text from radiobutton: ");
			this.regexContainer.setRegex(this.page2.wizard.suggestedRegexes.get(this.page2.wizard.selectedRegexIndex).getRegexString());
		}
		catch(Exception e)
		{
			//TODO here
		}
	}
    // this.mRuleWrapper.getRule().setRegEx(this.page2.finalRegex);    
    return true;
  }

  @Override
  public boolean canFinish() {
    return this.canFinish;
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
//    this.selection = selection;
  }

  public void setSamples (List<String> clues)
  {
    samples.clear ();

    if ( clues != null && !clues.isEmpty () ) {
      for (int i = 0; i < TABLE_ITEM_COUNT && i < clues.size (); i++) {
        samples.add (clues.get (i).trim ());
      }
    }
  }

}

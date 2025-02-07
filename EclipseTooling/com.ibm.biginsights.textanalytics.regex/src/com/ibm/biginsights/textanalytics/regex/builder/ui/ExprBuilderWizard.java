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
package com.ibm.biginsights.textanalytics.regex.builder.ui;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.regex.Activator;
import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.builder.ruleset.MatchStrategyType;
import com.ibm.biginsights.textanalytics.regex.command.RegexContainer;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;

/**
 * 
 *  Abraham
 * 
 */
public class ExprBuilderWizard extends Wizard {



  protected ExprBuilderWizardPage wizardPage = null;

  protected RegexContainer regexContainer = null;
  private String errorMessage = "";
  protected boolean rulesModelDirty = false;

  protected boolean enableHighLighting;

  public ExprBuilderWizard(RegexContainer regexWrapper, boolean enableHighLighting) {
    super();
    setWindowTitle(Messages.ExprBuilderWizard_REGEX_BUILDER); 
    this.regexContainer = regexWrapper;
    this.enableHighLighting = enableHighLighting;
  }

  @Override
  public void addPages() {
    this.wizardPage = new ExprBuilderWizardPage(Messages.ExprBuilderWizard_REGEX_BUILDER, 
        Messages.ExprBuilderWizard_REGEX_BUILDER, null); 
//    String name = this.mRuleWrapper.getRule().getName();
    String name = null;
    if (name == null) {
      name = "";  //$NON-NLS-1$
    }
    String testString = null;
    if (testString == null) {
      testString = "";  //$NON-NLS-1$
      // create list of names
      // List existingValues = new LinkedList();
      // Iterator it = regexContainer.getParentType().getRulesList().getItemList()
      // .iterator();
      // while (it.hasNext()) {
      // existingValues.add(((RuleWrapper) it.next()).getLabel()
      // .toUpperCase());
      // }
    }

    this.wizardPage.setInput(name, MatchStrategyType.MATCH_ALL_LITERAL,
        this.regexContainer.getRegex(), testString, this.enableHighLighting);
    addPage(this.wizardPage);
  }

  @Override
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);
    // enable contextual help
    for (int i = 0; i < getPages().length; i++) {
      final Control pageControl = (getPages()[i]).getControl().getShell();
      PlatformUI.getWorkbench().getHelpSystem().setHelp(pageControl,
          Activator.TA_EXPRESSION_BUILDER_WIZARD_CONTEXTID);
    }
  }

  @Override
  public boolean performFinish() {

    // if (!regexContainer.getRule().getName().equals(wizardPage.getRuleName())) {
    // rulesModelDirty = true;
    // regexContainer.getRule().setName(wizardPage.getRuleName());
    // }

//    if (!this.mRuleWrapper.getRule().getMatchStrategy().equals(this.wizardPage.getMatchStrategy())) {
//      this.rulesModelDirty = true;
//      this.mRuleWrapper.getRule().setMatchStrategy(this.wizardPage.getMatchStrategy());
//    }
//
//    if (!this.mRuleWrapper.getRule().getRegEx().equals(this.wizardPage.getRuleRegex())) {
//      this.rulesModelDirty = true;
//      this.mRuleWrapper.getRule().setRegEx(this.wizardPage.getRuleRegex());
//    }
//
//    if (((this.mRuleWrapper.getRule().getTestString() == null) && (this.wizardPage.getTestInput()
//        .length() > 0))
//        || ((this.mRuleWrapper.getRule().getTestString() != null) && !this.mRuleWrapper.getRule()
//            .getTestString().equals(this.wizardPage.getTestInput()))) {
//      this.rulesModelDirty = true;
//      this.mRuleWrapper.getRule().setTestString(this.wizardPage.getTestInput());
//    }

    //TODO tg: implement returning of generated rule here
	if(validateRegexSyntax())
	{
		this.regexContainer.setRegex(this.wizardPage.getRuleRegex());
	    return true;
	}    
	else
	{
		CustomMessageBox msgBox = CustomMessageBox.createErrorMessageBox(this.wizardPage.getShell(), Messages.ExprBuilderWizard_REGEX_BUILDER, Messages.ExprBuilderWizard_VALIDATION_ERROR + "\n\n" + errorMessage); //$NON-NLS-1$ //$NON-NLS-2$
		msgBox.open();	
		return false;
	}
	
  }
  
  public boolean validateRegexSyntax()
  {
	  String regexRule = RuleTextComposite.removeInvisibleCharacters(this.wizardPage.getRuleRegex());
      try
      {
	  	Pattern.compile(regexRule);
      }
      catch(PatternSyntaxException e)
      {
    	  errorMessage = e.getMessage().substring(0, e.getMessage().lastIndexOf("\n"));
    	  return false;
      }
	  return true;
  }
  
  public boolean performCancel()
  {
    this.regexContainer.setRegex(this.wizardPage.getRuleRegex());
	return true;	  
  }
  

  public boolean isRulesModelDirty() {
    return this.rulesModelDirty;
  }

  public boolean isHighLightingEnabled() {
    return this.wizardPage.isHighLightingEnabled();
  }

}

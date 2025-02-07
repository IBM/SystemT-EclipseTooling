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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.builder.ruleset.MatchStrategyType;

/**
 * 
 *  Abraham
 * 
 */
public class ExprBuilderWizardPage extends WizardPage {



  protected String ruleName = "", ruleRegex = "", testInput = "";    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

  protected MatchStrategyType matchStrategy;

  // protected List existingRuleNames;

  protected boolean isRefresh = false;

  protected Combo mComboMatchStrategy;

  // help section
  TabbedRegexLibraryControl regexLibControl;

  // rule regex section
  protected StyledText mTextRule;

  protected RuleTextComposite ruleTextComposite;

  protected boolean enableHighLighting;

  // test regex section
  protected RuleTestComposite testComposite;

  /**
   * call setInput mathode to set the variables
   * 
   * @param pageName
   * @param title
   * @param titleImage
   */
  public ExprBuilderWizardPage(String pageName, String title, ImageDescriptor titleImage) {
    super(pageName, title, titleImage);
    setDescription(Messages.ExprBuilderWizardPage_WIZARD_DESCRIPTION); 
    //setImageDescriptor(Activator.getImageDescriptor(Messages.ExprBuilderWizardPage_WIZARD_IMG));
  }

  /**
   * call setInput mathode to set the variables
   * 
   * @param pageName
   */
  public ExprBuilderWizardPage(String pageName) {
    super(pageName);
  }

  public void setInput(String ruleName, MatchStrategyType matchStrategy, String ruleRegex,
      String testInput, boolean enableHighLighting) {
    this.ruleName = ruleName;
    this.matchStrategy = matchStrategy;
    this.ruleRegex = ruleRegex;
    this.testInput = testInput;
    this.enableHighLighting = enableHighLighting;
    // this.existingRuleNames = existingRuleNames;
  }

  public void createControl(Composite parent) {
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.regex_builder");
    this.isRefresh = true;
    final Composite container = new Composite(parent, SWT.NULL);
    final FormLayout layout = new FormLayout();
    container.setLayout(layout);

    Control topControl = createRuleIdentControl(container);
    topControl = createRegexLibraryControl(container, topControl);
    topControl = createRuleTextControl(container, topControl);
    createTestRuleControl(container, topControl);

    this.regexLibControl.setRuleText(this.mTextRule);

    setErrorMessage(null);
    setControl(container);
    this.isRefresh = false;

    this.testComposite.testRegex();
    checkForMatchFlags();
  }

  protected Control createRuleIdentControl(Composite parent) {
//    final Label ruleNameLabel = new Label(parent, SWT.NONE);
//    ruleNameLabel
//        .setText(RegExMessages.getString("RuleSection.createRuleSection.RuleNameLabel") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
//    ruleNameLabel.setAlignment(SWT.CENTER);
//
//    FormData formData = new FormData();
//    formData.top = new FormAttachment(0, 5);
//    formData.left = new FormAttachment(0, 5);
//    ruleNameLabel.setLayoutData(formData);
//
//    final Text ruleText = new Text(parent, SWT.NONE);
//    ruleText.setText(this.ruleName);
//    ruleText.setEditable(false);
//
//    formData = new FormData();
//    formData.top = new FormAttachment(0, 5);
//    formData.left = new FormAttachment(ruleNameLabel, 0);
//    ruleText.setLayoutData(formData);
//
//    final Label label = new Label(parent, SWT.NONE);
//    label.setText(RegExMessages.getString("RuleSection.97")); //$NON-NLS-1$
//    label.setAlignment(SWT.CENTER);
//
//    formData = new FormData();
//    formData.top = new FormAttachment(0, 5);
//    formData.left = new FormAttachment(ruleText, 15);
//    label.setLayoutData(formData);
//
//    // create a group for a match strategy buttons
//    this.mComboMatchStrategy = new Combo(parent, SWT.READ_ONLY);
//    this.mComboMatchStrategy.addSelectionListener(new SelectionAdapter() {
//      /**
//       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
//       */
//      @Override
//      public void widgetSelected(SelectionEvent e) {
//        final MatchStrategyType type = MatchStrategyType
//            .get(ExprBuilderWizardPage.this.mComboMatchStrategy.getSelectionIndex());
//        if (!ExprBuilderWizardPage.this.isRefresh) {
//          ExprBuilderWizardPage.this.matchStrategy = type;
//        }
//      }
//    });
//
//    formData = new FormData();
//    formData.top = new FormAttachment(0, 5);
//    formData.left = new FormAttachment(label, 5);
//    formData.right = new FormAttachment(100, -5);
//    this.mComboMatchStrategy.setLayoutData(formData);
//
//    final String[] MATCH_STRATEGY = new String[] { RegExMessages.getString("RuleSection.0"), //$NON-NLS-1$
//        RegExMessages.getString("RuleSection.1"), //$NON-NLS-1$
//        RegExMessages.getString("RuleSection.2") }; //$NON-NLS-1$ 
//
//    this.mComboMatchStrategy.setItems(MATCH_STRATEGY);
//    this.mComboMatchStrategy.select(this.matchStrategy.getValue());
//
    return this.mComboMatchStrategy;

  }

  protected Control createRegexLibraryControl(Composite parent, Control topControl) {
//    final Text label = new Text(parent, SWT.NONE);
//    label.setText(RegExMessages.getString("ExprBuilderWizardPage.6")); //$NON-NLS-1$
//    label.setEditable(false);
//
//    FormData formData = new FormData();
//    formData.top = new FormAttachment(topControl, 5);
//    formData.left = new FormAttachment(0, 5);
//    label.setLayoutData(formData);

    final Text label2 = new Text(parent, SWT.NONE);
    label2.setText(Messages.ExprBuilderWizardPage_CONSTRUCT_DESCRIPTION); 
    label2.setEditable(false);

    FormData formData = new FormData();
    formData.top = new FormAttachment(topControl, 5);
    formData.left = new FormAttachment(0, 5);
    label2.setLayoutData(formData);

    final Composite tabComposite = new Composite(parent, SWT.BORDER);
    tabComposite.setLayout(new FormLayout());
    formData = new FormData();
    formData.top = new FormAttachment(label2, 5);
    formData.left = new FormAttachment(0, 5);
    formData.right = new FormAttachment(100, -5);
    formData.height = 200;
    formData.width = 700;
    tabComposite.setLayoutData(formData);

    this.regexLibControl = new TabbedRegexLibraryControl(getShell());
    this.regexLibControl.createControl(tabComposite);

    return tabComposite;

  }

  protected Control createRuleTextControl(Composite parent, Control topControl) {

    final Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.ExprBuilderWizardPage_RULE_DESCRIPTION); 
    label.setAlignment(SWT.CENTER);

    FormData formData = new FormData();
    formData.top = new FormAttachment(topControl, 5);
    formData.left = new FormAttachment(0, 5);
    label.setLayoutData(formData);

    // create a text box for the rule
    this.ruleTextComposite = new RuleTextComposite();
    this.mTextRule = this.ruleTextComposite.createControl(parent, null);
    this.mTextRule.setText(this.ruleRegex);
    this.mTextRule.addExtendedModifyListener(new ExtendedModifyListener() {
      public void modifyText(ExtendedModifyEvent event) {
        ExprBuilderWizardPage.this.testComposite.testRegex();
        checkForMatchFlags();
        if (!ExprBuilderWizardPage.this.isRefresh) {
          ExprBuilderWizardPage.this.ruleRegex = ExprBuilderWizardPage.this.mTextRule.getText();
        }
      }
    });
    this.ruleTextComposite.setHightLighting(this.enableHighLighting);

    formData = new FormData();
    formData.top = new FormAttachment(label, 5);
    formData.left = new FormAttachment(0, 5);
    formData.right = new FormAttachment(100, -5);
    formData.height = 93; // bottom = new FormAttachment (0, 100);
    this.mTextRule.setLayoutData(formData);

    return this.mTextRule;
  }

  private void checkForMatchFlags() {
    // check, if there are matchflags
    final String[] matchFlagConstructs = RegExConstructs
        .getRegularExpressionContructs(RegExConstructs.CONSTRUCTS_MATCHFLAGS);
    final String[] matchFlagRules = RegExConstructs
        .getRegexForRegularExpressionContructs(RegExConstructs.CONSTRUCTS_MATCHFLAGS);
    final String text = this.mTextRule.getText();
    for (int i = 0; i < matchFlagRules.length; i++) {
      final Button button = this.regexLibControl.getButtonForConstruct(matchFlagConstructs[i]);
      if (button != null) {
        // find character constructs
        final Pattern pattern = Pattern.compile(matchFlagRules[i]);
        final Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
          button.setSelection(true);
        } else {
          button.setSelection(false);
        }
      }
    }
  }

  protected Control createTestRuleControl(Composite parent, Control topControl) {
    this.testComposite = new RuleTestComposite(this.mTextRule);
    final Control lastControl = this.testComposite.createTestRuleControl(parent, topControl);
    this.testComposite.getMTextInput().setText(this.testInput);
    this.testComposite.getMTextInput().addExtendedModifyListener(new ExtendedModifyListener() {
      /**
       * @see org.eclipse.swt.custom.ExtendedModifyListener#modifyText(org.eclipse.swt.custom.ExtendedModifyEvent)
       */
      public void modifyText(ExtendedModifyEvent event) {
        if (!ExprBuilderWizardPage.this.isRefresh) {
          ExprBuilderWizardPage.this.testInput = ExprBuilderWizardPage.this.testComposite
              .getMTextInput().getText();
        }
      }
    });

    return lastControl;
  }

  public MatchStrategyType getMatchStrategy() {
    return this.matchStrategy;
  }

  // public String getRuleName() {
  // return ruleName;
  // }
  public boolean isHighLightingEnabled() {
    return this.ruleTextComposite.isHighLightingEnabled();
  }

  public String getRuleRegex() {
    return this.ruleRegex;
  }

  public String getTestInput() {
    return this.testInput;
  }

}

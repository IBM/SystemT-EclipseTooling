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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.regex.Activator;
import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionLeaf;
import com.ibm.biginsights.textanalytics.regex.learner.learner.utils.Converter;
import com.ibm.biginsights.textanalytics.regex.learner.utils.Traversal;

/**
 *  This class assembles the view used for the second page of the RegexLearner wizard
 *         (interaction page to refine expression leafs.)
 */
public class RegexLearnerWizardView2 extends WizardPage {


 
	String finalRegex;

  // references to controller and wizard
  RegexLearnerWizardController2 controller;

  RegexLearnerWizard wizard;

  // container
  Composite container;

  // COMPOSITES
  Composite leftPartComposite;
  Composite leftRightComposite;
  Composite bottomPartComposite;

  // Regex text box
  StyledText regexText;

  Label regexLabel;

  // Back and Next buttons to navigate from one subexpression to another
  Composite navigationComposite;

  Button nextSubexpressionButton;

  Button previousSubexpressionButton;





  // Table with positive samples
  Table samplesTable;

  // Styled Text Box for testing

  StyledText testBox;

  // RIGHT PART COMPOSITE
  Composite rightPartComposite;


  // group with information on the current subexpression

  Text currentSubexpressionLabel;

  // Refinement group - Settings
  Group refinementGroup;

  Button optionalCheckbox;

  Button applyButton;

  // Type of this subexpression group (with radio buttons)
  Composite typeComposite;

  Label typeLabel;

  // Type - Option: "Any of the symbols in this Character Class"
  int numOfRefinerOptions = 2;

  Button anySymbolRadioButton;

  Button changeCharClassButton;

  Label characterClassLabel2;

  Label characterClassLabel;

  Text characterClassExplanationText;

  Label minOccurrencesLabel;

  Label minOccurrencesLabel2;

  Text minOccurencesText;

  Label maxOccurrencesLabel;

  Label maxOccurrencesLabel2;

  Text maxOccurencesText;

  // Type - Option: "Integer Number Digit Range"
  Button digitRangeRadioButton;

  Composite digitRangeGroup;

  Label minRangeLabel;

  Text minRangeText;

  Label maxRangeLabel;

  Text maxRangeText;

  Button allowLeadingZerosButton;

  // Type - Option: "Alternation of the following samples"
  Button certainSamplesRadioButton;

  Table leafSamplesTable;

  Composite hintComposite;

  Label infoImageLabel;

  Text hintTextLabel;

  Composite applyComposite;

	Composite currentSubexpComposite;

	Button importButton;


  /*
   * Constructor
   */
  protected RegexLearnerWizardView2(String pageName, RegexLearnerWizard wizard) {
    super(pageName);
    setTitle(Messages.RegexLearnerWizardView2_REGEX_GENERATOR);
    //setImageDescriptor(Activator.getImageDescriptor(Messages.RegexLearnerWizardView2_REGEX_GEN_WIZARD_IMAGE));
    setDescription(Messages.RegexLearnerWizardView2_WIZARD_DESC);
    this.controller = new RegexLearnerWizardController2(this);
    this.wizard = wizard;
  }

  /*
   * create the user interface control items
   */
  @Override
  public void createControl(Composite parent) {
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.regex_generator");
    GridData gridData;
    GridLayout gd = new GridLayout();
    this.container = new Composite(parent, SWT.FILL);
    this.container.setLayout(gd);
            
    //CREATE LEFT RIGHT GRID
    this.leftRightComposite = new Composite(this.container, SWT.NONE);
    this.leftRightComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
   
    //left part
    this.leftPartComposite = new Composite(this.leftRightComposite, SWT.NONE);
    this.leftPartComposite.setLayout(new GridLayout(1, false));
    this.refinementGroup = new Group(this.leftPartComposite, SWT.NONE);
    this.refinementGroup.setLayout(new GridLayout(1, false));
    this.refinementGroup.setText(Messages.RegexLearnerWizardView2_LABEL_REFINE_CURR_SUBEXP);
    gridData = new GridData();
    gridData.minimumWidth = 420;
    gridData.verticalIndent = -5;
    gridData.widthHint = 420;
    this.refinementGroup.setLayoutData(gridData);
    this.refinementGroup.setSize(375,300);
    this.currentSubexpComposite = new Composite(this.refinementGroup, SWT.NONE);
    this.currentSubexpComposite.setLayout(new GridLayout(2, false));
    this.typeLabel = new Label(this.currentSubexpComposite, SWT.NONE);
    this.typeLabel.setText(Messages.RegexLearnerWizardView2_LABEL_SUBEXP);
    gridData = new GridData();
    gridData.verticalIndent = 0;
    gridData.minimumWidth = 170;
    gridData.widthHint = 170;
    this.typeLabel.setLayoutData(gridData);
    this.currentSubexpressionLabel = new Text(this.currentSubexpComposite, SWT.BOLD | SWT.READ_ONLY);
    this.currentSubexpressionLabel.setText("s"); //$NON-NLS-1$
    this.currentSubexpressionLabel.setFont(new Font(this.refinementGroup.getDisplay(), "sans serif", 10, SWT.BOLD)); //$NON-NLS-1$
    gridData = new GridData();
    gridData.minimumWidth = 130;
    gridData.widthHint = 130;
    gridData.grabExcessHorizontalSpace = true;
    this.currentSubexpressionLabel.setLayoutData(gridData);
    // create type composite (for radio buttons etc.)
    this.typeComposite = new Composite(this.refinementGroup, SWT.NONE);
    this.typeComposite.setLayout(new GridLayout(1, false));
    createAnySymbolOption();
    createCertainSamplesOption();
    createDigitRangeOption();
    // horizontal line
    final Label separator = new Label(this.refinementGroup, SWT.HORIZONTAL | SWT.SEPARATOR);
    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    this.applyComposite = new Composite(this.refinementGroup, SWT.NONE);
    this.applyComposite.setLayout(new GridLayout(2, false));
    this.optionalCheckbox = new Button(this.applyComposite, SWT.CHECK);
    this.optionalCheckbox.setText(Messages.RegexLearnerWizardView2_LABEL_SUBEXP_OPTIONAL);
    this.optionalCheckbox.setToolTipText(Messages.RegexLearnerWizardView2_INFO_PART_OCCURS_IN_SAMPLES);
    gridData = new GridData();
    gridData.minimumWidth = 270;
    gridData.widthHint = 270;
    gridData.verticalIndent = 0;
    this.optionalCheckbox.setLayoutData(gridData);
    this.applyButton = new Button(this.applyComposite, SWT.PUSH);
    this.applyButton.setText(Messages.RegexLearnerWizardView2_APPLY);
    this.applyButton.addSelectionListener(this.controller);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.RIGHT;
    this.applyButton.setLayoutData(gridData);
    
    // right PART
    this.rightPartComposite = new Composite(this.leftRightComposite, SWT.NONE);
    this.rightPartComposite.setLayout(new GridLayout(1, false));
//    this.matchingMessageComposite = new Composite(this.rightPartComposite, SWT.NONE);
//    this.matchingMessageComposite.setLayout(new RowLayout());
//    //this.matchingMessageImageLabel = new Label(this.matchingMessageComposite, SWT.NONE);
//    this.matchingMessageLabel = new Label(this.matchingMessageComposite, SWT.BOLD);
//    // create table with "whole" samples
        
    
    
    this.regexLabel = new Label(this.rightPartComposite, SWT.NONE);
    this.regexLabel.setText(Messages.RegexLearnerWizardView2_LABEL_REGEX);
    this.regexLabel.setFont(new Font(this.rightPartComposite.getDisplay(), "sans serif", 10, SWT.BOLD)); //$NON-NLS-1$
    this.regexText = new StyledText(this.rightPartComposite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.H_SCROLL);
    this.regexText.setEditable(false);
    gridData = new GridData();
    gridData.minimumWidth = 380;
    gridData.widthHint = 380;
    gridData.horizontalIndent = 5;
    gridData.minimumHeight = 50;
    gridData.heightHint = 50;
    //gridData.verticalAlignment = SWT.BEGINNING;
    //gridData.horizontalAlignment = 5;
    this.regexText.setLayoutData(gridData);
    this.regexText.setFont(new Font(this.rightPartComposite.getDisplay(), "sans serif", 10, SWT.BOLD)); //$NON-NLS-1$
    // create composite for "back" and "next" buttons
    this.navigationComposite = new Composite(this.rightPartComposite, SWT.NONE);
    this.navigationComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
    gridData = new GridData();
    gridData.horizontalIndent = 5;    
    this.navigationComposite.setLayoutData(gridData);
    
    // add previousSubexpressionButton
    this.previousSubexpressionButton = new Button(this.navigationComposite, SWT.PUSH);
    this.previousSubexpressionButton.setText(Messages.RegexLearnerWizardView2_BUTTON_BACK);
    this.previousSubexpressionButton.setToolTipText(Messages.RegexLearnerWizardView2_LABEL_REFINE_PREV_SUBEXP);
    this.previousSubexpressionButton.addSelectionListener(this.controller);
    this.previousSubexpressionButton.setEnabled(false);
    // add nextSubexpressionButton
    this.nextSubexpressionButton = new Button(this.navigationComposite, SWT.PUSH);
    this.nextSubexpressionButton.setText(Messages.RegexLearnerWizardView2_BUTTON_NEXT);
    this.nextSubexpressionButton.setToolTipText(Messages.RegexLearnerWizardView2_LABEL_REFINE_NEXT_SUBEXP);
    this.nextSubexpressionButton.addSelectionListener(this.controller);
    
    
    createTable(this.rightPartComposite);
    this.bottomPartComposite = new Composite(this.rightPartComposite, SWT.NONE);
    this.bottomPartComposite.setLayout(new GridLayout());
    this.importButton = new Button(this.bottomPartComposite, SWT.PUSH);
    this.importButton.setText(Messages.RegexLearnerWizardView2_IMPORT);
    this.importButton.addSelectionListener(this.controller);
    this.testBox = new StyledText(this.bottomPartComposite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.H_SCROLL);
    this.testBox.setEditable(true);
    gridData = new GridData(SWT.BOTTOM);
    gridData.minimumWidth = 380;
    gridData.widthHint = 380;
    gridData.minimumHeight = 100;
    gridData.heightHint = 100;
    gridData.horizontalIndent = 0;
    this.testBox.setLayoutData(gridData);
    this.testBox.setText(Messages.RegexLearnerWizardView2_INFO_TYPE_TO_TEST_REGEX);
    this.testBox.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        final ArrayList<StyleRange> rangesList = new ArrayList<StyleRange>();
        final Pattern pattern = Pattern.compile(RegexLearnerWizardView2.this.regexText.getText());
        final Matcher matcher = pattern.matcher(RegexLearnerWizardView2.this.testBox.getText());
        while (matcher.find()) {
          final StyleRange range = new StyleRange();
          range.start = matcher.start(0);
          range.length = matcher.end(0) - matcher.start(0);
          range.background = RegexLearnerWizardView2.this.bottomPartComposite.getDisplay()
              .getSystemColor(SWT.COLOR_YELLOW);
          rangesList.add(range);
        }
        final StyleRange ranges[] = new StyleRange[rangesList.size()];
        rangesList.toArray(ranges);
        if (ranges != null) {
          RegexLearnerWizardView2.this.testBox.setStyleRanges(ranges);
        }
      }
    });
    this.testBox.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = testBox.getText ();
      }
    });

    setControl(this.container);
  }

  /**
   * create the Type option "Any of the Symbols in this character class"
   */
  protected void createAnySymbolOption() {
    // add any symbols button
    this.anySymbolRadioButton = new Button(this.typeComposite, SWT.RADIO);
    this.anySymbolRadioButton.setText(Messages.RegexLearnerWizardView2_LABEL_ANY_SYMBOL_CHAR_CLASS);
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    this.anySymbolRadioButton.setLayoutData(gridData);
    this.anySymbolRadioButton.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = MessageFormat.format(Messages.RegExConstructs_SUBEXP_OPTION,
                      new Object[] { 1, numOfRefinerOptions,
                                    Messages.RegexLearnerWizardView2_LABEL_ANY_SYMBOL_CHAR_CLASS });
      }
    });
    this.anySymbolRadioButton.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        if (RegexLearnerWizardView2.this.anySymbolRadioButton.getSelection()) {
       	//enableDigitRange, enableSamplesTable,enableAnyChar)
          enableWidgets(false, false, true);
        }
      }

      @Override
      public void widgetSelected(SelectionEvent e) {
        if (RegexLearnerWizardView2.this.anySymbolRadioButton.getSelection()) {
          enableWidgets(false, false, true);
        }
      }
    });
    final Composite labelComposite = new Composite(this.typeComposite, SWT.NONE);
    labelComposite.setLayout(new GridLayout(3, false));
    // Label showing character class
    this.characterClassLabel = new Label(labelComposite, SWT.BOLD);
    this.characterClassLabel.setText(Messages.RegexLearnerWizardView2_LABEL_CHAR_CLASS);
    gridData = new GridData();
    gridData.horizontalIndent = 13;
    this.characterClassLabel.setLayoutData(gridData);
    this.characterClassLabel2 = new Label(labelComposite, SWT.BOLD);
    this.characterClassLabel2.setText(""); //$NON-NLS-1$
    this.characterClassLabel2.setFont(new Font(labelComposite.getDisplay(), "sans serif", 9, //$NON-NLS-1$
        SWT.BOLD));
    gridData = new GridData();
    gridData.minimumWidth = 180;
    gridData.widthHint = 180;
    gridData.grabExcessHorizontalSpace = false;
    this.characterClassLabel2.setLayoutData(gridData);
    this.changeCharClassButton = new Button(labelComposite, SWT.PUSH);
    this.changeCharClassButton.setText(Messages.RegexLearnerWizardView2_CHANGE);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.LEFT;
    gridData.minimumWidth = 70;
    gridData.widthHint = 70;
    this.changeCharClassButton.setLayoutData(gridData);
    this.changeCharClassButton.addSelectionListener(this.controller);
    this.changeCharClassButton.setEnabled(false);
    this.characterClassExplanationText = new Text(this.typeComposite, SWT.BOLD | SWT.READ_ONLY);
    this.characterClassExplanationText.setText(""); //$NON-NLS-1$
    this.characterClassExplanationText.setEnabled(false);
    changeCharClassButton.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = MessageFormat.format(Messages.RegexLearnerWizardView2_CHANGE_BUTTON_MESSAGE, characterClassLabel2.getText ());
      }
    });
    gridData = new GridData();
    gridData.minimumWidth = 320;
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalIndent = 17;
    this.characterClassExplanationText.setLayoutData(gridData);
    this.characterClassLabel.setEnabled(false);
    this.characterClassLabel2.setEnabled(false);
    this.characterClassExplanationText.setEnabled(false);
    // minimum # of symbols
    final Composite minBoxComposite = new Composite(this.typeComposite, SWT.NONE);
    minBoxComposite.setLayout(new RowLayout());
    gridData = new GridData();
    gridData.horizontalIndent = 15;
    minBoxComposite.setLayoutData(gridData);
    // label: "minimum"
    this.minOccurrencesLabel = new Label(minBoxComposite, SWT.BOLD);
    this.minOccurrencesLabel.setText(Messages.RegexLearnerWizardView2_SUBEXP_CONTAINS_ATLEAST);
    this.minOccurrencesLabel.setEnabled(false);
    // text box --> minimum number of symbols
    this.minOccurencesText = new Text(minBoxComposite, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
    this.minOccurencesText.setText(""); //$NON-NLS-1$
    this.minOccurencesText.setLayoutData(new RowData(50, 10));
    this.minOccurencesText.setEnabled(false);
    // make sure only digits can be entered into this field
    addOnlyDigitsListener(this.minOccurencesText);
    this.minOccurrencesLabel2 = new Label(minBoxComposite, SWT.BOLD);
    this.minOccurrencesLabel2.setText(" characters."); //$NON-NLS-1$
    this.minOccurrencesLabel2.setEnabled(false);
    minOccurencesText.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = minOccurrencesLabel.getText ()+minOccurencesText.getText ()+minOccurrencesLabel2.getText ();
      }
    });
    // max # of symbosl
    final Composite maxBoxComposite = new Composite(this.typeComposite, SWT.NONE);
    maxBoxComposite.setLayout(new RowLayout());
    gridData = new GridData();
    gridData.horizontalIndent = 15;
    maxBoxComposite.setLayoutData(gridData);
    // label: "maximum"
    this.maxOccurrencesLabel = new Label(maxBoxComposite, SWT.BOLD);
    this.maxOccurrencesLabel.setText(Messages.RegexLearnerWizardView2_SUBEXP_CONTAINS_ATMOST);
    this.maxOccurrencesLabel.setEnabled(false);
    // text box --> maximum number of symbols
    this.maxOccurencesText = new Text(maxBoxComposite, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
    this.maxOccurencesText.setText(""); //$NON-NLS-1$
    this.maxOccurencesText.setLayoutData(new RowData(50, 10));
    this.maxOccurencesText.setEnabled(false);
    // make sure only digits can be entered into this field
    addOnlyDigitsListener(this.maxOccurencesText);
    this.maxOccurrencesLabel2 = new Label(maxBoxComposite, SWT.BOLD);
    this.maxOccurrencesLabel2.setText(" characters."); //$NON-NLS-1$
    this.maxOccurrencesLabel2.setEnabled(false);
    maxOccurencesText.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = maxOccurrencesLabel.getText ()+maxOccurencesText.getText ()+maxOccurrencesLabel2.getText ();
      }
    });
  }

  /**
   * create the type option "alternation of the following samples"
   */
  protected void createCertainSamplesOption() {
    // add certain samples button
    this.certainSamplesRadioButton = new Button(this.typeComposite, SWT.RADIO);
    this.certainSamplesRadioButton.setText(Messages.RegexLearnerWizardView2_LABEL_ALTER_SAMPLES);
    final GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    this.certainSamplesRadioButton.setLayoutData(gridData);
    // create table with samples of the current leaf
    createSamplesTable(this.typeComposite);
    this.certainSamplesRadioButton.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = MessageFormat.format(Messages.RegExConstructs_SUBEXP_OPTION,
                    new Object[] { numOfRefinerOptions, numOfRefinerOptions,
                                   Messages.RegexLearnerWizardView2_LABEL_ALTER_SAMPLES });
      }
    });
    this.certainSamplesRadioButton.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        if (RegexLearnerWizardView2.this.certainSamplesRadioButton.getSelection()) {
          enableWidgets(false, true, false);
        }
      }

      @Override
      public void widgetSelected(SelectionEvent e) {
        if (RegexLearnerWizardView2.this.certainSamplesRadioButton.getSelection()) {
          enableWidgets(false, true, false);
        }
      }
    });
  }

  /**
   * creates the controls to set an integer number range (only applicable if type =
   * CharacterHierarchy.DIGIT)
   * 
   * @param ExpressionLeaf
   *          leaf - current leaf
   */
  protected void createDigitRangeOption() {
    GridData gridData;
    // add option to "At this position,..." group
    this.digitRangeRadioButton = new Button(this.typeComposite, SWT.RADIO);
    this.digitRangeRadioButton.setText(Messages.RegexLearnerWizardView2_LABEL_INT_IN_RANGE);
    gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    this.digitRangeRadioButton.setLayoutData(gridData);
    this.digitRangeRadioButton.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = MessageFormat.format(Messages.RegExConstructs_SUBEXP_OPTION,
                    new Object[] { 2, numOfRefinerOptions,
                                   Messages.RegexLearnerWizardView2_LABEL_INT_IN_RANGE });
      }
    });
    this.digitRangeRadioButton.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        if (RegexLearnerWizardView2.this.digitRangeRadioButton.getSelection()) {
        	//boolean enableDigitRange, boolean enableSamplesTable,boolean enableAnyChar)
          enableWidgets(true, false, false);
        }
      }

      @Override
      public void widgetSelected(SelectionEvent e) {
        if (RegexLearnerWizardView2.this.digitRangeRadioButton.getSelection()) {
          enableWidgets(true, false, false);
        }
      }
    });
    // create group for digit range
    this.digitRangeGroup = new Composite(this.typeComposite, SWT.NONE);
    this.digitRangeGroup.setLayout(new GridLayout(2, false));
    gridData = new GridData();
    gridData.horizontalIndent = 30;
    this.digitRangeGroup.setLayoutData(gridData);
    // label: "minimum"
    this.minRangeLabel = new Label(this.digitRangeGroup, SWT.BOLD);
    this.minRangeLabel.setText(Messages.RegexLearnerWizardView2_LABEL_MIN);
    gridData = new GridData();
    gridData.minimumWidth = 170;
    gridData.widthHint = 170;
    gridData.horizontalSpan = 1;
    gridData.horizontalAlignment = SWT.RIGHT;
    this.minRangeLabel.setLayoutData(gridData);
    // text box --> minimum range for integer numbers
    this.minRangeText = new Text(this.digitRangeGroup, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
    this.minRangeText.setText(""); //$NON-NLS-1$
    gridData = new GridData();
    gridData.minimumWidth = 50;
    gridData.widthHint = 50;
    gridData.horizontalSpan = 1;
    this.minRangeText.setLayoutData(gridData);
    // make sure only digits can be entered into this field
    addOnlyDigitsListener(this.minRangeText);
    // label: "maximum"
    this.maxRangeLabel = new Label(this.digitRangeGroup, SWT.BOLD);
    this.maxRangeLabel.setText(Messages.RegexLearnerWizardView2_LABEL_MAX);
    gridData = new GridData();
    gridData.minimumWidth = 170;
    gridData.widthHint = 170;
    gridData.horizontalSpan = 1;
    gridData.horizontalAlignment = SWT.RIGHT;
    this.maxRangeLabel.setLayoutData(gridData);
    // text box --> maximum range for integer numbers
    this.maxRangeText = new Text(this.digitRangeGroup, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
    this.maxRangeText.setText(""); //$NON-NLS-1$
    gridData = new GridData();
    gridData.minimumWidth = 50;
    gridData.widthHint = 50;
    gridData.horizontalSpan = 1;
    this.maxRangeText.setLayoutData(gridData);
    // make sure only digits can be entered into this field
    addOnlyDigitsListener(this.maxRangeText);
    // add "allow leading zeros" check box
    this.allowLeadingZerosButton = new Button(this.digitRangeGroup, SWT.CHECK);
    this.allowLeadingZerosButton.setText(Messages.RegexLearnerWizardView2_37);
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    gridData.horizontalAlignment = SWT.LEFT;
    this.allowLeadingZerosButton.setLayoutData(gridData);
  }

  /**
   * creates an table for the samples of this leaf
   * 
   * @param Composite
   *          parent - parent composite
   */
  private void createSamplesTable(Composite parent) {
    final Composite samplesTableComposite = new Composite(parent, SWT.NONE);
    samplesTableComposite.setLayout(new GridLayout(2, false));
    final int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
    this.leafSamplesTable = new Table(samplesTableComposite, style);
    GridData gridData = new GridData();
    gridData.heightHint = 80;
    gridData.widthHint = 180;
    gridData.minimumHeight = 80;
    gridData.heightHint = 80;
    gridData.horizontalIndent = 30;
    this.leafSamplesTable.setLayoutData(gridData);
    this.hintComposite = new Composite(samplesTableComposite, SWT.BOLD);
    this.hintComposite.setLayout(new RowLayout());
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.LEFT;
    gridData.verticalAlignment = SWT.BEGINNING;

    // Accessibility support
    this.leafSamplesTable.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        if (e.childID == ACC.CHILDID_SELF)
          e.result = Messages.RegexLearnerWizardView2_LABEL_SAMPLE_OF_SUBEXP;
      }
    });


    this.hintComposite.setLayoutData(gridData);
    final ImageDescriptor infoImageDescriptor = Activator.getImageDescriptor(Messages.RegexLearnerWizardView2_HINT_IMAGE);
    final Image infoImage = infoImageDescriptor.createImage();
    this.infoImageLabel = new Label(this.hintComposite, SWT.NONE);
    this.infoImageLabel.setImage(infoImage);
    this.hintTextLabel = new Text(this.hintComposite, SWT.BOLD | SWT.MULTI | SWT.READ_ONLY);
    this.hintTextLabel.setText(Messages.RegexLearnerWizardView2_LABEL_TABLE_DESC);
    this.leafSamplesTable.setLinesVisible(true);
    this.leafSamplesTable.setHeaderVisible(true);
    this.leafSamplesTable.setItemCount(RegexLearnerWizard.TABLE_ITEM_COUNT);
    this.leafSamplesTable.setSize(180, 80);
    // one and only column
    final TableColumn column1 = new TableColumn(this.leafSamplesTable, SWT.CENTER, 0);
    column1.setText(Messages.RegexLearnerWizardView2_LABEL_SAMPLE_OF_SUBEXP);
    column1.setWidth(180);
    final TableEditor editor = new TableEditor(this.leafSamplesTable);
    // The editor must have the same size as the cell and must not be any smaller than 50 pixels.
    editor.horizontalAlignment = SWT.LEFT;
    editor.grabHorizontal = true;
    editor.minimumWidth = 180;
    // editing the first column
    final int EDITABLECOLUMN = 0;
    this.leafSamplesTable.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        // Clean up any previous editor control
        final Control oldEditor = editor.getEditor();
        if (oldEditor != null) {
          oldEditor.dispose();
        }
        // Identify the selected row
        final TableItem item = (TableItem) e.item;
        if (item == null) {
          return;
        }
        // The control that will be the editor must be a child of the Table
        final Text newEditor = new Text(RegexLearnerWizardView2.this.leafSamplesTable, SWT.NONE);
        newEditor.setText(item.getText(EDITABLECOLUMN));
        newEditor.addModifyListener(new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent me) {
            final Text text = (Text) editor.getEditor();
            editor.getItem().setText(EDITABLECOLUMN, text.getText());
          }
        });
        newEditor.selectAll();
        newEditor.setFocus();
        editor.setEditor(newEditor, item, EDITABLECOLUMN);
      }
    });
    this.leafSamplesTable.redraw();
    parent.layout();
  }

  /**
   * create table for samples
   * 
   * @param - Composite parent - the parent container
   */
  private void createTable(Composite parent) {
    final int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
    // create the table and its layout / style
    this.samplesTable = new Table(parent, style);
    GridData gridData = new GridData();
    
    gridData.minimumWidth = 380;
    gridData.widthHint = 380;
   	gridData.minimumHeight = 200;
    gridData.heightHint = 200;
    gridData.horizontalIndent = 5;
    this.samplesTable.setLayoutData(gridData);
    this.samplesTable.setLinesVisible(true);
    this.samplesTable.setHeaderVisible(true);
    this.samplesTable.setItemCount(RegexLearnerWizard.TABLE_ITEM_COUNT);
   	this.samplesTable.setSize(420, 200);
   
    // add first column --> shows whether the whole expression matches this sample
    final TableColumn column0 = new TableColumn(this.samplesTable, SWT.CENTER, 0);
    column0.setText(Messages.RegexLearnerWizardView2_MATCH);
    column0.setWidth(45);
    //column0.setToolTipText("The current regular expression matches this sample\n The current regular expression does not match the sample");
    // samples column
    final TableColumn column1 = new TableColumn(this.samplesTable, SWT.CENTER, 1);
    column1.setText(Messages.RegexLearnerWizardView2_SAMPLES);
    column1.setWidth(320);
    this.samplesTable.redraw();
    parent.layout();
  }

  /**
   * TODO: check if in eclipse book
   * 
   * @param text
   *          - make sure only digits can be entered into this field snippet taken from
   *          http://dev.eclipse
   *          .org/viewcvs/index.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets
   *          /Snippet19.java?view=co
   */
  private void addOnlyDigitsListener(Text text) {
    text.addListener(SWT.Verify, new Listener() {
      @Override
      public void handleEvent(Event event) {
        final String string = event.text;
        final char[] chars = new char[string.length()];
        string.getChars(0, chars.length, chars, 0);
        for (int i = 0; i < chars.length; i++) {
          if (!(('0' <= chars[i]) && (chars[i] <= '9'))) {
            event.doit = false;
            return;
          }
        }
      }
    });
  }

  /**
   * enable certain widgets
   * 
   * @param enableDigitRange
   * @param enableSamplesTable
   * @param enableAnyChar
   */
  protected void enableWidgets(boolean enableDigitRange, boolean enableSamplesTable,
      boolean enableAnyChar) {
    this.minOccurencesText.setEnabled(enableAnyChar);
    this.maxOccurencesText.setEnabled(enableAnyChar);
    this.minOccurrencesLabel.setEnabled(enableAnyChar);
    this.minOccurrencesLabel2.setEnabled(enableAnyChar);
    this.maxOccurrencesLabel.setEnabled(enableAnyChar);
    this.maxOccurrencesLabel2.setEnabled(enableAnyChar);
    this.characterClassLabel.setEnabled(enableAnyChar);
    this.characterClassLabel2.setEnabled(enableAnyChar);
    this.characterClassExplanationText.setEnabled(enableAnyChar);
    this.changeCharClassButton.setEnabled(enableAnyChar);
    if (this.leafSamplesTable != null) {
      this.leafSamplesTable.setEnabled(enableSamplesTable);
      this.infoImageLabel.setEnabled(enableSamplesTable);
      this.hintTextLabel.setEnabled(enableSamplesTable);
    }
    if (this.minRangeText != null && !this.minRangeText.isDisposed ()) {
      this.minRangeText.setEnabled(enableDigitRange);
      this.minRangeLabel.setEnabled(enableDigitRange);
      this.maxRangeText.setEnabled(enableDigitRange);
      this.maxRangeLabel.setEnabled(enableDigitRange);
      this.allowLeadingZerosButton.setEnabled(enableDigitRange);
    }
  }

  /**
   * show the digit range controls
   * 
   * @param current
   *          expression leaf
   */
  protected void showDigitRangeControls(ExpressionLeaf leaf) {
    // set text fields to current values
    if (leaf.getMinimumRange() != null) {
      this.minRangeText.setText(leaf.getMinimumRange().toString());
    } else {
      this.minRangeText.setText(""); //$NON-NLS-1$
    }
    if (leaf.getMaximumRange() != null) {
      this.maxRangeText.setText(leaf.getMaximumRange().toString());
    } else {
      this.maxRangeText.setText(""); //$NON-NLS-1$
    }
    if (leaf.isDigitRange()) {
      selectRadioButton(this.digitRangeRadioButton);
      this.minRangeText.setEnabled(true);
      this.minRangeLabel.setEnabled(true);
      this.maxRangeText.setEnabled(true);
      this.maxRangeLabel.setEnabled(true);
      this.allowLeadingZerosButton.setEnabled(true);
    } else {
      this.minRangeText.setEnabled(false);
      this.minRangeLabel.setEnabled(false);
      this.maxRangeText.setEnabled(false);
      this.maxRangeLabel.setEnabled(false);
      this.allowLeadingZerosButton.setEnabled(false);
    }
    if (!leaf.isDigitRange() && !leaf.isSamplesAlternation()) {
      this.minOccurencesText.setEnabled(true);
      this.minOccurrencesLabel.setEnabled(true);
      this.minOccurrencesLabel2.setEnabled(true);
      this.maxOccurencesText.setEnabled(true);
      this.maxOccurrencesLabel.setEnabled(true);
      this.maxOccurrencesLabel2.setEnabled(true);
    }
    this.allowLeadingZerosButton.setSelection(leaf.isAllowLeadingZeros());
  }

  /**
   * fill the table with the samples of the leaf
   * 
   * @param ArrayList
   *          <String> samples - samples of the leaf (what may occur at this position)
   */
  protected void fillSamplesTable(ArrayList<String> samples) {
    this.leafSamplesTable.removeAll();
    int count = 0;
    if ((samples != null) && !samples.isEmpty()) {
      for (final String sample : samples) {
        final TableItem item = new TableItem(this.leafSamplesTable, SWT.NULL);
        item.setText(sample);
        count++;
      }
    }
    // the table always contains TABLE_ITEM_COUNT editable items
    for (int i = count; i < RegexLearnerWizard.TABLE_ITEM_COUNT; i++) {
      final TableItem item = new TableItem(this.leafSamplesTable, SWT.NONE);
      item.setText(""); //$NON-NLS-1$
    }
    this.leafSamplesTable.redraw();
    this.leafSamplesTable.layout();
  }

  /**
   * select the Button button and deselect all other buttons in the radioButtonsGroup
   * 
   * @param Button
   *          button - button to be selected
   */
  protected void selectRadioButton(Button button) {
    button.setSelection(true);
    for (final Control c : this.typeComposite.getChildren()) {
      if ((c != button) && (c instanceof Button) && (c != this.allowLeadingZerosButton)) {
        ((Button) c).setSelection(false);
      }
    }
  }

  /**
   * update the final regex string that is to be returned when the wizard finishes
   */
  protected void updateFinalRegex() {
    final Expression learned = this.controller.expression;
    this.finalRegex = Converter.toRegex(learned);
  }

  /**
   * show the warning that the current regex does not match all samples
   */
  protected void showMatchesNotAllWarning() {
//    final ImageDescriptor warningID = Activator.getImageDescriptor("warning.gif");
//    final Image warningImage = warningID.createImage();
//    this.matchingMessageImageLabel.setImage(warningImage);
//    this.matchingMessageLabel.setText("This Regular Expression does not match all samples.");
  }

  /**
   * show the information label that the current regex matches all samples
   */
  protected void showMatchesAllMessage() {
//    final ImageDescriptor okayID = Activator.getImageDescriptor("everything_ok.gif");
//    final Image warningImage = okayID.createImage();
//    this.matchingMessageImageLabel.setImage(warningImage);
//    this.matchingMessageLabel.setText("This Regular Expression matches all samples.");
  }

  /**
   * This method is called whenever the page is set visible or invisible. If it is set visible, the
   * table with the samples is refreshed. The selected regular expression (from page 1) is put into
   * the SuggestedRegex field of the controller.
   */
  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      // set learned expression
      this.controller.expression = this.wizard.suggestedRegexes.get(this.wizard.selectedRegexIndex)
          .getRegularExpression();
      // initialize traversal object
      this.controller.traversal = new Traversal(this.controller.expression);
      // start with first subexpression and cast it to expression leaf
      final ExpressionLeaf leaf = this.controller.traversal.getNextLeaf();
      this.controller.changeControls(leaf);
      // fill samples table
      fillSamplesTable(leaf.getSamples());
      // fill samples text box
      this.controller.fillSamplesTextBox(this.controller.expression);
      // set regex textbox
      this.controller.fillRegexTextBox(this.controller.expression);
      updateFinalRegex();
      // wizard can finish if regular expression in regexTextBox is not empty
      if (!this.regexText.getText().equals("")) { //$NON-NLS-1$
        this.wizard.canFinish = true;
      }
    }
    super.setVisible(visible);
  }
}

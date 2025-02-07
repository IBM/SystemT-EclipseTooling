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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterClass;
import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterHierarchy;
import com.ibm.biginsights.textanalytics.regex.learner.expression.AlternationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionLeaf;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionNode;
import com.ibm.biginsights.textanalytics.regex.learner.learner.utils.Converter;
import com.ibm.biginsights.textanalytics.regex.learner.utils.GroupRegex;
import com.ibm.biginsights.textanalytics.regex.learner.utils.Traversal;

/**
 * 
 * 
 *         This class contains the controller of the regexlearner wizard page 2. It also doubles
 *         up as a SelectionListener for the same page.
 * 
 */

public class RegexLearnerWizardController2 extends SelectionAdapter {



  RegexLearnerWizardView2 view;

  // The expression learned on the previous wizard page
  Expression expression;

  Traversal traversal;

  // highlighting information
  private int start = 0;

  private int length;

  private int styledRangeStart = 0;

  private int styledRangeLength = 0;

  // index of the group (in the groupRegexString) we are currently working on
  private int currentGroupIndex;

  private ArrayList<Boolean> isLeaf;

  // regex string
  private String regex = ""; //$NON-NLS-1$

  // Constructor
  public RegexLearnerWizardController2(RegexLearnerWizardView2 view) {
    this.view = view;
  }

  @Override
  public void widgetSelected(SelectionEvent event) {
    if (event.widget instanceof Button) {
      final Button button = (Button) event.widget;
      if (button == this.view.nextSubexpressionButton) {
        final boolean error = checkInputData(true);
        if (!error) {
          changeLeaf();
          final ExpressionLeaf nextLeaf = this.traversal.getNextLeaf();
          changeControls(nextLeaf);
          this.view.previousSubexpressionButton.setEnabled(true);
          fillRegexTextBox(this.expression);
          fillSamplesTextBox(this.expression);
          this.view.testBox.setText(this.view.testBox.getText());
        }
      }
      if (button == this.view.previousSubexpressionButton) {
        final boolean error = checkInputData(true);
        if (!error) {
          changeLeaf();
          final ExpressionLeaf previousLeaf = this.traversal.getPreviousLeaf();
          changeControls(previousLeaf);
          this.view.nextSubexpressionButton.setEnabled(true);
          fillRegexTextBox(this.expression);
          fillSamplesTextBox(this.expression);
          this.view.testBox.setText(this.view.testBox.getText());
        }
      }
      if (button == this.view.applyButton) {
        final boolean error = checkInputData(true);
        if (!error) {
          changeLeaf();
          changeControls(this.traversal.getCurrentLeaf());
          fillRegexTextBox(this.expression);
          fillSamplesTextBox(this.expression);
          this.view.leftPartComposite.layout();
          this.view.rightPartComposite.layout();
          this.view.testBox.setText(this.view.testBox.getText());
        }
      }
      if (button == this.view.changeCharClassButton) {
        final ChangeCharClassDialog dialog = new ChangeCharClassDialog(this.view.getShell(), 0);
        final String result = (String) dialog.open();
        if (result != null) {
          this.traversal.getCurrentLeaf().setType(result);
          this.traversal.getCurrentLeaf().setSamplesAlternation(false);
          changeControls(this.traversal.getCurrentLeaf());
          fillRegexTextBox(this.expression);
          fillSamplesTextBox(this.expression);
          this.view.leftPartComposite.layout();
          this.view.rightPartComposite.layout();
          this.view.testBox.setText(this.view.testBox.getText());
        }
      }
      if (button == this.view.importButton){
    	  loadSamples();
      }
      
    }
  }

  private void loadSamples() {
    final Shell shell = this.view.getShell();
    final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
    final String[] filterNames = new String[] { Messages.RegexLearnerWizardController2_TEXT_FILES, Messages.RegexLearnerWizardController2_ALL_FILES };
    final String[] filterExtensions = new String[] { "*.txt" }; //$NON-NLS-1$ //$NON-NLS-2$
    dialog.setFilterNames(filterNames);
    dialog.setFilterExtensions(filterExtensions);
    final String result = dialog.open();
    try {
      final BufferedReader reader = new BufferedReader(new FileReader(result));
      String sample;
      this.view.testBox.setText(""); //$NON-NLS-1$
      int count = 0;
      this.view.wizard.samples.clear();
      while (((sample = reader.readLine()) != null) && (count < RegexLearnerWizard.TABLE_ITEM_COUNT)) {
        if (!sample.trim().equals("")) { //$NON-NLS-1$
        	this.view.testBox.append(sample + "\n"); //$NON-NLS-1$
//          final TableItem item = new TableItem(this.view.table, SWT.NONE);
//          item.setText(sample);
//          this.view.wizard.samples.add(sample);
//          count++;
        }
      }
      // the table always contains TABLE_ITEM_COUNT editable items
//      for (int i = count; i < RegexLearnerWizard.TABLE_ITEM_COUNT; i++) {
//        final TableItem item = new TableItem(this.view.table, SWT.NONE);
//        item.setText("");
//      }
      this.view.testBox.redraw();
    } catch (final FileNotFoundException e) {
      final Status status = new Status(IStatus.ERROR, RegexLearnerWizard.WIZARD_ID, 0,
          e.getMessage(), null);
      ErrorDialog.openError(Display.getCurrent().getActiveShell(), Messages.RegexLearnerWizardController2_ERROR_FILENOTFOUND,
          Messages.RegexLearnerWizardController2_ERROR_CANNOT_OPEN_FILE + result, status);
      e.printStackTrace();
    } catch (final IOException e) {
      final Status status = new Status(IStatus.ERROR, RegexLearnerWizard.WIZARD_ID, 0,
          e.getMessage(), null);
      ErrorDialog.openError(Display.getCurrent().getActiveShell(), Messages.RegexLearnerWizardController2_ERROR_READFILE,
          Messages.RegexLearnerWizardController2_ERROR_READ_FILE + result, status);
      e.printStackTrace();
    }
  }

/**
   * checks if the input data for the leaf is consistent
   * 
   * @param showErrorMessages
   *          - hook indicating if error messages should be shown. This it not the case when calling
   *          this method from the performFinish() method.
   * @return true if the input data is consistent, false otherwise
   */
  protected boolean checkInputData(boolean showErrorMessages) {
    // set current samples to list of table
    final ArrayList<String> tempSamples = new ArrayList<String>();
    for (final TableItem item : this.view.leafSamplesTable.getItems()) {
      if (!item.getText().trim().equals("")) { //$NON-NLS-1$
        tempSamples.add(item.getText().trim());
      }
      if (item.getText().matches(" +")) { //$NON-NLS-1$
        tempSamples.add(item.getText());
      }
    }

    boolean error = false;
    // an empty leaf samples table is not possible if this option is selected
    if (tempSamples.isEmpty() && this.view.certainSamplesRadioButton.getSelection()) {
      if (showErrorMessages) {
        final Status status = new Status(IStatus.ERROR, RegexLearnerWizard.WIZARD_ID, 0,
            Messages.RegexLearnerWizardController2_ERROR_NO_SAMPLES, null);
        ErrorDialog
            .openError(
                Display.getCurrent().getActiveShell(),
                Messages.RegexLearnerWizardController2_ERROR_NO_SAMPLES_IN_TABLE,
                Messages.RegexLearnerWizardController2_INFO_ENTER_SUBEXP,
                status);
      }
      error = true;
    }
    // if the digitRangeButton is selected, the integer number range must be consistent with respect
    // to min <= max
    if (this.traversal.getCurrentLeaf().getType().equals(CharacterHierarchy.DIGIT)) {
      if (this.view.digitRangeRadioButton.getSelection()) {
        if ((this.view.minRangeText.getText().equals("") || this.view.maxRangeText.getText() //$NON-NLS-1$
            .equals("")) //$NON-NLS-1$
            || (Integer.parseInt(this.view.minRangeText.getText()) > Integer
                .parseInt(this.view.maxRangeText.getText()))) {
          if (showErrorMessages) {
            final Status status = new Status(IStatus.ERROR, RegexLearnerWizard.WIZARD_ID, 0,
                Messages.RegexLearnerWizardController2_ERROR_INCONSISTANT_RANGE, null);
            ErrorDialog
                .openError(
                    Display.getCurrent().getActiveShell(),
                    Messages.RegexLearnerWizardController2_ERROR_INCONSISTANT_RANGE,
                    Messages.RegexLearnerWizardController2_INFO_ENTER_MINMAX_FOR_INT,
                    status);
          }
          error = true;
        }
      }
    }
    return error;
  }

  /**
   * if the type of the expression leaf is a character class, return this. otherwise, find the
   * appropriate character class and return it.
   * 
   * @param ExpressionLeaf
   *          leaf
   */
  private String defineCharacterClassLabel(Expression nextLeaf) {
    CharacterClass charClass;
    if (CharacterHierarchy.isCharacterClass(nextLeaf.getType())) {
      charClass = CharacterHierarchy.getCharClassByName(nextLeaf.getType());
    } else {
      charClass = CharacterHierarchy.getCharacterClass(nextLeaf.getType());
    }
    return charClass.getDescription();
  }

  /**
   * return an explanation of the character class of the leaf
   * 
   * @param ExpressionLeaf
   *          leaf
   */
  private String defineCharacterClassExplanationLabel(Expression nextLeaf) {
    CharacterClass charClass;
    if (CharacterHierarchy.isCharacterClass(nextLeaf.getType())) {
      charClass = CharacterHierarchy.getCharClassByName(nextLeaf.getType());
    } else {
      charClass = CharacterHierarchy.getCharacterClass(nextLeaf.getType());
    }
    return Messages.RegexLearnerWizardController2_CHAR_CLASS_COMPRISES + charClass.getExplanation() + " (" //$NON-NLS-2$
        + charClass.getRegexString() + ")."; //$NON-NLS-1$
  }

  /**
   * change controls and data on refinementGroup group according to current subexpression
   * 
   * @param ExpressionLeaf
   *          leaf - current leaf
   */
  protected void changeControls(ExpressionLeaf leaf) {

    // set enablement of next & previous buttons
    if (this.traversal.hasPrevious()) {
      this.view.previousSubexpressionButton.setEnabled(true);
    } else {
      this.view.previousSubexpressionButton.setEnabled(false);
    }
    if (this.traversal.hasNext()) {
      this.view.nextSubexpressionButton.setEnabled(true);
    } else {
      this.view.nextSubexpressionButton.setEnabled(false);
    }

    // set data --> current subexpression label
    this.view.currentSubexpressionLabel.setText(Converter.toRegex(leaf));

    // set data -> optional checkbox
    if (leaf.isOptional()) {
      this.view.optionalCheckbox.setSelection(true);
    } else {
      this.view.optionalCheckbox.setSelection(false);
    }

    // remove type information controls
    dispose(this.view.typeComposite);

    // add type options
    this.view.createAnySymbolOption();
    // set data -> character class label
    this.view.characterClassLabel2.setText(defineCharacterClassLabel(leaf));
    final String charClassExplanation = defineCharacterClassExplanationLabel(leaf);
    this.view.characterClassExplanationText.setText(charClassExplanation);
    // set data -> minimum and maximum # of symbols text fields
    this.view.minOccurencesText.setText(leaf.getMinimum() + ""); //$NON-NLS-1$
    this.view.maxOccurencesText.setText(leaf.getMaximum() + ""); //$NON-NLS-1$
    if (leaf.getType().equals(CharacterHierarchy.DIGIT)) {
      this.view.numOfRefinerOptions = 3;
      this.view.createDigitRangeOption();
      this.view.showDigitRangeControls(leaf);
    }
    else {
      this.view.numOfRefinerOptions = 2;
    }
    this.view.createCertainSamplesOption();
    this.view.fillSamplesTable(leaf.getSamples());

    // if certainSamples --> select that radio button
    if (leaf.isSamplesAlternation()) {
      this.view.selectRadioButton(this.view.certainSamplesRadioButton);
    } else {
      this.view.leafSamplesTable.setEnabled(false);
      this.view.infoImageLabel.setEnabled(false);
      this.view.hintTextLabel.setEnabled(false);
      if (leaf.isDigitRange()) {
        this.view.selectRadioButton(this.view.digitRangeRadioButton);
      } else {
        this.view.selectRadioButton(this.view.anySymbolRadioButton);
        this.view.characterClassLabel.setEnabled(true);
        this.view.characterClassLabel2.setEnabled(true);
        this.view.characterClassExplanationText.setEnabled(true);
        this.view.changeCharClassButton.setEnabled(true);
        this.view.minOccurencesText.setEnabled(true);
        this.view.minOccurrencesLabel.setEnabled(true);
        this.view.minOccurrencesLabel2.setEnabled(true);
        this.view.maxOccurencesText.setEnabled(true);
        this.view.maxOccurrencesLabel.setEnabled(true);
        this.view.maxOccurrencesLabel2.setEnabled(true);
      }
    }

    // fill samples table with samples of the current subexpression leaf
    if (CharacterHierarchy.isCharacterClass(leaf.getType())) {
      this.view.fillSamplesTable(leaf.getSamples());
    } else {
      final ArrayList<String> samples = new ArrayList<String>();
      samples.add(leaf.getType());
      leaf.setSamplesAlternation(true);
    }

    this.view.updateFinalRegex();
    // check if final regex matches all of the samples
    boolean matchesAll = true;
    final Pattern pattern = Pattern.compile(this.view.finalRegex);
    for (final String sample : this.view.wizard.samples) {
      final Matcher matcher = pattern.matcher(sample);
      if (!matcher.find() || (matcher.group(0).length() < sample.length())) {
        matchesAll = false;
        break;
      }
    }
    if (!matchesAll) {
      this.view.showMatchesNotAllWarning();
    } else {
      this.view.showMatchesAllMessage();
    }
    this.view.container.layout();
  }

  /**
   * dispose the composite and all of its children, null them out
   * 
   * @param composite
   *          composite to be disposed
   */
  private void dispose(Composite composite) {
    // remove type information controls
    for (Control c : composite.getChildren()) {
      if (c instanceof Composite) {
        dispose((Composite) c);
      }
      c.dispose();
      c = null;
    }
  }

  /**
   * changes some fields of the leaf according to user input
   */
  protected void changeLeaf() {
    final ExpressionLeaf expr = this.traversal.getCurrentLeaf();

    if (expr != null) {
      expr.setOptional(this.view.optionalCheckbox.getSelection());
      expr.setMinimum(Integer.parseInt(this.view.minOccurencesText.getText()));
      expr.setMaximum(Integer.parseInt(this.view.maxOccurencesText.getText()));
      if (expr.getMinimum() > expr.getMaximum()) {
        expr.setMaximum(expr.getMinimum());
      }
      if (expr.getType().equals(CharacterHierarchy.DIGIT)) {
        if (!this.view.minRangeText.getText().equals("")) { //$NON-NLS-1$
          expr.setMinimumRange(Integer.parseInt(this.view.minRangeText.getText()));
        } else {
          expr.setMinimumRange(null);
        }
        if (!this.view.maxRangeText.getText().equals("")) { //$NON-NLS-1$
          expr.setMaximumRange(Integer.parseInt(this.view.maxRangeText.getText()));
        } else {
          expr.setMaximumRange(null);
        }
        expr.setAllowLeadingZeros(this.view.allowLeadingZerosButton.getSelection());
        // adjust number of digits of this leaf...
        if (!this.view.minRangeText.getText().equals("") //$NON-NLS-1$
            && !this.view.maxRangeText.getText().equals("") //$NON-NLS-1$
            && this.view.digitRangeRadioButton.getSelection()) {
          expr.setDigitRange(true);
          expr.setMaximum(this.view.maxRangeText.getText().length());
          expr.setMinimum(this.view.minRangeText.getText().length());
        } else {
          expr.setDigitRange(false);
        }
        if (!this.view.digitRangeRadioButton.getSelection()) {
          expr.setDigitRange(false);
        }
      }
      // set current samples to list of table
      final ArrayList<String> tempSamples = new ArrayList<String>();
      for (final TableItem item : this.view.leafSamplesTable.getItems()) {
        if (!item.getText().trim().equals("")) { //$NON-NLS-1$
          tempSamples.add(item.getText().trim());
        }
        if (item.getText().matches(" +")) { //$NON-NLS-1$
          tempSamples.add(item.getText());
        }
      }
      // if table is empty, keep old samples
      if (!tempSamples.isEmpty()) {
        expr.getSamples().clear();
        expr.addSamples(tempSamples);
      }
      expr.setSamplesAlternation(this.view.certainSamplesRadioButton.getSelection());
      if (expr.isSamplesAlternation()) {
        // adjust min and max according to samples
        Integer min = null;
        int max = 0;
        String allSamples = ""; //$NON-NLS-1$
        for (final String sample : expr.getSamples()) {
          allSamples += sample;
          if (min == null) {
            min = sample.length();
          } else if (sample.length() < min) {
            min = sample.length();
          }
          if (sample.length() > max) {
            max = sample.length();
          }
        }
        expr.setType(CharacterHierarchy.getCharacterClass(allSamples).getName());
        expr.setMinimum(min);
        expr.setMaximum(max);
      }
      this.view.updateFinalRegex();
    }
  }

  /**
   * This methods fills the regular expression textbox with the correct highlighting of the current
   * subexpression.
   * 
   * @param expr
   */
  void fillRegexTextBox(Expression expr) {
    this.regex = ""; //$NON-NLS-1$
    this.start = 0;
    for (final StyleRange sr : this.view.regexText.getStyleRanges()) {
      sr.isUnstyled();
    }
    // view.regexText.setStyleRanges(null);

    fillRegexTextBox2(expr);

    this.view.regexText.setText(this.regex);
    final StyleRange styleRange = new StyleRange();
    styleRange.start = this.styledRangeStart;
    styleRange.length = this.styledRangeLength;
    styleRange.fontStyle = SWT.BOLD;
    styleRange.background = this.view.container.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
    this.view.regexText.setStyleRange(styleRange);
    if (this.styledRangeStart > 50) {
      this.view.regexText.setHorizontalIndex(this.styledRangeStart - 30);
    }
  }

  /**
   * This method is used to fill the regular expression text box with the correct highlighting of
   * the current subexpression.
   * 
   * @param expr
   */
  private void fillRegexTextBox2(Expression expr) {
    final Expression current = this.traversal.getCurrentLeaf();

    if (expr instanceof ExpressionLeaf) {
      this.regex += Converter.toRegex(expr);
      this.length = this.regex.length() - this.start;
      if (expr == current) {
        this.styledRangeStart = this.start;
        this.styledRangeLength = this.length;
      }
      this.start = this.regex.length();
    } else {
      if (expr instanceof AlternationExpression) {
        if (((AlternationExpression) expr).isSamplesAlternation()) {
          this.regex += "("; //$NON-NLS-1$
          String bar = ""; //$NON-NLS-1$
          for (final Expression subexpression : ((ExpressionNode) expr).getSubexpressions()) {
            this.regex += bar + subexpression.getType();
            bar = "|"; //$NON-NLS-1$
          }
          this.regex += ")"; //$NON-NLS-1$
          this.length = this.regex.length() - this.start;
          if (expr == current) {
            this.styledRangeStart = this.start;
            this.styledRangeLength = this.length;
          }
          this.start = this.regex.length();
        } else {
          this.regex += "("; //$NON-NLS-1$
          String bar = ""; //$NON-NLS-1$
          for (final Expression subexpression : ((ExpressionNode) expr).getSubexpressions()) {
            this.regex += bar;
            bar = "|"; //$NON-NLS-1$
            if (bar.equals("|")) { //$NON-NLS-1$
              this.start++;
            }
            fillRegexTextBox2(subexpression);
          }
          this.regex += ")"; //$NON-NLS-1$
          this.start++;
        }
        // TODO check
        if (expr.isOptional()) {
          this.regex += "?"; //$NON-NLS-1$
          this.start++;
        }
      } else {
        // expression instanceof ConcatenationExpression
        this.regex += "("; //$NON-NLS-1$
        this.start++;
        for (final Expression subexpression : ((ExpressionNode) expr).getSubexpressions()) {
          fillRegexTextBox2(subexpression);
        }
        this.regex += ")"; //$NON-NLS-1$
        this.start++;
      }
    }
  }

  /**
   * This method fills the samples text box with the correct highlighting of current subexpressions.
   * 
   * @param expr
   */
  void fillSamplesTextBox(Expression expr) {
    // build regular expression with many groups...
    final GroupRegex gr = new GroupRegex(this.traversal);
    final String regexp = gr.getGroupRegex(expr);
    this.isLeaf = gr.getIsLeaf();
    this.start = 0;
    this.currentGroupIndex = gr.getCurrentGroupIndex();
    final Pattern pattern = Pattern.compile(regexp);

    // remove all items in the table
    this.view.samplesTable.removeAll();
    for (final Control c : this.view.samplesTable.getChildren()) {
      c.dispose();
    }

    // Set up as many rows as there are samples
    for (int i = 0; i < this.view.wizard.samples.size(); i++) {
      new TableItem(this.view.samplesTable, SWT.NONE);
    }

    final TableItem[] items = this.view.samplesTable.getItems();
    for (int i = 0; i < items.length; i++) {
      String samplesText = ""; //$NON-NLS-1$
      StyleRange styleRange = null;
      final String sample = this.view.wizard.samples.get(i);
      final Matcher matcher = pattern.matcher(sample);
      // the match is only a match if the WHOLE sample is matched!!
      if (matcher.find() && (sample.length() == matcher.group(0).length())) {
        samplesText = ""; //$NON-NLS-1$
        for (int j = 0; j < this.isLeaf.size(); j++) {
          if (this.isLeaf.get(j)) {
            final String groupString = matcher.group(j);
            if (groupString != null) {
              final int startp = samplesText.length();
              samplesText += groupString;
              if (j == this.currentGroupIndex) {
                styleRange = new StyleRange();
                styleRange.start = startp;
                styleRange.length = samplesText.length() - startp;
                styleRange.fontStyle = SWT.BOLD;
                styleRange.background = this.view.container.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
              }
            }
          }
        }
        final TableItem item = items[i];
        final StyledText text = new StyledText(this.view.samplesTable, SWT.SINGLE);
        text.setEditable(false);
        text.setText(sample);
        if (styleRange != null) {
          text.setStyleRange(styleRange);
        }
        final TableEditor editor = new TableEditor(this.view.samplesTable);
        editor.grabHorizontal = editor.grabVertical = true;
//        editor.horizontalAlignment = SWT.CENTER;
//        editor.minimumWidth = 60;
        editor.setEditor(text, item, 1);
        try{
   		ImageDescriptor tickImageDesc = ImageDescriptor.createFromFile(this.getClass(), Messages.RegexLearnerWizardController2_TICK_ICON);
   		final Image tickImage = tickImageDesc.createImage(); 
        //final Image tickImage = new Image(this.view.container.getDisplay(),"icons/tick.gif");
        //final Image tickImage = this.view.container.getDisplay().getSystemImage(SWT.image);
        //Image tickImage = new Image(this.view.container.getDisplay(), 0xFF, CHECK_WIDTH);
   		item.setImageIndent(1);
   		item.setImage(0, tickImage);
        //item.setImageIndent(2);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
//        item.setBackground(0, this.view.container.getDisplay().getSystemColor(SWT.COLOR_GREEN));
//        item.setText("YES");
      } else {
        final TableItem item = items[i];
        final StyledText text = new StyledText(this.view.samplesTable, SWT.SINGLE);
        text.setEditable(false);
        text.setText(sample);
        final TableEditor editor = new TableEditor(this.view.samplesTable);
        editor.grabHorizontal = editor.grabVertical = true;
        editor.setEditor(text, item, 1);
        editor.horizontalAlignment = SWT.CENTER;
        try{
        ImageDescriptor crossImageDesc = ImageDescriptor.createFromFile(this.getClass(), Messages.RegexLearnerWizardController2_CROSS_ICON);
       	final Image crossImage = crossImageDesc.createImage();
   		item.setImageIndent(1);
        item.setImage(0, crossImage);
//        item.setBackground(0, this.view.container.getDisplay().getSystemColor(SWT.COLOR_RED));
//        item.setText("NO");
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
      }
    }
    this.view.samplesTable.redraw();
    this.view.samplesTable.layout();
  }

}

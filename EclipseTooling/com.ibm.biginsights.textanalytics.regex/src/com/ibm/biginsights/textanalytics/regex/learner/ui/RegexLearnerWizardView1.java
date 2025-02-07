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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.learner.suggest.SuggestedRegex;

/**
 * This class includes the view elements for the first wizard page of the
 * RegexLearner Wizard.
 * 
 * 
 */
public class RegexLearnerWizardView1 extends WizardPage {



	// references to controller and wizard
	private final RegexLearnerWizardController1 controller;

	RegexLearnerWizard wizard;

	// control components
	Composite container;

	Button suggestRegexButton;

	GridData gridData;

	Button loadSamplesButton;

	Table table;

	Text informationLabel;

	Group regexSuggestionsGroup;

	// bias controls
	Composite biasComposite;

	Scale biasScale;

	Label specificLabel;

	Label generalLabel;

	private Composite informationComposite;

	int numbOfSuggestions = -1;

	// Constructor
	protected RegexLearnerWizardView1(String pageName, RegexLearnerWizard wizard) {
		super(pageName);
		setTitle(Messages.RegexLearnerWizardView1_REGEX_GENERATOR);
		// setImageDescriptor(Activator.getImageDescriptor(Messages.RegexLearnerWizardView1_REGEX_GEN_WIZARD_IMAGE));
		setDescription(Messages.RegexLearnerWizardView1_REGEX_GEN_WIZARD_DESC);
		this.controller = new RegexLearnerWizardController1(this);
		this.wizard = wizard;
	}

	/**
	 * create the user interface control items
	 * 
	 * @param Composite
	 *            parent - the parent container
	 */
	@Override
	public void createControl(Composite parent) {
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(parent,
						"com.ibm.biginsights.textanalytics.tooling.help.regex_generator");
		this.container = new Composite(parent, SWT.NULL);
		this.container.setLayout(new GridLayout(1, false));
		// composite for buttons ("Load samples from file" and
		// "Suggest Regular Expressions")
		final Composite buttonsComposite = new Composite(this.container,
				SWT.NULL);
		buttonsComposite.setLayout(new RowLayout());
		// create "Load samples from file" button
		this.loadSamplesButton = new Button(buttonsComposite, SWT.PUSH);
		this.loadSamplesButton
				.setText(Messages.RegexLearnerWizardView1_LABEL_LOAD_SAMPLES);
		this.loadSamplesButton.addSelectionListener (this.controller);
		// create "Suggest regular expression" button

		// create info image and info label
		// final Composite hintComposite = new Composite(buttonsComposite,
		// SWT.BOLD);
		// final RowLayout rowlayout = new RowLayout();
		// rowlayout.marginLeft = 30;
		// hintComposite.setLayout(rowlayout);
		// final ImageDescriptor infoID =
		// Activator.getImageDescriptor("info_obj.gif");
		// final Image infoImage = infoID.createImage();
		// final Label infoImageLabel = new Label(hintComposite, SWT.NONE);
		// infoImageLabel.setImage(infoImage);
		// final Label infoLabel = new Label(hintComposite, SWT.BOLD);
		// infoLabel.setText("Click at a row of this table to add or remove samples or load them from a text file.");
		// create table
		createTable(this.container);

		final Composite buttonsComposite2 = new Composite(this.container,
				SWT.NULL);
		buttonsComposite2.setLayout(new RowLayout());
		this.suggestRegexButton = new Button(buttonsComposite2, SWT.PUSH);
		this.suggestRegexButton
				.setText(Messages.RegexLearnerWizardView1_LABEL_GENERATE_REGEX);
		this.suggestRegexButton.addSelectionListener(this.controller);
    this.suggestRegexButton.setEnabled(false);

		// create radio button composite (for regular expressions)
		this.regexSuggestionsGroup = new Group(this.container, SWT.NULL);
		final GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		this.regexSuggestionsGroup.setLayout(layout);
		this.regexSuggestionsGroup
				.setText(Messages.RegexLearnerWizardView1_REG_EXPRESSION);

		createRadioButtons(null);

		gridData = new GridData();
		gridData.verticalAlignment = SWT.BEGINNING;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.minimumHeight = 100;
		// gridData.minimumWidth = 700;
		// gridData.widthHint = 700;
		this.regexSuggestionsGroup.setLayoutData(gridData);

    loadWizardSamples();

		// apply the control elements
		setControl(this.container);
	}

	public void createBiasScale() {
		if (this.biasComposite == null || this.biasComposite.isDisposed()) {
			this.biasComposite = new Composite(this.regexSuggestionsGroup,
					SWT.NONE);
			this.biasComposite.setLayout(new GridLayout(3, false));
			this.biasComposite
					.setToolTipText(Messages.RegexLearnerWizardView1_REGEX);
			this.specificLabel = new Label(this.biasComposite, SWT.NONE);
			this.specificLabel
					.setText(Messages.RegexLearnerWizardView1_LABEL_SPECIFIC_REGEX);
			gridData = new GridData();
			gridData.verticalIndent = 10;
			if(!System.getProperty("os.name").startsWith("Windows"))
			gridData.verticalIndent = 5;
			gridData.horizontalIndent = 100;
			this.specificLabel.setLayoutData(gridData);
			this.biasScale = new Scale(this.biasComposite, SWT.HORIZONTAL);
			this.biasScale.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
        @Override
        public void getName (AccessibleEvent e)
        {
          e.result = Messages.RegexLearnerWizardView1_REGEX_SLIDER;
        }
      });
			this.biasScale.setBounds(50, 40, 200, 40);
			this.biasScale.setMinimum(0);
			this.biasScale.setMaximum(4);
			this.biasScale.setSelection(2);
			this.biasScale.setPageIncrement(1);
			this.generalLabel = new Label(this.biasComposite, SWT.NONE);
			this.generalLabel
					.setText(Messages.RegexLearnerWizardView1_LABEL_GENERAL_REGEX);
			gridData = new GridData();
			gridData.verticalIndent = 10;
			if(!System.getProperty("os.name").startsWith("Windows"))
			gridData.verticalIndent = 5;
			this.generalLabel.setLayoutData(gridData);
			this.biasScale.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					final int temp = RegexLearnerWizardView1.this.controller.biasIndex;
					RegexLearnerWizardView1.this.controller.biasIndex = RegexLearnerWizardView1.this.biasScale
							.getSelection();

					if (temp != RegexLearnerWizardView1.this.controller.biasIndex) {
						RegexLearnerWizardView1.this.controller.suggestRegex();
						// RegexLearnerWizardView1.this.suggestRegexButton.setEnabled(false);
					}
				}
			});
		}

		// this.biasComposite.setVisible(false);

	}

	/**
	 * create table for samples
	 * 
	 * @param - Composite parent - the parent container
	 */
	private void createTable(Composite parent) {
		final int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		// create the table and its layout / style
		this.table = new Table(parent, style);
		final GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.minimumWidth = 800;
		gridData.widthHint = 800;
		if(System.getProperty("os.name").startsWith("Windows"))
		{
			gridData.minimumHeight = 150;
			gridData.heightHint = 150;
		}
		else
		{
			gridData.minimumHeight = 130;
			gridData.heightHint = 130;
		}
		this.table.setLayoutData(gridData);
		this.table.setLinesVisible(true);
		this.table.setHeaderVisible(true);
		this.table.setItemCount(RegexLearnerWizard.TABLE_ITEM_COUNT);
		this.table.setSize(400, 310);

		// Accessibility support
		this.table.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        if (e.childID == ACC.CHILDID_SELF)
          e.result = Messages.RegexLearnerWizardView1_LABEL_SAMPLES;
      }
		});

		// the one and only column
		final TableColumn column1 = new TableColumn(this.table, 0);
		column1.setText(Messages.RegexLearnerWizardView1_LABEL_SAMPLES);
		column1.setWidth(800);
		final TableEditor editor = new TableEditor(this.table);
		// The editor must have the same size as the cell and must not be any
		// smaller than 50 pixels.
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 800;
		// editing the first column
		final int EDITABLECOLUMN = 0;
		this.table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RegexLearnerWizardView1.this.wizard.canFinish = true;

				final Control oldEditor = editor.getEditor();
				if (oldEditor != null) {
					oldEditor.dispose();
				}
				final TableItem item = (TableItem) e.item;
				if (item == null) {
					return;
				}
				final Text newEditor = new Text(
						RegexLearnerWizardView1.this.table, SWT.NONE);
        editor.setEditor(newEditor, item, EDITABLECOLUMN);
				newEditor.setText(item.getText(EDITABLECOLUMN));
        newEditor.selectAll();
        newEditor.setFocus();
				newEditor.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent me) {
						final Text text = (Text) editor.getEditor();
						editor.getItem()
								.setText(EDITABLECOLUMN, text.getText());
					}
				});

				RegexLearnerWizardView1.this.suggestRegexButton
						.setEnabled(true);
			}
		});
		this.table.redraw();
		parent.layout();
	}

	/**
	 * create radio buttons with regex suggestions
	 * 
	 * @param ArrayList
	 *            <SuggestedRegex> suggestedRegexes - an array list of the
	 *            various regular expressions learned (by various learner
	 *            concepts)
	 */
	public void createRadioButtons(ArrayList<SuggestedRegex> suggestedRegexes) {
		try { // remove all control elements at this group
			for (final Control c : this.regexSuggestionsGroup.getChildren()) {
				if (suggestedRegexes != null
						&& c.getToolTipText() != null
						&& (c.getToolTipText().equals(
								Messages.RegexLearnerWizardView1_REGEX) || c
								.getToolTipText().equals("infoComposite"))) //$NON-NLS-2$
				{
					// System.out.println("control is: " + c.getToolTipText());
				} else {
					c.dispose();
				}
			}
			if (suggestedRegexes == null) {
				// create informationLabel
				this.informationLabel = new Text(this.regexSuggestionsGroup,
						SWT.NONE);
				this.informationLabel.setEditable (false);
				this.informationLabel
						.setText(Messages.RegexLearnerWizardView1_INFO_NO_REGEX_LEARNED);
				setPageComplete(false);
			} else {
				if (!suggestedRegexes.isEmpty()) {
					if (this.informationComposite == null
							|| this.informationComposite.isDisposed()) {
						informationComposite = new Composite(
								this.regexSuggestionsGroup, SWT.NONE);
						informationComposite.setLayout(new RowLayout());
						informationComposite.setToolTipText("infoComposite"); //$NON-NLS-1$
						GridData gridData = new GridData();
						gridData.verticalIndent = 2;
						informationComposite.setLayoutData(gridData);
						// final ImageDescriptor adviceID =
						// Activator.getImageDescriptor("info_obj.gif");
						// final Image adviceImage = adviceID.createImage();
						// final Label okImageLabel = new
						// Label(informationComposite, SWT.NONE);
						// okImageLabel.setImage(adviceImage);
						// create informationLabel
						this.informationLabel = new Text(informationComposite,
								SWT.NONE);
		        this.informationLabel.setEditable (false);
						this.informationLabel
								.setText(Messages.RegexLearnerWizardView1_GENERATED_REGEX_DESC);
						informationComposite.layout();
						informationComposite.setVisible(true);
						informationComposite.setEnabled(true);

						// System.out.println("created" +
						// this.informationComposite.isDisposed() +
						// this.informationComposite.isVisible());
					}

					createBiasScale();

					// create radio buttons (for regular expressions)
					final int numOfSuggs = suggestedRegexes.size ();
					int i = 0;
					for (final SuggestedRegex s : suggestedRegexes) {

						final Button button = new Button(
								this.regexSuggestionsGroup, SWT.RADIO);
						button.setText(s.getRegexString());
						button.setFont(new Font(
										this.regexSuggestionsGroup.getDisplay(),
										Messages.RegexLearnerWizardView1_FONT_SANS_SERIF,
										10, SWT.BOLD));

						// Accessibility support
						final int optionNum = i + 1;
						button.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
              @Override
              public void getName (AccessibleEvent e)
              {
                e.result = MessageFormat.format(Messages.RegExConstructs_OPTION,
                            new Object[] { optionNum, numOfSuggs, button.getText (), s.getHint () });
              }
            });
						gridData = new GridData();
						gridData.verticalIndent = 10;
						if(!System.getProperty("os.name").startsWith("Windows"))
						gridData.verticalIndent = 0;
						button.setLayoutData(gridData);
						button.addSelectionListener(new SelectionListener() {
							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
								// nothing happens
							}

							@Override
							public void widgetSelected(SelectionEvent e) {
								// regexSuggestionsGroup.getChildren()[0] is a
								// label
								// regexSuggestionsGroup.getChilden()[1] is a
								// label (each even number except for 0 is
								// a radio button)
								for (int j = 1; j < RegexLearnerWizardView1.this.regexSuggestionsGroup
										.getChildren().length; j++) {
									if (j % 2 == 0) {
										final Button regexButton = (Button) RegexLearnerWizardView1.this.regexSuggestionsGroup
												.getChildren()[j];
										if (regexButton.getSelection()) {
											RegexLearnerWizardView1.this.wizard.selectedRegexIndex = (j - 2) / 2;
										}
									}
								}
							}
						});

						final Label label = new Label(
								this.regexSuggestionsGroup, SWT.NONE);
						if (suggestedRegexes.size() > 1) {
							label.setText(s.getHint());
						}
						gridData = new GridData();
						gridData.horizontalIndent = 10;
						label.setLayoutData(gridData);
						// select first regular expression (default)
						if (i == 0) {
							button.setSelection(true);
						}
						i++;
					}
					// choose first radio button per default
					this.wizard.selectedRegexIndex = 0;
					setPageComplete(true);
					this.wizard.canFinish = true;
					this.regexSuggestionsGroup.layout();
					this.container.layout();
				} else {
					// no regular expressions could be learned
					this.informationLabel = new Text(
							this.regexSuggestionsGroup, SWT.NONE);
	        this.informationLabel.setEditable (false);
					this.informationLabel
							.setText(Messages.RegexLearnerWizardView1_INFO_NO_REGEX
									+ Messages.RegexLearnerWizardView1_INFO_DIVIDE_SAMPLES
									+ Messages.RegexLearnerWizardView1_INFO_NO_REGEX_FOR_CONCEPT);
					setPageComplete(false);
				}
			}
			this.regexSuggestionsGroup.layout();
			this.container.layout();
		} catch (Exception e) {
			System.out.println(Messages.RegexLearnerWizardView1_ERROR);
			// e.printStackTrace();
		}
	}

	public void loadWizardSamples()
	{
	  table.removeAll ();

	  // trim the 'samples' list, get only the first items.
    while (wizard.samples.size () > RegexLearnerWizard.TABLE_ITEM_COUNT) {
      wizard.samples.remove (RegexLearnerWizard.TABLE_ITEM_COUNT);
    }

    for (String s : wizard.samples) {
      final TableItem item = new TableItem(table, SWT.NONE);
        item.setText(s);
    }

    // the table always contains TABLE_ITEM_COUNT editable items
    for (int i = wizard.samples.size (); i < RegexLearnerWizard.TABLE_ITEM_COUNT; i++) {
      final TableItem item = new TableItem(table, SWT.NONE);
      item.setText("");     // $NON-NLS-1$
    }

    if (wizard.samples.size () > 0) {
      this.suggestRegexButton.setEnabled(true);
//      controller.suggestRegex ();
    }

    table.redraw();
    table.notifyListeners(SWT.Selection, new Event());
	}
}

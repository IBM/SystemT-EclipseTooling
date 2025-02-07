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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.learner.suggest.RegexSuggester;
import com.ibm.biginsights.textanalytics.regex.learner.suggest.SuggestedRegex;

/**
 *  This class contains the logic of the first page of the
 *         RegexLearner Wizard. It also doubles up as a SelectionListener for the
 *         first wizard page.
 */
public class RegexLearnerWizardController1 extends SelectionAdapter {


	
	// bias index
	int biasIndex = 2;

	// the view of the first page
	RegexLearnerWizardView1 view;

	// constructor
	public RegexLearnerWizardController1(RegexLearnerWizardView1 view) {
		this.view = view;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.widget instanceof Button) {
			final Button button = (Button) event.widget;
			if (button == this.view.suggestRegexButton) {
				suggestRegex();
				// this.view.suggestRegexButton.setEnabled(false);
				this.view.biasComposite.setEnabled(true);
			}
			if (button == this.view.loadSamplesButton) {
				loadSamples();
				final ArrayList<String> sampleStrings = new ArrayList<String>();
				for (final TableItem item : this.view.table.getItems()) {
					final String sampleString = item.getText();
					// only add those samples that contain more than the empty
					// string
					if (!sampleString.trim().equals("")) { //$NON-NLS-1$
						sampleStrings.add(item.getText());
					}
				}
				this.view.wizard.samples = sampleStrings;
				// if no samples, show message
				if (!sampleStrings.isEmpty()) {
					suggestRegex();
				}
				// this.view.suggestRegexButton.setEnabled(false);
			}
		}
	}

	/**
	 * This method loads the samples specified in a text file. The samples must
	 * be separated by newlines. The samples are put into the samples table.
	 */
	  void loadSamples() {
		final Shell shell = this.view.getShell();
		final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		final String[] filterNames = new String[] {
				Messages.RegexLearnerWizardController1_TEXT_FILES,
				Messages.RegexLearnerWizardController1_ALL_FILES };
		final String[] filterExtensions = new String[] { "*.txt" }; //$NON-NLS-1$ //$NON-NLS-2$
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		final String result = dialog.open();
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(
					result));
			String sample;
			this.view.table.removeAll();
			int count = 0;
			this.view.wizard.samples.clear();
			while (((sample = reader.readLine()) != null)
					&& (count < RegexLearnerWizard.TABLE_ITEM_COUNT)) {
				if (!sample.trim().equals("")) { //$NON-NLS-1$
					final TableItem item = new TableItem(this.view.table,
							SWT.NONE);
					item.setText(sample);
					this.view.wizard.samples.add(sample);
					count++;
				}
			}
			// the table always contains TABLE_ITEM_COUNT editable items
			for (int i = count; i < RegexLearnerWizard.TABLE_ITEM_COUNT; i++) {
				final TableItem item = new TableItem(this.view.table, SWT.NONE);
				item.setText(""); //$NON-NLS-1$
			}
			this.view.table.redraw();
			this.view.table.notifyListeners(SWT.Selection, new Event());
		} catch (final FileNotFoundException e) {
			final Status status = new Status(IStatus.ERROR,
					RegexLearnerWizard.WIZARD_ID, 0, e.getMessage(), null);
			ErrorDialog
					.openError(
							Display.getCurrent().getActiveShell(),
							Messages.RegexLearnerWizardController1_ERROR_FILENOTFOUND,
							Messages.RegexLearnerWizardController1_ERROR_CANNOT_OPENFILE
									+ result, status);
			// e.printStackTrace();
			// System.out.println("3");
		} catch (final IOException e) {
			final Status status = new Status(IStatus.ERROR,
					RegexLearnerWizard.WIZARD_ID, 0, e.getMessage(), null);
			ErrorDialog.openError(Display.getCurrent().getActiveShell(),
					Messages.RegexLearnerWizardController1_ERROR_READFILE,
					Messages.RegexLearnerWizardController1_ERROR_READ_FILE
							+ result, status);
			// e.printStackTrace();
			// System.out.println("2");
		} catch (Exception rr) {
			// TODO
		}
	}

	/**
	 * This method calls the algorithms learning regular expressions. It checks
	 * the resulting regular expressions for validity (if they match at least
	 * some of the samples). It then calls the method creating the radio buttons
	 * to choose one of the regexes.
	 */
	void suggestRegex() {
		// get sample strings from table
		final ArrayList<String> sampleStrings = new ArrayList<String>();
		for (final TableItem item : this.view.table.getItems()) {
			final String sampleString = item.getText();
			// only add those samples that contain more than the empty string
			if (!sampleString.trim().equals("")) { //$NON-NLS-1$
				sampleStrings.add(item.getText());
			}
		}
		this.view.wizard.samples = sampleStrings;
		// if no samples, show message
		if (sampleStrings.isEmpty()) {
			final Status status = new Status(IStatus.ERROR,
					RegexLearnerWizard.WIZARD_ID, 0,
					Messages.RegexLearnerWizardController1_NO_SAMPLES, null);
			ErrorDialog.openError(Display.getCurrent().getActiveShell(),
					Messages.RegexLearnerWizardController1_NO_SAMPLES_IN_TABLE,
					Messages.RegexLearnerWizardController1_INFO_SUGGEST_REGEX,
					status);
		} else {

			// get a regular expression
			try {
				this.view.wizard.suggestedRegexes = RegexSuggester
						.suggestRegexes(sampleStrings, this.biasIndex);
			} catch (Exception e) {
				// e.printStackTrace();
				this.view.wizard.suggestedRegexes = null;
			}
			// check if the suggested regexes are "useful" (match at least some
			// of the samples)
			final ArrayList<Integer> indicesToRemove = new ArrayList<Integer>();
			int index = 0;
			if (this.view.wizard.suggestedRegexes != null) {
				for (final SuggestedRegex regex : this.view.wizard.suggestedRegexes) {
					final String regexString = regex.getRegexString();
					// System.out.println("index=" + index + " " + regexString);
					// System.out.println(regex.getRegularExpression().toStringWithSamples());
					boolean matchedSample = false;
					for (final String sample : this.view.wizard.samples) {
						if (sample.matches(regexString)) {
							matchedSample = true;
						}
					}
					if (!matchedSample) {
						indicesToRemove.add(index);
					}
					index++;
				}
				if (indicesToRemove.size() > 0) {
					for (final int i : indicesToRemove) {
						this.view.wizard.suggestedRegexes.remove(i);
					}
				}
			}
			this.view.createRadioButtons(this.view.wizard.suggestedRegexes);
		}
	}
}

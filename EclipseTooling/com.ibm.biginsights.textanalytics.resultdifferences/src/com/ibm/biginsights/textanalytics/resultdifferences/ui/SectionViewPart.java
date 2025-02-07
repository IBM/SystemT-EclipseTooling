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
package com.ibm.biginsights.textanalytics.resultdifferences.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

/**
 * ViewPart extension that
 * 
 * 
 * 
 */

public class SectionViewPart extends ViewPart {



	/** FormToolkit to create Section with */
	private final FormToolkit fFormToolkit;

	/** Section main control */
	private Section fSection;

	/** style bits for creating the section */
	private int fStyle;

	/**
	 * Constructor
	 * 
	 * @param toolkit
	 *            FormToolkit to create Section with
	 * @param style
	 *            Style bits for creating the section
	 */
	public SectionViewPart(final FormToolkit toolkit, final int style) {
		super();
		fFormToolkit = toolkit;
		fStyle = style;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(final Composite parent) {
		fSection = fFormToolkit.createSection(parent, fStyle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (fSection != null && !fSection.isDisposed()) {
			fSection.setFocus();
		}
	}

	/**
	 * Returns the Section, or null if it was not created yet
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public Section getSection() {
		return fSection;
	}
}

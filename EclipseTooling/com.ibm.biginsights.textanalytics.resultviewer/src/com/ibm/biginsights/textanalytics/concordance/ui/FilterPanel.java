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

package com.ibm.biginsights.textanalytics.concordance.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;

/**
 * 
 * FilterPanel is a composite widget which is associated to a filter type and filter condition.
 *
 */
public class FilterPanel extends Composite {



	FilterCondition filterCondition;

	final Button cb1, cb2, clear, configure;
	final Label filterValue;

	final Label separator;

	protected Display display;
	private Color WHITE = new Color(display, 255, 255, 255);

	/**
	 * @param c
	 * @param style
	 */
	public FilterPanel(Composite composite, int style) {
		super(composite, style);

		display = composite.getDisplay();
		if (display.getHighContrast ()) { //If system is in high contrast mode, use system default colour scheme. Ref. Task 34323
		  this.setBackground (display.getSystemColor (SWT.COLOR_WIDGET_BACKGROUND));
      this.setForeground (display.getSystemColor (SWT.COLOR_WIDGET_FOREGROUND));
		}
		else {
      this.setBackground(WHITE);
		}

		GridLayout gl = new GridLayout();
		gl.numColumns = 4;
		this.setLayout(gl);

		cb1 = new Button(this, SWT.CHECK);
		cb1.setBackground(this.getBackground());
		cb1.setToolTipText(Messages.FilterView_checkboxApplyTooltip);
		cb1.setFont (new Font(display, "Arial", 10, SWT.BOLD));

		cb1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isSelected = cb1.getSelection();

				if(isSelected) {
					filterCondition.setFilterApplied(isSelected);
					apply(filterCondition);	
					cb1.setToolTipText(Messages.FilterView_checkboxRemoveTooltip);
				}else {
					filterCondition.setFilterApplied(isSelected);
					remove(filterCondition);
					cb1.setToolTipText(Messages.FilterView_checkboxApplyTooltip);
				}
				
			}		
		});
		
		cb1.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
		  @Override
		  public void getName (AccessibleEvent e)
		  {
		    String[] params = {cb1.getText (),filterValue.getText ()};
		    e.result = Messages.getString ("FilterPanel_cb1_AccessibilityMessage", params); //This makes it essential that filterValue is set in init().
		  }
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		cb1.setLayoutData (gd);

		cb2 = new Button(this, SWT.CHECK);
		cb2.setVisible(false);

		filterValue = new Label(this, SWT.NONE|SWT.WRAP);
		if (display.getHighContrast ()) {
		  filterValue.setBackground (display.getSystemColor (SWT.COLOR_WIDGET_BACKGROUND));
		  filterValue.setForeground (display.getSystemColor (SWT.COLOR_WIDGET_FOREGROUND));
		} else {
		  filterValue.setBackground(WHITE);
		}

		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace =  true;

		filterValue.setLayoutData(gd);

		clear = new Button(this, SWT.FLAT | SWT.NO_BACKGROUND);
		if (display.getHighContrast ()) {
		  clear.setBackground (display.getSystemColor (SWT.COLOR_WIDGET_BACKGROUND));
		} else {
		  clear.setBackground(WHITE);
		}
		clear.setImage(Activator.getImageDescriptor("clear.gif").createImage());		
		clear.setSize(16, 16);
		clear.setToolTipText(Messages.FilterView_clearButtonTooltip);
		clear.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
		  @Override
		  public void getName (AccessibleEvent e)
		  {
		    e.result = Messages.FilterView_clearButtonTooltip;
		  }
		});
		clear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clear(filterCondition);
			}
		});

		configure = new Button(this, SWT.FLAT);
		configure.setImage(Activator.getImageDescriptor("configure.gif")
				.createImage());
		if (display.getHighContrast ()) {
		  configure.setBackground (display.getSystemColor (SWT.COLOR_WIDGET_BACKGROUND));
		} else {
		  configure.setBackground(WHITE);
		}
		configure.setSize(12, 12);
		configure.setToolTipText(Messages.FilterView_configButtonTooltip);
		configure.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
		  @Override
		  public void getName (AccessibleEvent e)
		  {
		    e.result = Messages.FilterView_configButtonTooltip;
		  }
		});
		configure.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				configure(filterCondition);
			}
		});
		
		separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		separator.setLayoutData(gd);
	}

	public void init() {
		filterValue.setText(filterCondition.getFilterValue());
		cb1.setText (filterCondition.getFilterName ());
		
	}

	public FilterCondition getFilterCondition() {
		return filterCondition;
	}

	public void setFilterCondition(FilterCondition filterCondition) {
		this.filterCondition = filterCondition;
	}
	
	/**
	 * clears the filtercondition
	 * 
	 * @param fc
	 */
	public void clear(FilterCondition fc) {
		this.filterValue.setText(Messages.FilterView_notconfigured);

		fc.setStringFilter(null);
		fc.setConfigured(false);
		fc.clear();
		this.cb1.setSelection(false);
		this.cb1.setToolTipText(Messages.FilterView_checkboxApplyTooltip);
		fc.setFilterApplied(false);
	}
	
	/**
	 * configures the filtercondition
	 * 
	 * @param fc
	 */
	public void configure(FilterCondition fc) {
		fc.configure();
	}

	/**
	 * applies the filtercondition 
	 * 
	 * @param fc
	 */
	public void apply(FilterCondition fc){
		if(fc.isConfigured)
			fc.apply();
	}
	
	/**
	 * removes the filtercondition
	 * 
	 * @param fc
	 */
	public void remove(FilterCondition fc) {
		if(fc.isConfigured)
			fc.remove();
	}
	
	/**
	 * updates the filtervalues
	 * 
	 * @param value
	 */
	public void updateFilterValue(String value) {
		if(value != null) {
			this.filterValue.setText(value);
			this.cb1.setSelection(true);
		}
	}
}

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.concordance.model.IFiles;
import com.ibm.biginsights.textanalytics.concordance.model.impl.StringFilter;
import com.ibm.biginsights.textanalytics.concordance.ui.FilterCondition.FilterType;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;

/**
 * 
 * FilterViewer is part of ConcordanceView which helps to apply filters to the model
 *
 */
public class FilterViewer {



	private SashForm sashForm;
	
	private boolean isDisplayed;
	
	private FilterPanel[] filterPanel;

	Composite composite;
	
	ScrolledComposite sc;

	public FilterViewer(Composite c, int style) {
		this.sashForm = (SashForm) c;
		sc = new ScrolledComposite(c, SWT.V_SCROLL | SWT.NONE);
		composite = new Composite(sc, SWT.NONE);
		this.addComponents(composite);

		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	protected void addComponents(Composite c) {
		filterPanel = new FilterPanel[5];
		GridLayout g = new GridLayout(1, false);
		GridData gd = new GridData();
		g.marginWidth = 0;
		g.verticalSpacing = 0;
		g.horizontalSpacing = 0;
		c.setLayout(g);

		Label title = new Label(c, SWT.NONE);
		title.setText(Messages.FilterView_title);

		filterPanel[0] = new FilterPanel(c, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = 3;
		filterPanel[0].setLayoutData(gd);

		filterPanel[0].setFilterCondition(new FilterCondition(filterPanel[0],
				FilterType.SPAN_ATTR_NAME_FILTER,
				Messages.FilterView_notconfigured));
		filterPanel[0].init();

		filterPanel[1] = new FilterPanel(c, SWT.NONE);
		filterPanel[1].setFilterCondition(new FilterCondition(filterPanel[1],
				FilterType.SPAN_ATTR_VALUE_FILTER,
				Messages.FilterView_notconfigured));
		filterPanel[1].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterPanel[1].init();

		filterPanel[2] = new FilterPanel(c, SWT.NONE);
		filterPanel[2]
				.setFilterCondition(new FilterCondition(filterPanel[2],
						FilterType.INPUT_DOC_FILTER,
						Messages.FilterView_notconfigured));
		filterPanel[2].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterPanel[2].init();

		filterPanel[3] = new FilterPanel(c, SWT.NONE);
		filterPanel[3].setFilterCondition(new FilterCondition(filterPanel[3],
				FilterType.LEFT_CONTEXT_FILTER,
				Messages.FilterView_notconfigured));
		filterPanel[3].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterPanel[3].init();

		filterPanel[4] = new FilterPanel(c, SWT.NONE);
		filterPanel[4].setFilterCondition(new FilterCondition(filterPanel[4],
				FilterType.RIGHT_CONTEXT_FILTER,
				Messages.FilterView_notconfigured));
		filterPanel[4].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterPanel[4].init();
		this.refresh();

		//computes the size of composite when the label is expanded
		composite.addListener(SWT.Resize, new Listener() {
			int width = -1;
			int height = -1;

			@Override
			public void handleEvent(Event e) {
				int newWidth = composite.getSize().x;
				int newHeight = composite.getSize().y;
				if (newWidth != width) {
					sc.setMinHeight(composite
							.computeSize(newWidth, SWT.DEFAULT).y);
				}
				if (newHeight != height) {
					sc.setMinHeight(composite.computeSize(newWidth, newHeight).y);
				}
				width = newWidth;
				height = newHeight;
			}
		});
	}

	/**
	 * hides the filterviewer from concordance view
	 */
	public void hide() {
		this.sashForm.setWeights(new int[] { 100, 0 });
		this.setDisplayed(false);
	}

	/**
	 * shows filterviewer in concordance view
	 */
	public void show() {
		this.sashForm.setWeights(new int[] { 75, 25 });
		this.setDisplayed(true);
	}

	/**
	 * toggles filterviewer
	 */
	public void toggle() {
		if (isDisplayed) {
			this.hide();
		} else {
			this.show();
		}

	}

	/**
	 * refresh the filterviewer sashform
	 */
	public void refresh() {
		composite.layout(true);
		composite.pack();

		int w[] = this.sashForm.getWeights();
		this.sashForm.setWeights(new int[] { 100, 1 });
		this.sashForm.setWeights(w);

	}
	
	public void load(IProject project) {
		// serialize filters here
		SerializeObject serialize = new SerializeObject(FilterConditionQuery.class);	
		if(project != null) {
			IFile file = project.getFile("fc.xml");
			FilterConditionQuery fcq = null;
			try {
				fcq = (FilterConditionQuery) serialize.getObjectForStream(file.getContents());
				restoreFilterCondition(fcq);
			}catch(CoreException e) {
				//TODO
			}
		}		
	}
	
	public void save() {
		// serialize filters here
		SerializeObject serialize = new SerializeObject(FilterConditionQuery.class);

		ConcordanceView concView = getConcordanceView();
		if(concView != null && concView.getModel() != null && concView.getModel().getProject() != null) {
			IProject project = concView.getModel().getProject();
			IFile file = project.getFile("fc.xml");
	
			FilterConditionQuery fcq = getFilterConditionQuery();
			serialize.writeToFile(file, fcq);
		}
	}
	
	/** 
	 * Gets filter condition from each panel 
	 * @return FilterCondition
	 */
	protected FilterCondition[] getAllFilterCondition() {
		FilterCondition[] fc = new FilterCondition[5];
		int i=0;
		for (FilterPanel fp : this.filterPanel) {
			fc[i++] = fp.getFilterCondition();
		}
		return fc;
	}
	
	/**
	 * Get filter condtion query from filter condition
	 * 
	 * @return FilterConditionQuery
	 */
	protected FilterConditionQuery getFilterConditionQuery() {
		FilterCondition[] fcArray = getAllFilterCondition();
		
		FilterConditionQuery fcq = new FilterConditionQuery();
		
		for(FilterCondition fc: fcArray) {
			FilterType type = fc.getFilterType();
			switch(type) {
				case SPAN_ATTR_NAME_FILTER:
					String[] types = fc.getActiveTypes();
					if(types != null) {
						fcq.types = types;
					}
					break;
				
				case SPAN_ATTR_VALUE_FILTER:
					TreeMap<Boolean, String> docMap =  new TreeMap<Boolean, String>();
					if(fc.getStringFilter() != null) {
						docMap.put(fc.getStringFilter().isRegex(), fc.getStringFilter().getString());
						fcq.setInputDoc(docMap);
					}
					break;					
					
				case INPUT_DOC_FILTER:
					String[] files = fc.getActiveFiles();
					if(files != null) {
						fcq.files = files;
					}
					break;
				
				case LEFT_CONTEXT_FILTER:
					TreeMap<Boolean, String> leftMap =  new TreeMap<Boolean, String>();
					if(fc.getStringFilter() != null) {
						leftMap.put(fc.getStringFilter().isRegex(), fc.getStringFilter().getString());
						fcq.setLeftContext(leftMap);		
					}
					break;
				
				case RIGHT_CONTEXT_FILTER:
					TreeMap<Boolean, String> rightMap =  new TreeMap<Boolean, String>();
					if(fc.getStringFilter() != null) {
						rightMap.put(fc.getStringFilter().isRegex(), fc.getStringFilter().getString());
						fcq.setRightContext(rightMap);
					}
					break;
			}
			
		}
		return fcq;
	}
	
	public void restoreFilterCondition(FilterConditionQuery fcq) {
		FilterCondition[] fcArray = getAllFilterCondition();
		for (FilterCondition fc : fcArray) {
			FilterType type = fc.getFilterType();

			Iterator<Entry<Boolean, String>> iter = null;
			switch (type) {
			case SPAN_ATTR_NAME_FILTER:
				fc.setActiveTypes(fcq.types);

				fc.setFilterValue(fcq.types);
				if ((fcq.types != null) && (fcq.types.length >0))
				{
					fc.setConfigured(true);
				}
				break;

			case SPAN_ATTR_VALUE_FILTER:
				iter = fcq.getInputDoc().entrySet().iterator();
				if (iter.hasNext()) {
					Entry<Boolean, String> e = iter.next();
					fc.setFilterValue(e.getValue());
					fc.setStringFilter(new StringFilter(e.getKey(), e.getValue()));
					if ((fcq.types != null) && (fcq.types.length >0))
					{
						fc.setConfigured(true);
					}
				}
				break;

			case INPUT_DOC_FILTER:
				fc.setActiveFiles(fcq.files);
				fc.setFilterValue(fcq.files);
				if ((fcq.files != null) && (fcq.files.length >0))
				{
					fc.setConfigured(true);
				}
				break;

			case LEFT_CONTEXT_FILTER:
				iter = fcq.getLeftContext().entrySet().iterator();
				if (iter.hasNext()) {
					Entry<Boolean, String> e = iter.next();
					fc.setFilterValue(e.getValue());
					fc.setStringFilter(new StringFilter(e.getKey(), e.getValue()));
					fc.setConfigured(true);
				}
				break;
			case RIGHT_CONTEXT_FILTER:
				iter = fcq.getRightContext().entrySet().iterator();
				if (iter.hasNext()) {
					Entry<Boolean, String> e = iter.next();
					fc.setFilterValue(e.getValue());
					fc.setStringFilter(new StringFilter(e.getKey(), e
							.getValue()));
					fc.setConfigured(true);
				}
				break;
			}
			
			fc.setFilterApplied(false);
			fc.filterPanel.cb1.setSelection(false);			
		}
	}
	
	public boolean isDisplayed() {
		return isDisplayed;
	}

	public void setDisplayed(boolean isDisplayed) {
		this.isDisplayed = isDisplayed;
	}

	public void clearFilters() {
	
		for (int i = 0; i < filterPanel.length; i++) {
			this.filterPanel[i].filterValue
					.setText(Messages.FilterView_notconfigured);
			FilterCondition fc = this.filterPanel[i].getFilterCondition ();
			fc.setStringFilter(null);
		  fc.setConfigured(false);
		  fc.clear();
			this.filterPanel[i].cb1.setSelection(false);
			this.filterPanel[i].cb1.setToolTipText(Messages.FilterView_checkboxApplyTooltip);
		}
		this.refresh();
	}

	private ConcordanceView getConcordanceView() {
		IWorkbenchPart part = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getPartService().getActivePart();
		if (part instanceof ConcordanceView) {
			ConcordanceView concView = (ConcordanceView) part;
			return concView;
		}
		return null;
	}
	
	
	private boolean areFilterFilesSubsetOfModelFiles(IFiles[] filterFiles, IFiles[] modelFiles)
	{
		ArrayList filterFilesAL= new ArrayList();
		for(IFiles file :filterFiles)
		{
			filterFilesAL.add(file);
		}

		ArrayList modelFilesAL= new ArrayList();
		for(IFiles file :modelFiles)
		{
			modelFilesAL.add(file);
		}
		if (modelFilesAL.contains(filterFilesAL)== false)
		{
			return false;
		}
		else
		{
			return true;
		}

	}
	
	/**
	 * Gives an array of FilterConditions which are at the moment active for this FilterViewer
	 * @return FilterCondition[] if at least one filter is active, or else null
	 */
	public FilterCondition[] getAllSelectedFilterConditions() {
		ArrayList<FilterCondition> activeFilters = new ArrayList<FilterCondition>();
		FilterCondition[] fcArray = getAllFilterCondition();
		for (FilterCondition fc: fcArray) {
			if (fc.isFilterApplied() && fc.isConfigured()) {
				activeFilters.add(fc);
			}
		}
		return activeFilters.toArray(new FilterCondition[0]);
	}
	
	/**
	 * Applies all active filters (i.e. checkbox selected) on Annotation Explorer view rows.
	 */
	public void applyAllSelectedFilters() {
		FilterCondition[] fcArray = getAllSelectedFilterConditions();
		for (FilterCondition fc: fcArray) {
			fc.apply();
		}
	}
}

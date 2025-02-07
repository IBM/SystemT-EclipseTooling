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

import static com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel.StringFilterType.ANNOTATION_TEXT;
import static com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel.StringFilterType.LEFT_CONTEXT;
import static com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel.StringFilterType.RIGHT_CONTEXT;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.IStringFilter;
import com.ibm.biginsights.textanalytics.concordance.ui.filter.AnnotationTextFilterHandler;
import com.ibm.biginsights.textanalytics.concordance.ui.filter.FileFilterHandler;
import com.ibm.biginsights.textanalytics.concordance.ui.filter.LeftContextFilterHandler;
import com.ibm.biginsights.textanalytics.concordance.ui.filter.RightContextFilterHandler;
import com.ibm.biginsights.textanalytics.concordance.ui.filter.TypeFilterHandler;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;

/**
 * Applies filtercondition to the concordance view model 
 */
public class FilterCondition {



	enum FilterType {
		SPAN_ATTR_NAME_FILTER, 
		SPAN_ATTR_VALUE_FILTER, 
		INPUT_DOC_FILTER, 
		LEFT_CONTEXT_FILTER,
		RIGHT_CONTEXT_FILTER
	}
	
	FilterPanel filterPanel;
	
	FilterType filterType;

	String filterValue;

	boolean isConfigured;
	
	boolean isFilterApplied;

	String[] activeTypes, activeFiles;

	IStringFilter annotationFilter, leftFilter, rightFilter;

	public FilterCondition(FilterPanel filterPanel, FilterType type, String value) {
		this.filterPanel = filterPanel; 
		this.filterType = type;
		this.filterValue = value;
		this.isConfigured = false;
	}

	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}

	public String getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(String[] filters) {
		StringBuilder builder = new StringBuilder();
        for (int i = 0; i < filters.length-1; i++) {
            if(i<4) {
            	builder.append((String)filters[i]+", ");
            }else {
            	break;
            }
          }
		
        if(filters.length>0){
      	  if(filters.length > 5) {
      		  builder.append((String)filters[4]+" ...");
      	  }else {
      		  builder.append((String)filters[filters.length-1]+"");
      	  }
      	  this.setFilterValue(builder.toString());
        }else {
      	  this.setFilterValue(Messages.FilterView_notconfigured);
      	  this.setConfigured(false);
        }		
	}
	
	public void setFilterValue(String filterValue) {
		this.filterPanel.updateFilterValue(filterValue);
		this.filterValue = filterValue;
		this.isConfigured = true;
	}

	public boolean isConfigured() {
		return isConfigured;
	}

	public void setConfigured(boolean isConfigured) {
		this.isConfigured = isConfigured;
	}
	
	public boolean isFilterApplied() {
		return isFilterApplied;
	}

	public void setFilterApplied(boolean isFilterApplied) {
		this.isFilterApplied = isFilterApplied;
	}

	public String[] getActiveTypes() {
		return activeTypes;
	}

	public void setActiveTypes(String[] activeTypes) {
		this.activeTypes = activeTypes;
	}

	public String[] getActiveFiles() {
		return activeFiles;
	}

	public void setActiveFiles(String[] activeFiles) {
		this.activeFiles = activeFiles;
	}
	
	public IStringFilter getStringFilter() {
		IStringFilter filter = null;
		switch(getFilterType()) {
		case SPAN_ATTR_VALUE_FILTER:
			filter =  annotationFilter;
			break;
		
		case LEFT_CONTEXT_FILTER:
			filter = leftFilter;
			break;
			
		case RIGHT_CONTEXT_FILTER:
			filter = this.rightFilter;
			break;
		}
		return filter;
	}
	
	public void setStringFilter(IStringFilter stringFilter) {
		switch(getFilterType()) {
		case SPAN_ATTR_VALUE_FILTER:
			this.annotationFilter = stringFilter;
			break;
		
		case LEFT_CONTEXT_FILTER:
			this.leftFilter = stringFilter;
			break;
		case RIGHT_CONTEXT_FILTER:
			this.rightFilter = stringFilter;
			break;
		}
	}

	public String getFilterName() {
		FilterType type = getFilterType();
		String name = "";
		switch (type) {
		case SPAN_ATTR_NAME_FILTER:
			name = Messages.FilterView_spanattrname;
			break;

		case SPAN_ATTR_VALUE_FILTER:
			name = Messages.FilterView_spanattrvalue;
			break;

		case INPUT_DOC_FILTER:
			name = Messages.FilterView_inputdoc;
			break;
		case LEFT_CONTEXT_FILTER:
			name = Messages.FilterView_leftcontext;
			break;
		case RIGHT_CONTEXT_FILTER:
			name = Messages.FilterView_rightcontext;
			break;
		}
		return name;
	}
	
	public void clear() {
		FilterType type = this.getFilterType();
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
		ConcordanceView concView = null;
		IConcordanceModel model = null;
	    if (part instanceof ConcordanceView) {
	        concView = (ConcordanceView) part;
	        model = concView.getModel();
	   
			switch(type) {
			case SPAN_ATTR_NAME_FILTER:
				 model.getTypes().setAllActive();
				 this.activeTypes = new String[]{};
				break;
			case SPAN_ATTR_VALUE_FILTER:
				this.annotationFilter = model.getStringFilter(ANNOTATION_TEXT);
				model.resetStringFilter(ANNOTATION_TEXT);	
				break;
			case INPUT_DOC_FILTER:
				model.getFiles().setAllActive();
				this.activeFiles = new String[]{};
				break;
			case LEFT_CONTEXT_FILTER:
				this.leftFilter = model.getStringFilter(LEFT_CONTEXT);
				model.resetStringFilter(LEFT_CONTEXT);
				break;
			case RIGHT_CONTEXT_FILTER:
				this.rightFilter = model.getStringFilter(RIGHT_CONTEXT);
				model.resetStringFilter(RIGHT_CONTEXT);
				break;
			}

		    // Update filters in the UI
			concView.updateFilters();
		    // Refresh UI after filter update
			concView.refresh();	
			
			concView.getFilterViewer().refresh();
			concView.getFilterViewer().save();
	    }
	}
	
	public void configure() {
		FilterType type = this.getFilterType();
		switch(type) {
		case SPAN_ATTR_NAME_FILTER:
			TypeFilterHandler typeFilterHandler = new TypeFilterHandler();
			typeFilterHandler.execute(this);	
			break;
		case SPAN_ATTR_VALUE_FILTER:
			AnnotationTextFilterHandler annotationTextFilterHandler = new AnnotationTextFilterHandler();
			annotationTextFilterHandler.execute(this);
			break;
		case INPUT_DOC_FILTER:
			FileFilterHandler fileFilterHandler = new FileFilterHandler();
			fileFilterHandler.execute(this);			
			break;
		case LEFT_CONTEXT_FILTER:
			LeftContextFilterHandler leftContextFilterHandler = new LeftContextFilterHandler();
			leftContextFilterHandler.execute(this);
			break;
		case RIGHT_CONTEXT_FILTER:
			RightContextFilterHandler rightContextFilterHandler = new RightContextFilterHandler();
			rightContextFilterHandler.execute(this);
			break;

		}
	}
	
	public void apply() {
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
		ConcordanceView concView = null;
		IConcordanceModel model = null;
		
	    if (part instanceof ConcordanceView) {
	        concView = (ConcordanceView) part;
	        model =  concView.getModel();

	    
		FilterType type = this.getFilterType();

		switch(type) {
			case SPAN_ATTR_NAME_FILTER:
				if(this.activeTypes != null) {
					model.getTypes().setActiveTypes(this.activeTypes);
				}
				break;
			case SPAN_ATTR_VALUE_FILTER:
				if(this.annotationFilter != null) {
					model.setStringFilter(ANNOTATION_TEXT, this.annotationFilter);
					concView.updateFilters();
				}
				break;
			case INPUT_DOC_FILTER:
				if(this.activeFiles != null) {
					model.getFiles().setActiveFiles(this.activeFiles);
				}
				break;
			case LEFT_CONTEXT_FILTER:
				if(this.leftFilter != null) {
					model.setStringFilter(LEFT_CONTEXT, this.leftFilter);
					concView.updateFilters();
				}
				break;
			case RIGHT_CONTEXT_FILTER:
				if(this.rightFilter != null) {
					model.setStringFilter(RIGHT_CONTEXT, this.rightFilter);
					concView.updateFilters();
				}
				break;
	
			}
			concView.refresh();
	    }
	}
	
	public void remove() {
		FilterType type = this.getFilterType();
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
		ConcordanceView concView = null;
		IConcordanceModel model = null;
	    if (part instanceof ConcordanceView) {
	        concView = (ConcordanceView) part;
	        model = concView.getModel();
	   
			switch(type) {
				case SPAN_ATTR_NAME_FILTER:
					 this.activeTypes = model.getTypes().getActiveTypes().toArray(new String[]{});
					 model.getTypes().setActiveTypes(model.getTypes().getTypes().toArray(new String[]{}));
					break;
				case SPAN_ATTR_VALUE_FILTER:
					this.annotationFilter = model.getStringFilter(ANNOTATION_TEXT);
					model.resetStringFilter(ANNOTATION_TEXT);		
					concView.updateFilters();
					break;
				case INPUT_DOC_FILTER:
					this.activeFiles = model.getFiles().getActiveFiles().toArray(new String[]{});
					model.getFiles().setActiveFiles(model.getFiles().getFiles().toArray(new String[]{}));
					break;
				case LEFT_CONTEXT_FILTER:
					this.leftFilter = model.getStringFilter(LEFT_CONTEXT);
					model.resetStringFilter(LEFT_CONTEXT);
					concView.updateFilters();
					break;
				case RIGHT_CONTEXT_FILTER:
					this.rightFilter = model.getStringFilter(RIGHT_CONTEXT);
					model.resetStringFilter(RIGHT_CONTEXT);
					concView.updateFilters();
					break;
			}
		    // Refresh UI after filter update
			concView.refresh();			
	    }
	}
}

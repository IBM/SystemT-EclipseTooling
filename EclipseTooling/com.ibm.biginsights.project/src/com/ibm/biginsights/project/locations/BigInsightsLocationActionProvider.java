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
package com.ibm.biginsights.project.locations;

import java.io.File;
import java.util.Iterator;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.actions.AppsRefreshAction;
import com.ibm.biginsights.project.locations.actions.ConfFileRefreshAction;
import com.ibm.biginsights.project.locations.actions.ImportAppAction;
import com.ibm.biginsights.project.locations.actions.LocationDeleteAction;
import com.ibm.biginsights.project.locations.actions.LocationFileBrowseAction;
import com.ibm.biginsights.project.locations.actions.LocationHBaseShellAction;
import com.ibm.biginsights.project.locations.actions.LocationJAQLShellAction;
import com.ibm.biginsights.project.locations.actions.LocationJobDetailsAction;
import com.ibm.biginsights.project.locations.actions.LocationNewAction;
import com.ibm.biginsights.project.locations.actions.LocationPigShellAction;
import com.ibm.biginsights.project.locations.actions.LocationTestConnectionAction;
import com.ibm.biginsights.project.locations.actions.LocationUpdateAction;
import com.ibm.biginsights.project.locations.actions.OpenConfFileAction;
import com.ibm.biginsights.project.locations.actions.ViewAppAction;
import com.ibm.biginsights.project.locations.apps.BigInsightsApp;
import com.ibm.biginsights.project.locations.apps.BigInsightsAppFolder;
import com.ibm.biginsights.project.util.BIConstants;

public class BigInsightsLocationActionProvider extends CommonActionProvider {

	private ICommonViewerWorkbenchSite workbenchSite;
	private LocationNewAction newAction;
	private LocationNewAction newActionButton=null;
	private LocationUpdateAction updateAction;
	private AppsRefreshAction appsRefreshAction;
	private ImportAppAction importAppAction;
	private ViewAppAction viewAppAction;
	private LocationDeleteAction deleteAction;
	private LocationTestConnectionAction testConnectionAction;
	private LocationFileBrowseAction filebrowseAction;
	private LocationJobDetailsAction jobDetailsAction;
	private LocationJAQLShellAction jaqlShellAction;
	private LocationHBaseShellAction hbaseShellAction;
	private LocationPigShellAction pigShellAction;
	private ConfFileRefreshAction confFileRefreshAction;
	private OpenConfFileAction openFileAction;
	
	public BigInsightsLocationActionProvider() {
		
	}
	
	public void init(ICommonActionExtensionSite site)
	{
		ICommonViewerSite viewerSite = site.getViewSite();
		if (viewerSite instanceof ICommonViewerWorkbenchSite) {
			workbenchSite = (ICommonViewerWorkbenchSite)viewerSite;			
		}
	}
	
	public void fillContextMenu(IMenuManager menu){
		ISelection selection = workbenchSite.getSelectionProvider().getSelection();
	    if (!(selection instanceof IStructuredSelection))
	      return;

	    final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	    
	    Iterator itSel = structuredSelection.iterator();
	    boolean isRootSelected = false;
	    boolean isAppFolderSelected = false;
	    boolean isAppSelected = false;
	    boolean isLocationSelected = false;
	    boolean isConfFileFolderSelected = false;
	    boolean isConfFileSelected = false;	    
	    IBigInsightsLocation location = null;
	    
	    while (itSel.hasNext())
	    {
	    	Object selectedItem = itSel.next();
	    	isRootSelected = isRootSelected ||selectedItem instanceof BigInsightsLocationRoot;
	    	isLocationSelected = isLocationSelected || selectedItem instanceof IBigInsightsLocation;
	    	isAppSelected = isAppSelected || selectedItem instanceof BigInsightsApp;	    		
	    	isAppFolderSelected = isAppFolderSelected || selectedItem instanceof BigInsightsAppFolder;
	    	isConfFileFolderSelected = isConfFileFolderSelected || selectedItem instanceof BigInsightsConfFolder;
	    	isConfFileSelected = isConfFileSelected || (selectedItem instanceof File && ((File)selectedItem).isFile());
	    	if (location==null && selectedItem instanceof IBigInsightsLocation)
	    		location = (IBigInsightsLocation)selectedItem;
	    }

	    // if only one element is selected, add New for root, and otherwise Update and Delete
	    if (isRootSelected) {
	    	newAction = new LocationNewAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
	    	newAction.setId(BIConstants.LOCATIONS_NEWMENUID);
	    	menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, newAction);
	    }
	    
	    if (isAppFolderSelected) {
	    	appsRefreshAction = new AppsRefreshAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider(), workbenchSite);
	    	menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, appsRefreshAction);
	    	
	    	if(structuredSelection.size()==1){
		    	//add cascading menu View By -> Name  and View By -> Category
		    	menu.add(new ContributionItem() {
	
					public void fill(Menu contextMenu, int index) {
						MenuItem miViewby= new MenuItem(contextMenu, SWT.CASCADE);
						miViewby.setText(Messages.Location_ActionViewBy);
						
						Menu menuViewby = new Menu(contextMenu);
						miViewby.setMenu(menuViewby);
						
						MenuItem miName= new MenuItem(menuViewby, SWT.RADIO);
						miName.setText(Messages.Location_ActionViewBy_Name);
						miName.addSelectionListener(new SelectionListener(){
							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
							}
							@Override
							public void widgetSelected(SelectionEvent e) {
								Object obj = structuredSelection.getFirstElement();
								if(obj instanceof BigInsightsAppFolder){
									BigInsightsLocation loc = (BigInsightsLocation)((BigInsightsAppFolder)obj).getParent();
									
									if(loc.getViewBy() == BIConstants.VIEWBY_CATEGORY || 
									   loc.getViewBy() == BIConstants.VIEWBY_TYPE){
										
										loc.setViewBy(BIConstants.VIEWBY_NAME);
										((BigInsightsAppFolder)obj).refresh();
										LocationRegistry.getInstance().updateLocation(loc.getLocationName(), loc);  
									}
								}
							}
							
						});
						
						MenuItem miCategory= new MenuItem(menuViewby, SWT.RADIO);
						miCategory.setText(Messages.Location_ActionViewBy_Category);
						miCategory.addSelectionListener(new SelectionListener(){
							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
							}
							@Override
							public void widgetSelected(SelectionEvent e) {
								Object obj = structuredSelection.getFirstElement();
								if(obj instanceof BigInsightsAppFolder){
									BigInsightsLocation loc = (BigInsightsLocation)((BigInsightsAppFolder)obj).getParent();
									
									if(loc.getViewBy() == BIConstants.VIEWBY_NAME || 
									   loc.getViewBy() == BIConstants.VIEWBY_TYPE){
										
										loc.setViewBy(BIConstants.VIEWBY_CATEGORY);
										((BigInsightsAppFolder)obj).refresh();
										LocationRegistry.getInstance().updateLocation(loc.getLocationName(), loc);  
									}
								}
							}						
						});
						
						//add the view by type if server is 2.0 or above
						BigInsightsLocation location = (BigInsightsLocation)((BigInsightsAppFolder)structuredSelection.getFirstElement()).getParent();
						MenuItem miType=null;
						if(location.isVersion2orAbove()){
							miType= new MenuItem(menuViewby, SWT.RADIO);
							miType.setText(Messages.Location_ActionViewBy_Type);
							miType.addSelectionListener(new SelectionListener(){
								@Override
								public void widgetDefaultSelected(SelectionEvent e) {
								}
								@Override
								public void widgetSelected(SelectionEvent e) {
									Object obj = structuredSelection.getFirstElement();
									if(obj instanceof BigInsightsAppFolder){
										BigInsightsLocation loc = (BigInsightsLocation)((BigInsightsAppFolder)obj).getParent();
										
										if(loc.getViewBy() == BIConstants.VIEWBY_NAME || 
										   loc.getViewBy() == BIConstants.VIEWBY_CATEGORY){
											
											loc.setViewBy(BIConstants.VIEWBY_TYPE);
											((BigInsightsAppFolder)obj).refresh();
											LocationRegistry.getInstance().updateLocation(loc.getLocationName(), loc);  
										}
									}
								}						
							});
						}
					
						Object obj = structuredSelection.getFirstElement();
						if(obj instanceof BigInsightsAppFolder){
							BigInsightsLocation loc = (BigInsightsLocation)((BigInsightsAppFolder)obj).getParent();
							if(loc.getViewBy() == BIConstants.VIEWBY_NAME){
								miName.setSelection(true);
								miCategory.setSelection(false);
								if(miType != null) miType.setSelection(false);
							}else if(loc.getViewBy() == BIConstants.VIEWBY_CATEGORY){
								miName.setSelection(false);
								miCategory.setSelection(true);
								if(miType != null) miType.setSelection(false);
							}else{
								if(miType != null){
									miName.setSelection(false);
									miCategory.setSelection(false);
									miType.setSelection(true);
								}else{
									//1.4 and below default - for backward compatibility
									miName.setSelection(false);
									miCategory.setSelection(true);
								}
							}
						}
						
					}
		    	});
	    	}		
	    }
	    
	    if (isAppFolderSelected || isAppSelected) {
	    	viewAppAction = new ViewAppAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
	    	menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, viewAppAction);	
	    }

	    if (isAppSelected) {
	      	importAppAction = new ImportAppAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
	    	menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, importAppAction);	
	    }
	    
	    if (isConfFileSelected) {
	    	openFileAction = new OpenConfFileAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
	    	menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openFileAction);
	    }
	    
	    if (isConfFileFolderSelected) {
	    	confFileRefreshAction = new ConfFileRefreshAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
	    	menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, confFileRefreshAction);		    	
	    }
	    
	    if (isLocationSelected && !isAppSelected && !isAppFolderSelected && !isConfFileFolderSelected && !isConfFileSelected)
	    {
			updateAction = new LocationUpdateAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
			deleteAction = new LocationDeleteAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
			testConnectionAction = new LocationTestConnectionAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
			filebrowseAction = new LocationFileBrowseAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
			jobDetailsAction = new LocationJobDetailsAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
			jaqlShellAction = new LocationJAQLShellAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
			hbaseShellAction = new LocationHBaseShellAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
			pigShellAction = new LocationPigShellAction(workbenchSite.getPage(),workbenchSite.getSelectionProvider());
			
	    	menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, updateAction);		
	    	menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, deleteAction);	    	
	    	menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, testConnectionAction);
	    	menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, filebrowseAction);
	    	menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, jobDetailsAction);
//	    	if(System.getProperty("os.name").toLowerCase().indexOf("win") == -1){ //$NON-NLS-1$ //$NON-NLS-2$
	    		menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, jaqlShellAction);	    		
//	    	}
	    	// only show hbase shell if location is at least v2
	    	if (location.isVersion2orAbove()) {
	    		menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, pigShellAction);
	    		menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, hbaseShellAction);
	    	}
	    }
	    
	}
	
    public void fillActionBars(IActionBars actionBars) {
    	if(newActionButton == null){
	    	IToolBarManager toolbarManager = actionBars.getToolBarManager();
		    newActionButton = new LocationNewAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
		    newActionButton.setId(BIConstants.LOCATIONS_NEWBUTTONID);
		    newActionButton.setText(Messages.LOCATIONPAGE_ADD_BUTTON_LABEL);
	    	newActionButton.setEnabled(true);
		    toolbarManager.add(newActionButton);
    	}
    }
    
    public void updateActionBars() {
    	if(newActionButton != null && !newActionButton.isEnabled()){
    		newActionButton.setEnabled(true);
    	}
    }
}

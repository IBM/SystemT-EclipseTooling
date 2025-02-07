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
package com.ibm.biginsights.textanalytics.util.common.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.IDE;

import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;


public class ProjectBrowser extends Composite {



	protected Text tfProject;
	protected Button bBrowse;
	protected Label lProject;
	
	protected ILabelProvider labelProvider;
	protected ITreeContentProvider treeContentProvider;
	
	PropertyChangeSupport propertyChangeSupport;
	
	public ProjectBrowser(Composite parent, int style) {
		super(parent, style);
		propertyChangeSupport = new PropertyChangeSupport(this);
		populateUI(false);
	}
	
	public ProjectBrowser(Composite parent, int style, boolean showOnlyTextAnalyticsProjects) {
	  super(parent, style);
    propertyChangeSupport = new PropertyChangeSupport(this);
    populateUI(showOnlyTextAnalyticsProjects);
	}

	protected void populateUI(final boolean showOnlyTextAnalyticsProjects) {
		setLayout(new GridLayout(3, false));
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		lProject = new Label(this, SWT.NONE);
		lProject.setText("Project:");
		
		tfProject = new Text(this, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		tfProject.setBackground(new Color(tfProject.getDisplay(), 255, 255, 255));
		tfProject.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		bBrowse = new Button(this, SWT.PUSH);
		bBrowse.setText("Browse");
		
		bBrowse.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new ProjectLabelProvider());
				dialog.setTitle("Project selection");
				dialog.setMessage("Select a project from the list below or enter a pattern");
				IProject[] projects;
				if (showOnlyTextAnalyticsProjects) {
				  projects = ProjectUtils.getTextAnalyticsProjectsList ();
				} else {
				  projects = getOpenProjects();
				}
				if(projects != null){
					int len = projects.length;
					String[] contents = new String[len];
					
					for (int i = 0; i < projects.length; i++) {
						IProject proj = projects[i];
						contents[i] = proj.getName();
					}
					dialog.setElements(contents);
				}
				if (dialog.open() == Window.OK) {
					fireBrowseButtonClicked();
					setProject(dialog.getFirstResult().toString());
				}
			}

			/**
			 * This method is to filter closed projects  
			 * and returns only opened projects in workspace.
			 */
			private IProject[] getOpenProjects ()
			{
			  IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			  List<IProject> openProjects = new ArrayList<IProject>(allProjects.length);
			  for(IProject project : allProjects){
			    if(project.isOpen ()){
			      openProjects.add (project);
			    }
			  }
			  return openProjects.toArray(new IProject[openProjects.size ()]);
			}
		});
	}

	private void fireBrowseButtonClicked() {
		propertyChangeSupport.firePropertyChange("projectRefresh", false, true);
	}

	public void setProject(String project){
		String oldProject = tfProject.getText();
		oldProject = (oldProject == null? "":oldProject);
		if(!oldProject.equals(project)){
			tfProject.setText(project);
			propertyChangeSupport.firePropertyChange("projectName", oldProject, project);
		}
	}

  /**
   * Set the project with the new value. May notify or not depending on the boolean <b>bNotify</b>.
   * In some cases we may want to only change the value, not notify the listeners because that can
   * cause other unnecessary changes.
   * @param project
   * @param bNotify
   */
  public void setProject(String project, boolean bNotify) {
    if (!bNotify)
      tfProject.setText(project);
    else
      setProject(project);
  }

	public String getProject(){
		return tfProject.getText();
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener){
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener){
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	private class ProjectLabelProvider extends LabelProvider{

		@Override
		public Image getImage(Object element) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
		}
		
	}

	public void addModifyListenerForProjectTextField (ModifyListener modifyListener){
	  tfProject.addModifyListener (modifyListener);
	}
}

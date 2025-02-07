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
package com.ibm.biginsights.textanalytics.wizards.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.ProjectBrowser;

/**
 * The first page of New AQL File wizard if nothing has been selected in project explorer
 * 
 *
 */
public class NewAQLWizardProjectSelectionPage extends WizardPage
{



  private static final String PROPERTY_PROJECT_NAME = "projectName"; //$NON-NLS-1$
  private ProjectBrowser browser;
  private String selectedProject=""; //$NON-NLS-1$
  private IProject project=null;
  
  public NewAQLWizardProjectSelectionPage() {
    super("NewAQLFileWizardPage"); //$NON-NLS-1$
    setTitle(Messages.NewAQLWizardProjectSelectionPage_TITLE);
    setDescription(Messages.NewAQLWizardProjectSelectionPage_DESCRIPTION);
  }
  
  @Override
  public void createControl (Composite parent)
  {
    PlatformUI
    .getWorkbench()
    .getHelpSystem()
    .setHelp(parent,
        "com.ibm.biginsights.textanalytics.tooling.help.new_aqlfile");//$NON-NLS-1$
    Composite container = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    
    browser = new ProjectBrowser(container, SWT.NONE, true); //This will make the project browser show only text analytics projects.
    PropertyChangeListener listener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName() != null && PROPERTY_PROJECT_NAME.equals(event.getPropertyName())){
          project = null;
          selectedProject=(String)event.getNewValue ();
          project = ProjectUtils.getProject (selectedProject);
          if (project != null) {
            setPageComplete(true);
          }
        }
      }
    };
    browser.addPropertyChangeListener (listener);
    
    
    setControl(container);
    setPageComplete(false);

  }
  
  /**
   * {@inheritDoc}
   * <p>
   * Overriden method returns configuration page for modular 
   * project if selected project is modular, else the configuration page
   * for non modular project. Returns null if no project is selected (This
   * scenario should not be possible).
   * </p>
   */
  @Override
  public IWizardPage getNextPage ()
  {
    if (project != null) { //project should never be null at this point
      if (ProjectUtils.isModularProject (project)) {
        NewAQLWizardModulePage pg = ((NewAQLWizard)getWizard()).modularPage;
        pg.setProjectSelection (project);
        return pg;
      } else {
        NewAQLWizardPage pg = ((NewAQLWizard)getWizard()).page;
        pg.setProjectSelection (project);
        return pg;
      }
    } else {
      return null;
    }
    
  }
  

}

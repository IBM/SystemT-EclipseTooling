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
package com.ibm.biginsights.textanalytics.export;

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
 * The first page of New AQL Export wizard if nothing has been selected in project explorer
 * 
 * 
 */
public class ExportProjectSelectionPage extends WizardPage
{

  @SuppressWarnings("unused")


  private static final String PROPERTY_PROJECT_NAME = "projectName"; //$NON-NLS-1$
  private ProjectBrowser browser;
  private String selectedProject = "";
  private IProject project = null;
  Composite parent;

  public ExportProjectSelectionPage ()
  {
    super ("ExportProjectSelectionPage"); //$NON-NLS-1$
    setTitle ("Export Project Selection");//$NON-NLS-1$
    setDescription ("Select a project.");//$NON-NLS-1$
  }

  @Override
  public void createControl (Composite parent)
  {
    PlatformUI.getWorkbench ().getHelpSystem ().setHelp (parent,
    "com.ibm.biginsights.textanalytics.tooling.help.export_extractor");//$NON-NLS-1$
    this.parent = parent;
    Composite container = new Composite (parent, SWT.NONE);
    GridLayout layout = new GridLayout ();
    container.setLayout (layout);

    browser = new ProjectBrowser (container, SWT.NONE, true); // This will make the project browser show only text
    // analytics projects.
    PropertyChangeListener listener = new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent event)
      {
        if (event.getPropertyName () != null && PROPERTY_PROJECT_NAME.equals (event.getPropertyName ())) {
          project = null;
          selectedProject = (String) event.getNewValue ();
          project = ProjectUtils.getProject (selectedProject);
          if (project != null) {
            setPageComplete (true);
          }
        }
      }
    };
    browser.addPropertyChangeListener (listener);

    setControl (container);
    setPageComplete (false);

  }

  public IProject getProject ()
  {
    return project;
  }

  @Override
  public IWizardPage getNextPage ()
  {
    if (project != null) {
      ExportAOGWizard wizard = ((ExportAOGWizard) getWizard ());
      ExportAOGWizardPage pg = new ExportAOGWizardPage ("page", project.getName ());
      wizard.page = pg;
      wizard.project = project;
      wizard.addPage (pg);
      return pg;
    }
    else {
      return null;
    }

  }

}

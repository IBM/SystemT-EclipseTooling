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

package com.ibm.biginsights.textanalytics.aqlimport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;

public class ImportExtractorWizardPage extends WizardPage
{

  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+                //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

  private static final String IMPORT_BANNER = "icons/full/etool16/import_AOG_Banner.gif"; //$NON-NLS-1$

  // TODO I18n the labels/texts
  private Label extrZipLabel = null;
  private Text extrZipPath = null;
  private Button extrZipBrowse = null;
  private Label toProjectLabel = null;
  private Text toProjectName = null;

  private List<String> existingProjects = null;


  public ImportExtractorWizardPage (String pageName)
  {
    super (pageName);
  }

  public ImportExtractorWizardPage (String pageName, String title, ImageDescriptor titleImage)
  {
    super (pageName, title, titleImage);
  }

  @Override
  public void createControl (Composite parent)
  {
    setTitle(Messages.getString ("ImportExtractorWizard.TITLE"));     //$NON-NLS-1$

    setDescription(Messages.getString("ImportExtractorWizard.DESC")); //$NON-NLS-1$
    setImageDescriptor(Activator.getImageDescriptor(IMPORT_BANNER));
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.import_extractor");   //$NON-NLS-1$

    Composite panel = new Composite(parent, SWT.FILL );
    setControl(panel);
    GridLayout layout = new GridLayout(3, false);
    layout.verticalSpacing = 16;
    layout.marginRight = 4;
    panel.setLayout(layout);
  
    createZipBrowseArea (panel);
    createToProjectArea (panel);
    addListeners ();
    getExistingProjects ();
    setPageComplete (false);
  }

  private void createZipBrowseArea (Composite parent)
  {
    extrZipLabel = new Label (parent, SWT.NONE);
    extrZipLabel.setText (Messages.getString("ImportExtractorWizard.EXTRACTOR"));
    GridData extrZipLabelGd = new GridData(GridData.BEGINNING);
    extrZipLabel.setLayoutData (extrZipLabelGd);

    extrZipPath = new Text (parent, SWT.SINGLE | SWT.BORDER);
    GridData extrZipPathGd = new GridData(GridData.FILL_HORIZONTAL);
    extrZipPathGd.grabExcessHorizontalSpace = true;
    extrZipPath.setLayoutData (extrZipPathGd);
    extrZipPath.setText ("");
    Color bgColor = extrZipPath.getBackground ();
    extrZipPath.setEditable (false);
    extrZipPath.setBackground (bgColor);

    extrZipBrowse = new Button (parent, SWT.NONE);
    GridData extrZipBrowseGd = new GridData(GridData.END);
    extrZipBrowseGd.widthHint = 122;
    extrZipBrowse.setLayoutData (extrZipBrowseGd);
    extrZipBrowse.setText (Messages.getString("General.Browse"));
  }

  private void createToProjectArea (Composite parent)
  {
    // The label at the left
    toProjectLabel = new Label (parent, SWT.NONE);
    GridData toProjectLabelGd = new GridData(GridData.BEGINNING);
    toProjectLabel.setLayoutData (toProjectLabelGd);
    toProjectLabel.setText (Messages.getString("ImportExtractorWizard.TOPROJECT"));   //$NON-NLS-1$

    toProjectName = new Text (parent, SWT.SINGLE | SWT.BORDER);
    GridData toProjectPathGd = new GridData(GridData.FILL_HORIZONTAL);
    toProjectPathGd.grabExcessHorizontalSpace = true;
    toProjectName.setLayoutData (toProjectPathGd);
  }

  private void addListeners ()
  {
    addListenerToExtrBrowse ();
    addListenerToProjectField ();
  }

  private void addListenerToExtrBrowse ()
  {
    ModifyListener modifyListener = new ModifyListener() {
      @Override
      public void modifyText (ModifyEvent e)
      {
        if (extrZipPath.getText ().isEmpty () )
          setPageComplete (false);

        else
          setPageComplete (validatePageContent ());
      }
    };

    extrZipPath.addModifyListener (modifyListener);

    extrZipBrowse.addSelectionListener (new SelectionListener() {
      
      @Override
      public void widgetSelected (SelectionEvent arg0)
      {
        FileDialog fileSelectionDialog = new FileDialog (Display.getCurrent ().getActiveShell (), SWT.OPEN);
        fileSelectionDialog.setFilterExtensions (new String[] { "*.zip" });
        String filePath = fileSelectionDialog.open ();
        if (filePath != null)
          extrZipPath.setText (filePath);
      }
      
      @Override
      public void widgetDefaultSelected (SelectionEvent arg0)
      {
      }
    });

  }

  private void addListenerToProjectField ()
  {
    toProjectName.addModifyListener (new ModifyListener() {
      @Override
      public void modifyText (ModifyEvent e)
      {
        if (existingProjects.contains (getProjectName ())) {
          setErrorMessage (Messages.getString ("ImportExtractorWizard.PROJECT_EXISTS"));    //$NON-NLS-1$
          setPageComplete (false);
        }
        else
          setPageComplete (validatePageContent ());
      }
    });
  }

  private boolean validatePageContent ()
  {
    setErrorMessage (null);

    // Validate extractor zip field not empty
    if (extrZipBrowse.getText ().isEmpty ()) {
      setErrorMessage (Messages.getString ("ImportExtractorWizard.NO_PROJECT"));    //$NON-NLS-1$
      return false;
    }

    // Validate project field not empty.
    if (getProjectName ().isEmpty ()) {
      setErrorMessage (Messages.getString ("ImportExtractorWizard.NO_PROJECT"));    //$NON-NLS-1$
      return false;
    }

    // Validate project field not containing an existing project.
    if (existingProjects.contains (getProjectName ())) {
      setErrorMessage (Messages.getString ("ImportExtractorWizard.PROJECT_EXISTS"));    //$NON-NLS-1$
      return false;
    }

    return true;
  }

  private void getExistingProjects ()
  {
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

    existingProjects = new ArrayList<String> ();
    for (IProject p : projects) {
      existingProjects.add (p.getName ());
    }
  }

  public String getProjectName ()
  {
    return toProjectName.getText ();
  }

  public String getExtrZipFilePath ()
  {
    return extrZipPath.getText ();
  }
}

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
package com.ibm.biginsights.textanalytics.workflow.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.IDE;

import com.ibm.biginsights.project.ProjectNature;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;

public class ImportJsonRecordsWizardPage1 extends WizardPage
{


 
	Text jsonFile;
  Text tgtFolder;
  Button openAEnow;

  protected ImportJsonRecordsWizardPage1 (String pageName)
  {
    super (pageName);
    setTitle(Messages.import_json_results);
    setDescription(Messages.import_json_results_desc);
  }

  @Override
  public void createControl (Composite parent)
  {
    final Color _white = getShell ().getDisplay ().getSystemColor(SWT.COLOR_WHITE);
    final Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout(3, false));

    // First row -- result file location
    Label lblJsonResult = new Label (composite, SWT.NONE);
    lblJsonResult.setText (Messages.import_json_output_json);
    jsonFile = new Text (composite, SWT.BORDER);
    jsonFile.setEditable (false);
    jsonFile.setBackground (_white);
    jsonFile.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    Button browseFileSystem = new Button (composite, SWT.NONE);
    browseFileSystem.setText (Messages.import_json_browse_fs);
    browseFileSystem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        // Open file selection dialog
        FileDialog fileDialog = new FileDialog (getShell(), SWT.OPEN);
        fileDialog.setFilterNames (new String [] { Messages.import_json_output_json });
        fileDialog.setFilterExtensions (new String [] {"*.json;*.json.gz"});  //$NON-NLS-1$
        fileDialog.setFilterPath ("/");  //$NON-NLS-1$

        String fileAbsPath = fileDialog.open ();
        if (fileAbsPath != null) {
          jsonFile.setText (fileAbsPath);
          ((ImportJsonRecordsWizard)getWizard ()).setImportFilePath (fileAbsPath);
          setPageComplete();
        } 
      }
    });

    // Second row -- target folder where the result is imported to
    Label lbltargetFolder = new Label (composite, SWT.NONE);
    lbltargetFolder.setText (Messages.import_json_target_project);
    tgtFolder = new Text (composite, SWT.BORDER);
    tgtFolder.setEditable (false);
    tgtFolder.setBackground (_white);
    tgtFolder.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    Button browseWorkspace = new Button (composite, SWT.NONE);
    browseWorkspace.setText (Messages.import_json_browse_ws);
    browseWorkspace.addSelectionListener(new SelectionAdapter(){
      @Override
      public void widgetSelected (SelectionEvent e)
      {
        // Open project list dialog
        ElementListSelectionDialog dirDialog = new ElementListSelectionDialog (getShell (), new LabelProvider () {
          @Override
          public Image getImage (Object element)
          {
            return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
          }
        });

        dirDialog.setMultipleSelection (false);
        dirDialog.setTitle (Messages.import_json_select_project);
        dirDialog.setMessage (Messages.import_json_select_project_desc);
        IProject[] projects = ResourcesPlugin.getWorkspace ().getRoot ().getProjects ();

        if (projects != null) {
          List<String> biProjects = new ArrayList<String> ();

          for (int i = 0; i < projects.length; i++) {
            IProject proj = projects[i];
            try {
              // Only show BI projects
              if (proj.hasNature(ProjectNature.NATURE_ID))
                biProjects.add (proj.getName ());
            }
            catch (CoreException e1) {
              // Just don't add the project with problem to the list
            }
          }

          dirDialog.setElements (biProjects.toArray ());
        }

        if (dirDialog.open () == Window.OK &&
            dirDialog.getFirstResult () != null) {
          String selProject = dirDialog.getFirstResult ().toString ();
          tgtFolder.setText (selProject);
          ((ImportJsonRecordsWizard)getWizard ()).setImportTargetProject (selProject);
          setPageComplete();
        }
      }
    });

    // third row -- is result opened after import?
    Composite openImmed = new Composite (composite, SWT.NONE);
    GridData gd = new GridData (GridData.VERTICAL_ALIGN_BEGINNING);
    gd.horizontalSpan = 3;
    gd.verticalIndent = 20;
    openImmed.setLayoutData (gd);
    openImmed.setLayout(new GridLayout(2, false));

    openAEnow = new Button (openImmed, SWT.CHECK);
    Label lblOpenAEnow = new Label (openImmed, SWT.NONE);
    lblOpenAEnow.setText (Messages.import_json_open_immediate);
    openAEnow.addSelectionListener(new SelectionAdapter(){
      @Override
      public void widgetSelected (SelectionEvent e)
      {
        ((ImportJsonRecordsWizard)getWizard ()).setLoadImmediate (openAEnow.getSelection ());
      }
    });

    setControl (composite);
  }

  @Override
  public boolean isPageComplete ()
  {
    return !StringUtils.isEmpty (jsonFile.getText ()) && !StringUtils.isEmpty (tgtFolder.getText ());
  }

  private void setPageComplete()
  {
    setPageComplete (isPageComplete());
  }
  
}

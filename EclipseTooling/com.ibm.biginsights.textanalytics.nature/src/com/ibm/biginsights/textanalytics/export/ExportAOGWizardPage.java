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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class ExportAOGWizardPage extends WizardPage
{


 
	protected FileDirectoryPicker inputCollectionPicker;

  private static final String AOG_EXPORT_BANNER = "icons/full/etool16/export_AOG_Banner.gif"; //$NON-NLS-1$
  String projectName;
  String exportPath;
  String[] modules;
  FileDirectoryPicker aogPathPicker;

  protected Composite modulePanel;
  protected Table moduleTable;
  protected Composite secondPanel;
  protected Color white;
  boolean pageComplete;

  public ExportAOGWizardPage (String pageName, String projectName)
  {
    super (pageName);
    setTitle (Messages.getString ("ExportAOGWizardPage.AOG_EXPORT_SPECIFICATION")); //$NON-NLS-1$
    setImageDescriptor (getImageDescriptor (AOG_EXPORT_BANNER));
    this.projectName = projectName;
  }

  @Override
  public void createControl (Composite parent)
  {

    PlatformUI.getWorkbench ().getHelpSystem ().setHelp (parent,
    "com.ibm.biginsights.textanalytics.tooling.help.export_extractor");//$NON-NLS-1$

    Composite composite = new Composite (parent, SWT.NONE);
    composite.setLayout (new GridLayout ());

    createModulePanel (composite);
    populateModules (projectName, composite);

    // AOG path
    int aogPathWorkspaceOrExternal = FileDirectoryPicker.WORKSPACE_OR_EXTERNAL;
    aogPathPicker = new FileDirectoryPicker (composite, Constants.DIRECTORY_OR_PROJECT, aogPathWorkspaceOrExternal);
    aogPathPicker.setTitle(Messages.getString("ExportAOGWizardPage.DirectorySelection")); //$NON-NLS-1$
    aogPathPicker.setMessage (Messages.getString("GeneralPrefPage.AOG_LOCATION")); //$NON-NLS-1$
    aogPathPicker.setDescriptionLabelText (Messages.getString ("GeneralPrefPage.AOG_LOCATION")); //$NON-NLS-1$
    aogPathPicker.setAllowMultipleSelection (false);
    aogPathPicker.excludeFolders (".aog",".provenanceRewrite",".settings",".patterndiscovery"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    aogPathPicker.addModifyListenerForFileDirTextField (new ModifyListener () {
      @Override
      public void modifyText (ModifyEvent event)
      {
        pageComplete = isDataValid ();
      }
    });

    createOptionPannel (composite);
    setControl (composite);
    
    setPageComplete (pageComplete);

  }

  private ImageDescriptor getImageDescriptor (String filepath)
  {
    Bundle bundle = com.ibm.biginsights.textanalytics.nature.Activator.getDefault ().getBundle ();
    URL url = FileLocator.find (bundle, new Path (filepath), null);
    ImageDescriptor descriptor = ImageDescriptor.createFromURL (url);
    return descriptor;
  }

  private void createModulePanel (Composite composite)
  {

    secondPanel = new Composite (composite, SWT.NONE);
    GridLayout secondlayout = new GridLayout (2, false);
    secondPanel.setLayout (secondlayout);
    Label lbLanguage = new Label (secondPanel, SWT.NONE);
    lbLanguage.setText (Messages.getString ("ExportAOGWizardPage.EXTRACT_MODULES")); //$NON-NLS-1$

    final Composite moduleComposite = new Composite (secondPanel, SWT.NONE);
    GridLayout layout = new GridLayout (2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    moduleComposite.setLayout (layout);
    moduleComposite.setLayoutData (new GridData (GridData.FILL_BOTH));
    
    
    modulePanel = new Composite (moduleComposite, SWT.NONE);
    moduleTable = new Table (modulePanel, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    GridLayout moduleLayout = new GridLayout (1, false);
    moduleLayout.marginHeight = 10;
    moduleLayout.marginWidth = 10;
    modulePanel.setLayout (moduleLayout);
    
    Accessible lbLanguageLabel = lbLanguage.getAccessible();
    Accessible moduleTableAcc = moduleTable.getAccessible();
    lbLanguageLabel.addRelation(ACC.RELATION_LABEL_FOR, moduleTableAcc);
    moduleTableAcc.addRelation(ACC.RELATION_LABELLED_BY, lbLanguageLabel);

    GridData gdata = new GridData(GridData.FILL_BOTH);
    //gd.grabExcessHorizontalSpace = true;
    //gd.grabExcessVerticalSpace = true;
    gdata.widthHint = 250;
    gdata.heightHint = 100;
    moduleTable.setLayoutData(gdata);
    modulePanel.layout ();
    
    Composite secinnPanel = new Composite (moduleComposite, SWT.NONE);
    GridLayout secinnlayout = new GridLayout ();
    secinnlayout.marginHeight = 10;
    secinnlayout.marginWidth = 10;
    secinnPanel.setLayout (secinnlayout);
    secinnPanel.setLayoutData (new GridData (GridData.FILL_VERTICAL));
    Button selectAll = new Button (secinnPanel, SWT.PUSH);
    selectAll.setSize (160, 130);
    selectAll.setText (Messages.getString ("WizardPage.SELECT_ALL"));//$NON-NLS-1$
    selectAll.addSelectionListener (new SelectionAdapter () {
      public void widgetSelected (SelectionEvent e)
      {
        Control ctrl[] = modulePanel.getChildren ();
        TableItem[] tItems = ((Table) ctrl[0]).getItems ();
        for (TableItem tItem : tItems) {
          tItem.setChecked (true);
        }
        pageComplete = isDataValid ();
        setPageComplete (pageComplete);
      }
    });
    Button clear = new Button (secinnPanel, SWT.PUSH);
    clear.setSize (150, 90);
    clear.setText ("   Clear    ");//$NON-NLS-1$
    clear.addSelectionListener (new SelectionAdapter () {
      public void widgetSelected (SelectionEvent e)
      {
        Control ctrl[] = modulePanel.getChildren ();
        TableItem[] tItems = ((Table) ctrl[0]).getItems ();
        for (TableItem tItem : tItems) {
          tItem.setChecked (false);
        }
        pageComplete = isDataValid ();
        setPageComplete (pageComplete);
      }
    });
    // Controls for Dependent Modules
    isExportDependentModules = false;
    exportDependentModules = new Button (secondPanel, SWT.CHECK);
    exportDependentModules.setText ("Export dependent modules"); //$NON-NLS-1$
    exportDependentModules.setEnabled (true);
    GridData gd = new GridData (SWT.FILL, SWT.LEFT, true, true, 2, 2);
    exportDependentModules.setLayoutData (gd);
    exportDependentModules.addSelectionListener (new SelectionAdapter () {
      public void widgetSelected (SelectionEvent e)
      {
        if (exportDependentModules.getSelection ()) {
          pageComplete = isDataValid ();
          isExportDependentModules = true;
          setPageComplete (pageComplete);
        }
        else {
          pageComplete = isDataValid ();
          isExportDependentModules = false;
          setPageComplete (pageComplete);
        }
      }
    });

  }

  protected Button exportDependentModules;
  protected Button exportToDirectory;
  protected Button exportToZipOrJar;
  protected boolean isExportDependentModules;
  protected boolean isExportToZipOrJar;
  protected Text fileNameText;
  protected Label fileNameLabel;

  private void createOptionPannel (Composite expoComposite)
  {
    Group paginationPanel = new Group (expoComposite, SWT.NONE);
    paginationPanel.setText ("Options"); //$NON-NLS-1$
    GridLayout layout = new GridLayout (2, false);
    layout.marginHeight = 10;
    layout.marginWidth = 10;
    paginationPanel.setLayout (layout);
    paginationPanel.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    exportToDirectory = new Button (paginationPanel, SWT.RADIO);
    exportToDirectory.setText ("Export to destination directory"); //$NON-NLS-1$
    exportToDirectory.setEnabled (true);
    exportToDirectory.setSelection (true);

    exportToZipOrJar = new Button (paginationPanel, SWT.RADIO);
    exportToZipOrJar.setText ("Export to a jar or zip archive under the destination directory"); //$NON-NLS-1$
    exportToZipOrJar.setEnabled (true);
    GridData gd = new GridData (SWT.FILL, SWT.LEFT, true, true, 2, 2);
    exportToZipOrJar.setLayoutData (gd);
    exportToZipOrJar.addSelectionListener (new SelectionAdapter () {
      public void widgetSelected (SelectionEvent e)
      {
        if (exportToZipOrJar.getSelection ()) {
          fileNameLabel.setEnabled (true);
          fileNameText.setEnabled (true);
          pageComplete = isDataValid ();
          isExportToZipOrJar = true;
          setPageComplete (pageComplete);

        }
        else {
          fileNameLabel.setEnabled (false);
          fileNameText.setEnabled (false);
          pageComplete = isDataValid ();
          isExportToZipOrJar = false;
          setPageComplete (pageComplete);
        }
      }
    });

    GridData gd1 = new GridData (SWT.FILL, SWT.LEFT, false, false, 2, 1);

    fileNameLabel = new Label (paginationPanel, SWT.None);
    fileNameLabel.setText (Messages.getString ("ExportAOGWizardPage.FILE_NAME")); //$NON-NLS-1$
    fileNameLabel.setToolTipText (""); //$NON-NLS-1$
    fileNameLabel.setEnabled (false);
    fileNameLabel.setLayoutData (gd1);

    fileNameText = new Text (paginationPanel, SWT.BORDER | SWT.SINGLE);
    fileNameText.setLayoutData (new GridData (670, 20));
    fileNameText.setEnabled (false);
    fileNameText.setSize (600, 20);
    fileNameText.addModifyListener (new InputListener ());

  }

  private void populateModules (String projectName, Composite composite)
  {
     if (projectName == null || projectName.isEmpty ()) {
      if (modulePanel == null || modulePanel.isDisposed ()) {
        createModulePanel (composite);
        return;
      }

      if (modulePanel != null || !modulePanel.isDisposed ()) {
        Control ctrl[] = modulePanel.getChildren ();
        TableItem[] tItems = ((Table) ctrl[0]).getItems ();
        for (TableItem tItem : tItems) {
          tItem.dispose ();
        }
        modulePanel.redraw ();
        modulePanel.pack ();
      }

      return;
    }

    IProject project = ProjectUtils.getProject (projectName);
    boolean isModular = ProjectUtils.isModularProject (project);
    if (!isModular) {
      if (secondPanel != null && !secondPanel.isDisposed ()) {
        Composite com = secondPanel.getParent ();
        modulePanel.dispose ();
        secondPanel.dispose ();
        com.pack (true);
      }
    }
    else {

      if (secondPanel == null || secondPanel.isDisposed ()) {
        modulePanel.dispose ();
        createModulePanel (composite);
      }

    }
    String modules[] = null;
    if (isModular) {
      modules = ProjectUtils.getModules (project);
    }

    if (modules == null && !modulePanel.isDisposed ()) {
      setErrorMessage (Messages.getString ("ExportAOGWizardPage.ERR_MSG5"));//$NON-NLS-1$
    }
    
    if (modules != null && !modulePanel.isDisposed ()) {
      Control ctrl[] = modulePanel.getChildren ();
      TableItem[] tItems = ((Table) ctrl[0]).getItems ();
      for (TableItem tItem : tItems) {
        tItem.dispose ();
      }
      for (String module : modules) {

        TableItem item = new TableItem (moduleTable, SWT.NONE);
        item.setText (module);

      }
      moduleTable.addSelectionListener (new SelectionListener (){
        @Override
        public void widgetDefaultSelected (SelectionEvent arg0)
        {
          pageComplete = isDataValid ();
          setPageComplete (pageComplete);
          
        }

        @Override
        public void widgetSelected (SelectionEvent arg0)
        {
          pageComplete = isDataValid ();
          setPageComplete (pageComplete);
          
        }
      });
      modulePanel.redraw ();
      secondPanel.redraw ();
    }

    if (secondPanel != null && !secondPanel.isDisposed ()) secondPanel.redraw ();

    composite.redraw ();
    

  }

  public boolean isDataValid ()
  {
    setPageComplete (false);
    if (ProjectUtils.isModularProject (projectName)) {
      if (modulePanel != null && !modulePanel.isDisposed ()) {
        Control ctrl[] = modulePanel.getChildren ();
        boolean containsVal = false;
        List<String> moduleList = new ArrayList<String> ();
        for (Control control : ctrl) {
          if (control instanceof Table) {
            Table modTable = (Table) control;
            if (modTable.getItemCount () >= 1) {
              containsVal = true;
            }
            TableItem[] tabItems = modTable.getItems ();
            for (TableItem tabItem : tabItems ) {
              
              if (tabItem.getChecked ()) {
                moduleList.add (tabItem.getText ());
              }
            }
          }
        } 
        if (!containsVal) {
          setErrorMessage (Messages.getString ("ExportAOGWizardPage.ERR_MSG5"));//$NON-NLS-1$
          return false;
        }
        if (moduleList.isEmpty ()) {
          updateStatus (Messages.getString ("ExportAOGWizardPage.ERR_MSG1"));//$NON-NLS-1$
          return false;
        }
        else
          modules = moduleList.toArray (new String[0]);

      }
      else {
        updateStatus (Messages.getString ("ExportAOGWizardPage.ERR_MSG1"));//$NON-NLS-1$
        return false;
      }   
    
    }
    else {
      modules = new String[] { "genericModule" };//$NON-NLS-1$
    }

    exportPath = aogPathPicker.getFileDirValue ();
    if (exportPath.isEmpty ()) {
      updateStatus (Messages.getString ("ExportAOGWizardPage.ERR_MSG4"));//$NON-NLS-1$
      return false;
    }

    if (exportPath.startsWith (Constants.WORKSPACE_RESOURCE_PREFIX)) {
      exportPath = ProjectPreferencesUtil.getAbsolutePath (exportPath);
    }
    
    if (exportToZipOrJar != null && !exportToZipOrJar.isDisposed() && 
    		exportToZipOrJar.getSelection ()) {
      String file = fileNameText.getText ();

      if (file == null || file.isEmpty ()) {
        updateStatus (Messages.getString ("ExportAOGWizardPage.ERR_MSG2"));//$NON-NLS-1$
        return false;
      }

      if (!(file.endsWith (".zip") || file.endsWith (".jar"))) { //$NON-NLS-1$ //$NON-NLS-2$
        updateStatus (Messages.getString ("ExportAOGWizardPage.ERR_MSG3"));//$NON-NLS-1$
        return false;
      }

    }
    setPageComplete (true);
    updateStatus (null);
    return true;
  }

  private void updateStatus (String message)
  {
    setErrorMessage (message);
    setPageComplete (message == null);
  }

  public String getExportPath ()
  {
    return exportPath;
  }

  public void setExportPath (String exportPath)
  {
    this.exportPath = exportPath;
  }

  public String[] getModules ()
  {
    return modules;
  }

  public void setModules (String[] modules)
  {
    this.modules = modules;
  }

  public String getArchiveFileName ()
  {
    return fileNameText.getText ();
  }

  public boolean isExportDependentModules ()
  {
    return isExportDependentModules;
  }

  public boolean isExportToZipOrJar ()
  {
    return isExportToZipOrJar;
  }

  private class InputListener implements ModifyListener
  {

    @Override
    public void modifyText (ModifyEvent event)
    {
      pageComplete = isDataValid ();
      setPageComplete (pageComplete);
    }

  }
}

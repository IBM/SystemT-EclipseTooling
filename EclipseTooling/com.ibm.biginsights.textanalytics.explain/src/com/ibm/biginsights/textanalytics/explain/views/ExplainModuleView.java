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
package com.ibm.biginsights.textanalytics.explain.views;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;

import com.ibm.avatar.algebra.util.string.StringUtils;
import com.ibm.biginsights.textanalytics.explain.Messages;
import com.ibm.biginsights.textanalytics.explain.model.ExplainFolder;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class ExplainModuleView extends ViewPart {

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+          //$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	public static IFile tamFile = null;
	public static final String viewID = "com.ibm.biginsights.textanalytics.explain.views.ExplainModuleView";

	private FileDirectoryPicker tamPathPicker;
	private Composite emvComposite = null;   // Explain Module View Composite
	private FilteredTree moduleTree;
  private TreeViewer moduleTreeViewer;
  private Text planAogText = null;


  @Override
  public void createPartControl(Composite parent) {

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.explain_module_view");  //$NON-NLS-1$
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout());

    tamPathPicker = new FileDirectoryPicker(composite,
      Constants.FILE_ONLY, FileDirectoryPicker.WORKSPACE_OR_EXTERNAL);
    tamPathPicker.setDescriptionLabelText(Messages.ExplainModuleView_FILE_PICKER_DESCRIPTION);
    tamPathPicker.setAllowMultipleSelection(false);
    tamPathPicker.setAllowedFileExtensions(Constants.TAM_FILE_EXTENSTION_STRING);

    SashForm emSashForm = new SashForm (composite, SWT.VERTICAL);
    GridData emvSashGd = new GridData(SWT.FILL, SWT.FILL, true, true);
    emSashForm.setLayoutData(emvSashGd);

    emvComposite = new Composite(emSashForm, SWT.BORDER);
    emvComposite.setLayout(new GridLayout(1, true));
    GridData emvGd = new GridData(SWT.FILL, SWT.FILL, true, true);
    emvComposite.setLayoutData(emvGd);

    PatternFilter filter = new PatternFilter() {
      @Override
      protected boolean isLeafMatch (Viewer viewer, Object element)
      {
        if (element instanceof ExplainFolder)
          return false;
        else
          return super.isLeafMatch (viewer, element);
      }
    };

    moduleTree = new FilteredTree(emvComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
    moduleTreeViewer = moduleTree.getViewer();
    moduleTreeViewer.setContentProvider(new ExplainModuleContentProvider());
    moduleTreeViewer.setLabelProvider(new ExplainModuleLabelProvider());
    moduleTreeViewer.setAutoExpandLevel (3);
    ColumnViewerToolTipSupport.enableFor(moduleTreeViewer);

    Group planAogGroup = new Group (emSashForm, SWT.BORDER_SOLID);
    GridData gGd = new GridData (SWT.FILL, SWT.FILL, true, true);
    planAogGroup.setLayoutData (gGd);
    planAogGroup.setText (Messages.ExplainModuleView_PLANAOG);

    planAogGroup.setLayout (new GridLayout (1, true));
    planAogText = new Text (planAogGroup, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
    GridData tgd = new GridData (SWT.FILL, SWT.FILL, true, true);
    planAogText.setLayoutData (tgd);
    planAogText.setBackground (Display.getDefault ().getSystemColor (SWT.COLOR_WHITE));

    emSashForm.setWeights(new int[] {50,50});

    tamPathPicker.addModifyListenerForFileDirTextField(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent event) {
        if (moduleTree.getFilterControl ().getText ().length () > 0) {
          moduleTree.getFilterControl ().setText ("");
          moduleTreeViewer.refresh ();  // We need this refresh() command if we want auto expand all levels; otherwise the
                                        // setText() command above will take effect on the new tree and makes it collapse. 
        }
        String selectedFilePath = tamPathPicker.getFileDirValue ();
        moduleTreeViewer.setInput (selectedFilePath);
        planAogText.setText (getPlanAOG (ProjectPreferencesUtil.getAbsolutePath (selectedFilePath)));
      }
    });

    // If tam file was set in advance, open it.
    if (tamFile != null) {
      String tamPath = tamFile.getFullPath ().toString ();
      tamPathPicker.setFileDirValue (tamPath, true);
    }
  }

  @Override
  public void dispose ()
  {
    tamFile = null;
    super.dispose ();
  }

  @Override
  public void setFocus() {
    tamPathPicker.setFocus();
  }

  public void setModuleToExplain (String modulePathname) {
    boolean isWorkspaceRes = ProjectUtils.isWorkspaceResource (modulePathname);
    tamPathPicker.setFileDirValue (modulePathname, isWorkspaceRes);
  }

  public void setModuleToExplain (String modulePathname, boolean isWorkspaceResource) {
    tamPathPicker.setFileDirValue (modulePathname, isWorkspaceResource);
  }

  private String getPlanAOG (String pathname)
  {
    StringBuilder sb = new StringBuilder ();

    try {
      ZipFile tam = new ZipFile (pathname);
      ZipEntry planAogEntry = tam.getEntry("plan.aog");     //$NON-NLS-1$
      InputStream is = tam.getInputStream (planAogEntry);
      BufferedReader reader = new BufferedReader(new InputStreamReader (is));

      String line = null;
      while((line = reader.readLine()) != null) {
        sb.append(line);
        sb.append (System.getProperty ("line.separator"));  //$NON-NLS-1$
      }
    }
    catch (Throwable e) {
      if (StringUtils.isNullOrWhiteSpace (tamPathPicker.getFileDirValue ()))
        return "";
      else
        return MessageFormat.format (Messages.ExplainModuleView_ERROR_READ_TAM, new Object[] { pathname });
    }

    return sb.toString ();
  }
}

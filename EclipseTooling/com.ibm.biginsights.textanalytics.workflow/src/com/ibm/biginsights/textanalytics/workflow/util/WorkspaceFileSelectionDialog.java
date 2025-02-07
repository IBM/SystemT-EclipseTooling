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
package com.ibm.biginsights.textanalytics.workflow.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;
import com.ibm.biginsights.textanalytics.util.Messages;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public class WorkspaceFileSelectionDialog extends Dialog
{


 
	private String filePath = null;
  private FileDirectoryPicker fileBrowser;
  private boolean enableCreateNewFileOption = false;
  private String  createNewFileLabel = "";
  private String  newFileBaseName = "";
  private String  newFileDefaultExtension = "";
  private String  description = "";

  public WorkspaceFileSelectionDialog (Shell parentShell)
  {
    super (parentShell);
  }

  protected Control createDialogArea (Composite parent)
  {
    Composite topComposite = (Composite) super.createDialogArea (parent);

    fileBrowser = new FileDirectoryPicker (topComposite, Constants.FILE_ONLY, FileDirectoryPicker.WORKSPACE_ONLY);
    fileBrowser.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));

    fileBrowser.setDescriptionLabelText (Messages.getString("FileDirectoryPicker.SELECT_FILE"));
    fileBrowser.setAllowMultipleSelection(false);
    fileBrowser.setEnableShowAllFilesOption(true);      // to display the Show all files Option.
    fileBrowser.setCreateNewFileParameters (enableCreateNewFileOption,       // to display Create New File button
                                            createNewFileLabel,
                                            newFileBaseName,
                                            newFileDefaultExtension);
    fileBrowser.setEditable (false);

    fileBrowser.addModifyListenerForFileDirTextField (new ModifyListener () {
      @Override
      public void modifyText (ModifyEvent e)
      {
          filePath = fileBrowser.getFileDirValue ();
      }
    });

    return topComposite;
  }

  public String getFilePath ()
  {
    return filePath;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets .Shell)
   */
  protected void configureShell (Shell shell)
  {
    super.configureShell (shell);
    if (shell != null) {
      shell.setText (description);
    }
  }

  public void setCreateNewFileParameters (boolean enableCreateNewFileOption, String createNewFileLabel,
                                          String newFileBaseName, String newFileDefaultExtension,
                                          String description)
  {
    this.enableCreateNewFileOption = enableCreateNewFileOption;
    this.createNewFileLabel = createNewFileLabel;
    this.newFileBaseName = newFileBaseName;
    this.newFileDefaultExtension = newFileDefaultExtension;
    this.description = description;;
  }

  public IResource getSelectedResource ()
  {
    if (fileBrowser != null)
      return fileBrowser.getSelectedResource ();

    return null;
  }

}

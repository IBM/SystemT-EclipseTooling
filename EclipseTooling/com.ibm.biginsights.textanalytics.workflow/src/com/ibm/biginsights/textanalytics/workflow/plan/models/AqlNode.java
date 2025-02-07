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
package com.ibm.biginsights.textanalytics.workflow.plan.models;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLEditorUtils;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.FilteredFileDirectoryDialog;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.AQLNodeModel;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlGroupType;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlTypes;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;
import com.ibm.biginsights.textanalytics.workflow.util.Templates;

/**
 * this class represent an aql node in the Actions Plan three
 * 
 * 
 */
public class AqlNode extends TreeObject
{


  
	private static final int BASIC_INT = 0;
  private static final int CONCEPT_INT = 1;
  private static final int REFINEMENT_INT = 2;
  private static final int FINALS_INT = 3;

  protected AQLNodeModel aqlNodeModel;

  //  protected String viewName;
  //  private String comment;
  private AqlGroupType aqlGroup;

  public AqlNode (String label)
  {
    this (label, "");
  }

  public AqlNode (String label, String comment)
  {
    super (label, Icons.AQL_ICON);
    this.aqlNodeModel = new AQLNodeModel (label, comment);
  }

  public AqlNode (AQLNodeModel model, AqlGroupType aqlGroupType)
  {
    super (model.getViewname (), Icons.AQL_ICON);
    this.aqlNodeModel = model;
    setAqlGroup (aqlGroupType);
  }

  public AqlNode (AQLNodeModel model, AqlFolderNode aqlFolder)
  {
    super (model.getViewname (), Icons.AQL_ICON);
    this.aqlNodeModel = model;
    this.parent = aqlFolder;
    setAqlGroup (aqlFolder.getAqlType ());
  }

  public AqlNode (AQLNodeModel model, AqlGroup aqlGroup)
  {
    super (model.getViewname (), Icons.AQL_ICON);
    this.aqlNodeModel = model;
    this.parent = aqlGroup;
    setAqlGroup (aqlGroup.getAqlType ());
  }

  public AQLNodeModel toModel ()
  {
    return aqlNodeModel;
  }

  public AQLNodeModel getAQLNodeModel ()
  {
    return aqlNodeModel;
  }

  public void setAQLNodeModel (AQLNodeModel model)
  {
    this.aqlNodeModel = model;
  }

  public boolean insertTemplate (boolean doOutput, AqlTypes aqlType, IFile aqlFile, boolean doExport)
  {
    try {
      if (aqlFile == null)
        aqlFile = getFile ();

      if (aqlFile == null || !aqlFile.exists ()) {
        Shell shell = AqlProjectUtils.getActiveShell ();

        aqlFile = requestPath (shell);
        if (aqlFile == null)
          return false;

        ActionPlanView plan = AqlProjectUtils.getActionPlanView ();
        if (plan != null && plan.ready ())
          plan.serialize ();
      }

      AQLEditor editor = (AQLEditor) AqlProjectUtils.openFile (aqlFile, AQLEditor.EDITOR_ID);
      editor.insertText (Templates.getTemplateFromAqlType (aqlType, getViewName(), doOutput, doExport));
      editor.doSave (new NullProgressMonitor ());
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  private IFile requestPath (Shell shell)
  {
    FilteredFileDirectoryDialog dialog = new FilteredFileDirectoryDialog (shell, new WorkbenchLabelProvider (),
      new WorkbenchContentProvider (), Constants.FILE_ONLY);
    dialog.setCreateNewFileParameters (true,
            com.ibm.biginsights.textanalytics.util.Messages.getString("FileDirectoryPicker.CREATE_NEW_AQL_FILE"),
            com.ibm.biginsights.textanalytics.util.Messages.getString("FileDirectoryPicker.NEW_AQL_FILE_BASE_NAME"),
            com.ibm.biginsights.textanalytics.util.Messages.getString("FileDirectoryPicker.NEW_AQL_FILE_DEFAULT_EXTENSION"));

    dialog.setTitle (com.ibm.biginsights.textanalytics.util.Messages.getString("FileDirectoryPicker.SELECT_AQL_FILE"));

    dialog.addFilter(new ViewerFilter() {
      public boolean select(Viewer viewer, Object parentElement, Object element) {

        if (element instanceof IProject) {
          String projectName = ((IProject) element).getName();
          String srcProjectName = ActionPlanView.projectName;
          if (projectName.equals(srcProjectName)) {
            return true;
          } else
            return false;

        }
        return true;
      }
    });

    IResource iResource = dialog.getSelectedResource ();
    if (iResource != null && iResource instanceof IFile)
      return (IFile) iResource;
    else
      return null;
  }

  public IFile getFile ()
  {
    String path = aqlNodeModel.getAqlfilepath ();

    if (StringUtils.isEmpty (path))
      return null;
    else {
      IPath _path = (IPath) new Path (ProjectPreferencesUtil.getPath (path));
      return AqlProjectUtils.getWorkspaceRoot ().getFile (_path);
    }
  }

  public void setAqlfilepath (String filepath)
  {
    aqlNodeModel.setAqlfilepath (filepath);
  }

  public void setModuleName (String moduleName)
  {
    aqlNodeModel.setModuleName (moduleName);
  }

  public void setAqlFilepathAndModuleName (IFile aqlFile)
  {
    if (aqlFile != null && aqlFile.getParent () != null) {
      setModuleName (aqlFile.getParent ().getName ());
      setAqlfilepath (Constants.WORKSPACE_RESOURCE_PREFIX + aqlFile.getFullPath ().toString ());
    }
  }
  public IFile getFile (String path)
  {
    return AqlProjectUtils.getWorkspaceRoot ().getFileForLocation ((IPath) new Path (path));
  }

  public void doubleClick ()
  {
    if (ProjectUtils.isModularProject (ActionPlanView.projectName)) {
      boolean isViewOpened = AQLEditorUtils.openAQLViewInEditor (ActionPlanView.projectName, aqlNodeModel.getModuleName (), getViewName());
      if (!isViewOpened)
        requestModuleAndFileInfo ();
    }

    else {
      try {
        AQLEditorUtils.openAQLViewInEditor_nonModular (ActionPlanView.projectName, aqlNodeModel.getAqlfilepath (), getViewName());
      }
      catch (PartInitException e) {
        // If fails, try this way
        AQLEditorUtils.openAQLViewInEditor (ActionPlanView.projectName, getViewName());
      }
    }
  }

  private void requestModuleAndFileInfo ()
  {
    Shell shell = AqlProjectUtils.getActiveShell ();

    boolean fix = MessageDialog.openQuestion (shell, Messages.missing_file_title, Messages.missing_file_aqlnode_message_2);

    if (fix) {
      EditAQLLocationDialog editLocDialog = new EditAQLLocationDialog (shell, ActionPlanView.projectName);
      if (editLocDialog.open () == Window.CANCEL)
        return;

      IFile aqlFile = editLocDialog.getAqlFile ();
      if (aqlFile != null) {

        setAqlfilepath (Constants.WORKSPACE_RESOURCE_PREFIX + aqlFile.getFullPath ().toString ());
        aqlNodeModel.setModuleName (editLocDialog.getModuleName ());
        aqlNodeModel.setFileName (editLocDialog.getAqlFileName ());

        ActionPlanView plan = AqlProjectUtils.getActionPlanView ();
        if (plan != null && plan.ready ()) {
          try {
            plan.serialize ();
          }
          catch (Exception e) {
            // Do nothing
          }
        }
      }
    }
  }

  public void doclick ()
  {

  }

  public AqlGroupType getType ()
  {
    AqlGroupType type = null;
    TreeParent parent = this.getParent ();
    if (parent instanceof AqlGroup) {
      type = ((AqlGroup) parent).getAqlType ();
    }
    return type;
  }

  public int getOffset ()
  {
    int offset = 0;
    return offset;
  }

  public void setViewName (String viewName)
  {
    this.aqlNodeModel.setViewname (viewName);
  }

  public String getViewName ()
  {
    return aqlNodeModel.getViewname ();
  }

  public void writeOut (DataOutputStream writeOut) throws IOException
  {
    writeOutString (writeOut, getLabel());
    writeOutString (writeOut, getComment());
    writeOutString (writeOut, aqlNodeModel.getAqlfilepath());
    writeOutString (writeOut, aqlNodeModel.getModuleName ());
    writeOutString (writeOut, aqlNodeModel.getFileName ());

    writeOutBoolean (writeOut, aqlNodeModel.isOutput ());
    writeOutBoolean (writeOut, aqlNodeModel.isExport ());
    writeOutBoolean (writeOut, aqlNodeModel.isDebug ());

    writeOutAqlType (writeOut, aqlGroup);
  }

  public static AqlNode readIn (DataInputStream readIn) throws IOException
  {
    String label = readInString (readIn);
    String comment = readInString (readIn);
    String aqlFilePath = readInString (readIn);
    String moduleName = readInString (readIn);
    String fileName = readInString (readIn);

    boolean isOutput = readInBoolean (readIn);
    boolean isExport = readInBoolean (readIn);
    boolean isDebug = readInBoolean (readIn);

    AqlGroupType group = readInGroupType (readIn);

    AQLNodeModel model = new AQLNodeModel (label, comment, aqlFilePath, moduleName, fileName, isOutput, isExport, isDebug);

    return new AqlNode (model, group);
  }

  private void writeOutAqlType (DataOutputStream writeOut, AqlGroupType aqlGroup) throws IOException
  {
    switch (aqlGroup)
    {
      case BASIC:
        writeOut.writeInt (BASIC_INT);
        break;
      case CONCEPT:
        writeOut.writeInt (CONCEPT_INT);
        break;
      case REFINEMENT:
        writeOut.writeInt (REFINEMENT_INT);
        break;
      case FINALS:
        writeOut.writeInt (FINALS_INT);
        break;
      default:
        writeOut.writeInt (-1);
    }
  }

  private void writeOutString (DataOutputStream writeOut, String str2Write) throws IOException
  {
    String s = (str2Write == null) ? "" : str2Write;
    byte[] buffer = s.getBytes ();
    writeOut.writeInt (buffer.length);
    writeOut.write (buffer);
  }

  private void writeOutBoolean (DataOutputStream writeOut, boolean bool2Write) throws IOException
  {
    writeOut.writeBoolean (bool2Write);
  }

  private static String readInString (DataInputStream readIn)  throws IOException
  {
    int size = readIn.readInt ();
    byte[] byteArr = new byte[size];
    readIn.read (byteArr);
    return new String (byteArr);
  }

  private static boolean readInBoolean (DataInputStream readIn)  throws IOException
  {
    return readIn.readBoolean ();
  }

  private static AqlGroupType readInGroupType (DataInputStream readIn) throws IOException
  {
    int groupIdx = readIn.readInt ();

    if (groupIdx == BASIC_INT)
      return AqlGroupType.BASIC;
    if (groupIdx == CONCEPT_INT)
      return AqlGroupType.CONCEPT;
    if (groupIdx == REFINEMENT_INT)
      return AqlGroupType.REFINEMENT;
    if (groupIdx == FINALS_INT)
      return AqlGroupType.FINALS;
    else
      return null;
  }

  public void setComment (String comment)
  {
    this.aqlNodeModel.setComment (comment);
  }

  public String getComment ()
  {
    return aqlNodeModel.getComment ();
  }

  public AqlGroupType getAqlGroup ()
  {
    return aqlGroup;
  }

  public void setAqlGroup (AqlGroupType aqlGroup)
  {
    this.aqlGroup = aqlGroup;
  }

  public String toString()
  {
    String str = super.toString ();

    if (ActionPlanView.isSimplifiedView ()) {
      str += " [" + getPathName () + "]";
    }

    return str;
  }

  private String getPathName ()
  {
    List<String> pathName = new ArrayList<String> ();

    LabelNode parentLabelNode = getParentLabelNode ();
    while (parentLabelNode != null) {
      pathName.add (parentLabelNode.getLabel ());
      parentLabelNode = parentLabelNode.getParentLabelNode();
    }

    String pathNameStr = "";
    if (pathName.size () > 0) {
      for (int i = pathName.size () - 1; i > 0; i--) {
        pathNameStr += pathName.get (i) + " > ";
      }
      pathNameStr += pathName.get (0);
    }

    return pathNameStr;
  }
  
  public void setLabel(String label){
    this.label = label;
    this.aqlNodeModel.setViewname (label);
  }

  public String getAqlfilepathFromModuleAndFile (String moduleName, String aqlFileName)
  {
    IFolder srcFolder = ProjectUtils.getTextAnalyticsSrcFolder (ActionPlanView.projectName);
    IFolder moduleFolder = srcFolder.getFolder (moduleName);
    IFile aqlFile = moduleFolder.getFile (aqlFileName);

    if (aqlFile != null && aqlFile.exists ()) {
      return aqlFile.getLocation ().toOSString ();
    }

    return null;
  }

}

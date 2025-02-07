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
package com.ibm.biginsights.textanalytics.workflow.tasks;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.editors.TaggingEditor;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.tasks.models.DataFile;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.ResourceManager;

public class DataFilesTable
{



  private TableViewer viewer;
  private Action open_action;
  private Action delete_action;

  public static final Image OPEN_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/open.gif");
  public static final Image DELETE_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/delete.gif");

  public static final Image FILE_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/data_file.gif");

  /**
   * The content provider class is responsible for providing objects to the view. It can wrap existing objects in
   * adapters or simply return objects as-is. These objects may be sensitive to the current input of the view, or ignore
   * it and always show the same content (like Task List, for example).
   */
  class ViewContentProvider implements IStructuredContentProvider
  {

    DataFilesList files;

    public void inputChanged (Viewer v, Object oldInput, Object newInput)
    {}

    public void dispose ()
    {}

    public Object[] getElements (Object parent)
    {
      if (files == null) initialize ();
      return files.toArray ();
    }

    public void addDataFile (String label, String parent)
    {
      DataFile file = new DataFile (label, parent);
      if (!files.contains (file)) files.add (file);
    }

    public void removeDataFile (DataFile datafile)
    {
      files.remove (datafile);
    }

    private void initialize ()
    {
      files = new DataFilesList ();
    }
  }

  /**
   * label provider
   * 
   * 
   */
  class ViewLabelProvider extends LabelProvider implements ITableLabelProvider
  {
    public String getColumnText (Object obj, int index)
    {
      return getText (obj);
    }

    public Image getColumnImage (Object obj, int index)
    {
      return getImage (obj);
    }

    public Image getImage (Object obj)
    {
      return FILE_ICON;
    }
  }

  /**
   * sort the elements in the table
   * 
   * 
   */
  class NameSorter extends ViewerSorter
  {}

  /**
   * The constructor.
   */
  public DataFilesTable ()
  {}

  /**
   * This is a callback that will allow us to create the viewer and initialize it.
   */
  public void createPartControl (Composite parent)
  {
    viewer = new TableViewer (parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    viewer.setContentProvider (new ViewContentProvider ());
    viewer.setLabelProvider (new ViewLabelProvider ());
    viewer.setSorter (new NameSorter ());
    viewer.setInput (parent);
    makeActions ();
    hookContextMenu ();
    hookDoubleClickAction ();
  }

  /**
   * @param filePath
   */
  public void addDataFile (String label, String parent)
  {
    ((ViewContentProvider) viewer.getContentProvider ()).addDataFile (label, parent);
    viewer.refresh ();
  }

  public void clearFiles ()
  {
    ((ViewContentProvider) viewer.getContentProvider ()).initialize ();
    viewer.refresh ();
  }

  /**
	 * 
	 */
  private void hookContextMenu ()
  {
    MenuManager menuMgr = new MenuManager ("#PopupMenu");
    menuMgr.setRemoveAllWhenShown (true);
    menuMgr.addMenuListener (new IMenuListener () {
      public void menuAboutToShow (IMenuManager manager)
      {
        DataFilesTable.this.fillContextMenu (manager);
      }
    });
    Menu menu = menuMgr.createContextMenu (viewer.getControl ());
    viewer.getControl ().setMenu (menu);
    // getSite().registerContextMenu(menuMgr, viewer);
  }

  /**
   * @param manager
   */
  private void fillContextMenu (IMenuManager manager)
  {
    manager.add (open_action);
    manager.add (delete_action);
    // Other plug-ins can contribute there actions here
    manager.add (new Separator (IWorkbenchActionConstants.MB_ADDITIONS));
  }

  /**
   * initialize the actions that can be applied to the elements on this table
   */
  private void makeActions ()
  {
    makeOpenAction ();

    makeDeleteAction ();
  }

  // FIXME change the editor id to be the wanted editor for this kind of file
  /**
	 * 
	 */
  private void makeOpenAction ()
  {
    open_action = new Action () {
      public void run ()
      {
        ISelection selection = viewer.getSelection ();
        for (Object obj : ((IStructuredSelection) selection).toList ()) {
          if (obj instanceof DataFile) {
            String fileId = ((DataFile) obj).getPath ();
            String fileLabel = ((DataFile) obj).getLabel ();

            TaggingEditor editor = TaggingEditor.getOpenedEditorWithFile (fileId, fileLabel);
            if (editor == null) {
              try {
                AqlProjectUtils.openFile (((DataFile) obj).getInputFile (), TaggingEditor.EDITOR_ID);
              }
              catch (Exception e) {
                LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
                  MessageUtil.formatMessage (Messages.file_not_found, fileLabel, fileId), e);
              }
            }
            else {
              PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().activate (editor);
            }
          }
        }
      }
    };
    open_action.setText (Messages.open_action_text);
    open_action.setToolTipText (Messages.open_action_tootltip);
    open_action.setImageDescriptor (ImageDescriptor.createFromImage (OPEN_ICON));
  }

  /**
	 * 
	 */
  private void makeDeleteAction ()
  {
    delete_action = new Action () {
      public void run ()
      {
        ISelection selection = viewer.getSelection ();
        for (Object obj : ((IStructuredSelection) selection).toList ()) {
          if (obj instanceof DataFile) {
            ViewContentProvider content = (ViewContentProvider) viewer.getContentProvider ();
            content.removeDataFile ((DataFile) obj);
            viewer.refresh ();
          }
        }
      }
    };
    delete_action.setText (Messages.delete_action_text);
    delete_action.setToolTipText (Messages.delete_action_tootltip);
    delete_action.setImageDescriptor (ImageDescriptor.createFromImage (DELETE_ICON));
  }

  /**
	 * 
	 */
  private void hookDoubleClickAction ()
  {
    viewer.addDoubleClickListener (new IDoubleClickListener () {
      public void doubleClick (DoubleClickEvent event)
      {
        open_action.run ();
      }
    });
  }

  public List<String> getSelectedFiles ()
  {

    LinkedList<String> ret = new LinkedList<String> ();

    ISelection selection = viewer.getSelection ();
    for (Object obj : ((IStructuredSelection) selection).toList ()) {
      if (obj instanceof DataFile) {
        ret.add (((DataFile) obj).getLabel ());
      }
    }

    return ret;
  }

  /**
   * @return
   */
  public Action getOpenAction ()
  {
    return open_action;
  }

  /**
   * @return
   */
  public Action getDeleteAction ()
  {
    return delete_action;
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  public void setFocus ()
  {
    viewer.getControl ().setFocus ();
  }
}

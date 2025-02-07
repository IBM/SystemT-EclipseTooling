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
package com.ibm.biginsights.textanalytics.patterndiscovery.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.progress.WorkbenchJob;

import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;

/**
 * action that allows the user to export a given set of patterns from the pattern discovery view
 * 
 * 
 */
public abstract class ExportToCSV extends Action implements IWorkbenchAction
{


  
	public static final String ACTION_ID = "com.ibm.biginsights.textanalytics.patterndiscovery.export.export";
  private static final String JOB_TITLE = "Export Patterns";
  private static final String CSV_FILE_EXTENSION = "*.csv";

  protected static PatternDiscoveryJob pdjob;
  protected ArrayList<BubbleCSVRowModel> bubbles;

  public ExportToCSV (PatternDiscoveryJob pdjob)
  {
    setPDJob (pdjob);
    initActionProperties ();
  }

  /**
   * 
   */
  protected abstract void initActionProperties ();

  /**
   * @throws SQLException
   */
  protected abstract void loadBubblesModel () throws SQLException;

  /**
   * @param job
   */
  public static void setPDJob (PatternDiscoveryJob job)
  {
    pdjob = job;
  }

  /**
   * run the action defined by this class. loads the bubbles and request the user a location where to store a csv
   * representation of them
   */
  @Override
  public void run ()
  {
    WorkbenchJob _job = new WorkbenchJob (JOB_TITLE) {
      @Override
      public IStatus runInUIThread (IProgressMonitor monitor)
      {
        try {
          loadBubblesModel ();
        }
        catch (SQLException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace ();
        }
        // ask user for the location of the csv file
        // save all models to this location
        String filename = requestFile ();
        if (filename == null) return Status.CANCEL_STATUS;

        File csvFile = new File (filename);
        try {
          CSVWriter writer = new CSVWriter (new FileWriter (csvFile, false));
          writer.writeNext (BubbleCSVRowModel.getColumnLabels ());
          for (BubbleCSVRowModel model : bubbles)
            writer.writeNext (model.toStringArray ());
          writer.flush ();
          writer.close ();
        }
        catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace ();
        }
        return Status.OK_STATUS;
      }
    };
    _job.schedule ();
  }

  /**
   * request the location of the csv file from the user
   * 
   * @return
   */
  private String requestFile ()
  {
    Shell shell = Display.getCurrent ().getActiveShell ();
    FileDialog fileDialog = new FileDialog (shell, SWT.SAVE);
    fileDialog.setText (Messages.getString ("FileDirectoryPicker.FILE_SELECTION")); //$NON-NLS-1$
    fileDialog.setFilterExtensions (new String[] { CSV_FILE_EXTENSION });
    fileDialog.open ();

    String fileName = fileDialog.getFileName ();
    if (fileName != null && fileName.trim ().length () > 0) { return fileDialog.getFilterPath () + File.separator
      + fileName; }
    return null;
  }

  @Override
  public void dispose ()
  {
    // TODO Auto-generated method stub
  }
}

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
package com.ibm.biginsights.textanalytics.util.log;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;

public abstract class LogUtil {

	@SuppressWarnings("unused")

  
  private static class Log implements ILog {
    
    private final org.eclipse.core.runtime.ILog log;
    
    private final String pluginID;
    
    private Log(String pluginID, org.eclipse.core.runtime.ILog log) {
      super();
      this.pluginID = pluginID;
      this.log = log;
    }

    @Override
    public void logStatus(IStatus status) {
      this.log.log(status);
    }

    @Override
    public void logAndShowStatus(final IStatus status) {
      logStatus(status);
      Display.getDefault().syncExec(new Runnable() {
        @Override
        public void run() {
          Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
          switch (status.getSeverity()) {
          case IStatus.ERROR: {
            ErrorDialog.openError(shell, "Text Analytics Error", null, status);
            break;
          }
          case IStatus.WARNING: {
            MessageDialog.openWarning(shell, "Text Analytics Warning", status.getMessage());
            break;
          }
          default: {
            MessageDialog.openInformation(shell, "Text Analytics Information", status.getMessage());
            break;
          }
          }
        }
      });
    }

    @Override
    public void logError(String message) {
      log(IStatus.ERROR, message);
    }

    @Override
    public void logError(String message, Throwable t) {
      IStatus status = new Status(IStatus.ERROR, this.pluginID, message, t);
      logStatus(status);
    }

    @Override
    public void logAndShowError(String message) {
      logAndShow(IStatus.ERROR, message);
    }

    @Override
    public void logAndShowError(String message, Throwable t) {
      IStatus status = new Status(IStatus.ERROR, this.pluginID, message, t);
      logAndShowStatus(status);
    }

    @Override
    public void logWarning(String message) {
      log(IStatus.WARNING, message);
    }

    @Override
    public void logAndShowWarning(String message) {
      logAndShow(IStatus.WARNING, message);
    }

    @Override
    public void logInfo(String message) {
      log(IStatus.INFO, message);
    }

    @Override
    public void logAndShowInfo(String message) {
      logAndShow(IStatus.INFO, message);
    }
    
    private final void log(final int severity, final String message) {
      IStatus status = new Status(severity, this.pluginID, message);
      logStatus(status);
    }
    
    private final void logAndShow(final int severity, final String message) {
      IStatus status = new Status(severity, this.pluginID, message);
      logAndShowStatus(status);
    }
    
    /**
     * {@inheritDoc}
     * All messages will be assigned the status - INFO.
     */
    public void logDebug(String message, Throwable t) {
      if (PreferencesPlugin.getTextAnalyticsWorkspacePreferences ().getPrefLogDebugMessages ()) {
        String debugMessage = "DEBUG : "+message; //$NON-NLS-1$
        IStatus status = new Status(IStatus.INFO,  this.pluginID, debugMessage, t);
        logStatus(status);
      }
    }
    
    public void logDebug(String message) {
      logDebug(message, null);
    }
  }
  
  public static ILog getLogForPlugin(final String pluginID) {
    return new Log(pluginID, Platform.getLog(Platform.getBundle(pluginID)));
  }

}

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

/**
 * A convenience utility for logging to the Eclipse log, and for showing error/warning/info dialogs.
 */
public interface ILog {

 public static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

  /**
   * Log a status, if you have one. Else use one of the convenience methods.
   * 
   * @param status
   *          The status to log.
   */
  void logStatus(IStatus status);

  /**
   * Log a status, if you have one. Else use one of the convenience methods. Also shows the
   * appropriate kind of dialog (error/warning/info).
   * 
   * @param status
   *          The status to log.
   */
  void logAndShowStatus(IStatus status);

  /**
   * Log an error message.
   * 
   * @param message
   *          The message to log (should be localized).
   */
  void logError(String message);

  /**
   * Log an error message and an exception.
   * 
   * @param message
   *          The message to log (should be localized). If the message is null, the exception's
   *          message is used.
   * @param t
   *          The exception that should be logged. Must not be null.
   */
  void logError(String message, Throwable t);

  /**
   * Log and show an error message.
   * 
   * @param message
   *          The message to log and show (should be localized).
   */
  void logAndShowError(String message);

  /**
   * Log and show an error message and an exception.
   * 
   * @param message
   *          The message to log and show (should be localized). If the message is null, the
   *          exception's message is used.
   * @param t
   *          The exception that should be logged and shown. Must not be null.
   */
  void logAndShowError(String message, Throwable t);

  /**
   * Log a warning message.
   * 
   * @param message
   *          The message to log (should be localized).
   */
  void logWarning(String message);

  /**
   * Log and show a warning message.
   * 
   * @param message
   *          The message to log and show (should be localized).
   */
  void logAndShowWarning(String message);

  /**
   * Log an informational message.
   * 
   * @param message
   *          The message to log (should be localized).
   */
  void logInfo(String message);

  /**
   * Log and show an informational message.
   * 
   * @param message
   *          The message to log and show (should be localized).
   */
  void logAndShowInfo(String message);
  
  /**
   * Log a message if text analytics debug logging has been enabled in text analytics workspace preferences.
   * 
   * @param message
   * @param t
   */
  void logDebug (String message, Throwable t);
  
  /**
   * Log a message if text analytics debug logging has been enabled in text analytics workspace preferences.
   * 
   * @param message
   */
  void logDebug (String message);

}

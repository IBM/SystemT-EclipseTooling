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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util;

import org.eclipse.core.runtime.IStatus;

import com.ibm.biginsights.textanalytics.patterndiscovery.Activator;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class PDLogger implements IPDLog
{
	@SuppressWarnings("unused")

 
	private ILog iLog;
  private String classInfo;

  private static boolean isTest = false;

  private PDLogger (ILog iLog, String classInfo)
  {
    super ();
    this.iLog = iLog;
    this.classInfo = (classInfo != null) ? classInfo : "";
  }

  public static IPDLog getLogger ()
  {
    if (isTest)
      return PDJavaLogger.getLogger (null);
    else {
      ILog iLog = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);
      return new PDLogger (iLog, null);
    }
  }

  public static IPDLog getLogger (String classInfo)
  {
    if (isTest)
      return PDJavaLogger.getLogger (classInfo);
    else {
      ILog iLog = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);
      return new PDLogger (iLog, classInfo);
    }
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#logStatus(org.eclipse.core.runtime.IStatus)
   */
  @Override
  public void logStatus (IStatus status)
  {
    iLog.logStatus (status);
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#logAndShowStatus(org.eclipse.core.runtime.IStatus)
   */
  @Override
  public void logAndShowStatus (IStatus status)
  {
    iLog.logAndShowStatus (status);
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#logError(java.lang.String)
   */
  @Override
  public void logError (String message)
  {
    iLog.logError (getFullMessage (message));
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#logError(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void logError (String message, Throwable t)
  {
    iLog.logError (getFullMessage (message), t);
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#logAndShowError(java.lang.String)
   */
  @Override
  public void logAndShowError (String message)
  {
    iLog.logAndShowError (getFullMessage (message));
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#logAndShowError(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void logAndShowError (String message, Throwable t)
  {
    iLog.logAndShowError (getFullMessage (message), t);
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#logWarning(java.lang.String)
   */
  @Override
  public void logWarning (String message)
  {
    iLog.logWarning (getFullMessage (message));
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#logAndShowWarning(java.lang.String)
   */
  @Override
  public void logAndShowWarning (String message)
  {
    iLog.logAndShowWarning (getFullMessage (message));
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#logInfo(java.lang.String)
   */
  @Override
  public void logInfo (String message)
  {
    iLog.logInfo (getFullMessage (message));
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#logAndShowInfo(java.lang.String)
   */
  @Override
  public void logAndShowInfo (String message)
  {
    iLog.logAndShowInfo (getFullMessage (message));
  }
  
  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.util.log.ILog#logDebug(java.lang.String)
   */
  @Override
  public void logDebug (String message)
  {
    iLog.logDebug (getFullMessage (message));
  }
  
  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.util.log.ILog#logDebug(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void logDebug (String message, Throwable t)
  {
    iLog.logDebug (getFullMessage (message), t);
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#severe(java.lang.String)
   */
  @Override
  public void severe (String message)
  {
    logError (getFullMessage (message));
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#warning(java.lang.String)
   */
  @Override
  public void warning (String message)
  {
    logWarning (getFullMessage (message));
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#info(java.lang.String)
   */
  @Override
  public void info (String message)
  {
    logInfo (getFullMessage (message));
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#fine(java.lang.String)
   */
  @Override
  public void fine (String message)
  {
    logInfo (getFullMessage (message));
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#finer(java.lang.String)
   */
  @Override
  public void finer (String message)
  {
    logInfo (getFullMessage (message));
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog#finest(java.lang.String)
   */
  @Override
  public void finest (String message)
  {
    logInfo (getFullMessage (message));
  }

  private String getFullMessage (String message)
  {
    return classInfo + ": " + ((message != null) ? message : "");
  }

  public static boolean isTest ()
  {
    return isTest;
  }

  public static void setTest (boolean isTest)
  {
    PDLogger.isTest = isTest;
  }
}

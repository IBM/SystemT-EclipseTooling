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

import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;

public class PDJavaLogger implements IPDLog
{
	@SuppressWarnings("unused")

 
	private Logger logger = null;
  private String classInfo;

  private PDJavaLogger (Logger logger, String classInfo)
  {
    super ();
    this.logger = logger;
    this.classInfo = (classInfo != null) ? classInfo : "";
  }

  private PDJavaLogger (String classInfo)
  {
    super ();
    this.classInfo = (classInfo != null) ? classInfo : "";
    this.logger = Logger.getLogger (this.classInfo);
  }

  public static IPDLog getLogger (String classInfo)
  {
    return new PDJavaLogger (classInfo);
  }


  @Override
  public void logStatus (IStatus status)
  {
    info (status.getMessage ());
  }

  @Override
  public void logAndShowStatus (IStatus status)
  {
    logStatus (status);
    System.out.println (getFullMessage (status.getMessage ()));
  }

  @Override
  public void logError (String message)
  {
    severe (message);
  }

  @Override
  public void logError (String message, Throwable t)
  {
    logError (message);
    severe (t.toString ());
  }

  @Override
  public void logAndShowError (String message)
  {
    logError (message);
    System.out.println (getFullMessage (message));
  }

  @Override
  public void logAndShowError (String message, Throwable t)
  {
    logError (message, t);
    System.out.println (getFullMessage (message));
    t.printStackTrace ();
  }

  @Override
  public void logWarning (String message)
  {
    warning (message);
  }

  @Override
  public void logAndShowWarning (String message)
  {
    logWarning (message);
    System.out.println (getFullMessage (message));
  }

  @Override
  public void logInfo (String message)
  {
    info (message);
  }
  
  /**
   * Logs a message of level Level.FINE.
   */
  @Override
  public void logDebug (String message)
  {
    fine (message);
  }
  
  /**
   * Logs a message of level Level.FINE.
   */
  @Override
  public void logDebug (String message, Throwable t)
  {
    logDebug(message);
    fine (t.toString ());
  }

  @Override
  public void logAndShowInfo (String message)
  {
    logInfo (message);
    System.out.println (getFullMessage (message));
  }

  @Override
  public void severe (String message)
  {
    logger.severe (getFullMessage (message));
  }

  @Override
  public void warning (String message)
  {
    logger.warning (getFullMessage (message));
  }

  @Override
  public void info (String message)
  {
    logger.info (getFullMessage (message));
  }

  @Override
  public void fine (String message)
  {
    logger.fine (getFullMessage (message));
  }

  @Override
  public void finer (String message)
  {
    logger.finer (getFullMessage (message));
  }

  @Override
  public void finest (String message)
  {
    logger.finest (getFullMessage (message));
  }

  private String getFullMessage (String message)
  {
    return classInfo + ": " + ((message != null) ? message : "");
  }

}

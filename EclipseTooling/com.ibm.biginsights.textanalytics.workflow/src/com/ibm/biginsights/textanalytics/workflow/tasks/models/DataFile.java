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
package com.ibm.biginsights.textanalytics.workflow.tasks.models;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IStorageEditorInput;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.FileUtils;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.StringInput;

public class DataFile implements IAdaptable, IStorage
{
  @SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+         //$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  protected String path;
  protected String label;

  public DataFile (String label, String path)
  {
    this.label = label;
    this.path = path;
  }

  public String getPath ()
  {
    return path;
  }

  public String getLabel ()
  {
    return label;
  }

  public String toString ()
  {
    return label;
  }

  public boolean exists ()
  {
    return (null != getContents ());
  }

  public boolean isCollection () throws Exception
  {
    File file = com.ibm.avatar.algebra.util.file.FileUtils.createValidatedFile (ProjectPreferencesUtil.getAbsolutePath (path));
    if (file.isDirectory () || (path.matches (".*(\\.tar\\.gz|\\.tgz|\\.tar|\\.del|\\.zip)"))) { return true; }

    return false;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Object getAdapter (Class adapter)
  {
    return null;
  }

  @Override
  public InputStream getContents ()
  {
    File file = com.ibm.avatar.algebra.util.file.FileUtils.createValidatedFile (ProjectPreferencesUtil.getAbsolutePath (path));
    if (file == null || !file.exists ())
      return null;

    String content = "";

    try {
      if (isCollection ()) {
        content = AqlProjectUtils.getDataFileContent (file, label);
        if (content != null) {
          try {
            return new ByteArrayInputStream (content.getBytes (Constants.ENCODING));
          }
          catch (UnsupportedEncodingException e) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
              MessageUtil.formatMessage (Messages.file_not_found, label, path), e);
          }
        }
        else {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
            MessageUtil.formatMessage (Messages.file_not_found, label, path));
        }
      }
      else {
        try {
          content = FileUtils.fileToStr (file, Constants.ENCODING);
          return new ByteArrayInputStream (content.getBytes (Constants.ENCODING));
        }
        catch (IOException e) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
            MessageUtil.formatMessage (Messages.file_not_found, label, path), e);
        }
      }
    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
        MessageUtil.formatMessage (Messages.file_not_found, label, path), e);
    }

    return null;
  }

  @Override
  public IPath getFullPath ()
  {
    return null;
  }

  @Override
  public String getName ()
  {
    return label;
  }

  @Override
  public boolean isReadOnly ()
  {
    return true;
  }

  public IStorageEditorInput getInputFile () throws Exception
  {
    if (isCollection ()) {
      IStorageEditorInput input = new StringInput (this);
      return input;
    }
    else {
      IStorageEditorInput input = new StringInput (this);
      return input;
    }
  }

  public boolean equals (DataFile f)
  {
    if (path.equals (f.getPath ()) && label.equals (f.getLabel ())) return true;
    return false;
  }
}

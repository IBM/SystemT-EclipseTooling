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

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class StringInput implements IStorageEditorInput
{


 
	private IStorage storage;

  public StringInput (IStorage storage)
  {
    this.storage = storage;
  }

  public boolean exists ()
  {
    return true;
  }

  public ImageDescriptor getImageDescriptor ()
  {
    return null;
  }

  public String getName ()
  {
    return storage.getName ();
  }

  public IPersistableElement getPersistable ()
  {
    return null;
  }

  public IStorage getStorage ()
  {
    return storage;
  }

  public String getToolTipText ()
  {
    return "String-based file: " + storage.getName ();
  }

  @SuppressWarnings("rawtypes")
  public Object getAdapter (Class adapter)
  {
    return null;
  }
}

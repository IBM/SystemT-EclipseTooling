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
package com.ibm.biginsights.textanalytics.concordance.ui.filter;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.biginsights.textanalytics.concordance.model.IFiles;

class FileFilterContentProvider implements ITreeContentProvider {



  private String[] files = new String[] {};
  
  public FileFilterContentProvider(IFiles files) {
    super();
    this.files = files.getFiles().toArray(new String[] {});
  }

  public Object[] getChildren(Object parentElement) {
    return new Object[] {};
  }

  public Object getParent(Object element) {
    return null;
  }

  public boolean hasChildren(Object element) {
    return false;
  }

  public Object[] getElements(Object inputElement) {
    return this.files;
  }

  public void dispose() {
    // do nothing
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    // will not be called
  }

}

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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModelEntry;
import com.ibm.biginsights.textanalytics.concordance.model.IFiles;

public class FileFilter extends ViewerFilter {



  private IFiles files;

  public FileFilter(IFiles files) {
    super();
    this.files = files;
  }
  
  public void setFiles(IFiles newFiles) {
    this.files = newFiles;
  }

  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element) {
    if (element instanceof IConcordanceModelEntry) {
      final String type = ((IConcordanceModelEntry) element).getDocId();
      return this.files.isActiveFile(type);
    }
    return false;
  }

}

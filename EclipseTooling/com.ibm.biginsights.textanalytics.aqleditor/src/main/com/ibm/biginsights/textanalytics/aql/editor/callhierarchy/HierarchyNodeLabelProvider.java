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
package com.ibm.biginsights.textanalytics.aql.editor.callhierarchy;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import com.ibm.biginsights.textanalytics.aql.editor.Activator;

public class HierarchyNodeLabelProvider extends StyledCellLabelProvider 
{


  
  private IPath root = ResourcesPlugin.getWorkspace ().getRoot ().getLocation ();
  
  public Image getImage(Object element) {
    if(element instanceof HierarchyNode){
      return Activator.getDefault().getImageRegistry().get(((HierarchyNode)element).getImage());
    }
    return null;
  }
  
  @Override
  public void update(ViewerCell cell) {
    Object element = cell.getElement();
    StyledString text = new StyledString();
    if (element instanceof HierarchyNode) {
      HierarchyNode node = (HierarchyNode) element;
      cell.setImage(Activator.getDefault().getImageRegistry().get(((HierarchyNode)element).getImage()));
      text.append(node.getText ());
      if (!node.getModuleName ().isEmpty ()) {
        text.append (" : "); //$NON-NLS-1$
        text.append (node.getModuleName(), StyledString.QUALIFIER_STYLER);
      }
      if (!node.getFilePath ().isEmpty ()) {
        text.append (" - "); //$NON-NLS-1$
        
        text.append (new Path(node.getFilePath ()).makeRelativeTo (root).toPortableString (), StyledString.QUALIFIER_STYLER);
      }
    } 
    cell.setText(text.toString());
    cell.setStyleRanges(text.getStyleRanges());
    super.update(cell);
  }
}

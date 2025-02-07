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
package com.ibm.biginsights.textanalytics.treeview.control;


import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AnnotationPreference;

import com.ibm.biginsights.textanalytics.treeview.model.impl.TreeParent;
import com.ibm.biginsights.textanalytics.treeview.util.AQLTreeViewUtility;

/**
 * This class is used to set the labels of the TreeView nodes. It will be the name of the node for eg, "OutputViews"
 * or the actual OutputView name in the case of a Parent node or the actual value of the tuple if it is a TreeObject or leaf node
 * 
 *
 */
@SuppressWarnings("restriction")
public class TreeViewLabelProvider extends StyledCellLabelProvider implements ILabelProvider  {


	
  private Viewer viewer;
  private IPreferenceStore prefStore;
  private IPropertyChangeListener listener;

  public TreeViewLabelProvider(Viewer treeViewer)
	{
	  this.viewer = treeViewer;

	  // Defect 17676: Add property listener to preference store so when annotation
	  // color changes, the color in treeview will also changes.
	  this.prefStore = EditorsPlugin.getDefault ().getPreferenceStore ();
	  listener = new IPropertyChangeListener() {
      @Override
      public void propertyChange (PropertyChangeEvent event)
      {
        viewer.refresh ();
      }
    }; 
	  this.prefStore.addPropertyChangeListener (listener);
	}
	
	/**
	 * It will be the name of the node for eg, "OutputViews", or the actual OutputView name in the case of a Parent node or 
	 * the actual value of the tuple if it is a TreeObject or leaf node
	 */
		public String getText(Object obj) {
			if (obj != null)
			{
				if (obj instanceof TreeParent)
				{
					return ((TreeParent)obj).getName();
				}
				else
				{
				  String strVal = obj.toString ();
				  return strVal.trim().replaceAll ("[\\s]+", " "); //Trimming and replacing any white space in between with a single space //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			else
			{
				return ""; //$NON-NLS-1$
			}
		}
		/**
		 * This method is used if an image needs to be used to display a particular node
		 */
		@SuppressWarnings("unused")
    public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof TreeParent)
			   imageKey = ISharedImages.IMG_OBJ_FOLDER;
			//return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			//MRUDULA_TODO - Determine later if we want to have an image here
			return null;
		}
		
  /**
	 * This method which is extended from StyledCellLabelProvider
	 * is the one used for providing styling to the tree elements
	 * In this case, we require colouring
	 * @see org.eclipse.jface.viewers.StyledCellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
	 */
  public void update (ViewerCell cell)
  {
    Object obj = cell.getElement ();
    cell.setText (getText (obj));
    if (obj instanceof TreeParent) {
      if (((TreeParent) obj).isEndParentNode ()) {
        // Only if it is a parent node and that too the end parent node,
        // we will provide colouring

        String tupleName = ((TreeParent) obj).getName ();
        String viewName = ((TreeParent) obj).getParent ().getName ();
        String markerName = AQLTreeViewUtility.getMarkerForTupleName (viewName + "." + tupleName); //$NON-NLS-1$

        Color color = getCurrentColorPreference (markerName);

        if (color != null)
          cell.setBackground (color);
      }
    }
    super.update (cell);
  }

  /**
   * Look for annotation preference with given annotation type, then get current preference color for it.
   * This is different than the Eclipse provided API getColorPreferenceValue (), which returns the original
   * preference value set in plugin.xml, not the current value set in preference store.
   * @param annotationType
   * @return
   */
  @SuppressWarnings({ "unchecked" })
  private Color getCurrentColorPreference (String annotationType)
  {
    EditorsPlugin editorsPlugin = EditorsPlugin.getDefault ();
    List<AnnotationPreference> fAnnotationPreferences = editorsPlugin.getMarkerAnnotationPreferences().getAnnotationPreferences ();

    // Loop thru annotation preferences, find the one with given annotation type, then get its preference color.
    for (AnnotationPreference ap : fAnnotationPreferences) {
      if (ap.getAnnotationType ().equals (annotationType)) {
        String rgbString = prefStore.getString (ap.getColorPreferenceKey ());
        String[] rgb = rgbString.split (",");
        RGB rgbColor = new RGB (Integer.parseInt (rgb[0]), Integer.parseInt (rgb[1]), Integer.parseInt (rgb[2]));
        return new Color (null, rgbColor);
      }
    }

    return null;
  }
  
  @Override
  public void dispose ()
  {
    super.dispose ();
    if (listener != null) {
      // Disposing the the PropertyChangeListener.
      this.prefStore.removePropertyChangeListener (listener);
      listener = null;
    }
  }
}

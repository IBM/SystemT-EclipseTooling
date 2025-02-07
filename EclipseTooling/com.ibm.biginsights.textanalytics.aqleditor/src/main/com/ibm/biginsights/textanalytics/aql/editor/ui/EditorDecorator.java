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
package com.ibm.biginsights.textanalytics.aql.editor.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;


/**
 * Eclipse decoration service does not decorate editors. We only make
 * use of it to get notified so we update the decoration of AQL editors.
 */
public class EditorDecorator extends LabelProvider implements ILabelDecorator
{


  Image aqlEditorImage = null;
  Image aqlEditorErrorImage = null;

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
   */
  @Override
  public Image decorateImage (Image image, Object element)
  {
    decorateAQLEditors();

    return image;
  }

  private void decorateAQLEditors()
  {
    List<AQLEditor> aqlEditors = getAQLEditors ();
    for (AQLEditor editor : aqlEditors) {
      decorateAQLEditor(editor);
    }
  }

  private void decorateAQLEditor(AQLEditor aqlEditor)
  {
    IFileEditorInput editorInput = (IFileEditorInput)aqlEditor.getEditorInput ();
    IFile file = editorInput.getFile ();
    try {
      int problem = file.findMaxProblemSeverity (null, false, IResource.DEPTH_ZERO);
      if (problem == IMarker.SEVERITY_ERROR)
        aqlEditor.setEditorTitleImage(getAQLEditorErrorImage ());    // TODO: Ask Daiv for a good image and replace this one.
      else
        aqlEditor.setEditorTitleImage(getAQLEditorImage ());
    }
    catch (CoreException e) {
      // Do nothing.
    }
  }

  private List<AQLEditor> getAQLEditors ()
  {
    List<AQLEditor> allAQLEditors = new ArrayList<AQLEditor> ();

    // Make sure we can get the active page, from which we get the editors.
    if ( PlatformUI.getWorkbench () != null &&
         PlatformUI.getWorkbench ().getActiveWorkbenchWindow () != null &&
         PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage () != null ) {

      IEditorReference[] editorRefs = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().getEditorReferences ();
      for (IEditorReference editorRef : editorRefs) {
        IEditorPart editor = editorRef.getEditor (true);
        if (editor instanceof AQLEditor)
          allAQLEditors.add ((AQLEditor)editor);
      }

    }

    return allAQLEditors;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
   */
  @Override
  public String decorateText (String text, Object element)
  {
    return null;
  }

  private Image getAQLEditorImage ()
  {
    if (aqlEditorImage == null) {
      ImageDescriptor imgDesc = Activator.getImageDescriptor ("icons/aqlEditor.gif");
      aqlEditorImage = imgDesc.createImage ();
    }

    return aqlEditorImage;
  }

  private Image getAQLEditorErrorImage ()
  {
    if (aqlEditorErrorImage == null) {
      ImageDescriptor imgDesc = Activator.getImageDescriptor ("icons/aqlEditorError.gif");
      aqlEditorErrorImage = imgDesc.createImage ();
    }

    return aqlEditorErrorImage;
  }

}

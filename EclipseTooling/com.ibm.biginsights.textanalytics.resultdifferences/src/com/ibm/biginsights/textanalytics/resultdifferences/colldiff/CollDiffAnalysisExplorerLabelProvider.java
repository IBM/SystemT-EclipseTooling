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
package com.ibm.biginsights.textanalytics.resultdifferences.colldiff;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.model.WorkbenchAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.resultdifferences.ResultDifferencesUtil;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;

public class CollDiffAnalysisExplorerLabelProvider extends LabelProvider implements
IColorProvider, IFontProvider {

private static final String _COPYRIGHT = "Copyright IBM\n"+
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
* Returns a workbench label provider that is hooked up to the decorator
* mechanism.
* 
* @return a new <code>DecoratingLabelProvider</code> which wraps a <code>
*   new <code>WorkbenchLabelProvider</code>
*/
public static ILabelProvider getDecoratingWorkbenchLabelProvider() {
return new DecoratingLabelProvider(new WorkbenchLabelProvider(),
        PlatformUI.getWorkbench().getDecoratorManager()
                .getLabelDecorator());
}

/**
* Listener that tracks changes to the editor registry and does a full update
* when it changes, since many workbench adapters derive their icon from the file
* associations in the registry.
*/
private IPropertyListener editorRegistryListener = new IPropertyListener() {
public void propertyChanged(Object source, int propId) {
if (propId == IEditorRegistry.PROP_CONTENTS) {
fireLabelProviderChanged(new LabelProviderChangedEvent(CollDiffAnalysisExplorerLabelProvider.this));
}
}
};    
private ResourceManager resourceManager;

/**
* Creates a new workbench label provider.
*/
public CollDiffAnalysisExplorerLabelProvider() {
PlatformUI.getWorkbench().getEditorRegistry().addPropertyListener(editorRegistryListener);
}

/**
* Returns an image descriptor that is based on the given descriptor,
* but decorated with additional information relating to the state
* of the provided object.
*
* Subclasses may reimplement this method to decorate an object's
* image.
* 
* @param input The base image to decorate.
* @param element The element used to look up decorations.
* @return the resuling ImageDescriptor.
* @see org.eclipse.jface.resource.CompositeImageDescriptor
*/
protected ImageDescriptor decorateImage(ImageDescriptor input,
    Object element) {
return input;
}

/**
* Returns a label that is based on the given label,
* but decorated with additional information relating to the state
* of the provided object.
*
* Subclasses may implement this method to decorate an object's
* label.
* @param input The base text to decorate.
* @param element The element used to look up decorations.
* @return the resulting text
*/
protected String decorateText(String input, Object element) {
return input;
}

/* (non-Javadoc)
* Method declared on ILabelProvider
*/
public void dispose() {
PlatformUI.getWorkbench().getEditorRegistry().removePropertyListener(editorRegistryListener);
if (resourceManager != null)
resourceManager.dispose();
resourceManager = null;
super.dispose();
}

/**
* Returns the implementation of IWorkbenchAdapter for the given
* object.  
* @param o the object to look up.
* @return IWorkbenchAdapter or<code>null</code> if the adapter is not defined or the
* object is not adaptable. 
*/
@SuppressWarnings("restriction")
protected final IWorkbenchAdapter getAdapter(Object o) {
	WorkbenchAdapterFactory wbaf = new WorkbenchAdapterFactory();
    return (IWorkbenchAdapter)wbaf.getAdapter(o,  IWorkbenchAdapter.class);
  }


/**
* Returns the implementation of IWorkbenchAdapter2 for the given
* object.  
* @param o the object to look up.
* @return IWorkbenchAdapter2 or<code>null</code> if the adapter is not defined or the
* object is not adaptable. 
*/
@SuppressWarnings("restriction")
protected final IWorkbenchAdapter2 getAdapter2(Object o) {
	WorkbenchAdapterFactory wbaf = new WorkbenchAdapterFactory();
	return (IWorkbenchAdapter2)wbaf.getAdapter(o, IWorkbenchAdapter2.class);
}

/**
* Lazy load the resource manager
* 
* @return The resource manager, create one if necessary
*/
private ResourceManager getResourceManager() {
if (resourceManager == null) {
resourceManager = new LocalResourceManager(JFaceResources
  .getResources());
}

return resourceManager;
}

/* (non-Javadoc)
* Method declared on ILabelProvider
*/
public final Image getImage(Object element) {
//obtain the base image by querying the element
IWorkbenchAdapter adapter = getAdapter(element);
if (adapter == null) {
    return null;
}
ImageDescriptor descriptor = adapter.getImageDescriptor(element);
if (descriptor == null) {
    return null;
}

//add any annotations to the image descriptor
descriptor = decorateImage(descriptor, element);

return (Image) getResourceManager().get(descriptor);
}

/* (non-Javadoc)
* Method declared on ILabelProvider
*/
public final String getText(Object element) {
  if (element instanceof IFile)
  {
	  IFile file = (IFile)element;
    SystemTComputationResult model = ResultDifferencesUtil.getModelFromSTRFFile(file);
    //String fileName =files[1].getName();
    String modifiedFileName = ((IFile)element).getName();
    if (model != null)
    {
    	modifiedFileName = model.getDocumentID ();
    }
    return modifiedFileName;

  }
//query the element for its label
IWorkbenchAdapter adapter = getAdapter(element);
if (adapter == null) {
    return ""; //$NON-NLS-1$
}
String label = adapter.getLabel(element);
return decorateText(label, element);
}

/* (non-Javadoc)
* @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
*/
public Color getForeground(Object element) {
return getColor(element, true);
}

/* (non-Javadoc)
* @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
*/
public Color getBackground(Object element) {
return getColor(element, false);
}

/* (non-Javadoc)
* @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
*/
public Font getFont(Object element) {
IWorkbenchAdapter2 adapter = getAdapter2(element);
if (adapter == null) {
    return null;
}

FontData descriptor = adapter.getFont(element);
if (descriptor == null) {
    return null;
}

return (Font) getResourceManager().get(
FontDescriptor.createFrom(descriptor));
}

private Color getColor(Object element, boolean forground) {
IWorkbenchAdapter2 adapter = getAdapter2(element);
if (adapter == null) {
    return null;
}
RGB descriptor = forground ? adapter.getForeground(element) : adapter
        .getBackground(element);
if (descriptor == null) {
    return null;
}

return (Color) getResourceManager().get(
ColorDescriptor.createFrom(descriptor));
}


}


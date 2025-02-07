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
package com.ibm.biginsights.textanalytics.aql.editor;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.ibm.biginsights.textanalytics.aql.editor.refactoring.ResourceChangeListener;
import com.ibm.biginsights.textanalytics.aql.library.AQLLibrary;
import com.ibm.biginsights.textanalytics.aql.library.AQLModuleLibrary;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.launch.AQLLibraryUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin implements IStartup {



  // The plug-in ID
  public static final String PLUGIN_ID = "com.ibm.biginsights.textanalytics.aqleditor"; //$NON-NLS-1$
  private static AQLLibrary aqlLibrary;
  private static AQLModuleLibrary aql15Library;
  public static final String EDITOR_SCOPE = "com.ibm.biginsights.textanalytics.aqlEditorScope"; //$NON-NLS-1$
  public static final String ICON_VIEW     = "_icon_view";
  public static final String ICON_DICTIONARY      = "_icon_dictionary";
  public static final String ICON_TABLE      = "_icon_table";
  public static final String ICON_FUNCTION      = "_icon_function";
  public static final String ICON_IMPORT      = "_icon_import";
  public static final String ICON_PACKAGE      = "_icon_package";
  public static final String ICON_IMPORT_FILE = "_icon_import_file";
  public static final String ICON_SELECT = "_icon_select";
  public static final String ICON_DETAG = "_icon_detag";
  public static final String ICON_OUTPUT = "_icon_output";
  public static final String ICON_EXTERNAL_VIEW = "_icon_external_view";
  public static final String ICON_TEMPLATE = "_icon_template";
  public static final String ICON_SORT = "_icon_sort";

  public static final String ICON_MODULE = "_icon_module";
  public static final String ICON_IMPORT_MODULE = "_icon_import_module";
  public static final String ICON_IMPORT_VIEW = "_icon_import_view";
  public static final String ICON_IMPORT_TABLE = "_icon_import_table";
  public static final String ICON_IMPORT_FUNCTION = "_icon_import_function";
  public static final String ICON_IMPORT_DICTIONARY = "_icon_import_dictionary";

  //public static final String ICON_EXPORT_MODULE = "_icon_import_module";
  public static final String ICON_EXPORT_VIEW = "_icon_export_view";
  public static final String ICON_EXPORT_TABLE = "_icon_export_table";
  public static final String ICON_EXPORT_FUNCTION = "_icon_export_function";
  public static final String ICON_EXPORT_DICTIONARY = "_icon_export_dictionary";

  public static final String ICON_REQUIRE_DOCUMENT = "_icon_require_document";
  public static final String ICON_EXTERNAL_DICTIONARY = "_icon_ext_dictionary";
  public static final String ICON_EXTERNAL_TABLE = "_icon_ext_table";
  //public static final String ICON_MODULE_FUNCTION = "_icon_ext_tabel";
  //public static final String ICON_MODULE_TABLE = "_icon_ext_function";

  // The shared instance
  private static Activator plugin;

  /**
   * The constructor
   */
  public Activator() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    aqlLibrary = AQLLibrary.getInstance ();
    aql15Library = AQLModuleLibrary.getInstance ();
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  /**
   * Returns an image descriptor for the image file at the given
   * plug-in relative path
   *
   * @param path the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  //Returns the AQL library for 1.3 / non-modular projects
  public static IAQLLibrary getLibrary() {
    return aqlLibrary;
  }
  //Returns the AQL library for 1.5 / modular projects	
  public static IAQLLibrary getModularLibrary() {
    return aql15Library;
  }

  /** Returns the AQL library for the given project.
   * @param projectName
   * @return Modular AQL library or non-modular library depending on the project.
   */
  public static IAQLLibrary getLibraryForProject(String projectName) {
    if (ProjectUtils.isModularProject (projectName)) {
      AQLLibraryUtil.populateAQLLibrary (ProjectUtils.getProject (projectName));
      return getModularLibrary();
    }
    else
      return getLibrary();
  }

  protected void initializeImageRegistry(ImageRegistry reg) {
    super.initializeImageRegistry(reg);
    reg.put(ICON_DICTIONARY,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/dictionary.png")));
    reg.put(ICON_FUNCTION,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/function.png")));
    reg.put(ICON_IMPORT,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/include.png")));
    reg.put(ICON_IMPORT_FILE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/file.png")));
    reg.put(ICON_PACKAGE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/package.png")));
    reg.put(ICON_VIEW,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/view.png")));
    reg.put(ICON_TABLE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/table.png")));
    reg.put(ICON_DETAG,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/detag.png")));
    reg.put(ICON_OUTPUT,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/output.png")));
    reg.put(ICON_EXTERNAL_VIEW,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/external.png")));
    reg.put(ICON_SELECT,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/select.png")));
    reg.put(ICON_SORT,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/sort.gif")));
    reg.put(ICON_TEMPLATE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/template.gif")));

    reg.put(ICON_MODULE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/Module.png")));
    // Icons for import aql constructs 
    reg.put(ICON_IMPORT_MODULE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/importModule.png")));
    reg.put(ICON_IMPORT_VIEW,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/importView.png")));
    reg.put(ICON_IMPORT_TABLE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/importTable.png")));
    reg.put(ICON_IMPORT_FUNCTION,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/importFunction.png")));
    reg.put(ICON_IMPORT_DICTIONARY,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/importDictionary.png")));
    // Icons for export aql constructs
    reg.put(ICON_EXPORT_VIEW,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/exportView.png")));
    reg.put(ICON_EXPORT_TABLE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/exportTable.png")));
    reg.put(ICON_EXPORT_FUNCTION,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/exportFunction.png")));
    reg.put(ICON_EXPORT_DICTIONARY,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/exportDictionary.png")));
    // Icons for require document and ext table/funcion constructs
    reg.put(ICON_REQUIRE_DOCUMENT,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/require.png")));
    reg.put(ICON_EXTERNAL_DICTIONARY,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/externalDictionaryNode.png")));
    reg.put(ICON_EXTERNAL_TABLE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/externalTableNode.png")));

  }

  @Override
  public void earlyStartup() {
    ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener(), IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);
  }
}

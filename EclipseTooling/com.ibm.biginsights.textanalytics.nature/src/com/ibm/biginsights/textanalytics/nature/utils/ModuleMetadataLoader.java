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
package com.ibm.biginsights.textanalytics.nature.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.avatar.api.tam.DictionaryMetadata;
import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.avatar.api.tam.ModuleMetadataFactory;
import com.ibm.avatar.api.tam.TableMetadata;
import com.ibm.biginsights.textanalytics.aql.library.Messages;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class provides a common cache for modules and their metadata. Methods to manage the cache and also extract the
 * all dictionaries and tables of loaded modules is provided. While loading the metadata there is an option to show a
 * progress to indicate the loading process to the user.
 * 
 * 
 */
public class ModuleMetadataLoader
{
  @SuppressWarnings("unused")
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

  /*
   * Common cache to store module metadata associated to individual modules. key is modules names in aql code as well as
   * in dependent tam files. Value is metadata of these modules loaded from their tams and source respectively.
   */
  protected Map<String, ModuleMetadata> metadataMap = new HashMap<String, ModuleMetadata> ();
  private String currProjectName;

  private boolean metadataLoaded = false;

  // A progress bar label to show the status message as loading metadata
  private Label pgBarLabel = null;

  // A progress bar to show the status of the load process
  private ProgressBar progressBar = null;

  private static ModuleMetadataLoader instance = null;

  private ModuleLoadListenerSupport eventSupport = new ModuleLoadListenerSupport ();

  /**
   * Making the class singleton. This will enable only one instance to be able to access the methods.
   * 
   * @param pgBarLabel label for the progress bar indicating the load operation
   * @param progressBar progress bar widget to show the progress
   * @return returns a single instance of the class
   */
  public static ModuleMetadataLoader getInstance (Label pgBarLabel, ProgressBar progressBar)
  {
    if (instance == null) {
      synchronized (ModuleMetadataLoader.class) {
        try {
          instance = new ModuleMetadataLoader (pgBarLabel, progressBar);
        }
        catch (Exception e) {
          throw new RuntimeException ("Error instantiating ModuleMetadataLoader object", null);
        }
      }
    }
    return instance;
  }

  /**
   * method to return the class single instance when ever the progress bar and label are not required.
   * 
   * @return instance of the class .
   * @throws TextAnalyticsException for querying module metadata.
   */
  public static ModuleMetadataLoader getInstance ()
  {
    if (instance == null) {
      synchronized (ModuleMetadataLoader.class) {
        instance = new ModuleMetadataLoader (null, null);
      }
    }
    return instance;
  }

  /**
   * Initialize the values of the class members
   * 
   * @param label status message for the progress bar.
   * @param progressBar to show the status of the load operation
   */
  private ModuleMetadataLoader (Label pgBarLabel, ProgressBar progressBar)
  {
    this.pgBarLabel = pgBarLabel;
    this.progressBar = progressBar;

  }

  /**
   * Method to get the cache in its current state.
   * 
   * @return metadataMap that contains a map of module name to its metadata.
   */
  public Map<String, ModuleMetadata> getMetadataMap ()
  {
    return metadataMap;
  }

  /**
   * Method to start the process of loading the metadata for all the modules that were selected in the given project.
   * Initially the Thread class parameters are set based on the choice of having a progress bar to be displayed or not.
   * Then the thread is invoked to start the loading operation.
   * 
   * @param projectName name of the project currently selected in main tab.
   * @param modulesToLoad list of modules whose metadata is to be loaded.
   */
  public void load (String projectName, String[] modulesToLoad)
  {
  	load (projectName, modulesToLoad, true);
  }

  /**
   * Method to start the process of loading the metadata for all the modules that were selected in the given project.
   * Initially the Thread class parameters are set based on the choice of having a progress bar to be displayed or not.
   * Then the thread is invoked to start the loading operation.
   * 
   * @param projectName name of the project currently selected in main tab.
   * @param modulesToLoad list of modules whose metadata is to be loaded.
   * @param runOnUIThread TRUE: will run on UI thread; FALSE: will not running in a separate thread.
   */
  public void load (String projectName, String[] modulesToLoad, boolean runOnUIThread)
  {
  	// If we're loading modules for a different project, clear the existing map
  	if (!projectName.equals (currProjectName)) {
  		metadataMap.clear ();
  		currProjectName = projectName;
  	}

  	// list of actual modules to load, after removing already loaded modules from modulesToLoad parameter passed to this
    // method
    ArrayList<String> actualModulesToLoad = new ArrayList<String> ();

    // skip modules that are already loaded
    for (String module : modulesToLoad) {
      if (false == metadataMap.containsKey (module)) {
        actualModulesToLoad.add (module);
      }
    }

    // assign the actual modules to be loaded so that already loaded modules need not be ovrwritten.
    modulesToLoad = actualModulesToLoad.toArray (new String[actualModulesToLoad.size ()]);

    if (modulesToLoad.length == 0) return;

    LoadWorker modulesLoader = null;

    // Display a progress bar and status message as label for the user if
    // progress bar is needed to be shown.
    if (progressBar != null && false == progressBar.isDisposed ()) {
      pgBarLabel.setText (com.ibm.biginsights.textanalytics.nature.Messages.getString ("SystemTMainTab.LOADING_MODULE_METADATA"));
      pgBarLabel.setVisible (true);
      progressBar.setVisible (true);
      progressBar.setMinimum (0);
      progressBar.setMaximum (100);

      // set the parameters of the load thread to show the progress of the load operation
      modulesLoader = new LoadWorker (progressBar, pgBarLabel, modulesToLoad, projectName);
    }
    else {
      // set parameters of the load thread with out progress bar and status message being shown
      modulesLoader = new LoadWorker (null, null, modulesToLoad, projectName);
    }

    // Initialize and start the load operation.
    if (runOnUIThread)
    	Display.getDefault ().asyncExec (modulesLoader);
    else
    	modulesLoader.run ();
  }

  /**
   * Method to start the process of unloading the metadata for the module that were selected in the given project.
   * Initially the Thread class parameters are set based on the choice of having a progress bar to be displayed or not.
   * Then the thread is invoked to start the unload operation.
   * 
   * @param moduleToUnLoad list of modules whose metadata is to be loaded.
   */

  public void unLoad (String moduleToUnload)
  {
    UnloadWorker moduleUnloader = null;

    // Display a progress bar and status message as label for the user if
    // progress bar is needed to be shown.
    if (progressBar != null && false == progressBar.isDisposed ()) {
      pgBarLabel.setText (com.ibm.biginsights.textanalytics.nature.Messages.getString ("SystemTMainTab.UNLOADING_MODULE_METADATA"));
      pgBarLabel.setVisible (true);
      progressBar.setVisible (true);
      progressBar.setMinimum (0);
      progressBar.setMaximum (100);

      // set the parameters of the load thread to show the progress of the load operation
      moduleUnloader = new UnloadWorker (progressBar, pgBarLabel, moduleToUnload);
    }
    else {
      // set parameters of the load thread with out progress bar and status message being shown
      moduleUnloader = new UnloadWorker (null, null, moduleToUnload);
    }

    // It was run in UI thread using asyncExec() before, but it caused problem -- it
    // didn't unload fast enough, so the old data was still there and incorrectly used. 
    moduleUnloader.run ();
  }

  /**
   * Method to get the all external dictionaries associated with selected modules in the project.
   * 
   * @param modulesToCalcExtDicts Only these modules have to be considered while getting the external dicts.
   * @return a list of required external dictionaries associated with modules
   */
  public List<ExternalDictionary> getAllReferencedExternalDicts ()
  {

    // A list of referred external dictionaries.
    List<ExternalDictionary> allExtDictRefs = new ArrayList<ExternalDictionary> ();
    try {

      Collection<ModuleMetadata> metadataCollection = metadataMap.values ();
      for (ModuleMetadata moduleMetadata : metadataCollection) {

        String[] extDictNames = moduleMetadata.getExternalDictionaries ();

        // Get all referred dictionaries from the metadata in cache for a module
        for (int j = 0; j < extDictNames.length; j++) {

          // get the metadata associated with all the external dictionary names
          DictionaryMetadata md = moduleMetadata.getDictionaryMetadata (extDictNames[j]);
          String dictName = extDictNames[j];
          boolean mandatory = ProjectUtils.isDictRequired (md);

          // maintain entry for all the external dictionary references
          ExternalDictionary extDict = new ExternalDictionary (dictName, mandatory);
          allExtDictRefs.add (extDict);

        }// end: for-each extDict
      }// end: for-each moduleMetadata
    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.ERROR_ERR_MODULE_METADATA, e); //$NON-NLS-1$
    }
    return allExtDictRefs;
  }

  /**
   * Method to get all the external tables referred in all of the modules.
   * 
   * @param modulesToCalcExtTables a set of modules to be considered to fetch external tables.
   * @return a list of external tables referred by all the referred modules in the project.
   */
  public List<ExternalTable> getAllReferredExternalTables ()
  {
    //
    List<ExternalTable> allExtTableRefs = new ArrayList<ExternalTable> ();
    try {
      Collection<ModuleMetadata> metadataCollection = metadataMap.values ();
      for (ModuleMetadata moduleMetadata : metadataCollection) {

        String[] extTableNames = moduleMetadata.getExternalTables ();

        // Get only the required tables
        for (int j = 0; j < extTableNames.length; j++) {

          // get the metadata associated with the external tables referred.
          TableMetadata md = moduleMetadata.getTableMetadata (extTableNames[j]);
          String tabName = extTableNames[j];
          boolean mandatory = ProjectUtils.isTableRequired (md);

          // Add all the referred dictionaries .
          ExternalTable extDict = new ExternalTable (tabName, mandatory);
          allExtTableRefs.add (extDict);
        }// end for-each extTable
      }// end for-each moduleMetadata
    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.ERROR_ERR_MODULE_METADATA, e); //$NON-NLS-1$
    }
    return allExtTableRefs;
  }

  /**
   * method to clear module metadata cache.
   */
  public void clear ()
  {
    metadataMap.clear ();
    metadataLoaded = false;
  }

  /**
   * method to set the singleton instance to null. This is done to release all the resources held by the singleton
   * instance.
   */
  public void dispose ()
  {
    clear ();

    // release the progress bar and the label.
    progressBar = null;
    pgBarLabel = null;

    instance = null;
  }

  /**
   * This inner class is for managing unload operation of the modules (while displaying the progress bar if the user
   * desires)
   * 
   * 
   */
  final class UnloadWorker implements Runnable
  {

    ProgressBar progressBar = null;
    Label label = null;
    String moduleToUnload = null;
    String projectName = null;

    /**
     * Initialize values for unload operation with progress bar
     * 
     * @param pgBar progress bar to be displayed
     * @param pgBarLabel a label for the progress bar
     * @param moduleToUnload list of modules to be loaded to the cache
     */
    public UnloadWorker (ProgressBar pgBar, Label pgBarLabel, String moduleToUnload)
    {

      this.progressBar = pgBar;
      this.label = pgBarLabel;
      this.moduleToUnload = moduleToUnload;
    }

    /**
     * Thread to unload metadata and update the progress bar as and when completed
     */
    @Override
    public void run ()
    {

      // assigning total work as 100 because the total maximum value of the progress bar is set as 100.
      final int totalWork = 100;
      unloadMetadata (moduleToUnload, totalWork);

      // once the unload operation is completed set the progress bar to max and make it disabled
      if (progressBar != null && progressBar.isDisposed () == false) {
        progressBar.setSelection (totalWork);
        progressBar.setVisible (false);
        pgBarLabel.setVisible (false);
      }

    }

    /**
     * Method to remove metadata of a module and its dependent modules recursively. But the modules which are needed by
     * other modules(modules that are not being removed) will be retained.
     * 
     * @param moduleToUnload name of the module that has to be unloaded
     * @param totalWork
     */
    public void unloadMetadata (String moduleToUnload, int totalWork)
    {
      synchronized (metadataMap) {

        // step 1: Fetch metadata of moduleToUnload
        ModuleMetadata metadataOfModuleToUnload = metadataMap.get (moduleToUnload);
        if (metadataOfModuleToUnload == null) return;

        // step 2: remove moduleToUnload from the metadata map
        metadataMap.remove (moduleToUnload);

        // Treat 20% of work to be complete, once current module's metadata is removed
        int completedWork = (int) Math.round (totalWork * 0.20);

        // calculate the remaining work remaining for the current module
        int remainingWork = totalWork - completedWork;

        // step 3: for each module in the cache, verify if any of them have a dependency on moduleToUnload
        Set<String> keys = metadataMap.keySet ();

        if (keys.isEmpty ()) {
          updateProgressBar (completedWork);
        }

        for (String moduleName : keys) {
          if (moduleName.equals (moduleToUnload)) {
            continue; // no need to process moduleToUnload
          }

          ModuleMetadata metadata = metadataMap.get (moduleName);

          // get dependent modules from the modules in the cache. If the modules have removed module as one of the
          // dependent modules then add that module back to the cache.
          List<String> deps = metadata.getDependentModules ();
          if (deps.contains (moduleToUnload)) {

            // can't unload moduleToUnload because someone is dependent on it
            metadataMap.put (moduleToUnload, metadataOfModuleToUnload);
            return;
          }
          else {
            metadataMap.remove (moduleToUnload);

            // update the progress bar after the unload operation is completed.
            updateProgressBar (completedWork);
          }
        }// close for each module in keys set

        // step 4: remove all modules that moduleToUnload depends on, as long as no other module is dependent on them
        List<String> depModuleToUnload = metadataOfModuleToUnload.getDependentModules ();

        // split the remaining weight equally amongst the load action for remaining modules
        if (false == depModuleToUnload.isEmpty ()) {
          int workPerDepsModule = remainingWork / depModuleToUnload.size ();

          for (String depModule : depModuleToUnload) {
            unloadMetadata (depModule, workPerDepsModule);
          }
        }
        else {

          // update remaining work to be completed if there are no dependent modules
          updateProgressBar (remainingWork);
        }

        // fire an event after unloading of modules is completed
        try {
          eventSupport.fireModuleUnLoadedEvent (moduleToUnload);
        }
        catch (Exception e) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.ERROR_ERR_MODULE_METADATA, e); //$NON-NLS-1$       
        }
      }// close synchronized
    }// close unloadMetadata

  }// close UnloadWorker class

  /**
   * This class creates a separate thread for loading metadata for the selected module and updates the progress bar
   * while load happens.
   * 
   * 
   */
  final class LoadWorker implements Runnable
  {
    ProgressBar progressBar = null;
    Label label = null;
    String[] modules = null;
    String projectName = null;

    /**
     * Initialize values for load operation with progress bar
     * 
     * @param pgBar progress bar to be displayed
     * @param pgBarLabel a label for the progress bar
     * @param modules list of modules to be loaded to the cache
     * @param projectName name of the project selected
     */
    public LoadWorker (ProgressBar pgBar, Label pgBarLabel, String[] modules, String projectName)
    {

      this.progressBar = pgBar;
      this.label = pgBarLabel;
      this.modules = modules;
      this.projectName = projectName;
    }

    /**
     * Thread to load metadata and update the progress bar as and when completed
     */
    @Override
    public void run ()
    {
      // Set the total work to be completed by progress bar
      final int totalWork = 100;

      // get the bin path and the tam path of current project as well as for the referenced projects.
      String modulePath = null;
      try {
        modulePath = ProjectPreferencesUtil.getTamPathStr (projectName);
      }
      catch (CoreException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.ERROR_REQ_MODULES_GEN_ERROR, e); //$NON-NLS-1$
      }

      // initially assign work equal to total amount.
      int worked = totalWork;

      if (modules.length > 0) {

        // calculate work as per number of to be loaded modules.
        worked = worked / modules.length;
      }

      // load metadata for each module in the list
      for (String moduleName : modules) {
        try {
          if (false == metadataMap.containsKey (moduleName)) {
            loadMetadata (moduleName, modulePath, worked);
          }
        }
        catch (TextAnalyticsException e) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.ERROR_ERR_MODULE_METADATA, e); //$NON-NLS-1$        
        }

        // if progress bar is displayed update after completion of load operation
        if (progressBar != null) {
          progressBar.setSelection (totalWork);
          progressBar.setVisible (false);
          pgBarLabel.setVisible (false);
        }
      }
      try {
        eventSupport.fireModuleLoadedEvent (modules);
      }
      catch (Exception e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.ERROR_ERR_MODULE_METADATA, e); //$NON-NLS-1$       
      }
    }// end: loadMetadata run()

    /**
     * method to load metadata for module and dependent modules
     * 
     * @param moduleName name of the module to be loaded
     * @param modulePath path of the module's location
     * @param totalWork amount of work to be executed by the current invocation of this method, as a percentage of the
     *          overall metadata load operation
     * @throws TextAnalyticsException while reading metadata for modules
     */
    private void loadMetadata (String moduleName, String modulePath, int totalWork) throws TextAnalyticsException
    {
      setMetadataLoaded (false);

      Map<String, ModuleMetadata> metadataMap = ModuleMetadataLoader.getInstance ().metadataMap;
      synchronized (metadataMap) {

        // Do not load modules that were already loaded.
        if (false == metadataMap.containsKey (moduleName)) {

          // Load the current module's metadata and place it into the cache
          ModuleMetadata metadata = ModuleMetadataFactory.readMetaData (moduleName, modulePath);

          // Add the module and its metadata into the cache
          metadataMap.put (metadata.getModuleName (), metadata);

          // Treat 20% of work to be complete, once current module's metadata is loaded
          int completedWork = (int) Math.round (totalWork * 0.20);

          // update the progress bar once done loading metadata for a given module.
          updateProgressBar (completedWork);

          // calculate the work remaining for the current module
          int remainingWork = totalWork - completedWork;

          // get the dependent modules under the module loaded in previous step.
          List<String> deps = metadata.getDependentModules ();

          // id there are no dependent modules for a module then update the progress bar with the remaining weight of
          // the given module.
          if (deps.isEmpty ()) {
            updateProgressBar (remainingWork);
          }

          // If there are dependent modules for a given module then load such modules and their metadata to the cache.
          for (String depModule : deps) {

            // split the remaining weight equally amongst the load action for remaining modules
            int workPerDepModule = remainingWork / deps.size ();

            // recursive call for loading metadata for all dependent modules along with their work value assigned.
            loadMetadata (depModule, modulePath, workPerDepModule);
          }// close for each dep module
        }// close if metadata already loaded
      }// close synchronized
      setMetadataLoaded (true);
    }// close loadMetadata

  }// close worker thread

  /**
   * method for adding metadata load listener for notifying load completion
   * 
   * @param listener for change notification
   */
  public synchronized void addListener (ModuleLoadListener listener)
  {
    eventSupport.addListener (listener);
  }

  /**
   * method to remove a metadata load lister when not needed.
   * 
   * @param listener for change notification
   */
  public synchronized void removeListener (ModuleLoadListener listener)
  {
    eventSupport.removeListener (listener);
  }

  /**
   * method for setting the status of metadata load operation
   * 
   * @param flag set if load is completed otherwise load operation is incomplete
   */
  private synchronized void setMetadataLoaded (boolean flag)
  {
    metadataLoaded = flag;
  }

  /**
   * method to give the status of the metadata load operation
   * 
   * @return status of load operation
   */
  public synchronized boolean isMetadataLoaded ()
  {
    return metadataLoaded;
  }

  /**
   * method to update the progress bar status as and when work is completed
   * 
   * @param weight amount of work completed
   */
  private void updateProgressBar (int weight)
  {
    // if progress bar is displayed, update the progress bar after each module load completion of load operation
    if (progressBar != null && false == progressBar.isDisposed ()) {

      // Add the weight assigned to each module to progress of progress bar, after module load is completed
      progressBar.setSelection (progressBar.getSelection () + weight);

      // If the complete work is accomplished make progress bar and the label disabled.
      if (progressBar.getSelection () == 100) {
        progressBar.setVisible (false);
        pgBarLabel.setVisible (false);
        progressBar.setSelection (0);
      }// close if progress bar at 100
    }// close if progress bar not null
  }// close update progress bar
}// end: ModuleMetadataLoader class


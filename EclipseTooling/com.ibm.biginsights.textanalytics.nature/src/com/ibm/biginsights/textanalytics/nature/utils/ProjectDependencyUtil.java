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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.avatar.api.tam.ModuleMetadataFactory;
import com.ibm.avatar.aql.tam.ModuleResolver;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 *  Simon
 */
public class ProjectDependencyUtil
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
 
	private static final ILog logger = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);

  /**
   * This API bundles the Modules and its dependent modules to an archive file
   * 
   * @param directory : location where the archive file to be exported
   * @param fileName : archive file name. If its null, then write the dependent tams to the directory.
   * @param project : Project where the Module to be exported is present
   * @param moduleNames
   */
  public static void bundleDependentModules (String directory, String fileName, IProject project, String moduleNames[]) throws IOException, Exception
  {
    Map<String, InputStream> fileParts = new HashMap<String, InputStream> ();
    InputStream streamParts = null;
    InputStream inputStream = null;
    FileOutputStream outStream = null;
    String path = populateProjectDependencyPath (project);
    ModuleResolver resolver = new ModuleResolver (path);
    for (String moduleName : moduleNames) {
      String depModules = populateModuleDependencyPath (moduleName, path);
      if (depModules != null && !depModules.isEmpty ()) {
        String modules[] = depModules.split (Constants.DATAPATH_SEPARATOR);
        for (String module : modules) {

          if (module != null && !module.isEmpty ()) {
            streamParts = resolver.resolve (module);
            fileParts.put (module, streamParts);
          }
        }
      }
      streamParts = resolver.resolve (moduleName);
      fileParts.put (moduleName, streamParts);
    }
    if (fileName == null || fileName.isEmpty ()) {
      // Put the dependent TAMS to the Directory
      File file = null;
      for (String modName : fileParts.keySet ()) {
        file = new File (directory, modName + Constants.TAM_FILE_EXTENSION);
        outStream = new FileOutputStream (file);
        inputStream = fileParts.get (modName);

        byte buffer[] = new byte[10240]; // 10 KB is an optimal size
        int len = 0;
        while ((len = inputStream.read (buffer)) > 0) {
          outStream.write (buffer, 0, len);
        }

        inputStream.close ();
        outStream.close ();
      }

    }
    else if (fileName.indexOf (Constants.ZIP_FILE_EXTENSION) != -1) {
      // Put the TAM in ZIP
      File file = FileUtils.createValidatedFile (directory, fileName);
      ZipOutputStream zipOutputStream = new ZipOutputStream (new FileOutputStream (file));
      for (String modName : fileParts.keySet ()) {
        inputStream = fileParts.get (modName);
        ZipEntry newEntry = new ZipEntry (modName + Constants.TAM_FILE_EXTENSION);
        zipOutputStream.putNextEntry (newEntry);

        BufferedInputStream bufferedInputStream = new BufferedInputStream (inputStream);
        while (bufferedInputStream.available () > 0) {
          zipOutputStream.write (bufferedInputStream.read ());
        }
        zipOutputStream.closeEntry ();
        bufferedInputStream.close ();
        inputStream.close ();
      }
      zipOutputStream.finish ();
      zipOutputStream.close ();
    }

    else if (fileName.indexOf (Constants.JAR_FILE_EXTENSION) != -1) {
      File file = FileUtils.createValidatedFile (directory, fileName);
      JarOutputStream jarOutputStream = new JarOutputStream (new FileOutputStream (file));
      for (String modName : fileParts.keySet ()) {
        inputStream = fileParts.get (modName);
        JarEntry newEntry = new JarEntry (modName + Constants.TAM_FILE_EXTENSION);
        jarOutputStream.putNextEntry (newEntry);

        BufferedInputStream bufferedInputStream = new BufferedInputStream (inputStream);
        while (bufferedInputStream.available () > 0) {
          jarOutputStream.write (bufferedInputStream.read ());
        }
        jarOutputStream.closeEntry ();
        bufferedInputStream.close ();
        inputStream.close ();
      }
      jarOutputStream.finish ();
      jarOutputStream.close ();
    }

  }

  /**
   * This API is used to get the dependent Modules.
   * 
   * @param moduleName
   * @param projectDependencyPath
   * @return
   */
  public static String populateModuleDependencyPath (String moduleName, String projectDependencyPath)
  {
    List<String> depMods = new ArrayList<String> ();
    addDependentModules (moduleName, projectDependencyPath, depMods);

    // Build dependent module string.
    String depModStr = "";
    for (String mn : depMods) {
      depModStr += (depModStr.equals ("")) ? mn : Constants.DATAPATH_SEPARATOR + mn;
    }

    return depModStr;
  }

  /**
   * Recursively add a module's dependent modules to the overall list of dependent modules.
   * The first call should include an empty list. The recursive calls will build up the list.
   * @param moduleName The module name
   * @param path The path of all possible locations for the module and its dependent modules,
   *             like the path returned by populateProjectDependencyPath (project).
   * @param depModules The list of dependent modules already collected so far.
   */
  private static void addDependentModules (String moduleName, String path, List<String> depModules)
  {
    if (StringUtils.isEmpty (moduleName))
      return;

    ModuleMetadata modData = null;
    try {
      modData = ModuleMetadataFactory.readMetaData (moduleName, path);
    }
    catch (Exception e) {
      logger.logError (e.getMessage ());
    }

    if (modData == null)
      return;

    List<String> depModuleList = modData.getDependentModules ();
    if (StringUtils.isEmpty (depModuleList))
      return;

    for (String module : depModuleList) {
      if (!depModules.contains (module)) {
        depModules.add (module);
        addDependentModules (module, path, depModules);
      }
    }
  }

  /**
   * This API is used to calculate the Project Dependencies.
   * 
   * @param project
   * @return
   */
  public static String populateProjectDependencyPath (IProject project)
  {
    String path = "";//$NON-NLS-1$
    path = populateDependencyPath (project, path);

    if (!path.isEmpty ()) {
      String newPath = "";//$NON-NLS-1$
      String tempPath[] = path.split (Constants.DATAPATH_SEPARATOR);
      for (String pat : tempPath) {
        if (!pat.isEmpty ()) newPath += pat + Constants.DATAPATH_SEPARATOR;
      }
      path = newPath;
    }

    return path;
  }

  private static String populateDependencyPath (IProject project, String path)
  {
    try {
      if (project == null
        || !(project.hasNature (Constants.PLUGIN_NATURE_ID) && ProjectUtils.isModularProject (project))) return "";//$NON-NLS-1$

      String absPath = null;
      IFolder binDir = ProjectUtils.getTextAnalyticsBinFolder (project);
      if (binDir != null) {
        absPath = binDir.getLocation ().toString ();
      }
      if (absPath == null || absPath.isEmpty ()) return "";

      path = path + new File (absPath).toURI ().toString ()
        + Constants.DATAPATH_SEPARATOR;
      String importedTAMS = ProjectUtils.getImportedTams (project);
      if (importedTAMS != null && !importedTAMS.isEmpty ()) {
        String tamPath = "";//$NON-NLS-1$
        String tams[] = importedTAMS.split (Constants.DATAPATH_SEPARATOR);
        for (int i = 0; i < tams.length; i++) {
        	String tempAbsPath = ProjectPreferencesUtil.getAbsolutePath (tams[i]);
        	if(tempAbsPath != null)
	          tamPath += new File (tempAbsPath).toURI ().toString ()
	            + Constants.DATAPATH_SEPARATOR;
        }
        path = path + tamPath + Constants.DATAPATH_SEPARATOR;
      }
      List<IProject> referencedProjectArr = getRefdProjects(project);
      for (IProject iProject : referencedProjectArr) {
        path = path + populateDependencyPath (iProject, "");
      }

    }
    catch (CoreException e) {
      logger.logError (e.getMessage ());
    }
    return path;
  }

  public static List<IProject> getRefdProjects (IProject project) throws CoreException
  {
    List<IProject> iProjectList = new ArrayList<IProject> ();
    iProjectList.add (project);
    return getRefdProjects (iProjectList);
  }

  public static List<IProject> getRefdProjects (List<IProject> projects) throws CoreException
  {
    List<IProject> iProjectList = new ArrayList<IProject> ();

    for (IProject p : projects) {
      for (IProject pChild : p.getReferencedProjects ()) {
        if ( !iProjectList.contains (pChild) &&
             !pChild.getName ().equals (p.getName ()) )   // make sure the project is not in its referenced project list (maybe overcautious?)
          iProjectList.add (pChild);
      }
    }

    return iProjectList;
  }

  /**
   * API is used to check any modules in modulePath is matching with the module in Project.
   * 
   * @param project
   * @param modulePath
   * @return
   */
  public static Map<String, List<String>> isDuplicateTamExist (IProject project, String modulePath)
  {
    if (modulePath == null || modulePath.isEmpty ()) return null;

    Map<String, List<String>> map = new HashMap<String, List<String>> ();

    String absBinPath = null;
    IFolder binDir = ProjectUtils.getTextAnalyticsBinFolder (project);
    if (binDir != null) {
      absBinPath = binDir.getLocation ().toFile ().toURI ().toString ();
    }
    String[] modules = ProjectUtils.getAllModules (project);
    if (modules == null) return null;

    Set<String> moduleSet = new HashSet<String> (Arrays.asList (modules));

    String tempPath[] = modulePath.split (Constants.DATAPATH_SEPARATOR);
    for (String path : tempPath) {
      if (path != null && !path.isEmpty () && !path.equals (absBinPath)) {

        File filePath = null;
        try {
          filePath = new File (new URI (path));
          if (filePath.exists ()) {
            String fileName = filePath.getName ();
            boolean isFile = filePath.isFile ();
            boolean isDir = filePath.isDirectory ();
            if (isDir) {
              File files[] = filePath.listFiles ();
              for (File file : files) {
                process (map, moduleSet, path, file.getName ());
              }
            }
            else if (isFile && fileName.endsWith (Constants.TAM_FILE_EXTENSION)) {//$NON-NLS-1$
              process (map, moduleSet, path, fileName);
            }
            else if (isFile && fileName.endsWith (Constants.ZIP_FILE_EXTENSION)) {//$NON-NLS-1$
              ZipInputStream zipInputStream = null;
              try {
                zipInputStream = new ZipInputStream (new FileInputStream (filePath));
                ZipEntry entry = zipInputStream.getNextEntry ();
                while (entry != null) {
                  String entryName = entry.getName ();
                  process (map, moduleSet, path, entryName);
                  entry = zipInputStream.getNextEntry ();
                }

              }
              catch (FileNotFoundException e) {
                logger.logError (e.getMessage ());
              }
              catch (IOException e) {
                logger.logError (e.getMessage ());
              }
              finally {
                try {
                  if (zipInputStream != null) {
                    zipInputStream.close ();
                  }
                }
                catch (IOException e) {
                  logger.logError (e.getMessage ());
                }
              }

            }
            else if (isFile && fileName.endsWith (Constants.JAR_FILE_EXTENSION)) {//$NON-NLS-1$
              JarInputStream jarInputStream = null;
              try {
                jarInputStream = new JarInputStream (new FileInputStream (filePath));
                JarEntry entry = jarInputStream.getNextJarEntry ();
                while (entry != null) {
                  String entryName = entry.getName ();
                  process (map, moduleSet, path, entryName);
                  entry = jarInputStream.getNextJarEntry ();
                }

              }
              catch (FileNotFoundException e) {
                logger.logError (e.getMessage ());
              }
              catch (IOException e) {
                logger.logError (e.getMessage ());
              }
              finally {
                try {
                  if (jarInputStream != null) {
                    jarInputStream.close ();
                  }
                }
                catch (IOException e) {
                  logger.logError (e.getMessage ());
                }
              }

            }
          }
        }
        catch (URISyntaxException e1) {
          logger.logError (e1.getMessage ());
        }
        
      }
    }

    return map;

  }

  private static void process (Map<String, List<String>> map, Set<String> moduleSet, String path, String fileName)
  {

    if (fileName.endsWith (Constants.TAM_FILE_EXTENSION)) {
      fileName = fileName.replace (Constants.TAM_FILE_EXTENSION, "");//$NON-NLS-1$
      if (moduleSet.contains (fileName)) {
        List<String> list = map.get (fileName);
        if (list == null) {
          list = new ArrayList<String> ();
          list.add (path);
          map.put (fileName, list);
        }
        else {
          list.add (path);
        }
      }

    }

  }

}

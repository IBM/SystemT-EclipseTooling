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
package com.ibm.biginsights.textanalytics.aql.library;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


/**
 *  Kalakuntla
 * This class is used to hold the AQL library related information for modular projects..
 */
public class AQLModuleLibrary implements IAQLLibrary {

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

  // For modular projects library hold the map of projectnames vs AQLModueProject instances..
  private static HashMap<String, AQLModuleProject> library = new HashMap<String, AQLModuleProject>();

  //used to storing searchpath hascode, no repeating of searchpath parsing if file is opened from same searchpath
  private static ArrayList<Integer> parsedPath = null;
  
  private static AQLModuleLibrary libInstance = null;
  
  private ModularAQLModel aqlModel= null;
  
  private Set<String> moduleFileSet;// = new HashSet<String> ();
  
  private AQLModuleLibrary() {
    
  }
  
  public static synchronized AQLModuleLibrary getInstance() {
    if (libInstance == null) {
      libInstance = new AQLModuleLibrary();
      libInstance.aqlModel = new ModularAQLModel(libInstance);
    }
    return libInstance;
  }

  public HashMap<String, AQLProject> getLibraryMap() {
    //return library;
    return null;
  }

  public HashMap<String, AQLModuleProject> getModularLibraryMap() {
    return library;
  }

  @Override
  public void addParsedPath(Integer pp) {
    if(parsedPath == null)
    {
      parsedPath = new ArrayList<Integer>();
      parsedPath.add(pp);
    }
    else
    {
      parsedPath.add(pp);
    }
  } 

  @Override
  public ArrayList<Integer> getParsedPath() {
    return parsedPath;
  }

  @Override
  public List<AQLElement> getDictionaries(String filePath) {
    //System.out.println ("I am in get dictionaries method..");
    List<AQLElement> aqlDictionaries = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);
      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);
        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("DICTIONARY")) {
                  aqlDictionaries.add(a);
                }
              }
            }
            return aqlDictionaries;
          }
        }   
      }
    }
    return null;
  }

  public List<AQLElement> getExternalViews(String filePath) {
    //System.out.println ("I am in get ext views method..");
    List<AQLElement> aqlExtViews = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("EXTERNAL_VIEW")) {
                  aqlExtViews.add(a);
                }
              }
            }
            return aqlExtViews;
          }
        }   
      }
    }
    return null;
  }

  @Override
  public List<AQLElement> getFunctions(String filePath) {
    List<AQLElement> aqlFunctions = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("FUNCTION")) {
                  aqlFunctions.add(a);
                }
              }
            }
            return aqlFunctions;
          }
        }   
      }
    }
    return null;
  }

  @Override
  public List<AQLElement> getIncludedFiles(String filePath) {
    // There will be no included files in modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getSelects(String filePath) {
    List<AQLElement> aqlSelects = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("SELECT")) {
                  aqlSelects.add(a);
                }
              }
            }
            return aqlSelects;
          }
        }   
      }
    }
    return null;
  }

  @Override
  public List<AQLElement> getTables(String filePath) {
    List<AQLElement> aqlTables = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("TABLE")) {
                  aqlTables.add(a);
                }
              }
            }
            return aqlTables;
          }
        }   
      }
    }
    return null;
  }

  @Override
  public List<AQLElement> getViews(String filePath) {
    List<AQLElement> aqlViews = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("VIEW")) {
                  aqlViews.add(a);
                }
              }
            }
            return aqlViews;
          }
        }   
      }
    }
    return null;
  }

  //@Override
  public List<AQLElement> getModules(String filePath) {
    List<AQLElement> aqlModules = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("MODULE")) {
                  aqlModules.add(a);
                }
              }
            }
            return aqlModules;
          }
        }   
      }
    }
    return null;
  }

//@Override
  public List<String> getImportedModules(String filePath) {
    List<String> aqlImportModules = new ArrayList<String>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("IMPORT_MODULE")) {
                  aqlImportModules.add(a.name);
                }else if(a.type.equals("IMPORT_VIEW")){
                  aqlImportModules.add(a.fromModuleName);
                }else if(a.type.equals("IMPORT_DICTIONARY")){
                  aqlImportModules.add(a.fromModuleName);
                }else if(a.type.equals("IMPORT_FUNCTION")){
                  aqlImportModules.add(a.fromModuleName);
                }else if(a.type.equals("IMPORT_TABLE")){
                  aqlImportModules.add(a.fromModuleName);
                }
              }
            }
            return aqlImportModules;
          }
        }   
      }
    }
    return null;
  }
  /*
  //@Override
  public List<AQLElement> getImportedModules(String filePath) {
    List<AQLElement> aqlImportModules = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("IMPORT_MODULE")) {
                  aqlImportModules.add(a);
                }
              }
            }
            return aqlImportModules;
          }
        }   
      }
    }
    return null;
  }
*/
  public List<String> getImportModuleStmtModules(String filePath) {
    List<String> aqlImportModules = new ArrayList<String>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("IMPORT_MODULE")) {
                  aqlImportModules.add(a.name);
                }
              }
            }
            return aqlImportModules;
          }
        }   
      }
    }
    return null;
  }
  
  //@Override
  public List<AQLElement> getImportedViews(String filePath) {
    List<AQLElement> aqlImportViews = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("IMPORT_VIEW")) {
                  aqlImportViews.add(a);
                }
              }
            }
          }
        }   
      }
    }
    //System.out.println ("No of Imported views... "+ aqlImportViews.size ());
    List<String> importedModules =  getImportModuleStmtModules(filePath);
    // Call a method to get all exportedViews within a module..
    ArrayList<String> moduleFilesList = new ArrayList<String>();
    Set<String> moduleFileSet = null; //new HashSet<String> ();
    for(String importModuleName : importedModules){
      List<String> moduleFilePathList = getModuleSearchPathList (importModuleName);
      moduleFileSet = getAllCurrModuleFiles (moduleFilePathList);
      moduleFilesList.addAll (moduleFileSet);
      aqlImportViews.addAll (getExportedViews (moduleFilesList));
      //System.out.println ("No of Imported views... "+ aqlImportViews.size ());
    }
    //System.out.println ("No of Imported views... "+ aqlImportViews.size ());
    return aqlImportViews;
    //return null;
  }

  private List<String> getModuleSearchPathList(String modName){
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    AQLModule aqlModule = null;
    List<String> aqlFiles = new ArrayList<String>();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);
      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        if((moduleName.equalsIgnoreCase (modName)) ||(moduleName.contains (modName))){
          aqlModule = modules.get (moduleName);
          break;
        }
      }
    }
    if(aqlModule != null){
      aqlFiles = aqlModule.getAqlFilePaths (); 
    }
    return aqlFiles;
  }
  
  private Set<String> getAllCurrModuleFiles(List<String> modulePathList) {
    Iterator<String> iterator1 = modulePathList.iterator();
    moduleFileSet = new HashSet<String> ();
    //All files that are there in the search path
    while(iterator1.hasNext())
    {
      String temp =iterator1.next();
      IPath path = new Path(temp).makeAbsolute(); 
      try {
        new FileTraversal() {
          public void onFile( final File f ) {
            if(f.getName().endsWith(".aql"))
            {
              moduleFileSet.add(f.getAbsolutePath().toString());
            }
          }
        }.traverse(new File(path.toOSString()));
      } catch (IOException e) {
        //e.printStackTrace();
      }
    } 
    return moduleFileSet;
  }
  
  //@Override
  public List<AQLElement> getImportedDictionaries(String filePath) {
    List<AQLElement> aqlImportDictionaries = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("IMPORT_DICTIONARY")) {
                  aqlImportDictionaries.add(a);
                }
              }
            }
            //return aqlImportDictionaries;
          }
        }   
      }
    }
    //System.out.println ("No of Imported Dictionaries... "+ aqlImportDictionaries ());
    List<String> importedModules =  getImportModuleStmtModules(filePath);
    // Call a method to get all exportedDictionaries within a module..
    ArrayList<String> moduleFilesList = new ArrayList<String>();
    Set<String> moduleFileSet = null; //new HashSet<String> ();
    for(String importModuleName : importedModules){
      List<String> moduleFilePathList = getModuleSearchPathList (importModuleName);
      moduleFileSet = getAllCurrModuleFiles (moduleFilePathList);
      moduleFilesList.addAll (moduleFileSet);
      aqlImportDictionaries.addAll (getExportedDictionaries (moduleFilesList));
      //System.out.println ("No of Imported Dictionaries... "+ aqlImportDictionaries ());
    }
    //System.out.println ("No of Imported Dictionaries... "+ aqlImportDictionaries ());
    return aqlImportDictionaries;
    //return null;
  }

  //@Override
  public List<AQLElement> getImportedFunctions(String filePath) {
    List<AQLElement> aqlImportFunctions = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("IMPORT_FUNCTION")) {
                  aqlImportFunctions.add(a);
                }
              }
            }
            //return aqlImportFunctions;
          }
        }   
      }
    }
    //System.out.println ("No of Imported Functions... "+ aqlImportFunctions ());
    List<String> importedModules =  getImportModuleStmtModules(filePath);
    // Call a method to get all exportedDictionaries within a module..
    ArrayList<String> moduleFilesList = new ArrayList<String>();
    Set<String> moduleFileSet = null; //new HashSet<String> ();
    for(String importModuleName : importedModules){
      List<String> moduleFilePathList = getModuleSearchPathList (importModuleName);
      moduleFileSet = getAllCurrModuleFiles (moduleFilePathList);
      moduleFilesList.addAll (moduleFileSet);
      aqlImportFunctions.addAll (getExportedFunctions (moduleFilesList));
      //System.out.println ("No of Imported Functions... "+ aqlImportFunctions ());
    }
    //System.out.println ("No of Imported Functions... "+ aqlImportFunctions ());
    return aqlImportFunctions;
    //return null;
  }

  //@Override
  public List<AQLElement> getImportedTables(String filePath) {
    List<AQLElement> aqlImportTables = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("IMPORT_TABLE")) {
                  aqlImportTables.add(a);
                }
              }
            }
            //return aqlImportTables;
          }
        }   
      }
    }
    //System.out.println ("No of Imported Tables... "+ aqlImportTables ());
    List<String> importedModules =  getImportModuleStmtModules(filePath);
    // Call a method to get all exportedDictionaries within a module..
    ArrayList<String> moduleFilesList = new ArrayList<String>();
    Set<String> moduleFileSet = null; //new HashSet<String> ();
    for(String importModuleName : importedModules){
      List<String> moduleFilePathList = getModuleSearchPathList (importModuleName);
      moduleFileSet = getAllCurrModuleFiles (moduleFilePathList);
      moduleFilesList.addAll (moduleFileSet);
      aqlImportTables.addAll (getExportedTables (moduleFilesList));
      //System.out.println ("No of Imported Tables... "+ aqlImportTables ());
    }
    //System.out.println ("No of Imported Tables... "+ aqlImportTables ());
    return aqlImportTables;
    //return null;
  }

  //@Override
  public List<AQLElement> getExportedFunctions(String filePath) {
    List<AQLElement> aqlExportFunctions = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("EXPORT_FUNCTION")) {
                  aqlExportFunctions.add(a);
                }
              }
            }
            return aqlExportFunctions;
          }
        }   
      }
    }
    return null;
  }

  //@Override
  public List<AQLElement> getExportedDictionaries(String filePath) {
    List<AQLElement> aqlExportDictionaries = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("EXPORT_DICTIONARY")) {
                  aqlExportDictionaries.add(a);
                }
              }
            }
            return aqlExportDictionaries;
          }
        }   
      }
    }
    return null;
  }

  //@Override
  public List<AQLElement> getExportedTables(String filePath) {
    List<AQLElement> aqlExportTables = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("EXPORT_TABLE")) {
                  aqlExportTables.add(a);
                }
              }
            }
            return aqlExportTables;
          }
        }   
      }
    }
    return null;
  }

  //@Override
  public List<AQLElement> getExportedViews(String filePath) {
    List<AQLElement> aqlExportViews = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("EXPORT_VIEW")) {
                  aqlExportViews.add(a);
                }
              }
            }
            return aqlExportViews;
          }
        }   
      }
    }
    return null;
  }

  @Override
  public List<AQLElement> getElements(String filePath) {
    List<AQLElement> aqlElements;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);
      HashMap<String, AQLModule> modules = aqlProject.getAQLModules();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);
        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file
            aqlElements = aqlFile.get(i).getAQLElements();
            return aqlElements;
          }
        }  
      }
    }
    return null;
  }

  @Override
  public List<AQLElement> getViews(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getViews(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  @Override
  public List<AQLElement> getTables(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getTables(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  @Override
  public List<AQLElement> getDictionaries(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getDictionaries(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  @Override
  public List<AQLElement> getSelects(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getSelects(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  @Override
  public List<AQLElement> getFunctions(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getFunctions(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  @Override
  public List<AQLElement> getExternalViews(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getExternalViews(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  @Override
  public List<AQLElement> getIncludedFiles(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getIncludedFiles(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  public List<AQLElement> getModules(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getModules(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }
  
  public List<String> getImportedModules(List<String> searchPath) {

    List<String> allElements = new ArrayList<String>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<String> elements = getImportedModules(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }
/*
  public List<AQLElement> getImportedModules(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getImportedModules(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }
*/
  public List<AQLElement> getImportedViews(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getImportedViews(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  public List<AQLElement> getImportedDictionaries(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getImportedDictionaries(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  public List<AQLElement> getImportedFunctions(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getImportedFunctions(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  public List<AQLElement> getImportedTables(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getImportedTables(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  public List<AQLElement> getExportedFunctions(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getExportedFunctions(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  public List<AQLElement> getExportedDictionaries(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getExportedDictionaries(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  public List<AQLElement> getExportedTables(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getExportedTables(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  public List<AQLElement> getExportedViews(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getExportedViews(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  @Override
  public List<AQLElement> getElements(List<String> searchPath) {

    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getElements(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  @Override
  public AQLElement getElement(List<String> searchPath, String elementName) {
    List<AQLElement> allElements = getElements(searchPath);
    Iterator<AQLElement> iterator1 = allElements.iterator();
    while(iterator1.hasNext())
    {
      AQLElement temp =iterator1.next();
      if(temp.name.equals(elementName))
      {
        return temp;
      }
    }
    return null;
  }

  @Override
  public AQLElement getElement(String filePath, String elementName) {
    List<AQLElement> allElements = getElements(filePath);
    Iterator<AQLElement> iterator1 = allElements.iterator();
    while(iterator1.hasNext())
    {
      AQLElement temp =iterator1.next();
      if(temp.name.equals(elementName))
      {
        return temp;
      }
    }
    return null;
  }

  @Override
  public List<String> getElementsThatThisElementDependsOn(String element, String filePath) {
    List<AQLElement> allElements = getElements(filePath);
    Iterator<AQLElement> iterator1 = allElements.iterator();
    while(iterator1.hasNext())
    {
      AQLElement temp =iterator1.next();
      if(temp.name.equals(element))
      {
        return temp.dependsOnElement;
      }
    }
    return null;
  }

  @Override
  /**
   * elementName is expected to be of the format <modulename>.<viewname>
   */
  public List<String> getElementsThatThisElementDependsOn(String elementName,List<String> searchPath) {
    String[] elementNameParts = elementName.split ("\\."); //$NON-NLS-1$
    if (elementNameParts.length != 2) {
      return null;
    }
    List<AQLElement> allElements = getElements(searchPath);
    Iterator<AQLElement> iterator1 = allElements.iterator();
    while(iterator1.hasNext())
    {
      AQLElement temp =iterator1.next();
      if(temp.getModuleName ().equals (elementNameParts[0]) && temp.name.equals(elementNameParts[1]))
      {
        return temp.dependsOnElement;
      }
    }
    return null; //PersonPhone - Person & Phone
  }

  @Override
  /**
   * elementName is expected to be of the format <modulename>.<viewname>
   */
  public List<String> getElementsThatDependOnThis(String elementName,List<String> searchPath) {
    String[] elementNameParts = elementName.split ("\\."); //$NON-NLS-1$
    if (elementNameParts.length != 2) {
      return null;
    } //No use for split here. Having it for consistency.
    List<AQLElement> allElements = getElements(searchPath);
    Iterator<AQLElement> iterator1 = allElements.iterator();
    List<String> parents = new ArrayList<String>();
    while(iterator1.hasNext())
    {
      AQLElement temp =iterator1.next();
      ArrayList<String> childs= temp.dependsOnElement;
      if(childs == null)
        continue;
      Iterator<String> iterator2 = childs.iterator();
      String modName = temp.getModuleName ();
      while(iterator2.hasNext())
      {
        String child =iterator2.next();
        if (!child.contains(".")) {
          child = modName + "." + child;
        }
        if(child.equals(elementName))
        {
          parents.add(modName+"."+temp.getName());
          break;
        }
      }
    }
    return parents; // Return PersonPhoneFinal, Final2, Final3 - Give me what depends on PersonPhone
  }

  @Override
  public HashMap<String, AQLModuleProject> getModuleLibraryMap ()
  {
    return library;
  }

  @Override
  public List<AQLElement> getRequireDocSchema (String filePath)
  {
    List<AQLElement> aqlReqDocs = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("REQUIRE_DOCUMENT")) {
                  aqlReqDocs.add(a);
                }
              }
            }
            return aqlReqDocs;
          }
        }   
      }
    }
    return null;
  }

  @Override
  public List<AQLElement> getExternalTables (String filePath)
  {
    List<AQLElement> aqlTables = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("EXTERNAL_TABLE")) {
                  aqlTables.add(a);
                }
              }
            }
            return aqlTables;
          }
        }   
      }
    }
    return null;
  }

  @Override
  public List<AQLElement> getExternalDictionaries (String filePath)
  {
    List<AQLElement> aqlExtDicts = new ArrayList<AQLElement>();
    List<AQLElement> aqlElements = null;
    String projectName;
    String moduleName;
    Iterator<String> it = library.keySet().iterator();
    while (it.hasNext()) {
      projectName = it.next();
      AQLModuleProject aqlProject = library.get(projectName);

      HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);

        List<AQLFile> aqlFile = aqlModule.getAQLFiles();
        for (int i = 0; i < aqlFile.size(); i++) {
          if (aqlFile.get(i).filePath.equals(filePath)) {
            // got the file, not get elements for this file and filter list
            //to views and return
            aqlElements = aqlFile.get(i).getAQLElements();
            if(aqlElements !=null)
            {
              for (int j = 0; j < aqlElements.size(); j++) {
                AQLElement a = aqlElements.get(j);
                if (a.type.equals("EXTERNAL_DICTIONARY")) {
                  aqlExtDicts.add(a);
                }
              }
            }
            return aqlExtDicts;
          }
        }   
      }
    }
    return null;

  }

  @Override
  public List<AQLElement> getRequireDocSchema (List<String> searchPath)
  {
    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getRequireDocSchema(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  @Override
  public List<AQLElement> getExternalTables (List<String> searchPath)
  {
    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getExternalTables(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  @Override
  public List<AQLElement> getExternalDictionaries (List<String> searchPath)
  {
    List<AQLElement> allElements = new ArrayList<AQLElement>();
    for (int i = 0; i < searchPath.size(); i++) {
      String path = searchPath.get(i);
      List<AQLElement> elements = getExternalDictionaries(path);
      if(elements != null)
        allElements.addAll(elements);
    }
    return allElements;
  }

  @Override
  public Object getAQLModel ()
  {
    return this.aqlModel;
  }
  
  /**
   * Returns the allow_empty value for ExternalDictionary or ExternalTable element.
   * This method exists because ExternalDictionary and ExternalTable classes can 
   * not be accessed outside their packages. (Will uncomment this on delivering 21205)
   * @param elem Should ideally be an ExternalDictionary or ExternalTable object.
   * @return The allow_empty value for ExternalDictionary and ExternalTable objects. False otherwise.
   */
  public boolean isRequiredExternalTableOrDictionary(AQLElement elem) {
    if (elem instanceof ExternalDictionary) {
      return ((ExternalDictionary)elem).isRequired ();
    } else if (elem instanceof ExternalTable) {
      return ((ExternalTable)elem).isRequired();
    } else {
      return false;
    }
  }
}

class FileTraversal {
  public final void traverse( final File f ) throws IOException {
    if (f.isDirectory()) {
      onDirectory(f);
      final File[] childs = f.listFiles();
      for( File child : childs ) {
        traverse(child);
      }
      return;
    }
    onFile(f);            
  }

  public void onDirectory( final File d ) {
  }

  public void onFile( final File f ) {
  }
}

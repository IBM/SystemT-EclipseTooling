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

package com.ibm.biginsights.textanalytics.refactor.core;

import org.eclipse.osgi.util.NLS;

public class AQLElementRenameCoreTexts extends NLS {


  
  private static final String BUNDLE_NAME 
    = "com.ibm.biginsights.textanalytics.refactor.core.aqlelerenamecoretexts"; //$NON-NLS-1$
  
  static {
    NLS.initializeMessages( BUNDLE_NAME, AQLElementRenameCoreTexts.class );
  }

  // message fields
  public static String AQLElementRenameProcessor_name;
  public static String AQLElementRenameDelegate_noSourceFile;
  public static String AQLElementRenameDelegate_roFile;
  public static String AQLElementRenameDelegate_noAQLElement;
  public static String AQLElementRenameDelegate_collectingChanges;
  public static String AQLElementRenameDelegate_checking;
  public static String AQLElementRenameDelegate_eleNotFound;
  public static String AQLElementRenameDelegate_notValidRefactorCandidate;
  public static String AQLModuleRename_provananceFolder;
  public static String ToolongWorkpaceBeingIndexed;
  public static String AQLModuleRename_notValidFolderName;
  public static String AQLScriptRename_notValidScriptName;
  public static String AQLModuleSourceFolder_RenameNotAllowed;
  public static String AQLModuleBinFolder_RenameNotAllowed;
  public static String AQLModuleTextAnalyticsFolder_RenameNotAllowed;
  public static String AQLProjectUpdateReferences;
  public static String UpdateLaunchConfigurationProjectName;
  public static String UpdateLaunchConfigurationModuleName;
  public static String LaunchConfigurationNoLongerExists;
  public static String LaunchConfigurationNoLongerForProject;
  public static String AQLElementNameCanNotBeKeyword;
  public static String AQLFileNameCannotBeKeyword;
  public static String AQLModuleNameCanNotBeKeyword;
  public static String AQLUpdateProjectRefsWithoutPropertyFile;
  public static String AQLElementRefactoringNotSupportedForNonModularProjects;
  
}

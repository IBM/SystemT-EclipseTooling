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
package com.ibm.biginsights.textanalytics.nature.prefs;

import java.io.Serializable;

import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;

/**
 * Data object that holds SystemT project properties
 * 
 * 
 * 
 */
public class SystemTProperties implements Cloneable, Serializable
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

	private static final long serialVersionUID = 5553506063449648320L;

	protected String projectName;
	protected boolean enableProvenance;

	protected String mainAQLFile;

	protected String searchPath;
	
	protected boolean isModularProject;
	protected String moduleSrcPath;
	protected String moduleBinPath;
	protected String tamPath;
	protected String dependentProject;
	
	protected int lwTokenizerChoice;
	protected String lwConfigFile;
	protected String lwDataPath;
	
	protected boolean paginationEnabled;
	protected int numFilesPerPage;


	public SystemTProperties() {
		super();
	}

	public SystemTProperties(SystemTProperties props) {
		this.projectName = props.projectName;
		this.enableProvenance = props.enableProvenance;
		this.mainAQLFile = props.mainAQLFile;
		this.searchPath = props.searchPath;
		this.isModularProject = props.isModularProject;
		this.moduleSrcPath = props.moduleSrcPath;
		this.moduleBinPath = props.moduleBinPath;
		this.tamPath = props.tamPath;
		this.dependentProject = props.dependentProject;
		this.lwConfigFile = props.lwConfigFile;
		this.lwDataPath = props.lwDataPath;
		this.lwTokenizerChoice = props.lwTokenizerChoice;
		this.paginationEnabled = props.paginationEnabled;
		this.numFilesPerPage = props.numFilesPerPage;
	}

	public SystemTProperties(String projectName, boolean isModularProject,
			String moduleSrcPath, String moduleBinPath,
			boolean enableProvenance, String mainAQLFile, String searchPath,
			String tamPath, String dependentProject, int lwTokenizerChoice, String lwConfigFile, String lwDataPath, boolean paginationEnabled, int numFilesPerPage) {
		super();
		this.projectName = projectName;
		this.enableProvenance = enableProvenance;
		this.mainAQLFile = mainAQLFile;
		this.searchPath = searchPath;
		this.isModularProject = isModularProject;
		this.moduleSrcPath = moduleSrcPath;
		this.moduleBinPath = moduleBinPath;
		this.tamPath = tamPath;
		this.dependentProject = dependentProject;
		this.lwConfigFile = lwConfigFile;
		this.lwDataPath = lwDataPath;
		this.lwTokenizerChoice = lwTokenizerChoice;
		this.paginationEnabled = paginationEnabled;
		this.numFilesPerPage = numFilesPerPage;
	}

	
	public int getLwTokenizerChoice() {
		return lwTokenizerChoice;
	}

	public void setLwTokenizerChoice(int lwTokenizerChoice) {
		this.lwTokenizerChoice = lwTokenizerChoice;
	}

	public String getLwConfigFile() {
		return lwConfigFile;
	}

	public void setLwConfigFile(String lwConfigFile) {
		this.lwConfigFile = lwConfigFile;
	}

	public String getLwDataPath() {
		return lwDataPath;
	}

	public void setLwDataPath(String lwDataPath) {
		this.lwDataPath = lwDataPath;
	}

	public String getTamPath() {
		return tamPath;
	}

	public void setTamPath(String tamPath) {
		this.tamPath = tamPath;
	}

	public boolean isModularProject() {
		return isModularProject;
	}

	public void setModularProject(boolean isModularProject) {
		this.isModularProject = isModularProject;
	}

	/**
	 * Returns the value for property that determines where modules' compiled tams are placed.
	 * @return this path would be relative to the project it is in.
	 */
	public String getModuleBinPath() {
		return moduleBinPath;
	}

	/**
	 * Sets the value for property that determines where modules' compiled tams are placed.
	 * @param moduleBinPath Should be a path relative to the project.
	 */
	public void setModuleBinPath(String moduleBinPath) {
		this.moduleBinPath = moduleBinPath;
	}

	/**
   * Returns the value for text analytics source path property.
   * @return this path would be relative to the project it is in.
   */
	public String getModuleSrcPath() {
		return moduleSrcPath;
	}

	/**
   * Sets the value for text analytics source path property.
   * @param moduleSrcPath Should be a path relative to the project.
   */
	public void setModuleSrcPath(String moduleSrcPath) {
		this.moduleSrcPath = moduleSrcPath;
	}

	public boolean getEnableProvenance() {
		return enableProvenance;
	}

	public void setProvenance(boolean enabledProvenance) {
		this.enableProvenance = enabledProvenance;
	}

	public String getMainAQLFile() {
		return mainAQLFile;
	}

	public void setMainAQLFile(String mainAQLFile) {
		this.mainAQLFile = mainAQLFile;
	}

	public String getSearchPath() {
		return searchPath;
	}

	public void setSearchPath(String searchPath) {
		this.searchPath = searchPath;
	}

	public String getDependentProject() {
		return dependentProject;
	}

	public void setDependentProject(String dependentProject) {
		this.dependentProject = dependentProject;
	}
	
	public boolean isPaginationEnabled() {
		return paginationEnabled;
	}
	
	public void setPaginationEnabled(boolean paginationEnabled) {
		this.paginationEnabled = paginationEnabled;
	}
	
	public int getNumFilesPerPage() {
		return numFilesPerPage;
	}
	
	public void setNumFilesPerPage(int numFilesPerPage) {
		this.numFilesPerPage = numFilesPerPage;
	}

	public String getAogPath() {
		return ProjectPreferencesUtil.getDefaultAOGPath(ProjectPreferencesUtil
				.getProject(getProjectName()));
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getResultDir() {
		return ProjectPreferencesUtil
				.getDefaultResultDir(ProjectPreferencesUtil
						.getProject(projectName));
	}

	public void removeWorkspacePrefix() {
		setMainAQLFile(ProjectPreferencesUtil.getPath(getMainAQLFile()));
		setSearchPath(ProjectPreferencesUtil.getPath(getSearchPath()));
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof SystemTProperties)) {
			return false;
		}

		SystemTProperties arg = (SystemTProperties) obj;
		
		boolean equal = isEqual(arg.projectName, projectName)
				&& isEqual(arg.mainAQLFile, mainAQLFile)
				&& isEqual(arg.searchPath, searchPath)
				&& isEqual(arg.moduleSrcPath, moduleSrcPath)
				&& isEqual(arg.moduleBinPath, moduleBinPath)
				&& isEqual(arg.tamPath, tamPath)
				&& isEqual(arg.dependentProject, dependentProject)
				&& isEqual(arg.lwConfigFile,lwConfigFile )
				&& isEqual(arg.lwDataPath,lwDataPath )
				&& arg.lwTokenizerChoice == lwTokenizerChoice 
				&& arg.enableProvenance == enableProvenance
				&& arg.isModularProject == isModularProject
				&& arg.paginationEnabled == paginationEnabled
				&& arg.numFilesPerPage == numFilesPerPage;

		return equal;
	}

	private boolean isEqual(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		}

		if ((o1 == null) && (o2 != null)) {
			return false;
		}

		if ((o2 == null) && (o1 != null)) {
			return false;
		}

		if (!o1.getClass().equals(o2.getClass())) {
			return false;
		}

		return o1.equals(o2);
	}

}

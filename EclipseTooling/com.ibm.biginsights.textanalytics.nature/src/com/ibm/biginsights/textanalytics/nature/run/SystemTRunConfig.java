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
package com.ibm.biginsights.textanalytics.nature.run;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.FileUtils;

public class SystemTRunConfig extends SystemTProperties{

	@SuppressWarnings("unused")


	private static final long serialVersionUID = -7064846963617101732L;
	
	protected String lang;
	protected String inputCollection;
	protected String csvDelimiterString;
	protected List<String> filesToIgnore;
	
	protected String selectedModules;
	//subin
	protected Map<String,String> extDictionaryMapping;
	protected Map<String,String> extTableMapping;

	public SystemTRunConfig(SystemTProperties properties){
		this(null, null, null, properties);
	}
	
	public SystemTRunConfig(String lang, String inputCollection, String csvDelimiterString,
			SystemTProperties systemTProperties) {
		this(lang, inputCollection, csvDelimiterString, systemTProperties.getLwTokenizerChoice (),
			   systemTProperties.getLwConfigFile (), systemTProperties.getLwDataPath (),
			   null, new HashMap<String, String>(), new HashMap<String, String>(),
			   systemTProperties);
	}

	public SystemTRunConfig(String lang, String inputCollection, String csvDelimiterString,
    int tokenizerChoice, String lwConfigFile, String lwDataPath,String selectedModules,
    Map <String,String> extDictFileMapping, Map<String,String> extTableFileMapping,
    SystemTProperties systemTProperties) {
	  super(systemTProperties);
    this.selectedModules = selectedModules;
    this.lang = lang;
    this.inputCollection = inputCollection;
    this.csvDelimiterString = csvDelimiterString;
    extDictionaryMapping = extDictFileMapping;
    extTableMapping = extTableFileMapping;
    
    setTokenizerChoice(tokenizerChoice, lwConfigFile, lwDataPath);
    
    this.filesToIgnore = new LinkedList<String>();
	}
	
	public SystemTRunConfig(String lang, String inputCollection, String csvDelimiterString,
			SystemTProperties systemTProperties, List<String> filesToIgnore) {
		this(lang, inputCollection, csvDelimiterString, systemTProperties);
		this.setFilesToIgnore(filesToIgnore);
	}
	
	
	
	public String getSelectedModules() {
		return selectedModules;
	}

	public void setSelectedModules(String selectedModules) {
		this.selectedModules = selectedModules;
	}

	public String getInputCollection() {
		return inputCollection;
	}
	public void setInputCollection(String inputCollection) {
		this.inputCollection = inputCollection;
	}
	
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
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
	
	public int getTokenizerChoice() {
		return lwTokenizerChoice;
	}

	public void setTokenizerChoice(int tokenizerChoice, String lwConfigFile, String lwDataPath) {
		this.lwTokenizerChoice = tokenizerChoice;
		
		switch(tokenizerChoice){
		case Constants.TOKENIZER_CHOICE_WHITESPACE:
			this.lwConfigFile = null;
			this.lwDataPath = null;
			break;
		}
	}
	
	public void setFilesToIgnore(List<String> filesToIgnore) {
		this.filesToIgnore = filesToIgnore;
	}

	public List<String> getFilesToIgnore() {
		return filesToIgnore;
	}
	
	public Map<String,String> getExternalDictionariesFileMapping() {
	  return extDictionaryMapping;
	}
	
	public Map<String,String> getExternalTablesFileMapping() {
	  return extTableMapping;
	}

	public char getCsvDelimiterChar ()
	{
	  return FileUtils.getCsvDelimiterChar (csvDelimiterString);
	}

	@Override
	public void removeWorkspacePrefix(){
		super.removeWorkspacePrefix();
		setLwConfigFile(ProjectPreferencesUtil.getPath(getLwConfigFile()));
		setLwDataPath(ProjectPreferencesUtil.getPath(getLwDataPath()));
	}

}

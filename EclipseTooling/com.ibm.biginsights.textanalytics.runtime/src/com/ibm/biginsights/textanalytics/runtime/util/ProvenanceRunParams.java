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
package com.ibm.biginsights.textanalytics.runtime.util;

import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.ExternalTypeInfo;

/**
 * Container for various parameters needed to compute provenance.
 * 
 * 
 * 
 */
public class ProvenanceRunParams {

  // Path to folder containing the provenance AOG
  private String aogPath;

  // Language for processing documents
  private LangCode language;

  ExternalTypeInfo extTypeInfo;

  // Tokenizer configuration
  private TokenizerConfig tokenizerConfig;

  private boolean isProvenanceEnabled;

  String[] extViewNames = null;
  private String jsonDocumentLocation = null;

  //Modules selected by the launch configuration
  private String[] selectedModules;
  
  public ProvenanceRunParams(String[] selectedModules, String aogPath, LangCode lang, ExternalTypeInfo extTypeInfo, TokenizerConfig tokenizerConfig,
      boolean isProvenanceEnabled) {

    this.selectedModules = selectedModules;
    this.setAogPath(aogPath);
    this.setLanguage(lang);
    this.setExtTypeInfo (extTypeInfo);
    this.setTokenizerConfig(tokenizerConfig);
    this.setProvenanceEnabled(isProvenanceEnabled);
  }

  public ProvenanceRunParams(ProvenanceRunParams baseParams, String jsonDocLoc) {

    this(baseParams.selectedModules, baseParams.getAogPath (), baseParams.getLanguage (), baseParams.getExtTypeInfo (),
         baseParams.getTokenizerConfig (), baseParams.isProvenanceEnabled ());
    this.setJsonDocumentLocation (jsonDocLoc);
  }
  
  /**
   * Returns the selected Modules in Launch Configuration
   * @return
   */
  public String[] getSelectedModules ()
  {
    return this.selectedModules;
  }

  public void setAogPath(String aogPath) {
    this.aogPath = aogPath;
  }

  public String getAogPath() {
    return this.aogPath;
  }

  public void setLanguage(LangCode language) {
    this.language = language;
  }

  public LangCode getLanguage() {
    return this.language;
  }

  public ExternalTypeInfo getExtTypeInfo ()
  {
    return extTypeInfo;
  }

  public void setExtTypeInfo (ExternalTypeInfo extTypeInfo)
  {
    this.extTypeInfo = extTypeInfo;
  }

  public void setTokenizerConfig(TokenizerConfig tokenizerConfig) {
    this.tokenizerConfig = tokenizerConfig;
  }

  public TokenizerConfig getTokenizerConfig() {
    return this.tokenizerConfig;
  }

  public void setProvenanceEnabled(boolean isProvenanceEnabled) {
    this.isProvenanceEnabled = isProvenanceEnabled;
  }

  public boolean isProvenanceEnabled() {
    return this.isProvenanceEnabled;
  }

  public String getJsonDocumentLocation ()
  {
    return jsonDocumentLocation;
  }

  public void setJsonDocumentLocation (String jsonDocumentLocation)
  {
    this.jsonDocumentLocation = jsonDocumentLocation;
  }

  public String[] getExtViewNames ()
  {
    return extViewNames;
  }

  public void setExtViewNames (String[] extViewNames)
  {
    this.extViewNames = extViewNames;
  }

}

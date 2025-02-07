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
package com.ibm.biginsights.textanalytics.goldstandard.util;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.avatar.algebra.util.tokenize.BaseOffsetsList;
import com.ibm.avatar.algebra.util.tokenize.Tokenizer;
import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.model.Span;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Detects the current word selection. If 
 *  Krishnamurthy
 *
 */
public class WordDetector {


	
	/**
	 * Tokenizer configuration
	 */
	private Tokenizer lwTok  = null;
	
	private IFile gsFile;
	private IFolder gsFolder;
	private String PARAM_DETECT_WORD_BOUNDARIES;
	private String PARAM_LANGUAGE;
	
	private String inputText;
	
	private boolean autoDetectWordboundaries;
	
	/**
	 * Offset list for a given gsFile is stored in offsetList attribute. 
	 * Always use getTokens() method to retrieve offsetList. 
	 * It provides guard against null, as well as returns cached entry
	 */
	private BaseOffsetsList offsetList;
	
	/**
	 * Maps gsFileName with its WordDetector instance
	 */
	private static HashMap<String, WordDetector> wordDetectorMap = new HashMap<String, WordDetector>();
	
	/**
	 * @param gsFile
	 * @param inputText
	 */
	private WordDetector(IFile gsFile, String inputText) {
		super();
		this.gsFile = gsFile;
		this.inputText = inputText;
		
		initTokenizer();
		initParams();
	}

	private void initParams() {
		gsFolder = GoldStandardUtil.getGoldStandardFolder(gsFile);
		PARAM_DETECT_WORD_BOUNDARIES = Constants.GS_DETECT_WORD_BOUNDARIES; 
		PARAM_LANGUAGE = Constants.GS_LANGUAGE;
	}

	public static WordDetector getInstance(IFile gsFile, String inputText){
		WordDetector instance = wordDetectorMap.get(gsFile.getFullPath().toString());
		if(instance == null){
			instance = new WordDetector(gsFile, inputText);
			wordDetectorMap.put(gsFile.getFullPath().toString(), instance);
		}
		return instance;
	}
	
	private void initTokenizer() {
		try {
			TokenizerConfig.Standard standard = new TokenizerConfig.Standard();
			lwTok = standard.makeTokenizer();
		} catch (Exception e) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logWarning("Error instantiating Standard tokenizer.");		
		}
	}

	private BaseOffsetsList getTokens(){
		if(offsetList == null){
			PreferenceStore prefStore = GoldStandardUtil.getGSPrefStore(gsFolder);
			LangCode langCode = LangCode.strToLangCode(prefStore.getString(PARAM_LANGUAGE));
			offsetList = new BaseOffsetsList();
			lwTok.tokenizeStr(inputText, langCode, offsetList);
		}
		return offsetList;
	}
	
	public Span getCurrentSpan(){
		PreferenceStore prefStore = GoldStandardUtil.getGSPrefStore(gsFolder);
		if(prefStore.contains(PARAM_DETECT_WORD_BOUNDARIES)){
			autoDetectWordboundaries = prefStore.getBoolean(PARAM_DETECT_WORD_BOUNDARIES);
		}else{
			//it is not set yet, so, default it to true
			autoDetectWordboundaries = true;
		}
		
		ISelectionProvider selectionProvider = GoldStandardUtil.getGSEditor().getSelectionProvider();
		ISelection selection = selectionProvider.getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection)selection;
			String selectedText = textSelection.getText();
			if(StringUtils.isEmpty(selectedText)){//no span of text is selected; the cursor is merely placed at some location
				if(!autoDetectWordboundaries){
					//auto-detect word boundaries is not set. Also, selectedText is empty. So, return null Span
					return null;
				}else{
					int offset = textSelection.getOffset();
					BaseOffsetsList tokens = getTokens();
					int index = tokens.nextBeginIx(offset);
					int startIndex = tokens.begin(index);
					int endIndex = tokens.end(index);
					String text = inputText.substring(startIndex, endIndex);
					return new Span(startIndex, endIndex, text);
				}
			}else{
				/*some span of text is selected. However, if autoDetect word boundaries is selected, 
				 * we must expand to proper begin and end of words. Refer to defect 16149.
				 */
				if(autoDetectWordboundaries){
					
					int selectionStart = textSelection.getOffset();
					int selectionEnd = selectionStart + textSelection.getLength();
					BaseOffsetsList tokens = getTokens();
					
					int selectionStartTokenIdx = tokens.nextBeginIx(selectionStart);
					int selectionEndTokenIdx = tokens.prevBeginIx(selectionEnd);
					
					int startIndex = tokens.begin(selectionStartTokenIdx);
					int endIndex = tokens.end(selectionEndTokenIdx);
					String text = inputText.substring(startIndex, endIndex);
					return new Span(startIndex, endIndex, text);
					
				}else{//just return what ever is selected.
					int spanBegin = textSelection.getOffset();
					int spanEnd = spanBegin + textSelection.getLength();
					return new Span(spanBegin, spanEnd, selectedText);
				}
			}
		}
		return null;
	}
	

}

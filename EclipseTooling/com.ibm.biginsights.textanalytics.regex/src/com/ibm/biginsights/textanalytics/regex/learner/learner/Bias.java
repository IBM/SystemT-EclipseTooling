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
package com.ibm.biginsights.textanalytics.regex.learner.learner;

public class Bias {



  // bias information for all learners
  private int summarizer_minNoOfSamples = 1;

  private boolean digitsNeverAlternation = true;

  private boolean convertToCharClassOnly = false;

  // bias information for RIGACommonSubstrings
  private int minCommonSubstringLength = 3;

  private boolean useDigitCommonSubstrings = true;

  private boolean caseSensitiveCommonSubstrings = true;

  // bias information for RIGASummarizeFirst
  private boolean caseSensitiveSummarizing = false;

  public void setSummarizer_minNoOfSamples(int summarizer_minNoOfSamples) {
    this.summarizer_minNoOfSamples = summarizer_minNoOfSamples;
  }

  public int getSummarizer_minNoOfSamples() {
    return this.summarizer_minNoOfSamples;
  }

  public void setMinCommonSubstringLength(int minCommonSubstringLength) {
    this.minCommonSubstringLength = minCommonSubstringLength;
  }

  public int getMinCommonSubstringLength() {
    return this.minCommonSubstringLength;
  }

  public void setUseDigitCommonSubstrings(boolean useDigitCommonSubstrings) {
    this.useDigitCommonSubstrings = useDigitCommonSubstrings;
  }

  public boolean isUseDigitCommonSubstrings() {
    return this.useDigitCommonSubstrings;
  }

  public void setCaseSensitiveSummarizing(boolean caseSensitiveSummarizing) {
    this.caseSensitiveSummarizing = caseSensitiveSummarizing;
  }

  public boolean isCaseSensitiveSummarizing() {
    return this.caseSensitiveSummarizing;
  }

  public boolean isCaseSensitiveCommonSubstrings() {
    return this.caseSensitiveCommonSubstrings;
  }

  public void setCaseSensitiveCommonSubstrings(boolean caseSensitiveCommonSubstrings) {
    this.caseSensitiveCommonSubstrings = caseSensitiveCommonSubstrings;
  }

  public void setDigitsNeverAlternation(boolean digitsNeverAlternation) {
    this.digitsNeverAlternation = digitsNeverAlternation;
  }

  public boolean isDigitsNeverAlternation() {
    return this.digitsNeverAlternation;
  }

  public void setConvertToCharClassOnly(boolean convertToCharClassOnly) {
    this.convertToCharClassOnly = convertToCharClassOnly;
  }

  public boolean isConvertToCharClassOnly() {
    return this.convertToCharClassOnly;
  }

}

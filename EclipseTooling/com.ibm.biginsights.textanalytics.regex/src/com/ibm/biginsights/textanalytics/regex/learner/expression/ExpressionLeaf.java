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
package com.ibm.biginsights.textanalytics.regex.learner.expression;

import java.util.ArrayList;


/**
 * 
 * 
 *         All leafs of the tree representing a regular expressions (see description of class
 *         'Expression') must be objects of ExpressionLeaf. These objects encapsulate information
 *         about the character class of the leaf, the sample string parts that occurred at this
 *         position as well as information collected during the Refinement / User Interaction
 *         process, such as if the user wants a certain digit range, a predefined character class or
 *         an alternation of the samples list at this position. If both samplesAlternation and
 *         digitRange are set to false, the character class is used when creating the regular
 *         expression.
 */

public class ExpressionLeaf extends Expression implements Cloneable {



  // This is already in Expression, why is it duplicated here?
  /* the type of the Expression */
//  protected String type;

  /**
   * Type may be - any of the character classes - any character - a common Subsequence (CSS)
   */

  /**
   * the minimum and maximum of an integer range --> only applicable if type =
   * CharacterHierarchy.DIGIT
   */
  private boolean digitRange = false;

  private Integer minimumRange = null;

  private Integer maximumRange = null;

  private boolean allowLeadingZeros = false;

  /**
   * used at conversion time: indicates whether the character class type or an alternation of all
   * samples should be used.
   */
  private boolean samplesAlternation = false;

  /* all samples that occurred for this leaf */
  private ArrayList<String> samples = new ArrayList<String>();

  // CONSTRUCTOR
  public ExpressionLeaf(String type) {
    this.type = type;
  }

  // METHODS

  /**
   * @param expr
   *          Expression to compare with this one
   * @return true if Expression types are the same, false otherwise
   */
  @Override
  public boolean contains(Expression expr) {
    if (!(expr instanceof ExpressionLeaf)) {
      return false;
    }
    if (this.type.equals(((ExpressionLeaf) expr).getType())) {
      return true;
    }
    return false;
  }

  /**
   * @param expr
   *          Expression to compare with this one
   * @return true if Expression types are the same, false otherwise
   */
  @Override
  public boolean equals(Object expression) {
    // identity check
    if (this == expression) {
      return true;
    }
    // check correct type
    if (!(expression instanceof ExpressionLeaf)) {
      return false;
    }
    /*
     * cast argument to correct type + compare significant fields type is significant (samples, min,
     * max and optional are not significant)
     */
    if (this.type.equals(((ExpressionLeaf) expression).getType())) {
      return true;
    }
    return false;
  }

  /**
   * not used in implementation so far...
   * 
   * @return hash code of this object
   */
  @Override
  public int hashCode() {
    int result = 17;
    // the only significant field is type
    final int c = this.type.hashCode();
    result = 31 * result + c;
    return result;
  }

  /**
   * @return objects with same characteristics as this object
   */
  @Override
  public ExpressionLeaf clone() {
    final ExpressionLeaf clone = (ExpressionLeaf) super.clone();
    // produce a deep copy of the samples ArrayList<String>
    clone.samples = new ArrayList<String>();
    for (final String sample : this.samples) {
      clone.addSample(new String(sample));
    }
    return clone;
  }

  @Override
  public ExpressionLeaf invert() {
    return this;
  }

  /**
   * @param Expression
   *          expr to be inserted into this ExpressionLeaf object
   * @return true if the types of this ExpressionLeaf object and expr are the same, (maximum,
   *         minimum, samples,...) are adjusted in this case, returns false if types are not the
   *         same or if Expression is not an ExpressionLeaf.
   */
  @Override
  public boolean insert(Expression expr) {
    if (expr instanceof ExpressionLeaf) {
      return insertLeaf((ExpressionLeaf) expr);
    }
    /*
     * an ALTERNATION node cannot be inserted into a leaf a CONCATENATION node cannot be inserted
     * into a leaf
     */
    return false;
  }

  /**
   * inserts the informationLabel of another leaf at this leaf (if possible)
   * 
   * @param leaf
   *          ExpressionLeaf object to insert
   * @return true if insertion was successful, false otherwise
   */
  private boolean insertLeaf(ExpressionLeaf leaf) {
    if (this.type.equals(leaf.type)) {
      this.minimum = Math.min(this.minimum, leaf.minimum);
      this.maximum = Math.max(this.maximum, leaf.maximum);
      this.optional = this.optional || leaf.optional;
      addSamples(leaf.samples);
      return true;
    }
    return false;
  }

  /**
   * This method sorts the samples of the leaf by length in order to produce better matching -->
   * longest first --> Java Regex Engine NFA problem... :-(
   */
  public void sortSamples() {
    boolean swapped;
    do {
      swapped = false;
      for (int i = 0; i <= this.samples.size() - 2; i++) {
        if (this.samples.get(i).length() < this.samples.get(i + 1).length()) {
          final String temp = this.samples.get(i);
          this.samples.set(i, new String(this.samples.get(i + 1)));
          this.samples.set(i + 1, temp);
          swapped = true;
        }
      }
    } while (swapped);
  }

  // GETTER + SETTER METHODS
  public String getSample(int index) {
    return this.samples.get(index);
  }

  public void setSample(int index, String sample) {
    this.samples.set(index, sample);
  }

  public void addSample(String sample) {
    this.samples.add(sample);
  }

  public void addSamples(ArrayList<String> sample) {
    for (final String string : sample) {
      if (!this.samples.contains(string)) {
        this.samples.add(string);
      }
    }
  }

  public ArrayList<String> getSamples() {
    return this.samples;
  }

  // TO STRING - METHODS
  /**
   * @return a string containing the leaf informationLabel
   * */
  @Override
  public String toString() {
    String string = ""; //$NON-NLS-1$
    string += this.type;
    // if (minimum != 1 || maximum != 1) {
    string += "{" + this.minimum + "," + this.maximum + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    // }
    if (this.optional) {
      string += "{opt}"; //$NON-NLS-1$
    }
    return string;
  }

  /**
   * toStringWithSamples()
   * 
   * @return a string containing the leaf informationLabel including samples
   */
  @Override
  public String toStringWithSamples() {
    String string = toString() + "["; //$NON-NLS-1$
    for (final String sample : this.samples) {
      string += sample + " "; //$NON-NLS-1$
    }
    if (!this.samples.isEmpty()) {
      string = string.substring(0, string.length() - 1);
    }
    string += "]"; //$NON-NLS-1$
    if (isSamplesAlternation()) {
      string += "*"; //$NON-NLS-1$
    }
    return string;
  }

  // GETTER + SETTERS
  @Override
  public String getType() {
    return this.type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }

  public Integer getMinimumRange() {
    return this.minimumRange;
  }

  public void setMinimumRange(Integer minimumRange) {
    this.minimumRange = minimumRange;
  }

  public Integer getMaximumRange() {
    return this.maximumRange;
  }

  public void setMaximumRange(Integer maximumRange) {
    this.maximumRange = maximumRange;
  }

  public boolean isSamplesAlternation() {
    return this.samplesAlternation;
  }

  public void setSamplesAlternation(boolean samplesAlternation) {
    this.samplesAlternation = samplesAlternation;
  }

  public boolean isAllowLeadingZeros() {
    return this.allowLeadingZeros;
  }

  public void setAllowLeadingZeros(boolean allowLeadingZeros) {
    this.allowLeadingZeros = allowLeadingZeros;
  }

  public void setDigitRange(boolean digitRange) {
    this.digitRange = digitRange;
  }

  public boolean isDigitRange() {
    return this.digitRange;
  }

}

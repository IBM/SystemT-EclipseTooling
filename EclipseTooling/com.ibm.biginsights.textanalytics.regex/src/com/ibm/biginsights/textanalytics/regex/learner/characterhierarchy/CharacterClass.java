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
package com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 
 *         This class represents a character class such as 'Digit' or 'Letters'. The objects of this
 *         class are instantiated once per character class and then shared through the whole
 *         application.
 * 
 */

public class CharacterClass {



  private final String name;

  private final CharacterClass higher;

  private final Pattern regex;

  private final String regexString;

  private final String explanation;

  private final String description;

  private int level;

  /**
   * can only be instantiated from within this package
   * 
   * @param name
   *          Name of the CharacterClass
   * @param regex
   *          Regular Expression describing the character class (single character)
   * @param higher
   *          Parent node of this class in CharacterHierarchy
   */
  protected CharacterClass(String name, String regex, CharacterClass higher, String explanation,
      String description) {
    this.name = name;
    this.higher = higher;
    this.regexString = regex;
    this.regex = Pattern.compile(regex + "+"); //$NON-NLS-1$
    this.explanation = explanation;
    this.description = description;
    this.level = 0;
    // compute level of this node
    CharacterClass temp = higher;
    while (temp != null) {
      this.level++;
      temp = temp.getHigher();
    }
  }

  public String getRegexString() {
    return this.regexString;
  }

  public String getExplanation() {
    return this.explanation;
  }

  public String getDescription() {
    return this.description;
  }

  public String getName() {
    return this.name;
  }

  /**
   * get CharacterClass at next higher level in hierarchy
   * 
   * @return character class at next higher level in hierarchy
   */
  public CharacterClass getHigher() {
    return this.higher;
  }

  /**
   * This method checks if a string belongs to this character class or not.
   * 
   * @param string
   *          String to be tested
   * @return true if the String to be tested belongs to this character class, false otherwise
   */
  public boolean belongsTo(String string) {
    final Matcher matcher = this.regex.matcher(string);
    if (matcher.matches()) {
      return true;
    }
    return false;
  }

  /**
   * This method checks if a character belongs to this character class or not.
   * 
   * @param c
   *          character to be tested
   * @return true if the character to be tested belongs to this character class, false otherwise
   */
  public boolean belongsTo(char c) {
    return belongsTo(c + ""); //$NON-NLS-1$
  }

  /**
   * @return the level of this class in the character hierarchy
   */
  public int getLevel() {
    return this.level;
  }

  /**
   * @return the CharacterClass object that contains the common supercategory
   */
  public CharacterClass getCommonHigher(CharacterClass charClass) {

    // Character classes are the same
    if (this.name.equals(charClass.getName())) {
      return this;
    }
    // Character classes are on same level --> increase both
    if (this.level == charClass.getLevel()) {
      // this can only be the case if CharacterClass = "C", then both
      // "highers" should be null
      if ((this.higher == null) || (charClass.higher == null)) {
        return this;
      }
      return this.higher.getCommonHigher(charClass.getHigher());
    }
    CharacterClass higherLevel = charClass;
    CharacterClass lowerLevel = this;
    if (this.level < charClass.getLevel()) {
      higherLevel = this;
      lowerLevel = charClass;
    }
    lowerLevel = lowerLevel.getHigher();
    return lowerLevel.getCommonHigher(higherLevel);
  }

  @Override
  public String toString() {
    return "CharacterClass-" + this.name + "-" + this.regexString; //$NON-NLS-1$ //$NON-NLS-2$
  }

}

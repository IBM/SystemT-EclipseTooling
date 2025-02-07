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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.ibm.biginsights.textanalytics.regex.Messages;

/**
 * 
 * 
 * 
 *         This class contains an immutable, single instance of the character class hierarchy. It
 *         provides methods to retrieve the character class of a string or to retrieve a particular
 *         character class object by the name of the character class.
 * 
 */

public class CharacterHierarchy {



  public static final String ANY_CHARACTER = "Character-Class-ID_Any character"; //$NON-NLS-1$

  public static final String ALPHA_NUMERIC = "Character-Class-ID_Alpha-numeric"; //$NON-NLS-1$

  public static final String LETTER = "Character-Class-ID_Letter"; //$NON-NLS-1$

  public static final String LOWER = "Character-Class-ID_Lower-case"; //$NON-NLS-1$

  public static final String UPPER = "Character-Class-ID_Upper-case"; //$NON-NLS-1$

  public static final String DIGIT = "Character-Class-ID_Digit"; //$NON-NLS-1$

  public static final String NON_ALPHA_NUMERIC = "Character-Class-ID_Non-alpha-numeric"; //$NON-NLS-1$

  public static final String WHITESPACE = "Character-Class-ID_Whitespace"; //$NON-NLS-1$

  public static final String OTHER = "Character-Class-ID_Other"; //$NON-NLS-1$

  private static final HashMap<String, CharacterClass> hierarchy;

  private static final ArrayList<String> characterClasses;

  static {

    // create a list of all character class names
    characterClasses = new ArrayList<String>();
    characterClasses.add(ANY_CHARACTER);
    characterClasses.add(ALPHA_NUMERIC);
    characterClasses.add(LETTER);
    characterClasses.add(LOWER);
    characterClasses.add(UPPER);
    characterClasses.add(DIGIT);
    characterClasses.add(NON_ALPHA_NUMERIC);
    characterClasses.add(WHITESPACE);
    characterClasses.add(OTHER);

    // create the character class objects
    final CharacterClass root = new CharacterClass(ANY_CHARACTER, ".", null, Messages.CharacterHierarchy_ANY_SYMBOLS, //$NON-NLS-1$
        Messages.CharacterHierarchy_ANY_CHAR);
    final CharacterClass alpha_numeric = new CharacterClass(ALPHA_NUMERIC, "[\\d\\p{L}]", root, //$NON-NLS-1$
        Messages.CharacterHierarchy_DIGIT_AND_WORD_CHAR, Messages.CharacterHierarchy_ALPHANUMERIC_CHAR);
    final CharacterClass non_alpha_numeric = new CharacterClass(NON_ALPHA_NUMERIC, "[^\\d\\p{L}]", //$NON-NLS-1$
        root, Messages.CharacterHierarchy_NON_ALPHA_NUM_CHAR_MSG,
        Messages.CharacterHierarchy_NON_ALPHA_NUM_CHAR);
    final CharacterClass letter = new CharacterClass(LETTER, "\\p{L}", alpha_numeric, //$NON-NLS-1$
        Messages.CharacterHierarchy_UPPER_OR_LOWER_CHAR, Messages.CharacterHierarchy_LETTERS);
    final CharacterClass lower = new CharacterClass(LOWER, "\\p{Ll}", letter, Messages.CharacterHierarchy_LOWERCASE_LETTERS, //$NON-NLS-1$
        Messages.CharacterHierarchy_LOWERCASE_LETTERS2);
    final CharacterClass upper = new CharacterClass(UPPER, "\\p{Lu}", letter, Messages.CharacterHierarchy_UPPERCASE_LETTERS, //$NON-NLS-1$
        Messages.CharacterHierarchy_UPPERCASE_LETTERS2);
    final CharacterClass digit = new CharacterClass(DIGIT, "\\d", alpha_numeric, Messages.CharacterHierarchy_ANY_DIGITS, //$NON-NLS-1$
        Messages.CharacterHierarchy_DIGITS);
    final CharacterClass whitespace = new CharacterClass(WHITESPACE, "\\s", non_alpha_numeric, //$NON-NLS-1$
        Messages.CharacterHierarchy_WHITESPACE_CHAR, Messages.CharacterHierarchy_WHITESPACE_CHAR_CAPS);
    final CharacterClass other = new CharacterClass(OTHER, "[^\\d\\s\\p{L}]", non_alpha_numeric, //$NON-NLS-1$
        Messages.CharacterHierarchy_NON_ALPHANUMERIC_MSG,
        Messages.CharacterHierarchy_OTHER_CHAR_MSG);

    // create a hash map containing all character classes in order to be
    // able to retrieve them by their names
    hierarchy = new HashMap<String, CharacterClass>();
    hierarchy.put(root.getName(), root);
    hierarchy.put(alpha_numeric.getName(), alpha_numeric);
    hierarchy.put(non_alpha_numeric.getName(), non_alpha_numeric);
    hierarchy.put(letter.getName(), letter);
    hierarchy.put(lower.getName(), lower);
    hierarchy.put(upper.getName(), upper);
    hierarchy.put(digit.getName(), digit);
    hierarchy.put(whitespace.getName(), whitespace);
    hierarchy.put(other.getName(), other);

  }

  // prevent instantiation
  private CharacterHierarchy() {
    super();
  }

  /**
   * Retrieve the character class object a string.
   * 
   * @param string
   *          String whose most specific character class is to be returned
   * @return the most specific character class of the string
   */
  public static CharacterClass getCharacterClass(String string) {
    final Iterator<CharacterClass> iterator = hierarchy.values().iterator();
    CharacterClass lastFound = null;
    while (iterator.hasNext()) {
      final CharacterClass charClass = iterator.next();
      if ((charClass.belongsTo(string))
          && ((lastFound == null) || (lastFound.getLevel() < charClass.getLevel()))) {
        lastFound = charClass;
      }
    }
    return lastFound;
  }

  /**
   * retrieve the character class of a character
   * 
   * @param c
   *          char whose most specific character class is to be returned
   * @return the most specific character class of the char
   */
  public static CharacterClass getCharacterClass(char c) {
    return getCharacterClass(c + ""); //$NON-NLS-1$
  }

  /**
   * check if a string is a name of one of the character classes
   * 
   * @param type
   *          String type of an Expression
   * @return true if type is a predefined character class, false otherwise
   */
  public static boolean isCharacterClass(String type) {
    return characterClasses.contains(type);

  }

  /**
   * This method returns the singleton character class object that is identified by the String
   * provided as a parameter. It returns null if there is no such character class.
   * 
   * @param name
   *          of the character class to be returned
   * @return Character class object having that name, null if there is no such character class
   */
  public static CharacterClass getCharClassByName(String name) {
    return hierarchy.get(name);
  }

  public static String[] getCharClassNames() {
    final String[] charClasses = new String[characterClasses.size()];
    int i = 0;
    for (final String charClass : characterClasses) {
      charClasses[i] = charClass;
      i++;
    }
    return charClasses;
  }

}

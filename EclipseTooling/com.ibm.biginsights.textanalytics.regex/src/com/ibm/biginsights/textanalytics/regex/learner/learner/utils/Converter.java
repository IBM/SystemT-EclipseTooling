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
package com.ibm.biginsights.textanalytics.regex.learner.learner.utils;

import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterClass;
import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterHierarchy;
import com.ibm.biginsights.textanalytics.regex.learner.expression.AlternationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ConcatenationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionLeaf;

/**
 * 
 * 
 *         This class contains methods to convert an expression in the Java representation
 *         (ConcatenationExpression, AlternationExpression and ExpressionLeaf objects) into a
 *         regular expression string.
 */

public class Converter {



  // regular expression meta characters of the Java regex flavour
  // Defect 35588: Add '/' as a meta-character so the generated expression can be used in AQL statement directly.
  public static final char[] REGEX_META_CHARS = { '.', '?', ')', '(', '[', ']', '|', '$', '^', '+', '/', '*' };

  private Converter() {
    // prevent instantiation
  }

  /**
   * This method converts the Expression into a regular expression string.
   * 
   * @param expression
   *          - Expression to be converted
   * @return String representing the corresponding regular expression pattern
   */
  public static String toRegex(Expression expression) {
    if (expression instanceof ConcatenationExpression) {
      return toRegex((ConcatenationExpression) expression);
    }
    if (expression instanceof ExpressionLeaf) {
      return toRegex((ExpressionLeaf) expression);
    }
    if (expression instanceof AlternationExpression) {
      return toRegex((AlternationExpression) expression);
    }
    return null;
  }

  /**
   * This method converts the alternation expression into a regular expression string.
   * 
   * @param expression
   *          - AlternationExpression to be converted
   * @return String representing the corresponding regular expression pattern
   */
  public static String toRegex(AlternationExpression alt) {
    String regex = "("; //$NON-NLS-1$
    String bar = ""; //$NON-NLS-1$
    for (final Expression subexpression : alt.getSubexpressions()) {
      if (subexpression instanceof ExpressionLeaf) {
        regex += bar + toRegex(subexpression);
      } else {
        regex += bar + "(" + toRegex(subexpression) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
      }
      bar = "|"; //$NON-NLS-1$
    }
    regex += ")"; //$NON-NLS-1$
    if ((alt.getMinimum() != 1) || (alt.getMaximum() != 1)) {
      if (alt.getMinimum() == alt.getMaximum()) {
        regex += "{" + alt.getMinimum() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        regex += "{" + alt.getMinimum() + "," + alt.getMaximum() + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    }
    if (alt.isOptional()) {
      regex += "?"; //$NON-NLS-1$
    }
    return regex;
  }

  /**
   * This method converts the concatenation expression into a regular expression string.
   * 
   * @param expression
   *          - ConcatenationExpression to be converted
   * @return String representing the corresponding regular expression pattern
   */
  public static String toRegex(ConcatenationExpression conc) {
    String regex = ""; //$NON-NLS-1$
    for (final Expression subexpression : conc.getSubexpressions()) {
      regex += toRegex(subexpression);
    }
    if (conc.isOptional()) {
      regex += "}"; //$NON-NLS-1$
    }
    return regex;
  }

  /**
   * This method converts the Expression leaf into a regular expression string.
   * 
   * @param leaf
   *          - ExpressionLeaf to be converted
   * @return String representing the corresponding regular expression pattern
   */
  public static String toRegex(ExpressionLeaf leaf) {
    leaf.sortSamples();
    String regex = ""; //$NON-NLS-1$
    if (!leaf.isSamplesAlternation()) {
      if (leaf.getType().equals(CharacterHierarchy.DIGIT)) {
        if (leaf.isDigitRange()) {
          regex = numberRangeToRegex(leaf.getMinimumRange().intValue(), leaf.getMaximumRange()
              .intValue(), leaf.isAllowLeadingZeros());
          if (regex == null) {
            regex = "\\d"; //$NON-NLS-1$
          }
        } else {
          regex = "\\d"; //$NON-NLS-1$
        }
      } else if (CharacterHierarchy.isCharacterClass(leaf.getType())) {
        final CharacterClass charClass = CharacterHierarchy.getCharClassByName(leaf.getType());
        regex = charClass.getRegexString();
      } else {
        escape(leaf);
        if (leaf.getType().contains(" ")) { //$NON-NLS-1$
          leaf.setType(leaf.getType().replaceAll(" ", "\\\\s")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        regex = leaf.getType();
      }
    } else if (CharacterHierarchy.isCharacterClass(leaf.getType())) {
      if (leaf.getSamples().size() > 0) {
        regex = "("; //$NON-NLS-1$
        String bar = ""; //$NON-NLS-1$
        for (String sample : leaf.getSamples()) {
          sample = escape(sample);
          regex += bar + sample;
          bar = "|"; //$NON-NLS-1$
        }
        if (leaf.getSamples().size() > 0) {
          regex += ")"; //$NON-NLS-1$
        }
      }
    } else {
      escape(leaf);
      if (leaf.getType().contains(" ")) { //$NON-NLS-1$
        leaf.setType(leaf.getType().replaceAll(" ", "\\\\s")); //$NON-NLS-1$ //$NON-NLS-2$
      }
      regex = leaf.getType();
    }
    boolean addedOptional = false;
    if (!leaf.isSamplesAlternation() || isCharacterAlternation(leaf)) {
      if (!leaf.isDigitRange()) {
        if ((leaf.getMinimum() != 1) || (leaf.getMaximum() != 1)) {
          addedOptional = true;
          if (leaf.getMinimum() == leaf.getMaximum()) {
            if (leaf.isOptional()) {
              regex = "(" + regex + "{" + leaf.getMinimum() + "})?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
              regex += "{" + leaf.getMinimum() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
            }
          } else {
            if (leaf.isOptional()) {
              regex = "(" + regex; //$NON-NLS-1$
            }
            regex += "{" + leaf.getMinimum() + "," + leaf.getMaximum() + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (leaf.isOptional()) {
              regex += ")?"; //$NON-NLS-1$
            }
          }
        }
      }
    }
    if (leaf.isOptional() && !addedOptional) {
      regex += "?"; //$NON-NLS-1$
    }
    return regex;
  }

  /**
   * Escapes the metacharacters of the type strings of a leaf.
   * 
   * @param leaf
   */
  private static void escape(ExpressionLeaf leaf) {
    for (final char c : REGEX_META_CHARS) {
      // only escape if the metacharacter has not already been escaped
      if (!leaf.getType().contains("\\" + c)) { //$NON-NLS-1$
        if (leaf.getType().contains(c + "")) { //$NON-NLS-1$
          leaf.setType(leaf.getType().replaceAll("\\" + c, "\\\\" + c)); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }
  }

  /**
   * Escapes the metacharacters of a string.
   * 
   * @param text
   *          string in which metacharacters are to be escaped
   * @return the string with escaped metacharacters
   */
  public static String escape(String text) {
    for (final char c : REGEX_META_CHARS) {
      // add backslashes only if the metacharacter has not already been escaped
      if (!text.contains("\\" + c)) { //$NON-NLS-1$
        if (text.contains(c + "")) { //$NON-NLS-1$
          if (c == '$') {
            String text2 = ""; //$NON-NLS-1$
            for (final char c1 : text.toCharArray()) {
              if (c1 == '$') {
                text2 += "\\$"; //$NON-NLS-1$
              } else {
                text2 += escape(c1 + ""); //$NON-NLS-1$
              }
            }
            text = text2;
          } else {
            text = text.replaceAll("\\" + c, "\\\\" + c); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
      }
    }
    return text;
  }

  /**
   * This method checks if all samples of the leaf are single characters.
   * 
   * @param leaf
   *          the samples of this leaf are to be checked
   * @return true if all samples are single characters, false otherwise
   */
  private static boolean isCharacterAlternation(ExpressionLeaf leaf) {
    boolean result = true;
    for (final String s : leaf.getSamples()) {
      if (s.length() > 1) {
        result = false;
        break;
      }
    }
    return result;
  }

  /**
   * This methods produces a string matchin the integer number range from min to max.
   * 
   * @param number
   *          range, int min to int max
   * @return a string matching exactly this integer number range
   */
  private static String numberRangeToRegex(int min, int max, boolean allowLeadingZeros) {
    if (min > max) {
      return null;
    }
    String regex = "("; //$NON-NLS-1$
    // allow leading zeros
    if (allowLeadingZeros) {
      regex = "0*("; //$NON-NLS-1$
    }
    if (min == max) {
      regex += min + ")"; //$NON-NLS-1$
      return regex;
    }
    if ((min < 10) && (max < 10)) {
      regex += "[" + min + "-" + max + "])"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      return regex;
    }
    // number of digits of max
    final int maxDigits = new String(max + "").length() - 1; //$NON-NLS-1$
    // add regex part -> from max to its preceding greatest multiple of ten (that is larger than
    // min)
    for (int i = 0; i < maxDigits; i++) {
      final int maxDiv = max / (int) Math.pow(10, i + 1);
      final int minDiv = min / (int) Math.pow(10, i + 1);
      final int maxDigit = max / (int) Math.pow(10, i) % 10;
      String regexPart = ""; //$NON-NLS-1$
      if (minDiv < maxDiv) {
        String maxDivString = ""; //$NON-NLS-1$
        if (maxDiv > 0) {
          maxDivString += maxDiv;
        }
        int x = 1;
        if (i == 0) {
          x = 0;
        }
        if (maxDigit > 0) {
          regexPart += "(" + maxDivString + "[0-" + (maxDigit - x) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          for (int j = i; j > 0; j--) {
            regexPart += "[0-9]"; //$NON-NLS-1$
          }

        } else {
          regexPart += "(" + maxDivString + "0"; //$NON-NLS-1$ //$NON-NLS-2$
          for (int j = i; j > 0; j--) {
            regexPart += "0"; //$NON-NLS-1$
          }
        }
        regexPart += ")"; //$NON-NLS-1$
        if (regexPart.contains("]") || !regex.contains(regexPart)) { //$NON-NLS-1$
          if ((!allowLeadingZeros && (regex.length() > 2))
              || (allowLeadingZeros && (regex.length() > 5))) {
            regex += "|" + regexPart; //$NON-NLS-1$
          } else {
            regex += regexPart;
          }
        }
      }
    }
    // add 'middle part'
    String regexPart = ""; //$NON-NLS-1$
    // min < max (tested above!)
    for (int i = 1; i <= maxDigits + 1; i++) {
      final int minDiv = min / (int) Math.pow(10, i);
      final int maxDiv = max / (int) Math.pow(10, i);
      final int minDigit = min / (int) Math.pow(10, i - 1) % 10;
      final int maxDigit = max / (int) Math.pow(10, i - 1) % 10;
      if (minDiv == maxDiv) {
        String minDivString = ""; //$NON-NLS-1$
        if (minDiv > 0) {
          minDivString = minDiv + ""; //$NON-NLS-1$
        }
        if (minDigit + 1 < maxDigit - 1) {
          if (i == 1) {
            regexPart = minDivString + "[" + (minDigit) + "-" + (maxDigit) + "]" + regexPart; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          } else {
            regexPart = minDivString + "[" + (minDigit + 1) + "-" + (maxDigit - 1) + "]" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + regexPart;
          }
          regex += "|(" + regexPart + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        } else if ((minDigit < maxDigit) && (i == 1)) {
          regexPart = minDivString + "[" + (minDigit) + "-" + (maxDigit) + "]" + regexPart; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          if (regex.length() > 2) {
            regex += "|(" + regexPart + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          } else {
            regex += "(" + regexPart + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          }
        } else if (minDigit + 1 == maxDigit - 1) {
          regexPart = minDivString + (minDigit + 1) + regexPart;
          if (regex.length() > 2) {
            regex += "|(" + regexPart + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          } else {
            regex += "(" + regexPart + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
        break;
      }
      regexPart += "[0-9]"; //$NON-NLS-1$
    }
    // add regex part --> from min to its successing greatest multiple of ten (that is smaller than
    // max)
    for (int i = maxDigits - 1; i >= 0; i--) {
      final int maxDiv = max / (int) Math.pow(10, i + 1);
      final int minDiv = min / (int) Math.pow(10, i + 1);
      final int minDigit = min / (int) Math.pow(10, i) % 10;
      regexPart = ""; //$NON-NLS-1$
      if (minDiv < maxDiv) {
        int x = 0;
        if (i > 0) {
          x = 1;
        }
        String minDivString = ""; //$NON-NLS-1$
        if (minDiv > 0) {
          minDivString += minDiv;
        }
        if (minDigit + x < 10) {
          regexPart += "(" + minDivString + "[" + (minDigit + x) + "-9]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          for (int j = i; j > 0; j--) {
            regexPart += "[0-9]"; //$NON-NLS-1$
          }

          if (regexPart.contains("]") || !regex.contains(regexPart)) { //$NON-NLS-1$
            if (regex.length() > 2) {
              regex += "|" + regexPart + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            } else {
              regex += regexPart + ")"; //$NON-NLS-1$
            }
          }
        }
      }
    }
    regex += ")"; //$NON-NLS-1$
    return regex;

  }
}

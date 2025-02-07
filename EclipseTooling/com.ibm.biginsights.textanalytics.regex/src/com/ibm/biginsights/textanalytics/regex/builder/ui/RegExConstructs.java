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
package com.ibm.biginsights.textanalytics.regex.builder.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.ibm.biginsights.textanalytics.regex.Messages;

/**
 * We assume, that each construct occurs only once, there exists no constructs with different names
 * and same content. Otherwise we have to change the implementation of HashMap
 * TabbedRegexLibraryControl.constructToButton.
 * 
 *  Abraham
 * 
 */
public class RegExConstructs {



  public static final int CONSTRUCTS_CHARACTERS = 0;

  public static final int CONSTRUCTS_CHARACTERCLASSES = 1;

  public static final int CONSTRUCTS_PREDEFCHARACTERCLASSES = 2;

  public static final int CONSTRUCTS_BOUNDARYMATCHERS = 3;

  public static final int CONSTRUCTS_GREEDYQUANTIFIERS = 4;

  public static final int CONSTRUCTS_LOGICALOPERATORS = 5;

  public static final int CONSTRUCTS_MATCHFLAGS = 6;

  public static String getConstructName(int constructID) {
    switch (constructID) {
    case CONSTRUCTS_CHARACTERS:
      return Messages.RegExConstructs_CHARACTERS; 
    case CONSTRUCTS_CHARACTERCLASSES:
      return Messages.RegExConstructs_CHARACTER_CLASSES; 
    case CONSTRUCTS_PREDEFCHARACTERCLASSES:
      return Messages.RegExConstructs_PREDEFINED_CHAR_CLASSES; 
    case CONSTRUCTS_BOUNDARYMATCHERS:
      return Messages.RegExConstructs_BOUNDARY_MATCHES; 
    case CONSTRUCTS_GREEDYQUANTIFIERS:
      return Messages.RegExConstructs_GREEDY_QUALIFIERS; 
    case CONSTRUCTS_LOGICALOPERATORS:
      return Messages.RegExConstructs_LOGICAL_OPERATORS; 
    case CONSTRUCTS_MATCHFLAGS:
      return Messages.RegExConstructs_MATCH_FLAGS; 
    default:
      return null;
    }
  }

  private static Map<String, Object[]> constructsToVarQuestion = new HashMap<String, Object[]>();

  // regular-expression constructs
  // Characters
  private static final String CHARACTERS_BACKSLASH = "\\\\";  //$NON-NLS-1$

  private static final String CHARACTERS_BACKSLASH_RULE = "\\\\\\\\";  //$NON-NLS-1$

  private static final String CHARACTERS_BACKSLASH_DESCRIPTION = Messages.RegExConstructs_BACKSLASH_DESC; 

  private static final String CHARACTERS_TAB = "\\t";  //$NON-NLS-1$

  private static final String CHARACTERS_TAB_RULE = "\\\\t";  //$NON-NLS-1$

  private static final String CHARACTERS_TAB_DESCRIPTION = Messages.RegExConstructs_TAB_DESC; 

  private static final String CHARACTERS_NEWLINE = "\\n";  //$NON-NLS-1$

  private static final String CHARACTERS_NEWLINE_RULE = "\\\\n";  //$NON-NLS-1$

  private static final String CHARACTERS_NEWLINE_DESCRIPTION = Messages.RegExConstructs_NEWLINE_DESC; 

  private static final String CHARACTERS_CARRIAGERETURN = "\\r";  //$NON-NLS-1$

  private static final String CHARACTERS_CARRIAGERETURN_RULE = "\\\\r";  //$NON-NLS-1$

  private static final String CHARACTERS_CARRIAGERETURN_DESCRIPTION = Messages.RegExConstructs_CARRIAGE_RETURN_DESC; 

  private static final String CHARACTERS_ESCAPE = "\\e";  //$NON-NLS-1$

  private static final String CHARACTERS_ESCAPE_RULE = "\\\\e";  //$NON-NLS-1$

  private static final String CHARACTERS_ESCAPE_DESCRIPTION = Messages.RegExConstructs_ESCAPE_DESC; 

  public  static final String CHARACTERS_SPACE = " ";  //$NON-NLS-1$

  private static final String CHARACTERS_SPACE_RULE = " ";  //$NON-NLS-1$

  private static final String CHARACTERS_SPACE_DESCRIPTION = Messages.RegExConstructs_SPACE_DESC; 

  private static final String[] characters = { CHARACTERS_BACKSLASH, CHARACTERS_TAB,
      CHARACTERS_NEWLINE, CHARACTERS_CARRIAGERETURN, CHARACTERS_ESCAPE, CHARACTERS_SPACE };

  private static final String[] characters_rules = { CHARACTERS_BACKSLASH_RULE,
      CHARACTERS_TAB_RULE, CHARACTERS_NEWLINE_RULE, CHARACTERS_CARRIAGERETURN_RULE,
      CHARACTERS_ESCAPE_RULE, CHARACTERS_SPACE_RULE };

  // Character classes
  private static final String CHARACTERCLASSES_MATCHCLASS = Messages.RegExConstructs_MATCH_CLASS; 

  private static final String CHARACTERCLASSES_MATCHCLASS_RULE = Messages.RegExConstructs_MATCH_CLASS_RULE; 

  private static final String CHARACTERCLASSES_MATCHCLASS_DESCRIPTION = Messages.RegExConstructs_MATCH_CLASS_DESC; 

  private static final String CHARACTERCLASSES_NEGATECLASS = Messages.RegExConstructs_NEGATE_CLASS; 

  private static final String CHARACTERCLASSES_NEGATECLASS_RULE = Messages.RegExConstructs_NEGATE_CLASS_RULE; 

  private static final String CHARACTERCLASSES_NEGATECLASS_DESCRIPTION = Messages.RegExConstructs_NEGATE_CLASS_DESC; 

  private static final String CHARACTERCLASSES_MATCHRANGE = Messages.RegExConstructs_MATCH_RANGE; 

  private static final String CHARACTERCLASSES_MATCHRANGE_RULE = Messages.RegExConstructs_MATCH_RANGE_RULE; 

  private static final String CHARACTERCLASSES_MATCHRANGE_DESCRIPTION = Messages.RegExConstructs_MATCH_RANGE_DESC; 

  static {
    constructsToVarQuestion
        .put(
            CHARACTERCLASSES_MATCHCLASS,
            new Object[] { new String[] {
                Messages.RegExConstructs_MATCH_CLASS_CHAR, "abc", Messages.RegExConstructs_MATCH_CLASS_CHAR_DESC } });    //$NON-NLS-2$ //$NON-NLS-1$
    constructsToVarQuestion
        .put(
            CHARACTERCLASSES_NEGATECLASS,
            new Object[] { new String[] {
                Messages.RegExConstructs_NEGATE_CLASS_CHAR, "abc", Messages.RegExConstructs_NEGATE_CLASS_CHAR_DESC } });    //$NON-NLS-2$ //$NON-NLS-1$
    constructsToVarQuestion
        .put(
            CHARACTERCLASSES_MATCHRANGE,
            new Object[] {
                new String[] {
                    Messages.RegExConstructs_MATCH_RANGE_CHAR, "a", Messages.RegExConstructs_CHAR_START_RANGE_DESC },    //$NON-NLS-2$ //$NON-NLS-1$
                new String[] {
                    Messages.RegExConstructs_MATCH_RANGE_CHAR, "z", Messages.RegExConstructs_CHAR_END_RANGE_DESC } });    //$NON-NLS-2$ //$NON-NLS-1$
  }

  private static final String[] characterClasses = { CHARACTERCLASSES_MATCHCLASS,
      CHARACTERCLASSES_NEGATECLASS, CHARACTERCLASSES_MATCHRANGE, };

  private static final String[] characterClasses_rules = { CHARACTERCLASSES_MATCHCLASS_RULE,
      CHARACTERCLASSES_NEGATECLASS_RULE, CHARACTERCLASSES_MATCHRANGE_RULE, };

  // Predefined character classes
  public  static final String PREDEFCHARCLASSES_ANYCHARACTER = ".";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_ANYCHARACTER_RULE = "\\.";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_ANYCHARACTER_DESCRIPTION = Messages.RegExConstructs_MATCH_CHAR_EXCEPT_NEWLINE; 

  private static final String PREDEFCHARCLASSES_DIGIT = "\\d";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_DIGIT_RULE = "\\\\d";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_DIGIT_DESCRIPTION = Messages.RegExConstructs_DIGIT; 

  private static final String PREDEFCHARCLASSES_NONDIGIT = "\\D";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_NONDIGIT_RULE = "\\\\D";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_NONDIGIT_DESCRIPTION = Messages.RegExConstructs_NON_DIGIT; 

  private static final String PREDEFCHARCLASSES_WHITESPACE = "\\s";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_WHITESPACE_RULE = "\\\\s";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_WHITESPACE_DESCRIPTION = Messages.RegExConstructs_WHITESPACE_DESC; 

  private static final String PREDEFCHARCLASSES_NONWHITESPACE = "\\S";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_NONWHITESPACE_RULE = "\\\\S";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_NONWHITESPACE_DESCRIPTION = Messages.RegExConstructs_NON_WHITESPACE_DESC; 

  private static final String PREDEFCHARCLASSES_WORD = "\\w";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_WORD_RULE = "\\\\w";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_WORD_DESCRIPTION = Messages.RegExConstructs_WORD_CHAR_DESC; 

  private static final String PREDEFCHARCLASSES_NONWORD = "\\W";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_NONWORD_RULE = "\\\\W";  //$NON-NLS-1$

  private static final String PREDEFCHARCLASSES_NONWORD_DESCRIPTION = Messages.RegExConstructs_NON_WORD_CHAR_DESC; 

  private static final String[] preDefCharacterClasses = { PREDEFCHARCLASSES_ANYCHARACTER,
      PREDEFCHARCLASSES_DIGIT, PREDEFCHARCLASSES_NONDIGIT, PREDEFCHARCLASSES_WHITESPACE,
      PREDEFCHARCLASSES_NONWHITESPACE, PREDEFCHARCLASSES_WORD, PREDEFCHARCLASSES_NONWORD };

  private static final String[] preDefCharacterClasses_rules = {
      PREDEFCHARCLASSES_ANYCHARACTER_RULE, PREDEFCHARCLASSES_DIGIT_RULE,
      PREDEFCHARCLASSES_NONDIGIT_RULE, PREDEFCHARCLASSES_WHITESPACE_RULE,
      PREDEFCHARCLASSES_NONWHITESPACE_RULE, PREDEFCHARCLASSES_WORD_RULE,
      PREDEFCHARCLASSES_NONWORD_RULE };

  // Boundary matchers
  private static final String BOUNDARIES_LINEBEGINNING = "^";  //$NON-NLS-1$

  private static final String BOUNDARIES_LINEBEGINNING_RULE = "\\^";  //$NON-NLS-1$

  private static final String BOUNDARIES_LINEBEGINNING_DESCRIPTION = Messages.RegExConstructs_BEGIN_LINE_DESC; 

  private static final String BOUNDARIES_LINEEND = "$";  //$NON-NLS-1$

  private static final String BOUNDARIES_LINEEND_RULE = "\\$";  //$NON-NLS-1$

  private static final String BOUNDARIES_LINEEND_DESCRIPTION = Messages.RegExConstructs_END_LINE_DESC; 

  private static final String BOUNDARIES_WORDBOUNDARY = "\\b";  //$NON-NLS-1$

  private static final String BOUNDARIES_WORDBOUNDARY_RULE = "\\\\b";  //$NON-NLS-1$

  private static final String BOUNDARIES_WORDBOUNDARY_DESCRIPTION = Messages.RegExConstructs_WORD_BOUNDARY_DESC; 

  private static final String BOUNDARIES_NONWORDBOUNDARY = "\\B";  //$NON-NLS-1$

  private static final String BOUNDARIES_NONWORDBOUNDARY_RULE = "\\\\B";  //$NON-NLS-1$

  private static final String BOUNDARIES_NONWORDBOUNDARY_DESCRIPTION = Messages.RegExConstructs_NON_WORD_BOUNDARY_DESC; 

  private static final String[] boundaryMatchers = { BOUNDARIES_LINEBEGINNING, BOUNDARIES_LINEEND,
      BOUNDARIES_WORDBOUNDARY, BOUNDARIES_NONWORDBOUNDARY, };

  private static final String[] boundaryMatchers_rules = { BOUNDARIES_LINEBEGINNING_RULE,
      BOUNDARIES_LINEEND_RULE, BOUNDARIES_WORDBOUNDARY_RULE, BOUNDARIES_NONWORDBOUNDARY_RULE, };

  // Greedy quantifiers
  public  static final String GREEDYQUANTIFIERS_ONCEORNOT = "?";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_ONCEORNOT_RULE = "\\?";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_ONCEORNOT_DESCRIPTION = Messages.RegExConstructs_ONCEORNOT_DESC; 

  private static final String GREEDYQUANTIFIERS_ZEROORMORE = "*";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_ZEROORMORE_RULE = "\\*";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_ZEROORMORE_DESCRIPTION = Messages.RegExConstructs_ZEROORMORE_DESC; 

  private static final String GREEDYQUANTIFIERS_ONEORMORE = "+";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_ONEORMORE_RULE = "\\+";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_ONEORMORE_DESCRIPTION = Messages.RegExConstructs_ONEORMORE_DESC; 

  public static final String GREEDYQUANTIFIERS_EXACTLY_N_TIMES = "{n}";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_EXACTLY_N_TIMES_RULE = "\\{\\d\\}";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_EXACTLY_N_TIMES_DESCRIPTION = Messages.RegExConstructs_EXACTLY_N_TIMES_DESC; 

  public static final String GREEDYQUANTIFIERS_AT_LEAST_N_TIMES = "{n,}";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_AT_LEAST_N_TIMES_RULE = "\\{\\d,\\}";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_AT_LEAST_N_TIMES_DESCRIPTION = Messages.RegExConstructs_ATLEAST_N_TIMES_DESC; 

  public static final String GREEDYQUANTIFIERS_AT_LEAST_N_NOT_MORE_M = "{n,m}";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_AT_LEAST_N_NOT_MORE_M_RULE = "\\{\\d,\\d\\}";  //$NON-NLS-1$

  private static final String GREEDYQUANTIFIERS_AT_LEAST_N_NOT_MORE_M_DESCRIPTION = Messages.RegExConstructs_ATLEAST_N_NOT_MORE_THAN_M_DESC; 

  static {
    constructsToVarQuestion
        .put(
            GREEDYQUANTIFIERS_EXACTLY_N_TIMES,
            new Object[] { new String[] {
                Messages.RegExConstructs_EXACTLY_N_DESC, "n", Messages.RegExConstructs_NUMBER_MATCHES_LABEL } });    //$NON-NLS-2$ //$NON-NLS-1$
    constructsToVarQuestion
        .put(
            GREEDYQUANTIFIERS_AT_LEAST_N_TIMES,
            new Object[] { new String[] {
                Messages.RegExConstructs_ATLEAST_N_DESC, "n", Messages.RegExConstructs_MIN_NUMBER_MATCHES_LABEL } });    //$NON-NLS-2$ //$NON-NLS-1$
    constructsToVarQuestion
        .put(
            GREEDYQUANTIFIERS_AT_LEAST_N_NOT_MORE_M,
            new Object[] {
                new String[] {
                    Messages.RegExConstructs_MIN_MAX_MATCHES_LABEL, "n", Messages.RegExConstructs_MIN_MATCHES_LABEL },    //$NON-NLS-2$ //$NON-NLS-1$
                new String[] {
                    Messages.RegExConstructs_MATCH_N_NOT_MORETHAN_M_LABEL, "m", Messages.RegExConstructs_MAX_MATCHES_LABEL } });    //$NON-NLS-2$ //$NON-NLS-1$
  }

  private static final String[] greedyQuantifiers = { GREEDYQUANTIFIERS_ONCEORNOT,
      GREEDYQUANTIFIERS_ZEROORMORE, GREEDYQUANTIFIERS_ONEORMORE, GREEDYQUANTIFIERS_EXACTLY_N_TIMES,
      GREEDYQUANTIFIERS_AT_LEAST_N_TIMES, GREEDYQUANTIFIERS_AT_LEAST_N_NOT_MORE_M, };

  private static final String[] greedyQuantifiers_rules = { GREEDYQUANTIFIERS_ONCEORNOT_RULE,
      GREEDYQUANTIFIERS_ZEROORMORE_RULE, GREEDYQUANTIFIERS_ONEORMORE_RULE,
      GREEDYQUANTIFIERS_EXACTLY_N_TIMES_RULE, GREEDYQUANTIFIERS_AT_LEAST_N_TIMES_RULE,
      GREEDYQUANTIFIERS_AT_LEAST_N_NOT_MORE_M_RULE, };

  // Logical operators
  private static final String LOGICALOPEARATORS_XY = "XY";  //$NON-NLS-1$

  private static final String LOGICALOPEARATORS_XY_RULE = "";  //$NON-NLS-1$

  private static final String LOGICALOPEARATORS_XY_DESCRIPTION = "X followed by Y";  //$NON-NLS-1$

  private static final String LOGICALOPEARATORS_X_OR_Y = "X|Y";  //$NON-NLS-1$

  private static final String LOGICALOPEARATORS_X_OR_Y_RULE = "\\|";  //$NON-NLS-1$

  private static final String LOGICALOPEARATORS_X_OR_Y_DESCRIPTION = "X|Y: Either X or Y";  //$NON-NLS-1$

  static {
    constructsToVarQuestion
        .put(
            LOGICALOPEARATORS_XY,
            new Object[] {
                new String[] {
                	Messages.RegExConstructs_MATCH_X_FOLLOWED_Y, "X", Messages.RegExConstructs_ENTER_X },    //$NON-NLS-1$ //$NON-NLS-2$
                new String[] {
                    Messages.RegExConstructs_MATCH_X_FOLLOWED_Y, "Y", Messages.RegExConstructs_ENTER_Y } });    //$NON-NLS-2$ //$NON-NLS-1$
    constructsToVarQuestion
        .put(
            LOGICALOPEARATORS_X_OR_Y,
            new Object[] {
                new String[] {
                    Messages.RegExConstructs_MATCH_X_OR_Y, "X", Messages.RegExConstructs_ENTER_X },    //$NON-NLS-2$ //$NON-NLS-1$
                new String[] {
                    Messages.RegExConstructs_MATCH_X_OR_Y, "Y", Messages.RegExConstructs_ENTER_Y } });    //$NON-NLS-2$ //$NON-NLS-1$
  }

  private static final String[] logicalOperators = { LOGICALOPEARATORS_XY,
      LOGICALOPEARATORS_X_OR_Y, };

  private static final String[] logicalOperators_rules = { LOGICALOPEARATORS_XY_RULE,
      LOGICALOPEARATORS_X_OR_Y_RULE, };

  // Match Flags
  private static final String MATCHFLAGS_UNIXLINESMODE = "(?d)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_UNIXLINESMODE_RULE = "\\(\\?d\\)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_UNIXLINESMODE_DESCRIPTION = Messages.RegExConstructs_UNIX_LINESMODE_DESC; 

  private static final String MATCHFLAGS_UNIXLINESMODE_DESCRIPTION_TITLE = Messages.RegExConstructs_UNIX_LINESMODE_TITLE; 

  private static final String MATCHFLAGS_CASEINSENSITIVEMODE = "(?i)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_CASEINSENSITIVEMODE_RULE = "\\(\\?i\\)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_CASEINSENSITIVEMODE_DESCRIPTION = Messages.RegExConstructs_CASEINSENSITIVE_MODE_DESC; 

  private static final String MATCHFLAGS_CASEINSENSITIVEMODE_DESCRIPTION_TITLE = Messages.RegExConstructs_CASEINSENSITIVE_MODE_TITLE; 

  private static final String MATCHFLAGS_COMMENTS = "(?x)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_COMMENTS_RULE = "\\(\\?x\\)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_COMMENTS_DESCRIPTION = Messages.RegExConstructs_COMMENTS_DESC; 

  private static final String MATCHFLAGS_COMMENTS_DESCRIPTION_TITLE = Messages.RegExConstructs_COMMENTS_TITLE; 

  private static final String MATCHFLAGS_MULTILINE = "(?m)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_MULTILINE_RULE = "\\(\\?m\\)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_MULTILINE_DESCRIPTION = Messages.RegExConstructs_MULTILINE_DESC; 

  private static final String MATCHFLAGS_MULTILINE_DESCRIPTION_TITLE = Messages.RegExConstructs_MULTILINE_TITLE; 

  private static final String MATCHFLAGS_DOTALL = "(?s)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_DOTALL_RULE = "\\(\\?s\\)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_DOTALL_DESCRIPTION = Messages.RegExConstructs_DOTALL_DESC; 

  private static final String MATCHFLAGS_DOTALL_DESCRIPTION_TITLE = Messages.RegExConstructs_DOTALL_TITLE; 

  private static final String MATCHFLAGS_UNICODEAWARE = "(?u)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_UNICODEAWARE_RULE = "\\(\\?u\\)";  //$NON-NLS-1$

  private static final String MATCHFLAGS_UNICODEAWARE_DESCRIPTION = Messages.RegExConstructs_UNICODE_AWARE_DESC; 

  private static final String MATCHFLAGS_UNICODEAWARE_DESCRIPTION_TITLE = Messages.RegExConstructs_UNICODE_AWARE_TITLE; 

  private static final String[] matchFlags = { MATCHFLAGS_UNIXLINESMODE,
      MATCHFLAGS_CASEINSENSITIVEMODE, MATCHFLAGS_COMMENTS, MATCHFLAGS_MULTILINE, MATCHFLAGS_DOTALL,
      MATCHFLAGS_UNICODEAWARE, };

  private static final String[] matchFlags_rules = { MATCHFLAGS_UNIXLINESMODE_RULE,
      MATCHFLAGS_CASEINSENSITIVEMODE_RULE, MATCHFLAGS_COMMENTS_RULE, MATCHFLAGS_MULTILINE_RULE,
      MATCHFLAGS_DOTALL_RULE, MATCHFLAGS_UNICODEAWARE_RULE, };

  private static HashMap<String, String> constructToDescriptionMap;
  static {
    constructToDescriptionMap = new HashMap<String, String>();
    constructToDescriptionMap.put(CHARACTERS_BACKSLASH, CHARACTERS_BACKSLASH_DESCRIPTION);
    constructToDescriptionMap.put(CHARACTERS_TAB, CHARACTERS_TAB_DESCRIPTION);
    constructToDescriptionMap.put(CHARACTERS_NEWLINE, CHARACTERS_NEWLINE_DESCRIPTION);
    constructToDescriptionMap.put(CHARACTERS_CARRIAGERETURN, CHARACTERS_CARRIAGERETURN_DESCRIPTION);
    constructToDescriptionMap.put(CHARACTERS_ESCAPE, CHARACTERS_ESCAPE_DESCRIPTION);
    constructToDescriptionMap.put(CHARACTERS_SPACE, CHARACTERS_SPACE_DESCRIPTION);
    constructToDescriptionMap.put(CHARACTERCLASSES_MATCHCLASS,
        CHARACTERCLASSES_MATCHCLASS_DESCRIPTION);
    constructToDescriptionMap.put(CHARACTERCLASSES_NEGATECLASS,
        CHARACTERCLASSES_NEGATECLASS_DESCRIPTION);
    constructToDescriptionMap.put(CHARACTERCLASSES_MATCHRANGE,
        CHARACTERCLASSES_MATCHRANGE_DESCRIPTION);
    constructToDescriptionMap.put(PREDEFCHARCLASSES_ANYCHARACTER,
        PREDEFCHARCLASSES_ANYCHARACTER_DESCRIPTION);
    constructToDescriptionMap.put(PREDEFCHARCLASSES_DIGIT, PREDEFCHARCLASSES_DIGIT_DESCRIPTION);
    constructToDescriptionMap.put(PREDEFCHARCLASSES_NONDIGIT,
        PREDEFCHARCLASSES_NONDIGIT_DESCRIPTION);
    constructToDescriptionMap.put(PREDEFCHARCLASSES_WHITESPACE,
        PREDEFCHARCLASSES_WHITESPACE_DESCRIPTION);
    constructToDescriptionMap.put(PREDEFCHARCLASSES_NONWHITESPACE,
        PREDEFCHARCLASSES_NONWHITESPACE_DESCRIPTION);
    constructToDescriptionMap.put(PREDEFCHARCLASSES_WORD, PREDEFCHARCLASSES_WORD_DESCRIPTION);
    constructToDescriptionMap.put(PREDEFCHARCLASSES_NONWORD, PREDEFCHARCLASSES_NONWORD_DESCRIPTION);
    constructToDescriptionMap.put(BOUNDARIES_LINEBEGINNING, BOUNDARIES_LINEBEGINNING_DESCRIPTION);
    constructToDescriptionMap.put(BOUNDARIES_LINEEND, BOUNDARIES_LINEEND_DESCRIPTION);
    constructToDescriptionMap.put(BOUNDARIES_WORDBOUNDARY, BOUNDARIES_WORDBOUNDARY_DESCRIPTION);
    constructToDescriptionMap.put(BOUNDARIES_NONWORDBOUNDARY,
        BOUNDARIES_NONWORDBOUNDARY_DESCRIPTION);
    constructToDescriptionMap.put(GREEDYQUANTIFIERS_ONCEORNOT,
        GREEDYQUANTIFIERS_ONCEORNOT_DESCRIPTION);
    constructToDescriptionMap.put(GREEDYQUANTIFIERS_ZEROORMORE,
        GREEDYQUANTIFIERS_ZEROORMORE_DESCRIPTION);
    constructToDescriptionMap.put(GREEDYQUANTIFIERS_ONEORMORE,
        GREEDYQUANTIFIERS_ONEORMORE_DESCRIPTION);
    constructToDescriptionMap.put(GREEDYQUANTIFIERS_EXACTLY_N_TIMES,
        GREEDYQUANTIFIERS_EXACTLY_N_TIMES_DESCRIPTION);
    constructToDescriptionMap.put(GREEDYQUANTIFIERS_AT_LEAST_N_TIMES,
        GREEDYQUANTIFIERS_AT_LEAST_N_TIMES_DESCRIPTION);
    constructToDescriptionMap.put(GREEDYQUANTIFIERS_AT_LEAST_N_NOT_MORE_M,
        GREEDYQUANTIFIERS_AT_LEAST_N_NOT_MORE_M_DESCRIPTION);
    constructToDescriptionMap.put(LOGICALOPEARATORS_XY, LOGICALOPEARATORS_XY_DESCRIPTION);
    constructToDescriptionMap.put(LOGICALOPEARATORS_X_OR_Y, LOGICALOPEARATORS_X_OR_Y_DESCRIPTION);
    constructToDescriptionMap.put(MATCHFLAGS_UNIXLINESMODE, MATCHFLAGS_UNIXLINESMODE_DESCRIPTION);
    constructToDescriptionMap.put(MATCHFLAGS_CASEINSENSITIVEMODE,
        MATCHFLAGS_CASEINSENSITIVEMODE_DESCRIPTION);
    constructToDescriptionMap.put(MATCHFLAGS_COMMENTS, MATCHFLAGS_COMMENTS_DESCRIPTION);
    constructToDescriptionMap.put(MATCHFLAGS_MULTILINE, MATCHFLAGS_MULTILINE_DESCRIPTION);
    constructToDescriptionMap.put(MATCHFLAGS_DOTALL, MATCHFLAGS_DOTALL_DESCRIPTION);
    constructToDescriptionMap.put(MATCHFLAGS_UNICODEAWARE, MATCHFLAGS_UNICODEAWARE_DESCRIPTION);
  }

  private static HashMap<String, String> constructRulesToDescriptionMap;
  static {
    constructRulesToDescriptionMap = new HashMap<String, String>();
    constructRulesToDescriptionMap.put(CHARACTERS_BACKSLASH_RULE, CHARACTERS_BACKSLASH_DESCRIPTION);
    constructRulesToDescriptionMap.put(CHARACTERS_TAB_RULE, CHARACTERS_TAB_DESCRIPTION);
    constructRulesToDescriptionMap.put(CHARACTERS_NEWLINE_RULE, CHARACTERS_NEWLINE_DESCRIPTION);
    constructRulesToDescriptionMap.put(CHARACTERS_CARRIAGERETURN_RULE,
        CHARACTERS_CARRIAGERETURN_DESCRIPTION);
    constructRulesToDescriptionMap.put(CHARACTERS_ESCAPE_RULE, CHARACTERS_ESCAPE_DESCRIPTION);
    constructRulesToDescriptionMap.put(CHARACTERS_SPACE_RULE, CHARACTERS_SPACE_DESCRIPTION);
    constructRulesToDescriptionMap.put(CHARACTERCLASSES_MATCHCLASS_RULE,
        CHARACTERCLASSES_MATCHCLASS_DESCRIPTION);
    constructRulesToDescriptionMap.put(CHARACTERCLASSES_NEGATECLASS_RULE,
        CHARACTERCLASSES_NEGATECLASS_DESCRIPTION);
    constructRulesToDescriptionMap.put(CHARACTERCLASSES_MATCHRANGE_RULE,
        CHARACTERCLASSES_MATCHRANGE_DESCRIPTION);
    constructRulesToDescriptionMap.put(PREDEFCHARCLASSES_ANYCHARACTER_RULE,
        PREDEFCHARCLASSES_ANYCHARACTER_DESCRIPTION);
    constructRulesToDescriptionMap.put(PREDEFCHARCLASSES_DIGIT_RULE,
        PREDEFCHARCLASSES_DIGIT_DESCRIPTION);
    constructRulesToDescriptionMap.put(PREDEFCHARCLASSES_NONDIGIT_RULE,
        PREDEFCHARCLASSES_NONDIGIT_DESCRIPTION);
    constructRulesToDescriptionMap.put(PREDEFCHARCLASSES_WHITESPACE_RULE,
        PREDEFCHARCLASSES_WHITESPACE_DESCRIPTION);
    constructRulesToDescriptionMap.put(PREDEFCHARCLASSES_NONWHITESPACE_RULE,
        PREDEFCHARCLASSES_NONWHITESPACE_DESCRIPTION);
    constructRulesToDescriptionMap.put(PREDEFCHARCLASSES_WORD_RULE,
        PREDEFCHARCLASSES_WORD_DESCRIPTION);
    constructRulesToDescriptionMap.put(PREDEFCHARCLASSES_NONWORD_RULE,
        PREDEFCHARCLASSES_NONWORD_DESCRIPTION);
    constructRulesToDescriptionMap.put(BOUNDARIES_LINEBEGINNING_RULE,
        BOUNDARIES_LINEBEGINNING_DESCRIPTION);
    constructRulesToDescriptionMap.put(BOUNDARIES_LINEEND_RULE, BOUNDARIES_LINEEND_DESCRIPTION);
    constructRulesToDescriptionMap.put(BOUNDARIES_WORDBOUNDARY_RULE,
        BOUNDARIES_WORDBOUNDARY_DESCRIPTION);
    constructRulesToDescriptionMap.put(BOUNDARIES_NONWORDBOUNDARY_RULE,
        BOUNDARIES_NONWORDBOUNDARY_DESCRIPTION);
    constructRulesToDescriptionMap.put(GREEDYQUANTIFIERS_ONCEORNOT_RULE,
        GREEDYQUANTIFIERS_ONCEORNOT_DESCRIPTION);
    constructRulesToDescriptionMap.put(GREEDYQUANTIFIERS_ZEROORMORE_RULE,
        GREEDYQUANTIFIERS_ZEROORMORE_DESCRIPTION);
    constructRulesToDescriptionMap.put(GREEDYQUANTIFIERS_ONEORMORE_RULE,
        GREEDYQUANTIFIERS_ONEORMORE_DESCRIPTION);
    constructRulesToDescriptionMap.put(GREEDYQUANTIFIERS_EXACTLY_N_TIMES_RULE,
        GREEDYQUANTIFIERS_EXACTLY_N_TIMES_DESCRIPTION);
    constructRulesToDescriptionMap.put(GREEDYQUANTIFIERS_AT_LEAST_N_TIMES_RULE,
        GREEDYQUANTIFIERS_AT_LEAST_N_TIMES_DESCRIPTION);
    constructRulesToDescriptionMap.put(GREEDYQUANTIFIERS_AT_LEAST_N_NOT_MORE_M_RULE,
        GREEDYQUANTIFIERS_AT_LEAST_N_NOT_MORE_M_DESCRIPTION);
    constructRulesToDescriptionMap.put(LOGICALOPEARATORS_XY_RULE, LOGICALOPEARATORS_XY_DESCRIPTION);
    constructRulesToDescriptionMap.put(LOGICALOPEARATORS_X_OR_Y_RULE,
        LOGICALOPEARATORS_X_OR_Y_DESCRIPTION);
    constructRulesToDescriptionMap.put(MATCHFLAGS_UNIXLINESMODE_RULE,
        MATCHFLAGS_UNIXLINESMODE_DESCRIPTION);
    constructRulesToDescriptionMap.put(MATCHFLAGS_CASEINSENSITIVEMODE_RULE,
        MATCHFLAGS_CASEINSENSITIVEMODE_DESCRIPTION);
    constructRulesToDescriptionMap.put(MATCHFLAGS_COMMENTS_RULE, MATCHFLAGS_COMMENTS_DESCRIPTION);
    constructRulesToDescriptionMap.put(MATCHFLAGS_MULTILINE_RULE, MATCHFLAGS_MULTILINE_DESCRIPTION);
    constructRulesToDescriptionMap.put(MATCHFLAGS_DOTALL_RULE, MATCHFLAGS_DOTALL_DESCRIPTION);
    constructRulesToDescriptionMap.put(MATCHFLAGS_UNICODEAWARE_RULE,
        MATCHFLAGS_UNICODEAWARE_DESCRIPTION);
  }

  private static HashMap<String, String> constructToTitleDescriptionMap;
  static {
    constructToTitleDescriptionMap = new HashMap<String, String>();
    constructToTitleDescriptionMap.put(MATCHFLAGS_UNIXLINESMODE,
        MATCHFLAGS_UNIXLINESMODE_DESCRIPTION_TITLE);
    constructToTitleDescriptionMap.put(MATCHFLAGS_CASEINSENSITIVEMODE,
        MATCHFLAGS_CASEINSENSITIVEMODE_DESCRIPTION_TITLE);
    constructToTitleDescriptionMap.put(MATCHFLAGS_COMMENTS, MATCHFLAGS_COMMENTS_DESCRIPTION_TITLE);
    constructToTitleDescriptionMap
        .put(MATCHFLAGS_MULTILINE, MATCHFLAGS_MULTILINE_DESCRIPTION_TITLE);
    constructToTitleDescriptionMap.put(MATCHFLAGS_DOTALL, MATCHFLAGS_DOTALL_DESCRIPTION_TITLE);
    constructToTitleDescriptionMap.put(MATCHFLAGS_UNICODEAWARE,
        MATCHFLAGS_UNICODEAWARE_DESCRIPTION_TITLE);
  }

  static String[] getRegularExpressionContructs(int constructID) {
    switch (constructID) {
    case CONSTRUCTS_CHARACTERS:
      return characters;
    case CONSTRUCTS_CHARACTERCLASSES:
      return characterClasses;
    case CONSTRUCTS_PREDEFCHARACTERCLASSES:
      return preDefCharacterClasses;
    case CONSTRUCTS_BOUNDARYMATCHERS:
      return boundaryMatchers;
    case CONSTRUCTS_GREEDYQUANTIFIERS:
      return greedyQuantifiers;
    case CONSTRUCTS_LOGICALOPERATORS:
      return logicalOperators;
    case CONSTRUCTS_MATCHFLAGS:
      return matchFlags;
    default:
      return null;
    }
  }

  public static String getRegexForRegularExpressionContruct(String construct) {
    final String description = getDescriptionForConstruct(construct);
    final Iterator<?> it = constructRulesToDescriptionMap.entrySet().iterator();
    while (it.hasNext()) {
      final Entry<?, ?> ruleDescr = (Entry<?, ?>) it.next();
      if (ruleDescr.getValue().equals(description)) {
        return ruleDescr.getKey().toString();
      }
    }
    return "";  //$NON-NLS-1$
  }

  public static String[] getRegexForRegularExpressionContructs(int constructID) {
    switch (constructID) {
    case CONSTRUCTS_CHARACTERS:
      return characters_rules;
    case CONSTRUCTS_CHARACTERCLASSES:
      return characterClasses_rules;
    case CONSTRUCTS_PREDEFCHARACTERCLASSES:
      return preDefCharacterClasses_rules;
    case CONSTRUCTS_BOUNDARYMATCHERS:
      return boundaryMatchers_rules;
    case CONSTRUCTS_GREEDYQUANTIFIERS:
      return greedyQuantifiers_rules;
    case CONSTRUCTS_LOGICALOPERATORS:
      return logicalOperators_rules;
    case CONSTRUCTS_MATCHFLAGS:
      return matchFlags_rules;
    default:
      return null;
    }
  }

  public static Color getColorIDForRegularExpressionContructs(int constructID, Display device) {
    switch (constructID) {
    case CONSTRUCTS_CHARACTERS:
      // R 209 G 73 B 82 (rot)
      // return new Color(device, 235,215,178);
      return new Color(device, 246, 212, 154);
    case CONSTRUCTS_CHARACTERCLASSES:
      // R 161 G 203 B 90 (gr(C)(C)(C)n)
      // return new Color(device, 242, 242, 37);
      return new Color(device, 209, 196, 175);
    case CONSTRUCTS_PREDEFCHARACTERCLASSES:
      // R 139 G 182 B 210 (blau)
      return new Color(device, 200, 238, 203);
    case CONSTRUCTS_BOUNDARYMATCHERS:
      // R 162 G 143 B 117 (braun)
      return new Color(device, 215, 215, 215);
    case CONSTRUCTS_GREEDYQUANTIFIERS:
      // R 255 G 181 B 44 (orange)
      return new Color(device, 207, 165, 220);
    case CONSTRUCTS_LOGICALOPERATORS:
      // R 255 G 237 B 64 (gelb)
      return new Color(device, 239, 239, 101);
      // return new Color(device, 197,186,169);
    case CONSTRUCTS_MATCHFLAGS:
      // R 248 G 172 B 170 (pink)
      // return new Color(device, 109, 143, 196);
      return new Color(device, 200, 216, 241);
    default:
      // R 199 G 169 B 211 (violet)
      return new Color(device, 199, 169, 211);
    }
  }

  public static Object[] getVariablesInConstruct(String construct) {
    if (constructsToVarQuestion.containsKey(construct)) {
      return constructsToVarQuestion.get(construct);
    }
    return new Object[] {};
  }

  /*
   * get description
   */
  public static String getDescriptionForConstructRule(String constructRule) {
    return constructRulesToDescriptionMap.get(constructRule);
  }

  public static String getDescriptionForConstruct(String construct) {
    return constructToDescriptionMap.get(construct);
  }

  public static String getDescriptionTitleForConstruct(String construct) {
    return constructToTitleDescriptionMap.get(construct);
  }

}

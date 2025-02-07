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
package com.ibm.biginsights.textanalytics.regex.learner.suggest;

import java.util.ArrayList;

import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterHierarchy;
import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionLeaf;
import com.ibm.biginsights.textanalytics.regex.learner.learner.Bias;
import com.ibm.biginsights.textanalytics.regex.learner.learner.RIGAAlignFirst;
import com.ibm.biginsights.textanalytics.regex.learner.learner.RIGACommonSubstrings;
import com.ibm.biginsights.textanalytics.regex.learner.learner.RIGALearner;
import com.ibm.biginsights.textanalytics.regex.learner.learner.RIGASummarizeFirst;
import com.ibm.biginsights.textanalytics.regex.learner.learner.Sample;
import com.ibm.biginsights.textanalytics.regex.learner.utils.Traversal;

/**
 * This class acts as the broker between the graphical user interface and the RegEx learning
 * algorithms. It takes a set of sampleStrings (TODO: and a bias), configures the learning
 * algorithms and thus produces several regexes, which again can then be shown to the user on the
 * GUI.
 * 
 * 
 * 
 */
public class RegexSuggester {


  /**
   * This method suggests one to three regexes.
   * 
   * @param sampleStrings
   * @return
   */
  public static ArrayList<SuggestedRegex> suggestRegexes(ArrayList<String> sampleStrings,
      int biasIndex) {
    // create bias object
    final Bias bias = new Bias();
    switch (biasIndex) {
    case 0: {
      bias.setCaseSensitiveCommonSubstrings(true);
      bias.setCaseSensitiveSummarizing(true);
      bias.setMinCommonSubstringLength(2);
      bias.setSummarizer_minNoOfSamples(20);
      bias.setUseDigitCommonSubstrings(true);
      bias.setDigitsNeverAlternation(false);
      break;
    }
    case 1: {
      bias.setCaseSensitiveCommonSubstrings(true);
      bias.setCaseSensitiveSummarizing(false);
      bias.setMinCommonSubstringLength(2);
      bias.setSummarizer_minNoOfSamples(7);
      bias.setUseDigitCommonSubstrings(true);
      bias.setDigitsNeverAlternation(false);
      break;
    }
    case 2: {
      bias.setCaseSensitiveCommonSubstrings(true);
      bias.setCaseSensitiveSummarizing(false);
      bias.setMinCommonSubstringLength(2);
      bias.setSummarizer_minNoOfSamples(2);
      bias.setUseDigitCommonSubstrings(true);
      bias.setDigitsNeverAlternation(false);
      break;
    }
    case 3: {
      bias.setCaseSensitiveCommonSubstrings(false);
      bias.setCaseSensitiveSummarizing(false);
      bias.setMinCommonSubstringLength(3);
      bias.setSummarizer_minNoOfSamples(1);
      bias.setUseDigitCommonSubstrings(false);
      bias.setDigitsNeverAlternation(true);
      break;
    }
    case 4: {
      bias.setCaseSensitiveCommonSubstrings(false);
      bias.setCaseSensitiveSummarizing(false);
      bias.setMinCommonSubstringLength(2);
      bias.setSummarizer_minNoOfSamples(1);
      bias.setUseDigitCommonSubstrings(false);
      bias.setDigitsNeverAlternation(true);
      bias.setConvertToCharClassOnly(true);
      break;
    }
    }
    // remove duplicates
    final ArrayList<String> sampleStringsCopy = new ArrayList<String>();
    for (final String s : sampleStrings) {
      if (!sampleStringsCopy.contains(s)) {
        sampleStringsCopy.add(s);
      }
    }
    sampleStrings = sampleStringsCopy;
    ArrayList<Sample> samples = new ArrayList<Sample>();
    for (final String s : sampleStrings) {
      samples.add(new Sample(s));
    }
    final ArrayList<SuggestedRegex> regexes = new ArrayList<SuggestedRegex>();
    RIGALearner rIGALearner;
    Expression learned;
    SuggestedRegex suggestedRegex;
    // common substrings learner
    final RIGACommonSubstrings csslearner = new RIGACommonSubstrings(samples, bias);
    learned = csslearner.learn();
    if (learned != null) {
      // System.out.println(learned.toStringWithSamples());
      markClassOther(learned);
      final String hint = Messages.RegexSuggester_STRING_OCCUR_IN_SAMPLES_OPTION;
      suggestedRegex = new SuggestedRegex(learned, hint);
      regexes.add(suggestedRegex);
      // System.out.println("\nCSS LEARNER:\n" + learned.toStringWithSamples() + "\n"
      // + Converter.toRegex(learned));
    }
    samples = new ArrayList<Sample>();
    for (final String s : sampleStrings) {
      samples.add(new Sample(s));
    }
    // basic learner, case insensitive
    rIGALearner = new RIGASummarizeFirst(samples, bias);
    learned = rIGALearner.learn();
    // System.out.println(learned.toStringWithSamples());
    String hint = Messages.RegexSuggester_CONCEPT_SIMILAR_SERIALNO_OPTION;
    suggestedRegex = new SuggestedRegex(learned, hint);
    if (newRegex(regexes, suggestedRegex.getRegexString())) {
      final ArrayList<SuggestedRegex> toRemove = new ArrayList<SuggestedRegex>();
      boolean addNewRegex = true;
      for (final SuggestedRegex sr : regexes) {
        if (sr.getRegexString().contains(suggestedRegex.getRegexString())) {
          toRemove.add(sr);
        }
        if (suggestedRegex.getRegexString().contains(sr.getRegexString())) {
          addNewRegex = false;
        }
      }
      for (final SuggestedRegex sr : toRemove) {
        regexes.remove(sr);
      }
      if (addNewRegex) {
        regexes.add(suggestedRegex);
      }
    } else {
      // CSS has learned same regex as basic learner
      regexes
          .get(0)
          .setHint(
              Messages.RegexSuggester_CONCEPT_SIMILAR_SERIALNO_OPTION);
    }
    // simplelearner
    samples = new ArrayList<Sample>();
    for (final String s : sampleStrings) {
      samples.add(new Sample(s));
    }
    // simple learner with min-no-of-sample = 1
    rIGALearner = new RIGAAlignFirst(samples, bias);
    learned = rIGALearner.learn();
    markClassOther(learned);
    final Expression suggested = learned;
    // GUI cannot deal with a leaf that is a samples alternation, but has a max / and or min value
    // unequal 1
    Expression.updateLeafSamples(suggested, sampleStrings);
    Expression.updateExpression(suggested, true, bias);
    hint = Messages.RegexSuggester_CONCEPT_SIMILAR_SERIALNO_OPTIONALPARTS_OPTION;
    suggestedRegex = new SuggestedRegex(suggested, hint);
    if (newRegex(regexes, suggestedRegex.getRegexString())) {
      final ArrayList<SuggestedRegex> toRemove = new ArrayList<SuggestedRegex>();
      boolean addNewRegex = true;
      for (final SuggestedRegex sr : regexes) {
        if (sr.getRegexString().contains(suggestedRegex.getRegexString())) {
          toRemove.add(sr);
        }
        if (suggestedRegex.getRegexString().contains(sr.getRegexString())) {
          addNewRegex = false;
        }
      }
      for (final SuggestedRegex sr : toRemove) {
        regexes.remove(sr);
      }
      if (addNewRegex) {
        regexes.add(suggestedRegex);
      }
    }
    // samples = new ArrayList<Sample>();
    // for (String s: sampleStrings) {
    // samples.add(new Sample(s));
    // }
    //
    // simple learner with min-no-of-sample = 1/3 * size_of(samples_list)
    // int minNoOfSamples = Math.max(Math.min(samples.size()/2, 4), 2);
    // learner = new RIGAAlignFirst(samples, minNoOfSamples);
    // learned = learner.learn();
    // suggested = Simplifier.simplifyAlternations(learned);
    // suggested = Simplifier.generalizeAlternations(suggested);
    // markClassOther(suggested);
    // suggestedRegex = new SuggestedRegex(suggested, learned);
    // if (newRegex(regexes, suggestedRegex.getRegexString())) {
    // ArrayList<SuggestedRegex> toRemove = new ArrayList<SuggestedRegex>();
    // boolean addNewRegex = true;
    // for (SuggestedRegex sr: regexes) {
    // if (sr.getRegexString().contains(suggestedRegex.getRegexString())) {
    // toRemove.add(sr);
    // }
    // if (suggestedRegex.getRegexString().contains(sr.getRegexString())) {
    // addNewRegex = false;
    // }
    // }
    // for (SuggestedRegex sr: toRemove) {
    // regexes.remove(sr);
    // }
    // if (addNewRegex) {
    // suggestedRegex.setExplanation("Try this regular expression if the concept is similar to a phone number (0049-7031-161254),\nserial number (C83YZNA, ISBN 0-596-52812-4) etc."
    // +
    // " and there are many optional parts.");
    // regexes.add(suggestedRegex);
    // }
    // }
    // System.out.println("REGEX SUGGESTER DONE");
    return regexes;
  }

  /**
   * if the Character class of a leaf is "OTHER", we always want to show an alternation of all
   * possible characters at this position. (The OTHER character class comprises all symbols that are
   * not contained in any of the other character classes, i.e. [^A-Za-z_ ]).
   * 
   * @param expr
   */
  private static void markClassOther(Expression expr) {
    final Traversal traversal = new Traversal(expr);
    while (traversal.hasNext()) {
      final ExpressionLeaf leaf = traversal.getNextLeaf();
      if (leaf.getType().equals(CharacterHierarchy.OTHER)) {
        leaf.setSamplesAlternation(true);
      }
    }
  }

  /**
   * checks if the same expression has not been suggested by one of the other learners already
   * 
   * @param regexes
   *          regexes suggested so far
   * @param regex
   *          the new regex string
   * @return true if the new regex string is not equal to a regex suggested already, false if it's
   *         not a new regex.
   */
  private static boolean newRegex(ArrayList<SuggestedRegex> regexes, String regex) {
    for (final SuggestedRegex sr : regexes) {
      if (regex.equals(sr.getRegexString())) {
        return false;
      }
    }
    return true;
  }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterHierarchy;
import com.ibm.biginsights.textanalytics.regex.learner.expression.AlternationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ConcatenationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionLeaf;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionNode;
import com.ibm.biginsights.textanalytics.regex.learner.learner.utils.ListUtilities;
import com.ibm.biginsights.textanalytics.regex.learner.learner.utils.Summarizer;

/**
 * 
 * 
 *         This class implements the RIGACommonSubstrings algorithm, which learns a regular
 *         expression from a list of samples. It extracts strings that occur in many of the samples
 *         ('common substrings') and determines the interchangeable ones of them. If necessary, it
 *         then partitions the samples into multiple concept groups and then aligns them.
 */

public class RIGACommonSubstrings extends RIGALearner {



  // CONSTRUCTOR
  public RIGACommonSubstrings(ArrayList<Sample> samples, Bias bias) {
    super(samples, bias);
  }

  /**
   * class CommonSubstring used to sort the common substrings by their number of occurrences in all
   * samples
   */
  private class CommonSubstring implements Comparable<CommonSubstring> {
    private final String commonSubstring;

    private final int occurrences;

    private CommonSubstring(String string, int o) {
      this.commonSubstring = string;
      this.occurrences = o;
    }

    @Override
    public int compareTo(CommonSubstring cs) {
      return (this.occurrences == cs.occurrences) ? 0 : ((this.occurrences > cs.occurrences) ? 1
          : -1);
    }

  }

  // METHODS

  /**
   * This method implements the learning algorithm.
   */
  @Override
  public Expression learn() {

    // if only one sample, return this... (no alignment necessary)
    if (getSampleExpressions().size() == 1) {
      return getSampleExpressions().get(0);
    }

    // summarize expressions
    ArrayList<Expression> summarizedSamples = new ArrayList<Expression>();
    for (final Expression expression : getSampleExpressions()) {
      summarizedSamples.add(Summarizer.summarizeCongruencesCaseInsensitive(expression));
    }

    // sort units of the samples by character class
    final HashMap<String, ArrayList<String>> sampleUnitsByCharClass = new HashMap<String, ArrayList<String>>();
    for (final Expression expression : summarizedSamples) {
      for (final Expression subexpression : ((ExpressionNode) expression).getSubexpressions()) {
        ArrayList<String> list = sampleUnitsByCharClass.get(subexpression.getType());
        if (list == null) {
          list = new ArrayList<String>();
          sampleUnitsByCharClass.put(subexpression.getType(), list);
        }
        list.add(((ExpressionLeaf) subexpression).getSample(0));
      }
    }

    // find the common substrings in each character class category
    Set<String> keys = sampleUnitsByCharClass.keySet();
    final HashMap<String, ArrayList<String>> commonSubstringsByCharClass = new HashMap<String, ArrayList<String>>();
    for (final String s : keys) {
      commonSubstringsByCharClass.put(s, getLongestCommonSubstrings(sampleUnitsByCharClass.get(s)));
    }

    // put all common substrings (of all character class categories) in one ArrayList
    ArrayList<String> commonSubstrings = new ArrayList<String>();
    keys = commonSubstringsByCharClass.keySet();
    for (final String key : keys) {
      commonSubstrings.addAll(commonSubstringsByCharClass.get(key));
    }

    // adjust summarized samples according to commonSubstrings,
    // e.g. Letter[nokiapremicell] --> Letter[nokia] Letter[premicell]
    // (if nokia or premicell or both are common substrings)
    summarizedSamples = adjustCommonSubstringUnits(summarizedSamples, commonSubstrings);

    // sort samples by sequence of character classes of the units (useful for disjunct concepts)
    final HashMap<String, ArrayList<Expression>> expressionsByCharClassSequence = new HashMap<String, ArrayList<Expression>>();
    for (final Expression expression : summarizedSamples) {
      String type = ""; //$NON-NLS-1$
      for (final Expression subexpression : ((ExpressionNode) expression).getSubexpressions()) {
        type += subexpression.getType();
      }
      if (expressionsByCharClassSequence.get(type) == null) {
        expressionsByCharClassSequence.put(type, new ArrayList<Expression>());
      }
      expressionsByCharClassSequence.get(type).add(expression);
    }

    // "align" expressions of each char class category --> collect samples of corresponding units
    // summarizedSamples now only contains one ConcatenationExpression per character class sequence
    summarizedSamples.clear();
    keys = expressionsByCharClassSequence.keySet();
    for (final String key : keys) {
      final ArrayList<Expression> nodes = expressionsByCharClassSequence.get(key);
      final ExpressionNode node = (ExpressionNode) nodes.get(0);
      for (int i = 1; i < nodes.size(); i++) {
        for (int j = 0; j < node.getSubexpressions().size(); j++) {
          final ExpressionLeaf subexpression = (ExpressionLeaf) node.getSubexpression(j);
          final String newSample = ((ExpressionLeaf) ((ExpressionNode) nodes.get(i))
              .getSubexpression(j)).getSample(0);
          if (!subexpression.getSamples().contains(newSample)) {
            subexpression.addSample(newSample);
            subexpression.setMaximum(Math.max(subexpression.getMaximum(), newSample.length()));
            subexpression.setMinimum(Math.min(subexpression.getMinimum(), newSample.length()));
          }
        }
      }
      summarizedSamples.add(node);
    }

    if (!this.bias.isUseDigitCommonSubstrings()) {
      // for partitioning, do not use "digit"- common substrings
      final ArrayList<String> cssWithoutDigits = new ArrayList<String>();
      for (final String css : commonSubstrings) {
        if (!CharacterHierarchy.getCharacterClass(css).getName().equals(CharacterHierarchy.DIGIT)) {
          cssWithoutDigits.add(css);
        }
      }
      commonSubstrings = cssWithoutDigits;
    }

    // get interchangeable strings out of summarized samples
    // e.g. if [Nokia nokia] and [nokia NOK] the list of interchangeable strings would be [Noka
    // nokia NOK]
    final ArrayList<ArrayList<String>> interchangeableStrings = getInterchangeableStrings(
        summarizedSamples, commonSubstrings);

    // add interchangeable common substrings
    for (final Expression expression : summarizedSamples) {
      addInterchangeables(expression, interchangeableStrings);
    }

    // common substrings sorted by their number of occurrences in the samples in ascending order.
    final ArrayList<String> sortedCSS = getSortedCommonSubstrings(summarizedSamples,
        interchangeableStrings);

    ExpressionNode result;
    // build expressions
    if (sortedCSS.isEmpty()) {
      ExpressionNode aligned = (ExpressionNode) summarizedSamples.get(0);
      for (int i = 1; i < summarizedSamples.size(); i++) {
        aligned = Aligner.align(aligned, (ExpressionNode) summarizedSamples.get(i));
      }
      result = aligned;
    } else {
      result = buildExpression(sortedCSS, interchangeableStrings, summarizedSamples);
    }

    // update expression
    Expression.updateExpression(result, true, this.bias);

    return result;

  }

  /**
   * add all interchangeable strings to each leaf...
   * 
   * @param expression
   *          expression which is to be changed by adding interchangeable samples
   * @param interchangeables
   *          lists of mutually interchangeable strings
   */
  private void addInterchangeables(Expression expression,
      ArrayList<ArrayList<String>> interchangeables) {
    if (expression instanceof ExpressionNode) {
      for (final Expression subexpr : ((ExpressionNode) expression).getSubexpressions()) {
        addInterchangeables(subexpr, interchangeables);
      }
    } else {
      final ExpressionLeaf leaf = (ExpressionLeaf) expression;
      for (final ArrayList<String> inters : interchangeables) {
        if (inters.contains(leaf.getSample(0))) {
          int size = 0;
          for (final String inter : inters) {
            size = Math.max(size, inter.length());
            if (!leaf.getSamples().contains(inter)) {
              leaf.addSample(inter);
            }

          }
          if (!leaf.getType().equals(CharacterHierarchy.DIGIT)
              || (leaf.getType().equals(CharacterHierarchy.DIGIT) && (leaf.getSamples().size() < 5))) {
            leaf.setSamplesAlternation(true);
          }
          break;
        }
      }
    }

  }

  /**
   * This method partitions the sample expressions first by the most frequently occurring
   * interchangeable substring group, and then each of the subsets by the second frequently ocurring
   * substring etc. If the sets cannot be partitioned further (because there are no more
   * interchangeable substrings left), the expressions are aligned using Needleman-Wunsch and the
   * Node is built up recursively.
   * 
   * @param ArrayList
   *          <String> sortedCSS --> commonSubstrings sorted in ascending order
   * @param ArrayList
   *          <ArrayList<String>> interchangeableStrings --> lists of mutually interchangeable
   *          substrings
   * @param ArrayList
   *          <Expression> summarizedSamples --> sample expressions to be aligned.
   * @return one expression containing the final alignment.
   */
  private static ExpressionNode buildExpression(ArrayList<String> sortedCSS,
      ArrayList<ArrayList<String>> interchangeableStrings, ArrayList<Expression> summarizedSamples) {
    final ExpressionSubset with = new ExpressionSubset();
    final ExpressionSubset without = new ExpressionSubset();
    final String string = sortedCSS.get(0);
    final ArrayList<String> inters = getInterchangeables(string, interchangeableStrings);
    for (final Expression node : summarizedSamples) {
      boolean withString = false;
      for (final Expression leaf : ((ExpressionNode) node).getSubexpressions()) {
        for (final String inter : inters) {
          if (((ExpressionLeaf) leaf).getSamples().contains(inter)) {
            withString = true;
          }
        }
      }
      if (withString) {
        with.addExpression((ConcatenationExpression) node);
      } else {
        without.addExpression((ConcatenationExpression) node);
      }
    }

    defineNexts(with, sortedCSS, 1, interchangeableStrings);
    defineNexts(without, sortedCSS, 1, interchangeableStrings);

    with.buildAlignedExpression(interchangeableStrings);
    without.buildAlignedExpression(interchangeableStrings);

    ExpressionNode result = null;
    if ((with.getAlignedExpression() != null) && (without.getAlignedExpression() != null)) {
      result = new AlternationExpression();
      if (with.getAlignedExpression() instanceof AlternationExpression) {
        for (final Expression subexpr : with.getAlignedExpression().getSubexpressions()) {
          result.addSubexpression(subexpr);
        }
      } else {
        result.addSubexpression(with.getAlignedExpression());
      }
      if (without.getAlignedExpression() instanceof AlternationExpression) {
        for (final Expression subexpr : without.getAlignedExpression().getSubexpressions()) {
          result.addSubexpression(subexpr);
        }
      } else {
        result.addSubexpression(without.getAlignedExpression());
      }
    } else if ((with.getAlignedExpression() != null) && (without.getAlignedExpression() == null)) {
      result = with.getAlignedExpression();
    } else if ((with.getAlignedExpression() == null) && (without.getAlignedExpression() != null)) {
      result = without.getAlignedExpression();
    }
    return result;
  }

  /**
   * partitions nextWith and nextWithout according to the next common substring and its
   * interchangeables
   * 
   * @param ExpressionSubset
   *          expr --> expression subset to be partitioned further
   * @param ArrayList
   *          <String> sortedCSS --> common substrings sorted in ascending order
   * @param int subexprIndex --> current subexprIndex of the sortedCSS
   * @param ArrayList
   *          <ArrayList<String>> interchangeables --> substrings that are mutually interchangeable
   */
  private static void defineNexts(ExpressionSubset exprSubset, ArrayList<String> sortedCSS,
      int index, ArrayList<ArrayList<String>> interchangeables) {
    if (index < sortedCSS.size()) {
      final String nextCSS = sortedCSS.get(index);
      exprSubset.defineNext(nextCSS);
      index++;
      if (index < sortedCSS.size()) {
        if (exprSubset.getNextWith() != null) {
          defineNexts(exprSubset.getNextWith(), sortedCSS, index, interchangeables);
        }
        if (exprSubset.getNextWithout() != null) {
          defineNexts(exprSubset.getNextWithout(), sortedCSS, index, interchangeables);
        }
      }
    }
  }

  /**
   * @param String
   *          s1 and s2 --> Strings to be compared (if they are in an interchangeable list)
   * @param ArrayList
   *          <ArrayList<String>> --> the interchangeable strings
   * @return true if two strings are in an interchangeable list, false otherwise
   */
  private static boolean areCSS(String s1, String s2, ArrayList<ArrayList<String>> interchangeables) {
    for (final ArrayList<String> list : interchangeables) {
      if (list.contains(s1) && list.contains(s2)) {
        return true;
      }

    }
    return false;
  }

  /**
   * finds all substrings that occur in at least 2 of the samples (--> if stricter requirements,
   * (e.g. substrings that occur in at least 3/4/5/.. samples), throw the others away later) and
   * that have a minimum length of MIN_COMMON_SUBSTRING_LENGTH (defined in Constants)
   * 
   * @param textSamples
   * @return commonSubstrings ArrayList
   */
  private ArrayList<String> getLongestCommonSubstrings(ArrayList<String> textSamples) {
    final ArrayList<String> commonSubstrings = new ArrayList<String>();
    for (final String sample : textSamples) {
      for (final String sample2 : textSamples) {
        if (!(sample == sample2)) {
          final String lcs = longestCommonSubstring(sample, sample2);
          if (!commonSubstrings.contains(lcs) && !(lcs == null)) {
            commonSubstrings.add(lcs);
          }
        }
      }
    }
    // copy common substrings
    final ArrayList<String> commonSubstringsCopy = new ArrayList<String>();
    commonSubstringsCopy.clear();
    for (final String s : commonSubstrings) {
      commonSubstringsCopy.add(new String(s));
    }
    // remove common substrings that are contained in other common substrings
    for (final String m : commonSubstringsCopy) {
      for (int i = commonSubstrings.size() - 1; i >= 0; i--) {
        if (m.contains(commonSubstrings.get(i)) && (commonSubstrings.get(i).length() < m.length())) {
          commonSubstrings.remove(i);
        }
      }
    }
    return commonSubstrings;
  }

  /**
   * @param string1
   *          and string2 to be compared
   * @return the longest common contiguous substring, returns null if there is no common substring
   *         as long or longer than
   */
  public String longestCommonSubstring(String string1, String string2) {
    String lcs = null;
    String result = null;
    for (int start = 0; start < string1.length() + 1; start++) {
      result = null;
      for (int end = string1.length(); end >= start + this.bias.getMinCommonSubstringLength(); end--) {

        if (this.bias.isCaseSensitiveCommonSubstrings()) {
          // case sensitive matching
          if (string2.contains(string1.substring(start, end))) {
            result = string1.substring(start, end);
            break;
          }
        } else {
          // case insensitive matching
          if (string2.toLowerCase().contains(string1.substring(start, end).toLowerCase())) {
            result = string1.substring(start, end);
            break;
          }
        }
      }
      if (result != null) {
        if (lcs == null) {
          lcs = result;
        } else {
          if (result.length() > lcs.length()) {
            lcs = result;
          }
        }
      }
    }
    return lcs;
  }

  /**
   * splits leafs that contain two common substrings into two leafs
   * 
   * @param ArrayList
   *          <Expression> summarizedSamples
   * @param ArrayList
   *          <String> commonSubstrings
   * @return ArrayList<Expression>, leafs containing common substrings are now split into multiple
   *         leafs (useful for a better alignment) e.g. Letter[nokiapremicell] --> Letter[nokia]
   *         Letter[premicell] (if nokia and/or premicell is a common substring)
   */
  private static ArrayList<Expression> adjustCommonSubstringUnits(
      ArrayList<Expression> summarizedSamples, ArrayList<String> commonSubstrings) {
    // change sample expressions according to common substrings
    for (int i = 0; i < summarizedSamples.size(); i++) {
      final ConcatenationExpression changed = new ConcatenationExpression();
      for (final Expression subexpression : ((ExpressionNode) summarizedSamples.get(i))
          .getSubexpressions()) {
        // the sample expressions only contain exactly one sample per subexpression
        final String sample = ((ExpressionLeaf) subexpression).getSample(0);
        changed.addSubexpressions(getAdjustedUnits(sample, commonSubstrings));
      }
      summarizedSamples.set(i, changed);
    }
    return summarizedSamples;
  }

  /**
   * splits leafs that contain two common substrings into two leafs
   * 
   * @param String
   *          sample --> sample String of a certain leaf to be checked for commonSubstrings.
   * @return If a common substring is found, the sample string is split into multiple leafs.
   *         Otherwise, just one leaf containing this sample is returned.
   */
  private static ArrayList<Expression> getAdjustedUnits(String sample,
      ArrayList<String> commonSubstrings) {
    final ArrayList<Expression> adjustedUnits = new ArrayList<Expression>();
    String sampleType = CharacterHierarchy.getCharacterClass(sample).getName();
    // case insensitive...
    if (sampleType.equals(CharacterHierarchy.LOWER) || sampleType.equals(CharacterHierarchy.UPPER)) {
      sampleType = CharacterHierarchy.LETTER;
    }
    boolean found = false;
    for (final String commonSubstring : commonSubstrings) {
      if (sample.equals(commonSubstring)) {
        final ExpressionLeaf leaf = new ExpressionLeaf(sampleType);
        leaf.setMinimum(sample.length());
        leaf.setMaximum(sample.length());
        leaf.addSample(commonSubstring);
        adjustedUnits.add(leaf);
        found = true;
      } else if (sample.contains(commonSubstring)) {
      	String delimString = Pattern.quote (commonSubstring);		// split() expects a regex, quote the commonSubstring so its characters are not treated
                                                            		// as regex symbol; e.g., defect 56832 was caused by a '+' in commonSubstring.
      	final String[] samples = sample.split(delimString);
        for (int j = 0; j < samples.length; j++) {
          if (j != 0) {
            final ExpressionLeaf leaf = new ExpressionLeaf(sampleType);
            leaf.setMinimum(commonSubstring.length());
            leaf.setMaximum(commonSubstring.length());
            leaf.addSample(commonSubstring);
            adjustedUnits.add(leaf);
          }
          if (!samples[j].equals("")) { //$NON-NLS-1$
            adjustedUnits.addAll(getAdjustedUnits(samples[j], commonSubstrings));
            if (sample.endsWith(commonSubstring) && (j == samples.length - 1)) {
              final ExpressionLeaf leaf = new ExpressionLeaf(sampleType);
              leaf.setMinimum(commonSubstring.length());
              leaf.setMaximum(commonSubstring.length());
              leaf.addSample(commonSubstring);
              adjustedUnits.add(leaf);
            }
          }
          found = true;
        }
        break;
      }
    }
    if (!found) {
      final ExpressionLeaf leaf = new ExpressionLeaf(sampleType);
      leaf.setMinimum(sample.length());
      leaf.setMaximum(sample.length());
      leaf.addSample(sample);
      adjustedUnits.add(leaf);
    }
    return adjustedUnits;
  }

  /**
   * sorts the common substrings list by the number of occurrences of the common substrings in
   * ascending order (most often occurring one first)
   * 
   * @param ArrayList
   *          <Expression> summarizedSamples
   * @param ArrayList
   *          <ArrayList<String>> interchangeableStrings
   * @return ArrayList<String> common substrings sorted by their number of occurrences in the
   *         samples in ascending order.
   */
  private ArrayList<String> getSortedCommonSubstrings(ArrayList<Expression> summarizedSamples,
      ArrayList<ArrayList<String>> interchangeableStrings) {
    // find list of interchangeable strings
    final ArrayList<String> interchangeables = new ArrayList<String>();
    // print interchangeable List
    for (final ArrayList<String> list : interchangeableStrings) {
      for (final String s : list) {
        if (list.size() >= 1) {
          interchangeables.add(new String(s));
        }
      }
    }

    ArrayList<CommonSubstring> sortedCSS = new ArrayList<CommonSubstring>();
    for (final String css : interchangeables) {
      int occurrences = 0;
      for (final Expression node : summarizedSamples) {
        for (final Expression subexpr : ((ExpressionNode) node).getSubexpressions()) {
          if (((ExpressionLeaf) subexpr).getSamples().contains(css)) {
            occurrences++;
          }
        }
      }
      sortedCSS.add(new CommonSubstring(css, occurrences));
    }
    Collections.sort(sortedCSS);
    // eliminate "doubles" (interchangeables), keep the one that occurs most often
    final ArrayList<CommonSubstring> copy = new ArrayList<CommonSubstring>();
    for (int i = sortedCSS.size() - 1; i >= 0; i--) {
      boolean add = true;
      if (copy.size() == 0) {
        copy.add(sortedCSS.get(i));
      }
      for (final CommonSubstring css : copy) {
        if (areCSS(css.commonSubstring, sortedCSS.get(i).commonSubstring, interchangeableStrings)) {
          add = false;
        }
      }
      if (add) {
        copy.add(sortedCSS.get(i));
        add = false;
      }
    }
    sortedCSS = copy;
    final ArrayList<String> sortedCommonSubstrings = new ArrayList<String>();
    for (final CommonSubstring css : sortedCSS) {
      sortedCommonSubstrings.add(css.commonSubstring);
    }
    return sortedCommonSubstrings;
  }

  /**
   * get interchangeable strings out of summarized samples e.g. if [Nokia nokia] and [nokia NOK] the
   * list of interchangeable strings would be [Noka nokia NOK]
   * 
   * @param ArrayList
   *          <Expression> summarizedSamples
   * @return ArrayLists of ArrayLists<String> containing mutually interchangeable strings
   */
  private static ArrayList<ArrayList<String>> getInterchangeableStrings(
      ArrayList<Expression> summarizedSamples, ArrayList<String> commonSubstrings) {
    // 1) sort subexpressions into hash map depending on char class
    final HashMap<String, ArrayList<Expression>> sampleLists = sortUnitsByCharClass(summarizedSamples);
    // 2) find lists with interchangeable samples within in the char class categories
    final ArrayList<ArrayList<String>> interchangeableStrings = new ArrayList<ArrayList<String>>();
    final Set<String> keys = sampleLists.keySet();
    for (final String key : keys) {
      // all subexpressions of a certain char class category
      final ArrayList<Expression> sampleList = sampleLists.get(key);
      for (int i = 0; i < sampleList.size(); i++) {
        // only create interchangeables lists for common substrings
        if (ListUtilities.intersect(commonSubstrings,
            ((ExpressionLeaf) sampleList.get(i)).getSamples()) != null) {
          boolean inNoList = true;
          if (i != 0) {
            // check if items of this list occur in one of the interchangeable strings already, if
            // yes, next list
            for (final ArrayList<String> list : interchangeableStrings) {
              if (ListUtilities.intersect(list, ((ExpressionLeaf) sampleList.get(i)).getSamples()) != null) {
                inNoList = false;
              }
            }
          }
          if (inNoList) {
            boolean changed = true;
            while (changed) {
              changed = false;
              final ExpressionLeaf leaf1 = (ExpressionLeaf) sampleList.get(i);
              final ArrayList<String> list1 = leaf1.getSamples();
              for (int j = 0; j < sampleList.size(); j++) {
                final ExpressionLeaf leaf2 = (ExpressionLeaf) sampleList.get(j);
                if (!(leaf1 == leaf2)) {
                  final ArrayList<String> list2 = leaf2.getSamples();
                  if (ListUtilities.intersect(list1, list2) != null) {
                    for (final String s : list2) {
                      if (!list1.contains(s)) {
                        list1.add(s);
                        changed = true;
                      }
                    }
                  }
                }
              }
            }
            interchangeableStrings.add(((ExpressionLeaf) sampleList.get(i)).getSamples());
          }
        }
      }
    }
    return interchangeableStrings;
  }

  /**
   * returns array list of interchangeable strings of string s
   * 
   * @param String
   *          s
   * @param ArrayList
   *          <ArrayList<String>> interchangeables (lists of mutually interchangeable strings)
   * @return if the String is contained in one of the interchangeable lists, return this list,
   *         otherwise return null.
   */
  protected static ArrayList<String> getInterchangeables(String s,
      ArrayList<ArrayList<String>> interchangeables) {
    for (final ArrayList<String> list : interchangeables) {
      if (list.contains(s)) {
        return list;
      }
    }
    return null;
  }

  /**
   * @param ArrayList
   *          <Expression> samples
   * @return HashMap<String, ArrayList<Expression>> units of the sample expressions sorted depending
   *         on their character class type
   */
  private static HashMap<String, ArrayList<Expression>> sortUnitsByCharClass(
      ArrayList<Expression> samples) {
    final HashMap<String, ArrayList<Expression>> sampleLists = new HashMap<String, ArrayList<Expression>>();
    for (final Expression expression : samples) {
      for (final Expression subexpression : ((ExpressionNode) expression).getSubexpressions()) {
        String type = subexpression.getType();
        if (type.equals(CharacterHierarchy.UPPER) || type.equals(CharacterHierarchy.LOWER)) {
          type = CharacterHierarchy.LETTER;
        }
        if (sampleLists.get(type) == null) {
          sampleLists.put(type, new ArrayList<Expression>());
        }
        sampleLists.get(type).add(subexpression);
      }
    }
    return sampleLists;
  }

}

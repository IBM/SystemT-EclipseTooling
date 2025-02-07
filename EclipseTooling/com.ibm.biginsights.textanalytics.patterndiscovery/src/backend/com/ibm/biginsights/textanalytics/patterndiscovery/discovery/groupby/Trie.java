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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher.RuleBasedHasher;

/**
 *  Data Structure containing sequences of Integers and allows for fast lookup of an object of type T
 *         they are associated with. Allows for two things: - Match sequences of integers against it. Returns which of
 *         the sequences contained in the Trie match the input sequence. - Match (sorted) integer sets against it.
 *         Returns which subsets of it are stored with it. The class is generic wrt the Object that identifies the
 *         contained sequences. Used in {@link RuleBasedHasher} for efficient sequence matching.
 *  Fixed bugs with getting matches recursively, added additional methods for getting longest match
 */
public class Trie<T>
{

  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  private TrieNode root = new TrieNode (0);

  /**
   * Store the identifier for lookup under the given sequence.
   * 
   * @param sequence
   * @param identifier
   */
  public void present (List<Integer> sequence, T identifier)
  {
    presentR (sequence, identifier, 0, root);
  }

  private void presentR (List<Integer> sequence, T identifier, int depth, TrieNode node)
  {
    // find or create child
    Integer here = sequence.get (depth);
    TrieNode child = node.children.get (here);
    if (child == null) {
      child = new TrieNode (here);
      node.children.put (here, child);
    }
    // termination: end of sequence
    if (depth + 1 == sequence.size ()) {
      child.identifiers.add (identifier);
    }
    else {
      // recurr over child
      presentR (sequence, identifier, depth + 1, child);
    }
  }

  /**
   * Get the identifiers stored for all subsequences of the given sequence.
   * 
   * @param sequence
   * @return
   */
  public Collection<T> getMatches (List<Integer> sequence)
  {
    return getMatches (sequence, new HashSet<Integer> ());
  }

  /**
   * Get the identifiers stored for all subsequences of the given sequence. Returns ALL matches (expensive)
   * 
   * @param sequence
   * @param unmatchedTokens. A collection that will be filled with all tokens in the sequence that do not correspond to
   *          any identifier in the trie.
   * @return
   */
  public Collection<T> getMatches (List<Integer> sequence, Collection<Integer> unmatchedTokens)
  {
    unmatchedTokens.addAll (sequence);
    Set<T> matches = new HashSet<T> ();
    // start at any position
    for (int i = 0; i < sequence.size (); i++) {
      getMatchesR (sequence, i, i, root, matches, unmatchedTokens);
    }
    return matches;
  }

  /**
   * Recursive function used in getMatches
   * 
   * @param sequence
   * @param position
   * @param startPos
   * @param node
   * @param output
   * @param unmatchedTokens
   */
  private void getMatchesR (List<Integer> sequence, int position, int startPos, TrieNode node, Set<T> output,
    Collection<Integer> unmatchedTokens)
  {
    if (position >= sequence.size ()) return;
    Integer here = sequence.get (position);
    TrieNode child = node.children.get (here);
    if (child != null) {
      output.addAll (child.identifiers);
      if (child.identifiers.size () > 0) {
        unmatchedTokens.removeAll (sequence.subList (startPos, position + 1));
      }
      getMatchesR (sequence, position + 1, startPos, child, output, unmatchedTokens);
    }
  }

  /**
   * Gets longest match and returns only the one longest match rather than the entire set of possibilities IMPORTANT:
   * Takes care of subsumed entries here. Ex. can be reached at - if "can be" is found to be the longest match, function
   * will go through and only return "can be" and not "can be" and "be"
   * 
   * @param sequence
   * @param unmatchedTokens
   * @return
   * @throws PatternDiscoveryException
   */
  public Collection<T> getLongestMatches (String toHash, List<Integer> sequence, Collection<Integer> unmatchedTokens,
    Map<Integer, String> seqMap) throws PatternDiscoveryException
  {
    unmatchedTokens.addAll (sequence);
    Set<T> matches = new HashSet<T> ();
    // start at any position
    int curOffset = 0;
    int count = 0;
    HashMap<Integer, Integer> checkMap = new HashMap<Integer, Integer> ();
    while (curOffset < sequence.size ()) {
      getLongestMatchesR (toHash, sequence, curOffset, curOffset, root, matches, unmatchedTokens, seqMap);

      // Find the length of the newly added sequence
      Integer[] matchArr = matches.toArray (new Integer[matches.size ()]);
      String seq = "";
      for (Integer a : matchArr) {
        if (!checkMap.containsKey (a)) {
          seq = seqMap.get (a);
        }
        checkMap.put (a, 1);
      }

      try {
        if (seq != null) {
          String[] seqArr = seq.split (" ");
          int move = seqArr.length;

          if (move > 0) move = move - 1;
          curOffset = curOffset + move + 1;
        }
        count++;
      }
      catch (NullPointerException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GROUPING_SEQ_MAP_NULL_POINTER);
      }

    }
    return matches;
  }

  boolean foundTest = false;

  private boolean getLongestMatchesR (String toHash, List<Integer> sequence, int position, int startPos, TrieNode node,
    Set<T> output, Collection<Integer> unmatchedTokens, Map<Integer, String> seqMap)
  {
    foundTest = false;
    if (position >= sequence.size ()) return false;
    Integer here = sequence.get (position);
    TrieNode child = node.children.get (here);
    if (child != null) {
      boolean foundSomethingBelow = getLongestMatchesR (toHash, sequence, position + 1, startPos, child, output,
        unmatchedTokens, seqMap);
      if (!foundSomethingBelow) {
        output.addAll (child.identifiers);
        if (child.identifiers != null && child.identifiers.size () > 0) foundTest = true;
        if (child.identifiers.size () > 0) {
          unmatchedTokens.removeAll (sequence.subList (startPos, position + 1));
        }
      }
    }
    // Deals with the corner case where the entire token length is only 1
    // if (sequence.size()==1 && output.isEmpty()){
    if (output.isEmpty ()) {
      Collection<Integer> rootNode = new LinkedList<Integer> ();
      rootNode.add (here);
      // Checks if the token is a sequence or merely a word int (infrequent)
      if (seqMap.containsKey (here)) {
        if (toHash.contains (seqMap.get (here))) {
          // if (seqMap.get(here).equalsIgnoreCase(toHash)){
          output.addAll ((Collection<? extends T>) rootNode);
          unmatchedTokens.remove (rootNode);
          // unmatchedTokens.clear();
        }
      }
    }
    return foundTest;
  }

  /**
   * Get the identifiers stored for all subsequences of the given sequence while the matching may skip elements of the
   * sequence. If the sequence is a sorted list of integers this corresponds to subset matching.
   * 
   * @param sequence
   * @return
   */
  public Collection<T> getSubsets (List<Integer> sequence)
  {
    Set<T> matches = new HashSet<T> ();
    // start at any position
    getSubSetsR (sequence, 0, root, matches);
    return matches;
  }

  public void getSubSetsR (List<Integer> sequence, int position, TrieNode node, Set<T> output)
  {
    if (position >= sequence.size ()) return;
    Integer here = sequence.get (position);
    TrieNode child = node.children.get (here);
    if (child != null) {
      output.addAll (child.identifiers);
      getSubSetsR (sequence, position + 1, child, output);
    }
    getSubSetsR (sequence, position + 1, node, output);
  }

  @Override
  public String toString ()
  {
    return root.toString ();
  }

  /**
   * 
   */
  public class TrieNode
  {
    private HashMap<Integer, TrieNode> children = new HashMap<Integer, TrieNode> ();
    private int label;
    private Collection<T> identifiers = new HashSet<T> ();

    public TrieNode (int label)
    {
      this.label = label;
    }

    @Override
    public String toString ()
    {
      String str = label + ":::";

      for (TrieNode child : children.values ()) {
        str += child + " --- ";
      }

      return str;
    }
  }

}

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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.HashRule;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Trie;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis.SequenceLoader;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.SequenceOrganizer;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.nlp.DefaultWordIntegerMapping;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.nlp.WordIntegerMapping;

/**
 * Incorporates fuzzy grouping rules. Hash value computation and the application of rules to account the presence and
 * absence of subsequences. The hash score of a String m is computed as a function of a set S of sequences identified in
 * m. An initial version of S, S' is identified by finding all relevant sub-sequences. Rules are functions S->c(S).
 * Where OS is a set of modifications on S that may add or remove items. This Hasher computes c(s) for all rules,
 * resolves conflicts on them and then computes S from S' prior to hash-value computation. Conflict resolution strategy:
 * - apply all modifications that add items to S until they converge (currently not applicable) - then, apply all
 * deletions (one pass should suffice) The rules can be of the following form
 * <ul>
 * <li>DROP: If a given set of sequences (X1...Xn) is present, drop sequence Y. Syntax: DROP Y IF X1 AND ... AND Xn
 * <li>ADD: If a given set of sequences (X1...Xn) is present, ADD sequence Y. Syntax: ADD Y IF X1 AND ... AND Xn
 * </ul>
 * Sequences can be specified by their sequence number or as a String. Assumptions prior to dropping: - Numbers are
 * normalized - Order is discarded - Words not corresponding to an entry in the list of relevant sequences or present in
 * a rule body, contribute to the hash value with their string hash (TODO: make this optional!) TODO: Possible alternate
 * implementation: The applicable rules are identified by joining a table of rule antecedants with the sequence_matches
 * table. This Hasher has two modes of processing which can be chose at instruction time. The default configuration
 * hashes Strings and operates exactly as described above. The other ("setMode") mode (initialized when the constructor
 * without datablase URL is called) hashes based on sets of sequences and is useful when mining outputs already the
 * contained unsubsumed sequences.
 * 
 * 
 */
public class RuleBasedHasher implements HashFactory
{


 
	private DebugDBProcessor db;
  private WordIntegerMapping mapping;
  private Trie<HashRule> ruleTrie;
  private Trie<Integer> sequenceTrie;
  private boolean useInfrequentWords = true;
  private boolean setMode = false;
  Map<Integer, String> sequenceIDMap;
  HashMap<String, Integer> sequenceString2ID;
  ExperimentProperties properties;
  private SequenceOrganizer seqOrganizer;

  public RuleBasedHasher (String dbUrl, InputStream rules, InputStream relevantSequences, boolean useInfrequentWords,
    ExperimentProperties properties, Map<Integer, String> seqMap) throws PatternDiscoveryException
  {
    seqOrganizer = new SequenceOrganizer ();
    this.useInfrequentWords = useInfrequentWords;
    db = new DebugDBProcessor (dbUrl);
    db.setProperties (properties);

    // create WordIntegerMapping and load dictionary from DB
    // mapping = new NoMarkupWordIntegerMapping(false);
    loadDictionary ();
    this.properties = properties;
    this.sequenceIDMap = seqMap;
    // load rules (assumes one rule per line)
    Collection<HashRule> dropRules = parseRules (rules);
    // generate automaton/trie for rules
    if (Constants.DEBUG) System.out.println ("generating rule trie");
    createRuleTrie (dropRules);
    // generate automaton/trie to find relevant sequences (only those
    // present in at least one rule)
    // list all relevant sequenceIDs from rules
    Set<Integer> sequenceIDs = new HashSet<Integer> ();
    for (HashRule dropRule : dropRules) {
      sequenceIDs.addAll (Arrays.asList (dropRule.condition));
    }
    // add all sequences designated in a list of relevant sequences
    addRelevantSequences (relevantSequences, sequenceIDs);

    // retrieve the sequences for the sequence IDs.
    createSequenceTrie ();
    db.shutdown ();
  }

  /**
   * Constructor for set mode.
   * 
   * @param rules
   * @param relevantSequences
   * @param useInfrequentWords
   * @throws PatternDiscoveryException
   */
  public RuleBasedHasher (InputStream rules, InputStream relevantSequences, boolean useInfrequentWords,
    ExperimentProperties properties) throws PatternDiscoveryException
  {
    this.setMode = true;
    // load rules (assumes one rule per line)
    Collection<HashRule> dropRules = parseRules (rules);
    // generate automaton/trie for rules
    if (Constants.DEBUG) System.out.println ("generating rule trie");
    this.properties = properties;
    createRuleTrie (dropRules);
  }

  /**
   * Generates SequenceTrie from sequence table
   * 
   * @throws PatternDiscoveryException
   * @throws SQLException
   */
  private void createSequenceTrie () throws PatternDiscoveryException
  {
    int maxSequenceSize = Integer.parseInt (properties.getProperty (PropertyConstants.SEQUENCE_MAX_SIZE));
    int minSequenceSize = Integer.parseInt (properties.getProperty (PropertyConstants.SEQUENCE_MIN_SIZE));

    sequenceTrie = new Trie<Integer> ();
    if (Constants.DEBUG) System.out.println ("getting sequences and generating trie");
    // do this with one sequential scan over the db
    String sql = String.format ("SELECT sequenceID,wordID FROM sequences ORDER BY sequenceID,pos ASC");

    try {
      ResultSet sequence = db.readFromDB (sql);

      Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>> ();

      while (sequence.next ()) {
        Integer sequenceID = sequence.getInt (1);
        Integer wordID = sequence.getInt (2);

        if (map.containsKey (sequenceID)) {
          map.get (sequenceID).add (wordID);
        }
        else {
          List<Integer> list = new LinkedList<Integer> ();
          list.add (wordID);
          map.put (sequenceID, list);
        }
      }

      for (Integer sequenceID : map.keySet ()) {
        List<Integer> seq = map.get (sequenceID);
        if (seq.size () <= maxSequenceSize && seq.size () >= minSequenceSize)
          sequenceTrie.present (map.get (sequenceID), sequenceID);
      }
      // int lastSequence = -1;
      // List<Integer> thisSequence = new LinkedList<Integer> ();
      // while (sequence.next ()) {
      // int sequenceID = sequence.getInt (1);
      // int wordID = sequence.getInt (2);
      // if (sequenceID != lastSequence) {
      // if (thisSequence.size () > 0) sequenceTrie.present (thisSequence, lastSequence);
      // thisSequence = new LinkedList<Integer> ();
      // lastSequence = sequenceID;
      // }
      // thisSequence.add (wordID);
      // }
    }
    catch (SQLException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_LOAD_SEQUENCE_TRIE_DB_ERR);
    }
  }

  /**
   * @param relevantSequences
   * @param sequenceIDs
   * @throws PatternDiscoveryException
   */
  private void addRelevantSequences (InputStream relevantSequences, Set<Integer> sequenceIDs) throws PatternDiscoveryException
  {

    try {
      BufferedReader br2 = new BufferedReader (new InputStreamReader (relevantSequences, GroupByNewProcessor.ENCODING));

      while (br2.ready ()) {
        String line = br2.readLine ();
        sequenceIDs.add (Integer.parseInt (line));
      }

      br2.close ();
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RELEVANT_SEQUENCE_WRITE_ERR);
    }
  }

  /**
   * @param dropRules
   */
  private void createRuleTrie (Collection<HashRule> dropRules)
  {
    ruleTrie = new Trie<HashRule> ();
    for (HashRule hashRule : dropRules) {
      ruleTrie.present (Arrays.asList (hashRule.condition), hashRule);
    }
  }

  /**
   * Parse rule file
   * 
   * @param rules
   * @return
   * @throws PatternDiscoveryException
   */
  private Collection<HashRule> parseRules (InputStream rules) throws PatternDiscoveryException
  {
    if (Constants.DEBUG) System.out.println ("parsing drop rules");
    Collection<HashRule> dropRules = new LinkedList<HashRule> ();

    try {
      BufferedReader br = new BufferedReader (new InputStreamReader (rules, GroupByNewProcessor.ENCODING));

      while (br.ready ()) {
        String line = br.readLine ();
        if (line == null) break;
        if (line.trim ().startsWith ("#")) continue;
        dropRules.add (new HashRule (line));
      }

      br.close ();
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RULES_READ_ERR);
    }
    return dropRules;
  }

  private void loadDictionary () throws PatternDiscoveryException
  {
    mapping = new DefaultWordIntegerMapping ();
    if (Constants.DEBUG) System.out.println ("Loading dictionary");
    try {
      ResultSet words = db.readFromDB ("SELECT wordID, surface FROM app.dictionary ORDER BY wordID");
      while (words.next ()) {
        String surface = words.getString ("surface");
        int num = mapping.intForWord (surface);
        // test num against wordID
        if (num != words.getInt ("wordID")) { throw new RuntimeException ("missing number in dictionary: "
          + words.getInt ("wordID") + " - " + num); }
      }
    }
    catch (SQLException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_LOAD_DICTIONARY_DB_ERR);
    }
  }

  /**
   * Computes the actual hash score on the set Implemented FNV instead of previous hash with 1000th prime number
   * 
   * @param sSet
   * @return
   */
  public int hashScore (Collection<Integer> sSet)
  {

    long offset = new Long ("2166136261");
    int FNV_prime = 16777619;
    for (Integer c : sSet) {
      offset = offset ^ c;
      offset = offset * FNV_prime;
    }
    return (int) offset;
  }

  /*
   * (non-Javadoc)
   * @see com.ibm.avatar.discovery.groupby.HashFactory#hash(java.lang.String)
   */
  // @Override
  public int hash (String s, ExperimentProperties properties, CSVWriter writer) throws IOException, PatternDiscoveryException
  {
    if (setMode) throw new RuntimeException ("Hashing strings not allowed in set mode.");
    Collection<Integer> sSet = computeS (s, properties, writer);
    return hashScore (sSet);
  }

  /**
   * Hashing in set mode.
   * 
   * @param matchingSequences The matching sequences (features) of the entity to be matched
   * @param unmatched The set of words in the string that is to be hashed that does not belong to any frequent sequence.
   *          May be incorporated into the hash value if the hasher is configured accordingly.
   * @return
   */
  public int hash (Collection<Integer> matchingSequences, Collection<Integer> unmatched)
  {
    if (!setMode) throw new RuntimeException ("Hashing sets not allowed in default(String) mode.");
    Collection<Integer> sSet = applyRules (matchingSequences, unmatched);
    return hashScore (sSet);
  }

  /**
   * @param sPrime
   * @param unmatched
   * @return
   */
  private Collection<Integer> applyRules (Collection<Integer> sPrime, Collection<Integer> unmatched)
  {
    // get also the tokens that are in no match to add the respective words
    // directly to hashing
    Integer[] matchesSorted = sPrime.toArray (new Integer[sPrime.size ()]);
    Arrays.sort (matchesSorted);
    List<Integer> sPrimeSorted = Arrays.asList (matchesSorted);
    // truncate sPrime to size 500
    if (sPrimeSorted.size () > 500) {
      if (Constants.DEBUG) System.err.println ("WARNING: sPrime pruned to size 500");
      sPrimeSorted = sPrimeSorted.subList (0, 500);
    }

    // find applicable rules
    Collection<HashRule> applicable = ruleTrie.getSubsets (sPrimeSorted);
    // execute their postconditions to derive S
    for (HashRule rule : applicable) {
      if (rule.type.equals (HashRule.Type.ADD)) sPrime.add (rule.effect);
    }
    for (HashRule rule : applicable) {
      if (rule.type.equals (HashRule.Type.DROP)) sPrime.remove (rule.effect);
    }
    // compute hash on S.
    Collection<Integer> sSet = new ArrayList<Integer> (sPrime.size () + unmatched.size ());
    sSet.addAll (sPrime);
    if (useInfrequentWords) sSet.addAll (unmatched);
    return sSet;
  }

  /* Return the sequence set to generate text based set with mapping */
  public Collection<Integer> getSeqSet (String s, ExperimentProperties properties, CSVWriter writer) throws PatternDiscoveryException
  {
    if (setMode) throw new RuntimeException ("Hashing strings not allowed in set mode.");
    Collection<Integer> sSet = computeS (s, properties, writer);
    return sSet;
  }

  // private List<List<Integer>> getLongestSequences (int [] sequence, int min, int max)
  // {
  // Trie<Integer> t = new Trie<Integer> ();
  // t.present (sequence, identifier)
  //
  // return result;
  // }

  // For Debugging
  public Collection<Integer> computeS (String s, ExperimentProperties properties, CSVWriter writer) throws PatternDiscoveryException
  {
    // transform String into integer sequence^
    int[] sequence = SequenceLoader.stringToSequence (s, mapping);
    // compute S' through matching
    // match automaton/trie based on present rules
    List<Integer> sequenceList = new ArrayList<Integer> ();
    for (int item : sequence) {
      sequenceList.add (item);
    }
    Collection<Integer> unmatched = new HashSet<Integer> ();

    sequenceString2ID = new HashMap<String, Integer> ();
    for (int key : sequenceIDMap.keySet ()) {
      String strSeq = sequenceIDMap.get (key);
      sequenceString2ID.put (strSeq, key);
    }

    // TODO: the below is a heuristic to prevent the application of subsumed
    // rules
    Collection<Integer> sPrime = sequenceTrie.getLongestMatches (s, sequenceList, unmatched, sequenceIDMap);

    // TODO: testing where all matches are pulled rather than just the
    // longest
    // Collection<Integer> sPrime = sequenceTrie.getMatches(sequenceList,
    // unmatched);

    Collection<Integer> firstPassSet = applyRules (s, sPrime, unmatched, properties, writer);

    if (properties.getProperty (PropertyConstants.APPLY_RULES_ON_SIGNATURE).equalsIgnoreCase ("true")) {
      Collection<Integer> secondPassSet = new HashSet<Integer> ();

      for (int id : firstPassSet) {
        Collection<Integer> newSeqSet = new HashSet<Integer> ();
        HashMap<Integer, Integer> seqLoc = new HashMap<Integer, Integer> ();
        String seqString = "";
        if (sequenceIDMap.containsKey (id)) {
          seqString = sequenceIDMap.get (id);
        }
        else {
          seqString = mapping.wordForInt (id); // Not a frequent
          // sequence - check word
          // mappings
        }

        StringTokenizer st = new StringTokenizer (seqString);

        // Store word into array and location of word inside the array
        // in the hashmap
        int loc = 0;
        String[] storeSeq = new String[st.countTokens ()];
        while (st.hasMoreTokens ()) {
          String word = st.nextToken ();
          int wordSeqID = 0;
          if (sequenceString2ID.containsKey (word)) {
            wordSeqID = sequenceString2ID.get (word);
          }
          else {
            wordSeqID = mapping.intForWord (word);
          }
          newSeqSet.add (wordSeqID);
          storeSeq[loc] = word;
          seqLoc.put (wordSeqID, loc);
          loc++;
        }
        unmatched.clear ();
        Collection<Integer> newSeqSetJoin = applyRules (s, newSeqSet, unmatched, properties, writer);

        // take results and find location from hashmap
        Iterator<Integer> seqIt = newSeqSetJoin.iterator ();
        ArrayList<Integer> seqSortedList = new ArrayList<Integer> ();
        while (seqIt.hasNext ()) {
          int curLoc = seqLoc.get (seqIt.next ());
          seqSortedList.add (curLoc);
        }
        // Sort location list
        Collections.sort (seqSortedList);
        String newSeq = "";
        Iterator<Integer> seqIt2 = seqSortedList.iterator ();
        while (seqIt2.hasNext ()) {
          newSeq = newSeq + " " + storeSeq[seqIt2.next ()];
        }

        newSeq = newSeq.trim ();
        if (sequenceString2ID.containsKey (newSeq)) {
          secondPassSet.add (sequenceString2ID.get (newSeq.trim ()));
        }
        else {
          Object[] temp = newSeqSetJoin.toArray ();
          for (Object tempID : temp) {
            secondPassSet.add ((Integer) tempID);
          }
          // System.out.println(seqString);
          // System.out.println(newSeq);
          // System.out.println(seqSortedList);
        }

        // If just use
        // secondPassSet.addAll(applyRules(newSeqSet, unmatched));
      }
      return secondPassSet;
    }
    else {
      return firstPassSet;
    }
  }

  // For Debugging
  private Collection<Integer> applyRules (String s, Collection<Integer> sPrime, Collection<Integer> unmatched,
    ExperimentProperties properties, CSVWriter writer)
  {
    // get also the tokens that are in no match to add the respective words
    // directly to hashing
    Integer[] matchesSorted = sPrime.toArray (new Integer[sPrime.size ()]);
    Arrays.sort (matchesSorted);
    List<Integer> sPrimeSorted = Arrays.asList (matchesSorted);

    // truncate sPrime to size 500
    if (sPrimeSorted.size () > 500) {
      if (Constants.DEBUG) System.err.println ("WARNING: sPrime pruned to size 500");
      sPrimeSorted = sPrimeSorted.subList (0, 500);
    }
    // For printing out before anything has been pruned
    String before = sPrime.toString ();
    String beforeStr = sequence2String (sPrime, sequenceIDMap, s);

    // find applicable rules
    Collection<HashRule> applicable = ruleTrie.getSubsets (sPrimeSorted);
    // execute their postconditions to derive S
    for (HashRule rule : applicable) {
      if (rule.type.equals (HashRule.Type.ADD)) sPrime.add (rule.effect);
      if (properties.getProperty (PropertyConstants.ENABLE_DEBUGGING).equalsIgnoreCase ("true")) {
        String after = sPrime.toString ();
        String afterStr = sequence2String (sPrime, sequenceIDMap, s);
        String[] line = { s, "ADD", Integer.toString (rule.effect) + " : " + sequenceIDMap.get (rule.effect),
          rule.toString (), before + " : " + beforeStr, after + " : " + afterStr };
        writer.writeNext (line);
      }
    }
    
    for (HashRule rule : applicable) {
      if (rule.type.equals (HashRule.Type.DROP)) {
        //before = sPrime.toString ();
        beforeStr = sequence2String (sPrime, sequenceIDMap, s);
        sPrime.remove (rule.effect);
        // TODO : here we want to store this drop so later we can provide this information to the suer upon request
        // String after = sPrime.toString ();
        String afterStr = sequence2String (sPrime, sequenceIDMap, s);
        // String[] line = { s, "DROP", Integer.toString (rule.effect) + " : " + sequenceIDMap.get (rule.effect),
        // sequenceIDMap.get (rule.effect), rule.toString (), rule.toString (sequenceIDMap), before + " : " + beforeStr,
        // beforeStr, after + " : " + afterStr, afterStr };

        String[] line = { s, rule.toString (sequenceIDMap), beforeStr, afterStr };
        writer.writeNext (line);
      }
    }
    
    // compute hash on S.
    Collection<Integer> sSet = new ArrayList<Integer> (sPrime.size () + unmatched.size ());
    sSet.addAll (sPrime);
    if (useInfrequentWords) sSet.addAll (unmatched);
    return sSet;
  }
  
  public WordIntegerMapping getMapping ()
  {
    return mapping;
  }

  public void setMapping (WordIntegerMapping mapping)
  {
    this.mapping = mapping;
  }

  public DebugDBProcessor getDb ()
  {
    return db;
  }

  public String sequence2String (Collection<Integer> sequenceSet, Map<Integer, String> seqMap, String context)
  {
    // Convert int to text
    Iterator<Integer> getSeqInt = sequenceSet.iterator ();
    String sequenceID = "{";
    while (getSeqInt.hasNext ()) {
      sequenceID = sequenceID + seqMap.get (getSeqInt.next ()) + ";";
    }
    // for (int c =0; c<finalSequence.length; c++){
    // sequenceID = sequenceID + map.wordForInt((Integer)finalSequence[c]) +
    // ";";
    // }
    sequenceID = sequenceID.substring (0, sequenceID.length () - 1) + "}";
    
    return seqOrganizer.getSequence (sequenceID, context);
  }

  /**
   * Set of Functions added in for debugging that have nearly the same purpose as above
   */

  /**
   * For Debugging Returns the set before rules are applied
   * 
   * @throws PatternDiscoveryException
   */
  public Collection<Integer> computeSdebug (String s) throws PatternDiscoveryException
  {
    // transform String into integer sequence^
    int[] sequence = SequenceLoader.stringToSequence (s, mapping);
    // compute S' through matching
    // match automaton/trie based on present rules
    List<Integer> sequenceList = new ArrayList<Integer> ();
    for (int item : sequence) {
      sequenceList.add (item);
    }
    Collection<Integer> unmatched = new HashSet<Integer> ();
    Collection<Integer> sPrime = sequenceTrie.getLongestMatches (s, sequenceList, unmatched, sequenceIDMap);
    return sPrime;
  }

}

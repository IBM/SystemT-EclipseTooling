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
package com.ibm.biginsights.textanalytics.patterndiscovery.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util methods used by the bubbles in PD
 * 
 * 
 */
public class PDBubbleUtils
{



  public static final String SEQUENCE_SEPARATOR = ";";
  private static String SYMBOLS_PATTERN = "[\"!#@\\(\\);:\\-=,%<>()\\/\\+\\*\\]\\[\\.\\p{P}]";

  /**
   * given a signature and a sample context this method try to rearrange the signature segments based in the positions
   * that they appear in the context
   * 
   * @param signature
   * @param sampleContext
   * @return
   */
  public static String fixSignature (String signature, String sampleContext)
  {
    sampleContext = sampleContext.replaceAll ("[\t\r\n]+", " ");
    sampleContext = sampleContext.replaceAll (SYMBOLS_PATTERN, " ");
    sampleContext = sampleContext.replaceAll ("( )+", " ");
    
    ArrayList<String> segments = splitSegments (signature);

    List<SegmentPosition> firstOccurenceList = new ArrayList<SegmentPosition>();
    
    for (String segment : segments) {

      List<SegmentPosition> positions = getPositions (segment, sampleContext);
      if (!positions.isEmpty ())
        firstOccurenceList.add (positions.get (0));

    }

    SegmentPosition[] firstOccurence = new SegmentPosition[0];
    if (!firstOccurenceList.isEmpty ()) {

      firstOccurence = new SegmentPosition[firstOccurenceList.size ()];
      firstOccurenceList.toArray (firstOccurence);
      Arrays.sort (firstOccurence);

    }

    return rebuildSequence (firstOccurence);
  }

  /**
   * @param keysOrdered
   * @param segmentsMap
   * @return
   */
  public static String rebuildSequence (SegmentPosition[] segmentsOrdered)
  {
    String ret = "{";
    for (SegmentPosition segment : segmentsOrdered) {
      if(segment != null)
        ret += segment.segment + ";";
    }
    ret = ret.substring (0, ret.length () - 1) + "}";
    return ret;
  }

  /**
   * @param segment
   * @param sampleContext
   * @return
   */
  public static List<SegmentPosition> getPositions (String segment, String sampleContext)
  {
    List<SegmentPosition> positions = new ArrayList<SegmentPosition> ();

    Pattern pat = Pattern.compile (Pattern.quote (segment));
    Matcher mat = pat.matcher (sampleContext);

    while (mat.find ()) {
      positions.add (new SegmentPosition (mat.start (0), mat.end (0), segment));
    }

    return positions;
  }

  /**
   * Inner class that defines the position of a given segment in the context of text. This class will help to identify
   * the correct position for the segments when we need to resort them.
   * 
   * 
   */
  public static class SegmentPosition implements Comparable<SegmentPosition>
  {
    int start;
    int end;
    
    String segment;

    /**
     * constructor
     * @param start
     * @param end
     */
    public SegmentPosition (int start, int end, String segment)
    {
      this.start = start;
      this.end = end;
      this.segment = segment;
    }

    @Override
    public int compareTo (SegmentPosition arg0)
    {
      // are equals
      if (this.equals (arg0)) return 0;

      // this covers the other one
      if (this.start <= arg0.start && this.end >= arg0.end) return 1;

      // this is covered
      if (this.start >= arg0.start && this.end <= arg0.end) return -1;

      // then just returns the one that ends to most right
      return (this.end > arg0.end) ? 1 : -1;

    }

    @Override
    public boolean equals (Object obj)
    {
      if (obj instanceof SegmentPosition) return this.equals ((SegmentPosition) obj);

      return super.equals (obj);
    }

    public boolean equals (SegmentPosition obj)
    {
      if (obj != null && this.start == obj.start && this.end == obj.end) return true;
      return false;
    }

  }

  /**
   * @param signature
   * @return
   */
  public static ArrayList<String> splitSegments (String signature)
  {
    ArrayList<String> segments = new ArrayList<String> ();

    // we now that the signatures are composed of a pattern such as
    // {xy;rst;nml}
    // therefore we need to remove the brackets and then split on ';'

    if (signature == null || signature.length () < 2) return segments;

    String clean = signature.substring (1, signature.length () - 1);
    for (String str : clean.split (SEQUENCE_SEPARATOR))
      segments.add (str);

    return segments;
  }

  /**
   * Provides the longest common sequence among the list of contexts provided as parameter
   * 
   * @param contexts
   * @return
   */
  public static String getLongestCommonSequence (List<String> contexts)
  {
    // validate input
    if (contexts == null || contexts.size () < 1) return "";

    // leaf case
    if (contexts.size () == 1) return contexts.get (0);

    if (contexts.size () > 1) {
      String str1 = getLongestCommonSequence (contexts.subList (0, contexts.size () / 2));
      String str2 = getLongestCommonSequence (contexts.subList (contexts.size () / 2, contexts.size ()));
      return lcs (str1, str2);
    }

    return null;
  }

  /**
   * Compute The Longest Common Sequence between the two strings provided as parameters
   * 
   * @param str1
   * @param str2
   * @return
   */
  private static String lcs (String str1, String str2)
  {
    String[] str1Tokens = str1.toLowerCase ().trim ().split ("[ ]+");
    String[] str2Tokens = str2.toLowerCase ().trim ().split ("[ ]+");

    if (str1Tokens.length == 0 || str2Tokens.length == 0) return "";

    int[][] path = new int[str1Tokens.length + 1][str2Tokens.length + 1];

    for (int i = 1; i <= str1Tokens.length; i++) {
      for (int j = 1; j <= str2Tokens.length; j++) {
        if (str1Tokens[i - 1].equals (str2Tokens[j - 1])) {
          path[i][j] = path[i - 1][j - 1] + 1;
        }
        else {
          path[i][j] = (path[i][j - 1] > path[i - 1][j]) ? path[i][j - 1] : path[i - 1][j];
        }
      }
    }

    return backtrackLCS (path, str1Tokens, str2Tokens, str1Tokens.length, str2Tokens.length);
  }

  /**
   * From the computed location load the longest common sequences as a readable string
   * 
   * @param path
   * @param str1Tokens
   * @param str2Tokens
   * @param i
   * @param j
   * @return
   */
  private static String backtrackLCS (int[][] path, String[] str1Tokens, String[] str2Tokens, int i, int j)
  {
    if (i < 1 || j < 1)
      return "";

    else if (str1Tokens[i - 1].equals (str2Tokens[j - 1]))
      return backtrackLCS (path, str1Tokens, str2Tokens, i - 1, j - 1) + " " + str1Tokens[i - 1];

    else {
      if (path[i][j - 1] > path[i - 1][j])
        return backtrackLCS (path, str1Tokens, str2Tokens, i, j - 1);
      else
        return backtrackLCS (path, str1Tokens, str2Tokens, i - 1, j);
    }
  }

  // FIXME: use this as a test case
  // public static void main (String[] args)
  // {
  // String str1 = "hello world  haha  world heheh pu   ha pu";
  // String str2 = "hello world  heheh ha pu";
  // String str3 = "hello world but pu ha";
  // String str4 = "hello you , your world ha pu";
  // String str5 = "so hello my world and pu ha";
  // List<String> testList = Arrays.asList (str1, str2, str3, str4, str5);
  // System.out.println (lcs (str1, str2));
  // System.out.println (getLongestCommonSequence (testList));
  // }
}

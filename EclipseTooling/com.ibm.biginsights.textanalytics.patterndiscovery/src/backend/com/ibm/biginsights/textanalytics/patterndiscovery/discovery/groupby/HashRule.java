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

import java.util.Arrays;
import java.util.Map;

/**
 * Parses and represents a hash rule. The current syntax is ADD|DROP Y IF X1 AND ... AND Xn While Y, X1, ... Xn are
 * sequence IDs.
 * 
 * 
 */
public class HashRule
{



	public enum Type
  {
    ADD, DROP
  }

  public int effect; // the sequence to be added or dropped
  public Integer[] condition;// the sequences that need to be presented (assumed to be sorted in ascending order)
  public Type type; // add or drop rule

  public HashRule (int effect, Integer[] condition)
  {
    this.effect = effect;
    this.condition = condition;
  }

  /**
   * Creates the rule as specified in the String. Syntax: ADD|DROP Y IF X1 AND ... AND Xn
   * 
   * @param toParse
   */
  public HashRule (String toParse)
  {
    if (toParse.trim ().startsWith ("DROP")) this.type = Type.DROP;
    if (toParse.trim ().startsWith ("ADD")) this.type = Type.ADD;
    // check well-formedness
    String syntaxRegex = "\\s*((ADD)|(DROP))\\s+(-?\\d+|('[\\w\\s]+'))\\s+IF\\s+(-?\\d+|('[\\w\\s]+'))(\\s+AND\\s+(-?\\d+|('[\\w\\s]+')))*\\s*";
    if (!toParse.matches (syntaxRegex)) { throw new RuntimeException ("Hash rules need to be of the form "
      + syntaxRegex + "- " + toParse); }
    // split
    String[] splitted = toParse.split ("\\s*(ADD|DROP|IF|AND)\\s*");
    // set data translate strings, and sort condition
    effect = convert (splitted[1]);
    condition = new Integer[splitted.length - 2];
    for (int i = 2; i < splitted.length; i++) {
      String s = splitted[i];
      condition[i - 2] = convert (s.trim ());
    }
    // sort condition
    Arrays.sort (condition);
  }

  private int convert (String s)
  {
    s = s.trim ();
    if (s.startsWith ("'")) {
      s = s.substring (1, s.length () - 1);// strip of 's
      throw new RuntimeException ("Strings in hash rules not supported yet");
      // TODO: tokenize and translate to numbers
      // TODO: look up sequence
    }
    else {
      return Integer.parseInt (s);
    }

  }

  public String toString ()
  {
    return type.name () + " " + effect + " IF " + Arrays.toString (condition);
  }

  public String toString (Map<Integer, String> sequenceIDMap)
  {
    String effectStr = sequenceIDMap.get (effect);
    String[] conditionStrs = new String[condition.length];
    for (int i = 0; i < condition.length; i++)
      conditionStrs[i] = sequenceIDMap.get (condition[i]);

    return type.name () + " " + effectStr + " IF " + Arrays.toString (conditionStrs);
  }

  public String getEffect ()
  {
    return Integer.toString (effect);
  }

  public String getCondition ()
  {
    String ret = "";

    for (int i : condition) {
      ret += i + " ";
    }

    return ret.trim ();
  }

}

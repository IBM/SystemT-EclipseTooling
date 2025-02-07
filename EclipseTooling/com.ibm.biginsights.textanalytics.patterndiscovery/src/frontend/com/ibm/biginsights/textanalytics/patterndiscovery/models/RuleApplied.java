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
package com.ibm.biginsights.textanalytics.patterndiscovery.models;

import java.util.HashSet;
import java.util.Set;

/**
 * defines a model for a rule applied to a given pattern
 * 
 * 
 */
public class RuleApplied implements Comparable<RuleApplied>
{


 
	/** the rule that was applied to get this signature */
  private String ruleString;
  /** the signature that identifies this element */
  private String signature;

  /**
   * the children are the set of <b>signatures</b> that existed before a set of rules got applied so the signature
   * became the one of this element
   */
  private Set<RuleApplied> children;

  /**
   * @param signature
   */
  public RuleApplied (String signature, String ruleString)
  {
    super ();
    this.signature = signature;
    this.ruleString = ruleString;
    this.children = new HashSet<RuleApplied> ();
  }

  /**
   * @param child
   */
  public void addChildren (RuleApplied child)
  {
    if (!children.contains (child)) children.add (child);
  }

  /**
   * provides the xml representation of this element which include the rule node (if it is not the root node, in which
   * case there is not further rule)
   * 
   * @return
   */
  public String toXML ()
  {
    StringBuilder isb = new StringBuilder ();

    if (ruleString != null) {
      isb.append ("<branch>");
      isb.append (String.format ("<attribute name=\"name\" value=\"Rule: %s\"/>", escapeHTMLSpecials (ruleString)));
      isb.append (getSignatureXMLNode ());
      isb.append ("</branch>");
    }
    else {
      isb.append (getSignatureXMLNode ());
    }

    return isb.toString ();
  }

  /**
   * return the xml node representation of the signature for this element
   * 
   * @return
   */
  private String getSignatureXMLNode ()
  {
    StringBuilder isb = new StringBuilder ();

    String strout = escapeHTMLSpecials (signature);

    if (children.size () < 1) {
      isb.append ("<leaf>");
      isb.append (String.format ("<attribute name=\"name\" value=\"%s\"/>", strout));
      isb.append ("</leaf>");
    }
    else {
      isb.append ("<branch>");
      isb.append (String.format ("<attribute name=\"name\" value=\"%s\"/>", strout));

      for (RuleApplied child : children) {
        isb.append (child.toXML ());
      }

      isb.append ("</branch>");
    }

    return isb.toString ();
  }

  /**
   * Escape special HTML characters in the input string.
   * 
   * @param str
   * @return
   */
  private String escapeHTMLSpecials (String str)
  {
    if (0 == str.length ()) { return str; }

    String ret;

    // Need to do the ampersands first
    ret = str.replace ("&", "&amp;");
    ret = ret.replace ("<", "&lt;");
    ret = ret.replace (">", "&gt;");
    ret = ret.replaceAll ("\"", "&quot;");
    ret = ret.replace ("'", "&#39;");

    return ret;
  }

  @Override
  public String toString ()
  {
    return toXML ();
  }

  /**
   * overwrites default equals method in order to compare elements in Lists
   */
  @Override
  public boolean equals (Object obj)
  {
    if (obj instanceof RuleApplied) return compareTo ((RuleApplied) obj) == 0;

    return super.equals (obj);
  }

  /**
   * check if the basic two properties are the same
   */
  @Override
  public int compareTo (RuleApplied o)
  {
    if (!o.ruleString.equals (ruleString)) return -1;
    if (!o.signature.equals (signature)) return -1;
    return 0;
  }

  // setters & getters

  public String getRuleString ()
  {
    return ruleString;
  }

  public void setRuleString (String ruleString)
  {
    this.ruleString = ruleString;
  }

  public String getSignature ()
  {
    return signature;
  }

  public void setSignature (String signature)
  {
    this.signature = signature;
  }

  public Set<RuleApplied> getChildren ()
  {
    return children;
  }

  public void setChildren (Set<RuleApplied> children)
  {
    this.children = children;
  }
}

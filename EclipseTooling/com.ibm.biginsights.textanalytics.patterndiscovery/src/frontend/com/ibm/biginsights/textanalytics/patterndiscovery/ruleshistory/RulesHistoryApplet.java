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
package com.ibm.biginsights.textanalytics.patterndiscovery.ruleshistory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JPanel;

import prefuse.Visualization;
import prefuse.data.Tree;
import prefuse.data.io.TreeMLReader;
import prefuse.util.FontLib;
import prefuse.util.ui.JSearchPanel;

/**
 * Component for displaying the rules history graph.
 * 
 *  Reiss
 */
public final class RulesHistoryApplet extends JApplet
{



  /**
   * Dummy version ID for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Encoding used when translating binary to and from Java strings.
   */
  public static final String ENCODING = "UTF-8";

  /**
   * IMPORTANT: if you change the following constants, please, make sure to update the xml header @see
   * RulesHistoryJob#run
   */

  /**
   * Name of the attribute of a graph node that holds the human-readable string ID of the node.
   */
  public static final String NODE_SIGNATURE_ATTR_NAME = "name";

  /**
   * Name of the column in the Prefuse table for the edges of the history graph that holds the source node of a given
   * edge.
   */
  public static final String EDGE_SRC_COL_NAME = "edge_src";

  /**
   * Name of the column in the Prefuse table for the edges of the history graph that holds the target node of a given
   * edge.
   */
  public static final String EDGE_DEST_COL_NAME = "edge_dest";

  /**
   * Name of the property inside the Java object representation of the TreeML that holds the nodes of the tree.
   */
  private static final String treeNodes = "tree.nodes";

  /** Main entry point into the vizualization. */
  public RulesHistoryApplet (String xml)
  {
    super ();
    try {

//      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance ();
//      DocumentBuilder db = dbf.newDocumentBuilder ();
//      Document dom = db.parse (new ByteArrayInputStream (xml.getBytes (ENCODING)));
//
//      // Expected format of XML is:
//      // <tree>
//      // <!-- data schema -->
//      // <key id="name" for="node" attr.name="name" attr.type="string"/>
//      // <key id="gender" for="node" attr.name="gender" attr.type="string"/>
//      // <!-- nodes -->
//      // <node id="1">
//      // <data key="signature">{Signature}</data>
//      // <data key="rule">Rule[x,y]</data>
//      // </node>
//      // <!-- edges -->
//      // <edge source="1" target="2"/>
//      // </tree>
//
//      // Navigate to <tree> tag.
//      Element treeTag = (Element) dom.getFirstChild ();
//
//      // Convert into a Prefuse Tree object.
//      // First we create backing tables.
//      // The identifiers of graph nodes must be numeric.
//      Table edgeTable = new Table ();
//      edgeTable.addColumn (EDGE_SRC_COL_NAME, int.class);
//      edgeTable.addColumn (EDGE_DEST_COL_NAME, int.class);
//
//      Table nodeTable = new Table ();
//
//      // First child of the <tree> tag should be the <declarations> tag,
//      // and under that should be a bunch of attributeDecl tags, one per
//      // node attribute.
//      Element declsRoot = (Element) treeTag.getElementsByTagName ("declarations").item (0);
//
//      // Old code didn't work; getElementsByTagName(), for some strange
//      // reason, recurses all the way down the tree and returns *every*
//      // tag with the indicated name
//      // NodeList decls = declsRoot.getElementsByTagName("attributeDecl");
//      // for (int i = 0; i < decls.getLength(); i++) {
//      // Element attrElem = (Element) decls.item(i);
//
//      ArrayList<Element> decls = getChildrenByName (declsRoot, "attributeDecl");
//      for (Element attrElem : decls) {
//
//        String nameStr = attrElem.getAttribute ("name");
//
//        // There's also a "type" string, but we know that all columns
//        // are strings.
//        nodeTable.addColumn (nameStr, String.class);
//
//      }
//
//      Tree tree = new Tree (nodeTable, edgeTable, null, EDGE_SRC_COL_NAME, EDGE_DEST_COL_NAME);
//
//      // Second child should be the actual root of the tree, which should
//      // be a 'branch' tag.
//      Element domRoot = getChildrenByName (treeTag, "branch").get (0);
//      // (Element) treeTag.getElementsByTagName("branch")
//      // .item(0);
//
//      prefuse.data.Node root = tree.addRoot ();
//
//      domToTree (domRoot, root, tree);
//
//      // The Tree class has no way to get its size...
//      int treeSize = 0;
//      for (@SuppressWarnings("rawtypes")
//      Iterator itr = tree.nodes (); itr.hasNext (); itr.next ()) {
//        treeSize++;
//      }
//
//      if (Constants.DEBUG_PROVENANCE)
//        System.err.printf ("Converted XML into a Prefuse tree with %d elements.\n", treeSize);

      Tree tree = (Tree)new TreeMLReader ().readGraph (new ByteArrayInputStream (xml.getBytes ()));
     
      // Create the Java object that knows how to display the provenance
      // tree.
      JComponent prettyGraphics = demo (tree, NODE_SIGNATURE_ATTR_NAME);

      // Make the object we just created the owner of the applet's
      // graphics canvas
      setContentPane (prettyGraphics);

    }
    catch (Throwable t) {
      System.err.printf ("Caught exception:\n");
      t.printStackTrace ();
    }
  }

  /**
   * This method is derived from the method by the same name in {@link prefuse.demos.TreeView}. We fix up the URL
   * handling and add some additional hooks to aid in debugging.
   * 
   * @param tree the (tree-shaped) graph to be displayed
   * @param labelAttrName name of the node attribute containing the node labels
   * @param detailAttrName name of the node attribute containing the detailed text to display when the user clicks on a
   *          graph node.
   * @return the visualization component, ready to display
   */
  public static JComponent demo (Tree tree, final String labelAttrName)
  {
    Color BACKGROUND = Color.WHITE;
    Color FOREGROUND = Color.BLACK;

    // System.err.printf("Creating TreeView.\n");

    // create a new treemap
    final TreeView tview = new TreeView (tree, labelAttrName);
    tview.setBackground (BACKGROUND);
    tview.setForeground (FOREGROUND);

    // System.err.printf("Creating search panel.\n");

    // create a search panel for the tree map
    JSearchPanel search = new JSearchPanel (tview.getVisualization (), treeNodes, Visualization.SEARCH_ITEMS,
      labelAttrName, true, true);
    search.setShowResultCount (true);
    search.setBorder (BorderFactory.createEmptyBorder (5, 5, 4, 0));
    search.setFont (FontLib.getFont ("Tahoma", Font.PLAIN, 11));
    search.setBackground (BACKGROUND);
    search.setForeground (FOREGROUND);

    // Lay everything out in the panel: Search box on top, graph in the
    // middle, description on the bottom
    JPanel panel = new JPanel (new BorderLayout ());
    panel.setBackground (BACKGROUND);
    panel.setForeground (FOREGROUND);
    panel.add (search, BorderLayout.NORTH);
    panel.add (tview, BorderLayout.CENTER);
    // Laura: removed the description for now
    // panel.add(description, BorderLayout.SOUTH);
    return panel;
  }

  /**
   * De-escapes escaped characters from a string as returned by a parser.
   * 
   * @param str input string
   * @return the string, with any escaped characters inside the string de-escaped
   */
  public static final String deescapeStr (String str)
  {

    StringBuilder sb = new StringBuilder ();

    final char ESCAPE = '\\';
    int len = str.length ();

    // input string of length 0
    if (len == 0) return str;

    // input string of length > 0
    int pos = 0;

    while (pos < len) {
      if (str.charAt (pos) == ESCAPE) {

        if (pos < len - 1) {

          switch (str.charAt (pos + 1)) {
            // Handle built-in escape codes
            case 'n':
              sb.append ('\n');
              pos += 2;
            break;

            case 'r':
              sb.append ('\r');
              pos += 2;
            break;

            case 't':
              sb.append ('\t');
              pos += 2;
            break;

            case 'u':
              // Unicode escape; count the number of characters
              int numChars = 0;
              boolean foundNonHex = false;
              while (numChars < 4 && (false == foundNonHex)) {
                char c = str.charAt (pos + 2 + numChars);
                if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                  numChars++;
                }
                else {
                  foundNonHex = true;
                }

              }
              if (0 == numChars) { throw new IllegalArgumentException ("No hex characters in unicode esape"); }
              sb.append (decodeHex (str, pos + 2, numChars));
              pos += 2 + numChars;
            break;

            default:
              // Other characters just get passed through.
              sb.append (str.charAt (pos + 1));
              pos += 2;
            break;
          }

        }
        else {
          throw new IllegalArgumentException ("Escape character at end of string");
        }
      }
      else {
        // append the char at the current position
        sb.append (str.charAt (pos));

        // go to next char
        pos++;
      }
    }

    return sb.toString ();
  }

  /**
   * Decode a hex escape within a string
   * 
   * @param str the original string
   * @param startOff offset of the hex escape
   * @param len number of hex characters to decode
   * @return the decoded character
   */
  public static char decodeHex (String str, int startOff, int len)
  {
    // First two chars are the "\x" or u
    char accum = 0;

    for (int i = startOff; i < startOff + len; i++) {
      char curChar = str.charAt (i);

      char baseChar;
      if (curChar >= '0' && curChar <= '9') {
        baseChar = '0';
      }
      else if (curChar >= 'a' && curChar <= 'f') {
        baseChar = 'a' - 8;
      }
      else if (curChar >= 'A' && curChar <= 'F') {
        baseChar = 'A' - 8;
      }
      else {
        throw new RuntimeException ("Unexpected char in hex escape;" + " should never happen");
      }
      accum += (str.charAt (i) - baseChar);
      if (i < str.length () - 1) {
        accum <<= 4;
      }
    }

    return accum;
  }
}

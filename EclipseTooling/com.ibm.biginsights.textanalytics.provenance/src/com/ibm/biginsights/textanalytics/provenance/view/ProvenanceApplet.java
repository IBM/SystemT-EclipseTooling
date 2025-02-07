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
package com.ibm.biginsights.textanalytics.provenance.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.util.FontLib;
import prefuse.util.ui.JSearchPanel;
import prefuse.visual.VisualItem;

import com.ibm.biginsights.textanalytics.util.common.AQLUtils;
import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * Component for displaying the provenance. This is essentially the original
 * ProvenanceApplet from the web-based UI. Graphics need some touch up.
 * 
 *  Reiss
 */
public final class ProvenanceApplet extends JApplet {



	/**
	 * Dummy version ID for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Encoding used when translating binary to and from Java strings.
	 */
	private static final String ENCODING = "UTF-8";

	/**
	 * Name of the applet parameter containing the URL (possibly relative) where
	 * the XML file containing input data is located. Called "fileName" for
	 * legacy reasons.
	 */
	public static final String XML_URL_PARAM = "fileName";

	/**
	 * Name of the attribute of a graph node that holds the human-readable
	 * string ID of the node.
	 */
	public static final String NODE_ID_ATTR_NAME = "ID";

	/**
	 * Name of the attribute of each graph node that holds the pretty-printed
	 * HTML rendition of the result tuple that the node represents.
	 */
	public static final String TUPLE_HTML_ATTR_NAME = "Tuple";

	/**
	 * Name of the column in the Prefuse table for the edges of the provenance
	 * graph that holds the source node of a given edge.
	 */
	public static final String EDGE_SRC_COL_NAME = "edge_src";

	/**
	 * Name of the column in the Prefuse table for the edges of the provenance
	 * graph that holds the target node of a given edge.
	 */
	public static final String EDGE_DEST_COL_NAME = "edge_dest";

	/**
	 * Name of the property inside the Java object representation of the TreeML
	 * that holds the nodes of the tree.
	 */
	private static final String treeNodes = "tree.nodes";

	private String projectName; 
  private String viewName; 

	/** Main entry point into the vizualization. */
	public ProvenanceApplet(String xml, String projectName, String viewName) {
		super();

		this.projectName = projectName;
    this.viewName = viewName;

    try {
			/*
			 * // Read the location (relative to the applet's base URL) of the
			 * XML // input data. String xmlURLStr = xmlPathStr; URL xmlURL =
			 * new URL(xmlURLStr);
			 * 
			 * // Read the XML that's at the other end of the URL and echo it to
			 * // the console. InputStreamReader in = new
			 * InputStreamReader(xmlURL.openStream(), ENCODING); StringBuilder
			 * sb = new StringBuilder(); char[] buf = new char[1024]; int nread;
			 * while (0 < (nread = in.read(buf))) { sb.append(buf, 0, nread); }
			 * String xml = sb.toString();
			 */

			//System.err.printf("XML is:\n%s\n", xml);

			// Convert XML back to bytes so we can parse it.

			// Parse the XML into a DOM tree. SAX wasn't working so well inside
			// the browser.
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(new ByteArrayInputStream(xml
					.getBytes(ENCODING)));

			// Expected format of XML is:
			// <tree>
			// <declarations>
			// <attributeDecl name="ID" type="String"/>
			// ...
			// </declarations>
			// <branch>
			// <attribute name="ID" value="All Results"/>
			// ...
			// <leaf>
			// <attribute name="ID" value="ID: 10\nnumber: '466-9176'\n"/>
			// ...
			// </leaf>
			// </branch>
			// </tree>

			// Navigate to <tree> tag.
			Element treeTag = (Element) dom.getFirstChild();

			// Convert into a Prefuse Tree object.
			// First we create backing tables.
			// The identifiers of graph nodes must be numeric.
			Table edgeTable = new Table();
			edgeTable.addColumn(EDGE_SRC_COL_NAME, int.class);
			edgeTable.addColumn(EDGE_DEST_COL_NAME, int.class);

			Table nodeTable = new Table();

			// First child of the <tree> tag should be the <declarations> tag,
			// and under that should be a bunch of attributeDecl tags, one per
			// node attribute.
			Element declsRoot = (Element) treeTag.getElementsByTagName(
					"declarations").item(0);

			// Old code didn't work; getElementsByTagName(), for some strange
			// reason, recurses all the way down the tree and returns *every*
			// tag with the indicated name
			// NodeList decls = declsRoot.getElementsByTagName("attributeDecl");
			// for (int i = 0; i < decls.getLength(); i++) {
			// Element attrElem = (Element) decls.item(i);

			ArrayList<Element> decls = getChildrenByName(declsRoot,
					"attributeDecl");
			for (Element attrElem : decls) {

				String nameStr = attrElem.getAttribute("name");

				// There's also a "type" string, but we know that all columns
				// are strings.
				nodeTable.addColumn(nameStr, String.class);

			}

			Tree tree = new Tree(nodeTable, edgeTable, null, EDGE_SRC_COL_NAME,
					EDGE_DEST_COL_NAME);

			// Second child should be the actual root of the tree, which should
			// be a 'branch' tag.
			Element domRoot = getChildrenByName(treeTag, "branch").get(0);
			// (Element) treeTag.getElementsByTagName("branch")
			// .item(0);

			prefuse.data.Node root = tree.addRoot();

			domToTree(domRoot, root, tree);

			// The Tree class has no way to get its size...
			@SuppressWarnings("unused")
      int treeSize = 0;
			for (@SuppressWarnings("rawtypes")
			Iterator itr = tree.nodes(); itr.hasNext(); itr.next()) {
				treeSize++;
			}

			if(Constants.DEBUG_PROVENANCE)
			System.err.printf(
					"Converted XML into a Prefuse tree with %d elements.\n",
					treeSize);

			// Create the Java object that knows how to display the provenance
			// tree.
			JComponent prettyGraphics = demo(tree, NODE_ID_ATTR_NAME,
					TUPLE_HTML_ATTR_NAME);

			// Make the object we just created the owner of the applet's
			// graphics canvas
			setContentPane(prettyGraphics);

		} catch (Throwable t) {
			System.err.printf("Caught exception:\n");
			t.printStackTrace();
		}
	}

	/**
	 * A method that really ought to have been built into the Element API.
	 * 
	 * @param parent
	 *            a DOM tree node that is a tag
	 * @param name
	 *            name of a tag to search for
	 * @return all tags that are IMMEDIATE children of the parent and have the
	 *         indicated name
	 */
	private ArrayList<Element> getChildrenByName(Element parent,
			String name) {
		// Use the "official" API to get a list of all the crud (tags, text,
		// etc.) immediately below the specified tag
		NodeList allChildren = parent.getChildNodes();

		ArrayList<Element> ret = new ArrayList<Element>();

		for (int i = 0; i < allChildren.getLength(); i++) {
			Node childNode = allChildren.item(i);
			if (childNode instanceof Element) {
				Element childElem = (Element) childNode;
				if (childElem.getTagName().equalsIgnoreCase(name)) {
					ret.add(childElem);
				}
			}
		}

		return ret;
	}

	/**
	 * Recursively convert a DOM tree to a Prefuse Tree data structrue.
	 * 
	 * @param domRoot
	 *            current root of the DOM subtree
	 * @param root
	 *            current root of the Prefuse subtree
	 * @param tree
	 *            the Prefuse tree that we are building up
	 */
	private void domToTree(Element domRoot, prefuse.data.Node root,
			Tree tree) {

		// Parse the attributes of this node.
		parseAttrs(domRoot, root);

		// Deal with any interior nodes that are children of this node.
		// NodeList branches = domRoot.getElementsByTagName("branch");
		// for (int i = 0; i < branches.getLength(); i++) {
		// Element branchDomRoot = (Element) branches.item(i);
		ArrayList<Element> branches = getChildrenByName(domRoot, "branch");
		for (Element branchDomRoot : branches) {
			prefuse.data.Node branchRoot = tree.addChild(root);
			domToTree(branchDomRoot, branchRoot, tree);
		}

		// Deal with any leaf nodes that are children of this node.
		// NodeList leaves = domRoot.getElementsByTagName("leaf");
		// for (int i = 0; i < leaves.getLength(); i++) {
		// Element leafDomRoot = (Element) leaves.item(i);

		ArrayList<Element> leaves = getChildrenByName(domRoot, "leaf");
		for (Element leafDomRoot : leaves) {
			prefuse.data.Node leafRoot = tree.addChild(root);
			parseAttrs(leafDomRoot, leafRoot);
		}
	}

	private void parseAttrs(Element domRoot, prefuse.data.Node root) {
		// NodeList attrs = domRoot.getElementsByTagName("attribute");
		// for (int i = 0; i < attrs.getLength(); i++) {
		// Element attrElem = (Element) attrs.item(i);

		ArrayList<Element> attrs = getChildrenByName(domRoot, "attribute");
		for (Element attrElem : attrs) {
			String nameStr = attrElem.getAttribute("name");
			String valueStr = attrElem.getAttribute("value");

			// We know that all our values are strings, and that any special
			// characters are escaped with backslashes.
			String deEscapedValue = deescapeStr(valueStr);

			root.set(nameStr, deEscapedValue);
		}
	}

	/**
	 * This method is derived from the method by the same name in
	 * {@link prefuse.demos.TreeView}. We fix up the URL handling and add some
	 * additional hooks to aid in debugging.
	 * 
	 * @param tree
	 *            the (tree-shaped) graph to be displayed
	 * @param labelAttrName
	 *            name of the node attribute containing the node labels
	 * @param detailAttrName
	 *            name of the node attribute containing the detailed text to
	 *            display when the user clicks on a graph node.
	 * @return the visualization component, ready to display
	 */
	public JComponent demo(Tree tree, final String labelAttrName, String detailAttrName) {
		Color BACKGROUND = Color.WHITE;
		Color FOREGROUND = Color.BLACK;

		// System.err.printf("Creating TreeView.\n");

		// create a new treemap
		final TreeView tview = new TreeView(tree, labelAttrName);
		tview.setBackground(BACKGROUND);
		tview.setForeground(FOREGROUND);

		// System.err.printf("Creating search panel.\n");

		// create a search panel for the tree map
		JSearchPanel search = new JSearchPanel(tview.getVisualization(),
				treeNodes, Visualization.SEARCH_ITEMS, labelAttrName, true,
				true);
		search.setShowResultCount(true);
		search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
		search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
		search.setBackground(BACKGROUND);
		search.setForeground(FOREGROUND);

		// Laura: removed the description for now.
		/*
		// Description of the node the user is currently hovering over.
		// Use JLabel instead of JFastLabel so that the description will
		// automagically resize itself as needed.
		final JLabel description = new JLabel("");
		description.setVerticalAlignment(SwingConstants.BOTTOM);
		description.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		description.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
		description.setBackground(BACKGROUND);
		description.setForeground(FOREGROUND);
   */
		// Add an event handler to display the description of the any node the
		// mouse is hovering over
		tview.addControlListener(new ControlAdapter() {
		  public void itemClicked(VisualItem item, MouseEvent e) {
		    if (e.getClickCount () == 2) {
		      String nodeId = item.getString ("ID");                        // $NON-NLS-1$
		      if (nodeId.indexOf ("\n") >= 0) {
		        String view = nodeId.substring (0, nodeId.indexOf ("\n"));  // $NON-NLS-1$
		        AQLUtils.openAQLEditorForView (projectName, view, viewName);
		      }
		    }
		  }
		  /*
			public void itemEntered(VisualItem item, MouseEvent e) {
				String str = "<html>\n <ul>\n";
				if (item.canGetString("Type"))
					str = str
							+ "<li><b><font size=\"3\" face=\"Verdana\" color=\"Red\">Type: </font></b><font size=\"3\" face=\"Verdana\" color=\"Black\">"
							+ item.getString("Type") + "</font>\n";
				if (item.canGetString("Operation"))
					str = str
							+ "<li><b><font size=\"3\" face=\"Verdana\" color=\"Red\">Operation: </font></b><font size=\"3\" face=\"Verdana\" color=\"Black\">"
							+ item.getString("Operation") + "</font>\n";
				if (item.canGetString("Rule"))
					str = str
							+ "<li><b><font size=\"3\" face=\"Verdana\" color=\"Red\">Rule: </font></b><font size=\"3\" face=\"Verdana\" color=\"Black\">"
							+ item.getString("Rule") + "</font>\n";
				if (item.canGetString("Tuple"))
					str = str
							+ "<li><b><font size=\"3\" face=\"Verdana\" color=\"Red\">Tuple: </font></b><font size=\"3\" face=\"Verdana\" color=\"Black\">"
							+ item.getString("Tuple") + "</font>\n";
				description.setText(str);
			}

			public void itemExited(VisualItem item, MouseEvent e) {
				description.setText(null);
			} */
		});

		// Old code put the search box to the right of the description box,
		// which caused problems when the description box resized itself.
		// Box box = new Box(BoxLayout.X_AXIS);
		// box.add(Box.createHorizontalStrut(10));
		// box.add(title);
		// box.add(Box.createHorizontalGlue());
		// box.add(search);
		// box.add(Box.createHorizontalStrut(3));
		// box.setBackground(BACKGROUND);

		// Lay everything out in the panel: Search box on top, graph in the
		// middle, description on the bottom
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND);
		panel.setForeground(FOREGROUND);
		panel.add(search, BorderLayout.NORTH);
		panel.add(tview, BorderLayout.CENTER);
		//Laura: removed the description for now
		//panel.add(description, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * De-escapes escaped characters from a string as returned by a parser.
	 * 
	 * @param str
	 *            input string
	 * @return the string, with any escaped characters inside the string
	 *         de-escaped
	 */
	public static final String deescapeStr(String str) {

		StringBuilder sb = new StringBuilder();

		final char ESCAPE = '\\';
		int len = str.length();

		// input string of length 0
		if (len == 0)
			return str;

		// input string of length > 0
		int pos = 0;

		while (pos < len) {
			if (str.charAt(pos) == ESCAPE) {

				if (pos < len - 1) {

					switch (str.charAt(pos + 1)) {
					// Handle built-in escape codes
					case 'n':
						sb.append('\n');
						pos += 2;
						break;

					case 'r':
						sb.append('\r');
						pos += 2;
						break;

					case 't':
						sb.append('\t');
						pos += 2;
						break;

					case 'u':
						// Unicode escape; count the number of characters
						int numChars = 0;
						boolean foundNonHex = false;
						while (numChars < 4 && (false == foundNonHex)) {
							char c = str.charAt(pos + 2 + numChars);
							if ((c >= '0' && c <= '9')
									|| (c >= 'a' && c <= 'f')
									|| (c >= 'A' && c <= 'F')) {
								numChars++;
							} else {
								foundNonHex = true;
							}

						}
						if (0 == numChars) {
							throw new IllegalArgumentException(
									"No hex characters in unicode esape");
						}
						sb.append(decodeHex(str, pos + 2, numChars));
						pos += 2 + numChars;
						break;

					default:
						// Other characters just get passed through.
						sb.append(str.charAt(pos + 1));
						pos += 2;
						break;
					}

				} else {
					throw new IllegalArgumentException(
							"Escape character at end of string");
				}
			} else {
				// append the char at the current position
				sb.append(str.charAt(pos));

				// go to next char
				pos++;
			}
		}

		return sb.toString();
	}

	/**
	 * Decode a hex escape within a string
	 * 
	 * @param str
	 *            the original string
	 * @param startOff
	 *            offset of the hex escape
	 * @param len
	 *            number of hex characters to decode
	 * @return the decoded character
	 */
	public static char decodeHex(String str, int startOff, int len) {
		// First two chars are the "\x" or u
		char accum = 0;

		for (int i = startOff; i < startOff + len; i++) {
			char curChar = str.charAt(i);

			char baseChar;
			if (curChar >= '0' && curChar <= '9') {
				baseChar = '0';
			} else if (curChar >= 'a' && curChar <= 'f') {
				baseChar = 'a' - 8;
			} else if (curChar >= 'A' && curChar <= 'F') {
				baseChar = 'A' - 8;
			} else {
				throw new RuntimeException("Unexpected char in hex escape;"
						+ " should never happen");
			}
			accum += (str.charAt(i) - baseChar);
			if (i < str.length() - 1) {
				accum <<= 4;
			}
		}

		return accum;
	}
}

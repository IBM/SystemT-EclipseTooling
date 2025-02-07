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

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.CircleLayout;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

import com.ibm.biginsights.textanalytics.patterndiscovery.controlers.PatternUIControlListener;
import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.AQLUtils;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PatternUIDecoratorLayout;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PatternUILabelRenderer;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PatternUIRenderer;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;
import com.ibm.biginsights.textanalytics.patterndiscovery.views.CommonSignatureTableView;

/**
 * defines the applet that is displayed by the PD view
 * 
 * 
 */
public class PatternDiscoveryApplet extends JApplet
{



  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;
  private ArrayList<BubbleModel> table;
  private int noOfNodes, totalAvailable;
  private PatternDiscoveryJob job;
  private String databaseName;
  private Graph graph;
  private Visualization vis;
  private int[] palette;
  private int minWidth;

  /**
   * constructor that receives a @PrefuseJob as parameter. This is used to obtain the common global variables such as
   * project location and properties.
   */
  public PatternDiscoveryApplet (PatternDiscoveryJob job, int minSize, int maxSize)
  {
    super ();
    this.job = job;
    databaseName = job.getDBName ();
    table = new ArrayList<BubbleModel> ();
    noOfNodes = AQLUtils.getNodesFromDB (job, table, minSize, maxSize);
    totalAvailable = job.getAvailableBubbles ();
    JComponent content = demo ();
    setLayout (new BorderLayout ());
    setContentPane (content);
  }

  public PatternDiscoveryJob getJob ()
  {
    return job;
  }

  /**
   * load the groups from the DB and assign nodes representation to each of them
   */
  private void loadNodes ()
  {
    int[] red = { 255, 238, 100, 0, 152, 147, 238, 255, 255, 135, 255, 255, 100, 0, 255, 193, 113, 255, 139, 255 };
    int[] green = { 222, 130, 149, 255, 251, 112, 221, 160, 105, 206, 255, 0, 100, 255, 0, 193, 198, 127, 101, 215 };
    int[] blue = { 173, 238, 237, 255, 152, 219, 130, 122, 180, 250, 0, 255, 255, 0, 0, 193, 113, 36, 8, 0 };

    palette = new int[noOfNodes];

    for (int i = 0; i < noOfNodes; ++i) {
      int rr = red[i % 20];
      int gg = green[i % 20];
      int bb = blue[i % 20];
      palette[i] = ColorLib.rgba (rr, gg, bb, 500);
    }

    minWidth = 0;

    for (int i = 0; i < noOfNodes; i++) {

      Node n = graph.addNode ();
      n.set ("ID", table.get (i).id);
      n.set ("Size", table.get (i).size);
      n.set ("Signature", table.get (i).signature);
      n.set ("OriginalSignature", table.get (i).originalSignature);
      n.set ("Precision", table.get (i).precision);
      n.set ("Captured", table.get (i).captured);
      n.set ("Color", palette[i]);
      n.set ("PreviousSignature", table.get (i).signature);
      n.set ("Visible", 1);
      n.set ("Label", table.get (i).signature + "\n[" + table.get (i).size + "]");

      minWidth = (minWidth < (table.get (i).signature.length () * 7)) ? (table.get (i).signature.length () * 7)
        : minWidth;
    }
  }

  /**
   * This method builds a JPanel that its context is the Patter Discovery display results to be displayed.
   * 
   * @return the JPanel object
   */
  private JComponent demo ()
  {
    JPanel panel = new JPanel (new BorderLayout ());

    graph = new Graph ();

    graph.addColumn ("ID", Integer.class);
    graph.addColumn ("Size", Integer.class);
    graph.addColumn ("Signature", String.class);
    graph.addColumn ("OriginalSignature", String.class);
    graph.addColumn ("Precision", Float.class);
    graph.addColumn ("Captured", Float.class);
    graph.addColumn ("Color", Integer.class);
    graph.addColumn ("PreviousSignature", String.class);
    graph.addColumn ("Visible", Integer.class);
    graph.addColumn ("Label", String.class);

    loadNodes ();
    JLabel label = new JLabel (String.format (Messages.PD_VIEW_STATISTIC, totalAvailable, noOfNodes));
    panel.add (label, BorderLayout.NORTH);

    // handle the case that we don't have any result
    if (noOfNodes == 0) {
      label.setText (Messages.PD_NO_PATTERNS);
    }

    vis = new Visualization ();

    vis.add ("graph", graph);

    PatternUIRenderer r = new PatternUIRenderer ();
    // r.setDiameter(minWidth);

    DefaultRendererFactory drf = new DefaultRendererFactory (r);

    drf.add (new InGroupPredicate ("nodedec"), new PatternUILabelRenderer ("Label"));

    vis.setRendererFactory (drf);

    final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema ();
    DECORATOR_SCHEMA.setDefault (VisualItem.INTERACTIVE, false);
    DECORATOR_SCHEMA.setDefault (VisualItem.TEXTCOLOR, ColorLib.rgb (0, 50, 50));
    DECORATOR_SCHEMA.setDefault (VisualItem.FONT, FontLib.getFont (getUnicodeFont (), 12));

    vis.addDecorators ("nodedec", "graph.nodes", DECORATOR_SCHEMA);

    DataColorAction fill = new DataColorAction ("graph.nodes", "ID", Constants.NOMINAL, VisualItem.FILLCOLOR, palette);

    ColorAction edges = new ColorAction ("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray (200));

    ActionList color = new ActionList ();
    color.add (fill);
    color.add (edges);

    ActionList layout = new ActionList (noOfNodes);

    layout.add (new CircleLayout ("graph"));
    layout.add (new RepaintAction ());
    layout.add (new PatternUIDecoratorLayout ("nodedec"));

    vis.putAction ("color", color);
    vis.putAction ("layout", layout);

    Display d = new Display (vis);

    d.setSize (500, 500);

    // d.addControlListener(new DragControl());
    d.addControlListener (new PanControl ());
    d.addControlListener (new ZoomControl ());

    PatternUIControlListener listener = new PatternUIControlListener ();
    listener.setApplet (this);
    listener.setDatabaseName (databaseName);
    listener.setDisplay (d);
    d.addControlListener (listener);

    vis.run ("layout");
    vis.run ("color");

    panel.add (d, BorderLayout.CENTER);
    // panel.setPreferredSize(new Dimension(500, 500));

    return panel;
  }

  /**
   * this method is used by the action listener to handle the onClick event. when an item gets clicked, we use its
   * common signature to render the common signature table view. See @CommonSignatureTableView for more details in how
   * this obtain its values
   * 
   * @param contentSelected the CommonSignature context of the clicked element.
   */
  public void itemClicked (final String contentSelected)
  {
    org.eclipse.swt.widgets.Display.getDefault ().asyncExec (new Runnable () {
      @Override
      public void run ()
      {
        try {

          IWorkbenchPage wbPage = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();

          CommonSignatureTableView prevView = (CommonSignatureTableView) wbPage.findView (CommonSignatureTableView.VIEW_ID);

          if (prevView != null) {
            prevView.update (contentSelected, job);
          }
          else {
            CommonSignatureTableView.setVars (contentSelected, job);
          }
          wbPage.showView (CommonSignatureTableView.VIEW_ID);

        }
        catch (PartInitException e) {
          ErrorMessages.LogErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_OPENING_COMMON_SIGNATURE_VIEW, e);
        }

      }
    });
  }

  /*
   * Attempt to set a unicode font for the node render based on the platform OS.
   */
  private static String getUnicodeFont ()
  {

    String osName = System.getProperty ("os.name");

    if (osName.startsWith ("Windows")) { return "Arial Unicode MS"; }

    if ("Linux".equals (osName)) { return "FreeSans"; }

    // TODO: Add more fonts here

    // Default value
    return "Tahoma";
  }

  public int getNumOfNodes ()
  {
    return noOfNodes;
  }

  public int getTotalAvailable ()
  {
    return totalAvailable;
  }

}

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
package com.ibm.biginsights.textanalytics.patterndiscovery.filters;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;

/**
 * action that allows the user to set a min/max range for the bubbles that they prefer to be displayed
 * 
 * 
 */
public class FilterBySizeAction extends Action implements IWorkbenchAction
{



  public static final String ACTION_ID = "com.ibm.biginsights.textanalytics.patterndiscovery.views.FilterBySizeAction";

  private static String minSize, maxSize;
  private int smallestSize, biggestSize;
  private static PatternDiscoveryJob job;
  private Composite composite;

  // protected

  public FilterBySizeAction (PatternDiscoveryJob ajob, Composite composite)
  {
    setId (ACTION_ID);
    setJob (ajob);
    reloadRange ();
    this.composite = composite;
    setText ("Filter by Size");
    setImageDescriptor (AbstractUIPlugin.imageDescriptorFromPlugin (
      "com.ibm.biginsights.textanalytics.patterndiscovery", "icons/filter.png"));
  }

  public static void setJob (PatternDiscoveryJob ajob)
  {
    job = ajob;
  }

  /**
   * set the current min and max
   * 
   * @param min
   * @param max
   */
  public void setCurrentValues (int min, int max)
  {
    minSize = Integer.toString (min);
    maxSize = Integer.toString (max);
  }

  /**
   * reload the current available range for the bubbles sizes
   */
  public void reloadRange ()
  {
    // job.loadRange ();
    int[] range = job.getRange ();
    smallestSize = range[0];
    biggestSize = range[1];
  }

  /**
   * request min and max from the user and applied them to the applet displaying the bubbles
   */
  @Override
  public void run ()
  {
    Shell shell = composite.getShell ();

    reloadRange ();
    setCurrentValues (job.getMin (), job.getMax ());

    MinMaxInputValidator validator = new MinMaxInputValidator (smallestSize, biggestSize);
    MinMaxInputDialog dlg = new MinMaxInputDialog (shell, "Filter patterns by size", "Minimum Size: ",
      "Maximum Size: ", minSize, maxSize, validator);

    if (dlg.open () == Window.OK) {
      // User clicked OK; update the label with the input
      minSize = dlg.getMin ();
      maxSize = dlg.getMax ();
      filter ();
    }
  }

  /**
   * do filter the bubbles based in the range defined by the user
   */
  private void filter ()
  {
    // System.err.print("Value entered ----> " + minSize);
    job.setMinSize (Integer.parseInt (minSize));
    job.setMaxSize (Integer.parseInt (maxSize));
    job.setProcessLevel (false);
    job.setResetMinMax (false);
    job.schedule ();
  }

  /**
   * Validate the min and max values entered by the user based in the size availablity of all the bubbles
   * 
   * 
   */
  class MinMaxInputValidator
  {

    private int min, max;

    public MinMaxInputValidator (int min, int max)
    {
      this.setMin (min);
      this.setMax (max);
    }

    /**
     * Validates the String. Returns null for no error, or an error message
     * 
     * @param newText the String to validate
     * @return String
     */
    public String isValid (String min, String max)
    {
      int minvalue, maxvalue;
      try {
        minvalue = Integer.parseInt (min);
        maxvalue = Integer.parseInt (max);
      }
      catch (Exception e) {
        return "invalid integer value";
      }

      // Determine if input is too short or too long
      if (minvalue < this.getMin ()) return "the minimum value entered is too small";
      if (maxvalue > this.getMax ()) return "the maximum value entered is too high";
      if (minvalue > maxvalue) return "the minimum value must be smaller than the maximum value";

      // Input must be OK
      return null;
    }

    /**
     * @param min the min to set
     */
    public void setMin (int min)
    {
      this.min = min;
    }

    /**
     * @return the min
     */
    public int getMin ()
    {
      return min;
    }

    /**
     * @param max the max to set
     */
    public void setMax (int max)
    {
      this.max = max;
    }

    /**
     * @return the max
     */
    public int getMax ()
    {
      return max;
    }
  }

  @Override
  public void dispose ()
  {
    //
  }

}

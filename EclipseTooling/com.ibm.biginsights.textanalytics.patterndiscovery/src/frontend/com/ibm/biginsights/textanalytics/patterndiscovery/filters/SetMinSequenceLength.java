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
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

import com.ibm.avatar.algebra.datamodel.Pair;
import com.ibm.biginsights.textanalytics.patterndiscovery.Activator;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;

/**
 * defines action that allows the user to sets the minimum sequence length
 * 
 * 
 */
public class SetMinSequenceLength extends Action
{


 
	public static PatternDiscoveryJob job;
  Composite activeComposite;

  /**
   * defines the action parameters
   * 
   * @param job
   * @param activeComposite
   */
  public SetMinSequenceLength (PatternDiscoveryJob job, Composite activeComposite)
  {
    setText (Messages.SEQUENCE_MIN_SIZE_UI_LABEL);
    setToolTipText (Messages.SEQUENCE_MIN_SIZE_UI_TOOLTIP);
    setImageDescriptor (Activator.getImageDescriptor ("setMinSeqLength.gif"));    //$NON-NLS-1$
    setJob (job);
    this.activeComposite = activeComposite;
  }

  public static void setJob (PatternDiscoveryJob ajob)
  {
    job = ajob;
  }

  /**
   * request the new min-seq-length from the user and re-run pattern discovery using this new value
   */
  @Override
  public void run ()
  {
    String dialogTitle = Messages.SEQUENCE_MIN_SIZE_UI_LABEL;
    String dialogMessage = Messages.SEQUENCE_MIN_SIZE_UI_DIALOG_MESSAGE;
    int initialValue = Integer.parseInt (job.getProperty (PropertyConstants.SEQUENCE_MIN_SIZE));

    Pair<Integer, Integer> min_max = job.getMinMaxSeqLength ();

    ScaleDialog input = new ScaleDialog (activeComposite.getShell (), dialogTitle, dialogMessage, initialValue,
      min_max.first, min_max.second, new PositiveNumValidator (min_max));

    if (input.open () == Window.OK) {
      int newValue = input.getValue ();
      if (newValue != initialValue) {
        job.setProperty (PropertyConstants.SEQUENCE_MIN_SIZE, String.format ("%d", newValue)); //$NON-NLS-1$
        job.setProperty (PropertyConstants.RECOMPUTE_SEQUENCES, Boolean.TRUE.toString ());
        job.setProcessLevel (true);
        job.setResetMinMax (true);
        job.resetMinMax ();
        job.schedule ();
      }
    }
  }

  /**
   * creates a validator that ensure that the value is a positive integer
   * 
   * 
   */
  class PositiveNumValidator implements IInputValidator
  {
    int min, max;
    String errorMessage;

    public PositiveNumValidator (Pair<Integer, Integer> min_max)
    {
      this.min = min_max.first;
      this.max = min_max.second;
      errorMessage = String.format (Messages.SEQUENCE_MIN_SIZE_UI_ERROR_MESSAGE, min, max);
    }

    @Override
    public String isValid (String newText)
    {
      try {
        Integer value = Integer.parseInt (newText);
        if (value < min || value > max) return errorMessage;
      }
      catch (Exception e) {
        return Messages.SEQUENCE_MIN_SIZE_UI_INVALID_VALUE;
      }
      return null;
    }
  }

}

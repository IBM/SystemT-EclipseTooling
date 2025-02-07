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
package com.ibm.biginsights.textanalytics.patterndiscovery.single;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDBubbleUtils;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDBubbleUtils.SegmentPosition;

public class SequenceSignatureReplacementTest
{

  public static void main (String[] args)
  {
    new SequenceSignatureReplacementTest ();
  }

  public void runTest ()
  {
    testSplitSegments ();
    testGetPositions ();
    testRebuildSequence ();
    testFixSignature ();
  }

  // ===================================
  // ======Test cases for this class====
  // ===================================

  public void testSplitSegments ()
  {
    String[] expectedValues = { "ab", "cd", "de" };
    String sequence = "{ab;cd;de}";
    ArrayList<String> produced = PDBubbleUtils.splitSegments (sequence);
    for (int i = 0; i < expectedValues.length; i++) {
      Assert.assertTrue (expectedValues[i].equals (produced.get (i)));
    }
  }

  // FIXME: rewrite test with new objects
  public void testGetPositions ()
  {
    String context = "abcdab";
    // ===
    List<SegmentPosition> positionsForAB = PDBubbleUtils.getPositions ("ab", context);
    List<SegmentPosition> positionsForCD = PDBubbleUtils.getPositions ("cd", context);
    // ===
    SegmentPosition expectedForAB1 = new SegmentPosition (0, 2, "ab");
    SegmentPosition expectedForAB2 = new SegmentPosition (4, 6, "ab");
    SegmentPosition expectedForCD = new SegmentPosition (2, 4, "cd");
    // ===
    Assert.assertTrue (expectedForAB1.equals (positionsForAB.get (0)));
    Assert.assertTrue (expectedForAB2.equals (positionsForAB.get (1)));
    // ===
    Assert.assertTrue (expectedForCD.equals (positionsForCD.get (0)));
  }

  public void testRebuildSequence ()
  {
    SegmentPosition expectedForAB = new SegmentPosition (0, 2, "ab");
    SegmentPosition expectedForCD = new SegmentPosition (2, 4, "cd");
    SegmentPosition expectedForDE = new SegmentPosition (4, 6, "de");
    
    String expected = "{ab;cd;de}";
    SegmentPosition [] positions = new SegmentPosition[]{expectedForAB, expectedForCD, expectedForDE};
    Assert.assertTrue (expected.equals (PDBubbleUtils.rebuildSequence (positions)));
  }

  public void testFixSignature ()
  {
    String signature = "{cd;ab;ed}";
    String context = "_12abkcdedlm:+";
    String expected = "{ab;cd;ed}";
    Assert.assertTrue (expected.equals (PDBubbleUtils.fixSignature (signature, context)));
  }
}

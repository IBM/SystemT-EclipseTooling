package com.ibm.biginsights.textanalytics.aql.editor.callhierarchy;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Junit for CallHierarchyUtil.getCurrentTokenBetweenQuotes(...) and CallHierarchyUtil.getCurrentToken(...)
 * It tests, for given cursor positions on sentences, whether the methods pick up the correct token.
 * e.g.
 * abcd |candidate asdfkjs - should pick up "" ( | denotes the cursor position)
 * abcd can|didate asdfasd - should pick up "candidate"
 * abcd candidate| asdfasd - should pick up "candidate"
 * abcd "mod.candid|ate" asdad - should pick up "mod.candidate"
 * abcd candida|te;efgh nexstmt - should pick up "candidate"
 * abcd "mod.candidate"| asdadf - should pick up ""
 * 
 *
 */
public class CallHierarchyTokenDetectionTest
{
  /**
   * Tests the method CallHierarchyUtil.getCurrentTokenBetweenQuotes(...)
   */
  @Test
  public void testGetCurrentTokenBetweenQuotes ()
  {
    String sentences[] = { "select * from \"mymod.withdot.Person\";" };
    int cursorPositions[][] = { { 20, 21, 35, 36 } }; // These positions correspond to before and after the 1st dot, and
                                                      // before and after the last double quote.
    String expectedTokens[][] = { { "mymod.withdot.Person", "mymod.withdot.Person", "mymod.withdot.Person", "" } };
    for (int i = 0; i < 1; i++) {
      for (int j = 0; j < cursorPositions[i].length; j++) {
        String token = CallHierarchyUtil.getCurrentTokenBetweenQuotes (sentences[i], cursorPositions[i][j]);
        System.out.println ("testGetCurrentTokenBetweenQuotes - Expected: " + expectedTokens[i][j] + " - Actual: "
          + token);
        assertEquals ("Expected and detected tokens should match.", expectedTokens[i][j], token);
      }
    }
  }

  /**
   * Tests the method CallHierarchyUtil.getCurrentToken(...)
   */
  @Test
  public void testGetCurrentToken ()
  {
    String sentences[] = { "create view Person", "from nMod.Person P;",
      "output view PhoneNumber;output view nMod.Person;" };
    int cursorPositions[][] = { { 0, 15, 18 }, { 6, 9, 13, 16 }, { 15, 23, 38, 41 } }; // Each set meant for a sentence
                                                                                       // above.
    String expectedTokens[][] = { { "", "Person", "Person" },
      { "nMod.Person", "nMod.Person", "nMod.Person", "nMod.Person" },
      { "PhoneNumber", "PhoneNumber", "nMod.Person", "nMod.Person" } }; // The set of tokens expected to be detected by
                                                                        // getCurrentToken(...)
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < cursorPositions[i].length; j++) {
        String token = CallHierarchyUtil.getCurrentToken (sentences[i], cursorPositions[i][j]);
        System.out.println ("testGetCurrentToken - Expected: " + expectedTokens[i][j] + " - Actual: " + token);
        assertEquals ("Expected and detected tokens should match.", expectedTokens[i][j], token);
      }
    }
  }

}
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
package com.ibm.biginsights.textanalytics.concordance.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModelEntry;

/**
 * Simple sorting. This could be made more fancy if desired.
 */
public class ConcordanceViewerComparator extends ViewerComparator {



  private int primarySortOrder = IConcordanceModel.COLUMN_FILE_ID;

  private int secondarySortOrder = IConcordanceModel.COLUMN_ANNOTATION_TEXT;

  private static final int compare(final IConcordanceModelEntry e1,
      final IConcordanceModelEntry e2, final int sort) {
    switch (sort) {
    case IConcordanceModel.COLUMN_FILE_ID:
      return e1.getDocId().compareTo(e2.getDocId());
    case IConcordanceModel.COLUMN_LEFT_CONTEXT:
      return reverseStringCompare(e1.getLeftContext().trim(), e2.getLeftContext().trim());
    case IConcordanceModel.COLUMN_ANNOTATION_TEXT:
      return e1.getAnnotationText().compareTo(e2.getAnnotationText());
    case IConcordanceModel.COLUMN_RIGHT_CONTEXT:
      return e1.getRightContext().trim().compareTo(e2.getRightContext().trim());
    case IConcordanceModel.COLUMN_ANNOTATION_TYPE:
      return e1.getAnnotationType().compareTo(e2.getAnnotationType());
    default:
      return 0;
    }
  }

  // Check reverse string equality. This uses stupid code point comparison, no localization or other
  // fancy stuff. It might be more correct to reverse the strings and compare them, but presumably
  // that would be less efficient. Who cares.
  private static final int reverseStringCompare(final String s1, final String s2) {
    int i1 = s1.length() - 1;
    int i2 = s2.length() - 1;
    char c1, c2;
    // Iterate over strings until one of them is exhausted.
    while ((i1 >= 0) && (i2 >= 0)) {
      c1 = s1.charAt(i1);
      c2 = s2.charAt(i2);
      if (c1 < c2) {
        return -1;
      } else if (c1 > c2) {
        return 1;
      }
      --i1;
      --i2;
    }
    // When strings are equal up to the end of one string, we need to check which one was the
    // shorter one.
    if (i1 < i2) {
      return -1;
    } else if (i1 > i2) {
      return 1;
    }
    // Strings are equal.
    return 0;
  }

  @Override
  public int compare(Viewer viewer, Object ele1, Object ele2) {
    IConcordanceModelEntry e1 = (IConcordanceModelEntry) ele1;
    IConcordanceModelEntry e2 = (IConcordanceModelEntry) ele2;
    int comp = compare(e1, e2, this.primarySortOrder);
    if (comp == 0) {
      comp = compare(e1, e2, this.secondarySortOrder);
    }
    return comp;
  }

  public final void setSortOrder(int order) {
    if (order != this.primarySortOrder) {
      this.secondarySortOrder = this.primarySortOrder;
      this.primarySortOrder = order;
    }
  }

//  private static final void testReverseStringCompare(String s1, String s2) {
//    final int comp = reverseStringCompare(s1, s2);
//    System.out.print("Reverse string compare: ");
//    if (comp == 0) {
//      System.out.println(s1 + " = " + s2);
//    } else if (comp < 0) {
//      System.out.println(s1 + " < " + s2);
//    } else {
//      System.out.println(s1 + " > " + s2);
//    }
//  }

//  public static void main(String[] s) {
//    testReverseStringCompare("foo", "bar");
//    testReverseStringCompare("foobar", "bar");
//    testReverseStringCompare("foo", "barfoo");
//    testReverseStringCompare("fool", "foop");
//    testReverseStringCompare("foo", "foo");
//  }

}

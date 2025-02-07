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
package com.ibm.biginsights.textanalytics.regex.learner.learner.utils;

import java.util.ArrayList;

/**
 * 
 * 
 * 
 *         This class provides methods two compare two ArrayLists and to find the intersection of
 *         two ArrayLists.
 * 
 */

public final class ListUtilities {



  private ListUtilities() {
    // prevent instantiation
  }

  /**
   * @param list1
   *          and list2 of type ArrayList<String> to be compared
   * @return true if the lists contain the same elements (in arbitrary order), false if they don't
   *         contain exactly the same elements
   */
  public static <E> boolean compareLists(ArrayList<E> list1, ArrayList<E> list2) {
    if (list1 == null) {
      if (list2 == null) {
        return true;
      }
      return list2.isEmpty();
//      if (list2.isEmpty()) {
//        return true;
//      }
//      return false;
    }
    // tg: added second null check
    if (list2 == null) {
      return list1.isEmpty();
    }
    for (final E s : list1) {
      if (!list2.contains(s)) {
        return false;
      }
    }
    for (final E s : list2) {
      if (!list1.contains(s)) {
        return false;
      }
    }
    return true;
  }

  /**
   * returns null if no intersection, a new list containg the items in the intersection otherwise
   * 
   * @param list1
   *          , list2 - ArrayLists of type string
   */
  public static <E> ArrayList<E> intersect(ArrayList<E> list1, ArrayList<E> list2) {
    ArrayList<E> intersection = null;
    for (final E s : list1) {
      if (list2.contains(s)) {
        if (intersection == null) {
          intersection = new ArrayList<E>();
        }
        intersection.add(s);
      }
    }
    return intersection;
  }

}

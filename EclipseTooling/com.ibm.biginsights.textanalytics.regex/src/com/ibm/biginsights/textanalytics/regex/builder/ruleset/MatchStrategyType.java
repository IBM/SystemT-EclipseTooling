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
package com.ibm.biginsights.textanalytics.regex.builder.ruleset;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ibm.biginsights.textanalytics.regex.Messages;


/**
 * <!-- begin-user-doc --> A representation of the literals of the enumeration '
 * <em><b>Match Strategy Type</b></em>', and utility methods for working with them. <!--
 * end-user-doc -->
 * 
 * @see com.ibm.biginsights.textanalytics.regex.builder.ruleset.RuleSetPackage#getMatchStrategyType()
 * @model
 * @generated
 */
public final class MatchStrategyType {


  
  private final int value;
  
  /**
   * The '<em><b>Match First</b></em>' literal value. <!-- begin-user-doc -->
   * <p>
   * If the meaning of '<em><b>Match First</b></em>' literal object isn't clear, there really should
   * be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * 
   * @see #MATCH_FIRST_LITERAL
   * @model name="matchFirst"
   * @generated
   * @ordered
   */
  public static final int MATCH_FIRST = 0;

  /**
   * The '<em><b>Match All</b></em>' literal value. <!-- begin-user-doc -->
   * <p>
   * If the meaning of '<em><b>Match All</b></em>' literal object isn't clear, there really should
   * be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * 
   * @see #MATCH_ALL_LITERAL
   * @model name="matchAll"
   * @generated
   * @ordered
   */
  public static final int MATCH_ALL = 1;

  /**
   * The '<em><b>Match Complete</b></em>' literal value. <!-- begin-user-doc -->
   * <p>
   * If the meaning of '<em><b>Match Complete</b></em>' literal object isn't clear, there really
   * should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * 
   * @see #MATCH_COMPLETE_LITERAL
   * @model name="matchComplete"
   * @generated
   * @ordered
   */
  public static final int MATCH_COMPLETE = 2;

  /**
   * The '<em><b>Match First</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @see #MATCH_FIRST
   * @generated
   * @ordered
   */
  public static final MatchStrategyType MATCH_FIRST_LITERAL = new MatchStrategyType(MATCH_FIRST,
      Messages.MatchStrategyType_MATCH_FIRST, Messages.MatchStrategyType_MATCH_FIRST);

  /**
   * The '<em><b>Match All</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @see #MATCH_ALL
   * @generated
   * @ordered
   */
  public static final MatchStrategyType MATCH_ALL_LITERAL = new MatchStrategyType(MATCH_ALL,
      Messages.MatchStrategyType_MATCH_ALL, Messages.MatchStrategyType_MATCH_ALL);

  /**
   * The '<em><b>Match Complete</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc
   * -->
   * 
   * @see #MATCH_COMPLETE
   * @generated
   * @ordered
   */
  public static final MatchStrategyType MATCH_COMPLETE_LITERAL = new MatchStrategyType(
      MATCH_COMPLETE, Messages.MatchStrategyType_MATCH_COMPLETE, Messages.MatchStrategyType_MATCH_COMPLETE);

  /**
   * An array of all the '<em><b>Match Strategy Type</b></em>' enumerators. <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * 
   * @generated
   */
  private static final MatchStrategyType[] VALUES_ARRAY = new MatchStrategyType[] {
      MATCH_FIRST_LITERAL, MATCH_ALL_LITERAL, MATCH_COMPLETE_LITERAL, };

  /**
   * A public read-only list of all the '<em><b>Match Strategy Type</b></em>' enumerators. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  public static final List VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

  /**
   * Returns the '<em><b>Match Strategy Type</b></em>' literal with the specified literal value.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  public static MatchStrategyType get(String literal) {
    for (int i = 0; i < VALUES_ARRAY.length; ++i) {
      final MatchStrategyType result = VALUES_ARRAY[i];
      if (result.toString().equals(literal)) {
        return result;
      }
    }
    return null;
  }

  /**
   * Returns the '<em><b>Match Strategy Type</b></em>' literal with the specified name. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
//  public static MatchStrategyType getByName(String name) {
//    return null;
//  }

  /**
   * Returns the '<em><b>Match Strategy Type</b></em>' literal with the specified integer value.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  public static MatchStrategyType get(int value) {
    switch (value) {
    case MATCH_FIRST:
      return MATCH_FIRST_LITERAL;
    case MATCH_ALL:
      return MATCH_ALL_LITERAL;
    case MATCH_COMPLETE:
      return MATCH_COMPLETE_LITERAL;
    }
    return null;
  }

  /**
   * Only this class can construct instances. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  private MatchStrategyType(int value, String name, String literal) {
    super();
    this.value = value;
//    super(value, name, literal);
  }
  
  public int getValue() {
    return this.value;
  }

} // MatchStrategyType

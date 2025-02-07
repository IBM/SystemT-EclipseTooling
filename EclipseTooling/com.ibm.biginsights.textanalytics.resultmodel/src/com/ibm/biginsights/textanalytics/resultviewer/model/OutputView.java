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
package com.ibm.biginsights.textanalytics.resultviewer.model;

import javax.xml.bind.Unmarshaller;

/**
 * Models a SystemT output view. Marshalled and unmarshalled with JAXB.
 */
public class OutputView {



  private static final String[] emptyStringArray = new String[] {};

  private static final FieldType[] emptyTypeArray = new FieldType[] {};

  private static final OutputViewRow[] emptyRowArray = new OutputViewRow[] {};

  // The name of the view; must not be null
  private String name;
  
  // The original qualified view name. Above mentioned name could be an output view alias.
  private String origViewName;

  // The field names of this view; must not be null
  private String[] fieldNames;

  // The types of the fields; must not be null and have the same size as the field name array
  private FieldType[] fieldTypes;

  // The rows in the view; must not be null
  private OutputViewRow[] rows;
  
  /**
   * Default constructor is required for JAXB.
   */
  public OutputView() {
    super();
  }

  /**
   * Constructor to initialize the basic facts about a view.
   * 
   * @param name
   *          The name of the view. Alias name if present in output view statement.
   * @param origViewName
   *          The actual qualified name of the view. For non-modular code, name and origViewName should have same value.
   * @param fieldNames
   *          The names of the fields.
   * @param fieldTypes
   *          The types of the fields.
   */
  public OutputView(String name, String origViewName, String[] fieldNames, FieldType[] fieldTypes) {
    super();
    this.name = name;
    this.fieldNames = fieldNames;
    this.fieldTypes = fieldTypes;
    this.rows = emptyRowArray;
    this.origViewName = origViewName;
  }
  
  /**
   * Constructor similar to the one above, uses given parameter value as name as well as actual view name.
   * Created for maintaining code compatibility
   * @param name
   * @param fieldNames
   * @param fieldTypes
   */
  public OutputView(String name, String[] fieldNames, FieldType[] fieldTypes) {
    this(name, name, fieldNames, fieldTypes);
  }

  /**
   * Constructor to initialize the basic facts about a view.
   * 
   * @param name
   *          The name of the view. Alias name if present in output view statement.
   * @param origViewName
   *          The actual qualified name of the view. For non-modular code, name and origViewName should have same value.
   */
  public OutputView(String name, String origViewName) {
    super();
    this.name = name;
    this.fieldNames = emptyStringArray;
    this.fieldTypes = emptyTypeArray;
    this.rows = emptyRowArray;
    this.origViewName = origViewName;
  }
  
  /**
   * Constructor similar to the one above, uses given parameter value as name as well as actual view name.
   * Created for maintaining code compatibility
   * @param name
   * @param origViewName
   */
  public OutputView(String name) {
    this(name,name);
  }

  public String[] getFieldNames() {
    return this.fieldNames;
  }

  public void setFieldNames(String[] fieldNames) {
    this.fieldNames = fieldNames;
  }

  public FieldType[] getFieldTypes() {
    return this.fieldTypes;
  }

  public void setFieldTypes(FieldType[] fieldTypes) {
    this.fieldTypes = fieldTypes;
  }

  public OutputViewRow[] getRows() {
    return this.rows;
  }

  public void setRows(OutputViewRow[] rows) {
    this.rows = rows;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
  
  public void setOrigViewName(String origName) {
    this.origViewName = origName;
  }
   
  public String getOrigViewName() {
    return this.origViewName;
  }

  // JAXB marshalls empty arrays as non-existing nodes (i.e., it doesn't marshall them). On
  // unmarshalling, these arrays are null. Seems like a bug to me. This is one way to get around
  // this issue, since we don't want to have to deal with null values. JAXB calls this method after
  // unmarshalling, so we can make sure that all arrays are properly initialized.
  public void afterUnmarshal(@SuppressWarnings("unused") Unmarshaller um,
      @SuppressWarnings("unused") Object parent) {
    if (this.fieldNames == null) {
      this.fieldNames = emptyStringArray;
    }
    if (this.fieldTypes == null) {
      this.fieldTypes = emptyTypeArray;
    }
    if (this.rows == null) {
      this.rows = emptyRowArray;
    }
  }

  // Only used for testing.
  @Override
  public boolean equals(Object o) {
    if (o instanceof OutputView) {
      OutputView view = (OutputView) o;
      if (!this.name.equals(view.name)) {
        return false;
      }
      if (this.fieldNames.length != view.fieldNames.length) {
        return false;
      }
      for (int i = 0; i < this.fieldNames.length; i++) {
        if (!this.fieldNames[i].equals(view.fieldNames[i])) {
          return false;
        }
      }
      for (int i = 0; i < this.fieldTypes.length; i++) {
        if (this.fieldTypes[i] != view.fieldTypes[i]) {
          return false;
        }
      }
      for (int i = 0; i < this.rows.length; i++) {
        if (!this.rows[i].equals(view.rows[i])) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}

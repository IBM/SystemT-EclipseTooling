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

package com.ibm.biginsights.textanalytics.aql.library;

import java.util.ArrayList;
import java.util.List;

public class AQLFile {

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
  String filePath;
  private List<AQLElement> elements;

  public List<AQLElement> getAQLElements() {
    return elements;
  }

  public void deleteAllElements() {
    try {
      if(!elements.equals(null))
      elements.clear();
    } catch (NullPointerException e) {
      //e.printStackTrace();
    }
  }

  public void addElement(AQLElement element) {
    if(elements == null)
    {
      elements = new ArrayList<AQLElement>();
    }
    elements.add(element);
    
  }

}

class View extends AQLElement {
  //public List<String> dependentView;
}

class Select extends AQLElement {
}

class IncludedFile extends AQLElement {
}

class Function extends AQLElement {
}

class Dictionary extends AQLElement {
}

class Table extends AQLElement {
}

class ExternalView extends AQLElement {
}

class Detag extends AQLElement {
}

class OutputView extends AQLElement {
}

class Module extends AQLElement {
}

class ImportModule extends AQLElement {
}

class ImportView extends AQLElement {
  private AQLElement fromModule;

  public AQLElement getFromModule ()
  {
    return fromModule;
  }

  public void setFromModule (AQLElement fromModule)
  {
    this.fromModule = fromModule;
  }
}

class ImportTabel extends AQLElement {
  private AQLElement fromModule;

  public AQLElement getFromModule ()
  {
    return fromModule;
  }

  public void setFromModule (AQLElement fromModule)
  {
    this.fromModule = fromModule;
  }
}

class ImportFunction extends AQLElement {
  private AQLElement fromModule;

  public AQLElement getFromModule ()
  {
    return fromModule;
  }

  public void setFromModule (AQLElement fromModule)
  {
    this.fromModule = fromModule;
  }
}

class ImportDictionary extends AQLElement {
  private AQLElement fromModule;

  public AQLElement getFromModule ()
  {
    return fromModule;
  }

  public void setFromModule (AQLElement fromModule)
  {
    this.fromModule = fromModule;
  }
}

class RequireDocument extends AQLElement {
}

class ExportView extends AQLElement {
}

class ExportFunction extends AQLElement {
}

class ExportTabel extends AQLElement {
}

class ExportDictionary extends AQLElement {
}

class ExternalFunction extends AQLElement {
}

class ExternalDictionary extends AQLElement {
  private boolean required = true;
  
  public boolean isRequired() {
    return this.required;
  }
  
  public void setRequired(boolean value) {
    this.required = value;
  }
}

class ExternalTable extends AQLElement {
private boolean required = true;
  
  public boolean isRequired() {
    return this.required;
  }
  
  public void setRequired(boolean value) {
    this.required = value;
  }
}

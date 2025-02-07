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
package com.ibm.biginsights.textanalytics.treeview.model.impl;

import com.ibm.biginsights.textanalytics.treeview.model.TreeObjectType;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public class NullTreeObject extends AbstractTreeObject {

    private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" //$NON-NLS-1$
            +
            "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";

    public NullTreeObject() {
        super(TreeObjectType.NULL);
    }

    @Override
    public String toString() {
        return Constants.NULL_DISPLAY_VALUE;
    }

}

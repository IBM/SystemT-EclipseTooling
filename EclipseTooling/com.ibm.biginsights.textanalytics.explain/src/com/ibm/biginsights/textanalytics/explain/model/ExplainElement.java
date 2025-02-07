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

package com.ibm.biginsights.textanalytics.explain.model;

import org.eclipse.swt.graphics.Image;

public interface ExplainElement
{

	public String getName();
  public String getDisplayedName();
	public String getComment();
  public String getTooltip();
	public Image getIcon();
	public ExplainElement getParent();
	public void setParent (ExplainElement parentElement);
	public ExplainElement[] getChildren();
	public boolean hasChildren();

}

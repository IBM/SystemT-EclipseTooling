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
package com.ibm.biginsights.project.templates;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager;

import com.ibm.biginsights.project.templates.TemplateFactory.TemplateFactoryKeys;

public interface ITemplateCreator {

	// mapper-related methods
	public void createMapperType(IType type, ImportsManager imports, Map<TemplateFactoryKeys, Object>data, IProgressMonitor monitor) throws CoreException;
	public String getMapperSuperClassName();
	public String getMapperClassParameters(Map<TemplateFactoryKeys, Object>data);

	// reducer-related methods
	public void createReducerType(IType type, ImportsManager imports, Map<TemplateFactoryKeys, Object>data, IProgressMonitor monitor) throws CoreException;
	public String getReducerSuperClassName();
	public String getReducerClassParameters(Map<TemplateFactoryKeys, Object>data);
	
	// driver-related methods
	public void createDriverType(IType type, ImportsManager imports, Map<TemplateFactoryKeys, Object>data, IProgressMonitor monitor) throws CoreException;
	
	// BigSheets methods
	public void createMacroType(IType type, ImportsManager imports, Map<TemplateFactoryKeys, Object>data, IProgressMonitor monitor) throws CoreException;
	public String getMacroSuperClassName();
	public String getMacroClassParameters(Map<TemplateFactoryKeys, Object>data);
	public void createReaderType(IType type, ImportsManager imports, Map<TemplateFactoryKeys, Object>data, IProgressMonitor monitor) throws CoreException;
	public String getReaderSuperClassName(Map<TemplateFactoryKeys, Object> data);
	public String getReaderClassParameters(Map<TemplateFactoryKeys, Object>data);
	
}

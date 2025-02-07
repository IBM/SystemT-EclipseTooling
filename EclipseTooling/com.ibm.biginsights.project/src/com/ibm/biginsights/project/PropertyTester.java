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
package com.ibm.biginsights.project;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	public PropertyTester() {

	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property.equals("isMigrationRequired")) {
			if (receiver instanceof IProject)
				return MigrateProject.isMigrationRequired((IProject)receiver).isMigrationRequired;
			else if (receiver instanceof IJavaProject) 
				return MigrateProject.isMigrationRequired(((IJavaProject)receiver).getProject()).isMigrationRequired;
		}
		else if (property.equals("isBigInsightsProject")) {
			IProject project = null;
			if (receiver instanceof IProject) {
				project = (IProject)receiver;
			}
			else if (receiver instanceof IFolder) {
				project = ((IFolder)receiver).getProject();
			}
			else if (receiver instanceof IJavaElement) {
				project = ((IJavaElement)receiver).getJavaProject().getProject();
			}
			if (project!=null) {
				try {
					return project.hasNature(ProjectNature.NATURE_ID);
				} catch (CoreException e) {
					return false;
				}
			}
		}
		return false;
	}

}

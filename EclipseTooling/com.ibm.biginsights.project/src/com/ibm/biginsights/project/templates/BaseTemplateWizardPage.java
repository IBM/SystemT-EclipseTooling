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

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.refactoring.StubTypeContext;
import org.eclipse.jdt.internal.corext.refactoring.TypeContextChecker;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.CompletionContextRequestor;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.ControlContentAssistHelper;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaTypeCompletionProcessor;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.jdt.internal.ui.wizards.TypedViewerFilter;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.ProjectNature;
import com.ibm.biginsights.project.templates.TemplateFactory.TemplateFactoryKeys;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;

public abstract class BaseTemplateWizardPage extends NewTypeWizardPage {

	private ITemplateCreator _creator;	
	protected HashMap<TemplateFactoryKeys, Object>data;
	private StubTypeContext _contentAssistContext;
	
	public BaseTemplateWizardPage(String pageName, HashMap<TemplateFactoryKeys, Object>data) {
		super(true, pageName);		
		this.data = data;
	}	
  	
	public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {
				
		super.createType(monitor);
	}
			
	public void init(IStructuredSelection selection) {
		IJavaElement element = getInitialJavaElement(selection);
		initContainerPage(element);
		initTypePage(element);		
	}
	
	abstract public void updateDataInUI();
	
	@Override
	public void createControl(Composite parent) {
	    initializeDialogUnits(parent);
	    Composite composite = new Composite(parent, SWT.NONE);
	    int numCols = 4;
	    composite.setLayout(new GridLayout(numCols, false));

	    createContainerControls(composite, numCols);

	    createPackageControls(composite, numCols);
	    createSeparator(composite, numCols);
	    createTypeNameControls(composite, numCols);

	    // don't show super-class because we set it anyway
	    createSuperClassControls(composite, numCols);
	    createSuperInterfacesControls(composite, numCols);
	    
	    setControl(composite);	    
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), "com.ibm.biginsights.mapreduce.help.create_mapreduce_app"); //$NON-NLS-1$
	    
	    doUpdateStatus();

	}

	// method is called when source folder changes
	protected IStatus containerChanged() {			
		data.put(TemplateFactoryKeys.BASE_SRC_FOLDER, this.getPackageFragmentRoot());
		IStatus status = super.containerChanged();
		if ((status.isOK() || status.getSeverity()==IStatus.WARNING))
		{
			// only check for BI project if status is ok so far (i.e. a value was set and it's a Java project)
			String str= getPackageFragmentRootText();
			IPath path= new Path(str);
			IResource res= ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			IProject project= res.getProject();

			// check for BI nature
			try {
				if (!project.hasNature(ProjectNature.NATURE_ID)) {
					((StatusInfo)status).setError(Messages.BASETEMPLATEWIZARDPAGE_NOT_BI_PROJECT_ERROR);				
				}
				else {
					// update the templateCreator when the source folder has changed as it could be for a diff project
					determineTemplateCreator(project);
				}
			}
			catch (Exception ex)  {
				
			}
		}
		return status;
	}
	
	protected IStatus packageChanged() {
		IStatus s = super.packageChanged();
		if (this.getPackageFragment()!=null && !this.getPackageFragment().isDefaultPackage())
			data.put(TemplateFactoryKeys.BASE_PACKAGE, this.getPackageFragment());
		return s;
	}
	
	protected void handleFieldChangedNoStatusUpdate(String fieldName) {
		super.handleFieldChanged(fieldName);
	}
	
	protected void handleFieldChanged(String fieldName) {		
		super.handleFieldChanged(fieldName);
		doUpdateStatus();
	}
	
	public void doUpdateStatus() {		
		IStatus[] status = new IStatus[] {
				fContainerStatus, fPackageStatus, fTypeNameStatus, fSuperClassStatus, fSuperInterfacesStatus};		
		updateStatus(status);		
	}
	
	public void setVisible(boolean visible) {		
		super.setVisible(visible);
		if (visible && this.getTypeName()!=null && this.getTypeName().length()>0) {
			handleFieldChanged(CONTAINER);
			doUpdateStatus();		
		}
	}
	
	private void determineTemplateCreator(IProject project) {
		// first determine the right templateCreator class which is used across the generator methods
		// depending on the project of the selected source folder, create the driver class
		//  if the project is not a BigInsights project, just use the default template		
		_creator = null;
		try {
			if (project.hasNature(ProjectNature.NATURE_ID)) {
				String projectBIVersion = BIProjectPreferencesUtil.getBigInsightsLibrariesVersion(project);
	        	_creator = TemplateFactory.getTemplateCreator(projectBIVersion);
			}					
		} catch (CoreException e) {
			 
		}

		if (_creator==null)
			_creator = TemplateFactory.getDefaultTemplateCreator();

	}

	public ITemplateCreator getTemplateCreator() {
		return this._creator;
	}

	protected Text createEntryField(Composite composite, String lableText, final TemplateFactoryKeys dataKey, boolean readonly)
	{
		Label lblTextObject = new Label(composite, SWT.NONE);
		lblTextObject.setFont(composite.getFont());		
		lblTextObject.setText(lableText);
		
		final Text txtInputfield = new Text(composite, SWT.SINGLE | SWT.BORDER);   
		txtInputfield.setEnabled(!readonly);
        GridData gdText = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gdText.horizontalSpan = 2;
        gdText.grabExcessHorizontalSpace = true;
        txtInputfield.setLayoutData(gdText);
        txtInputfield.setFont(composite.getFont());  
        ModifyListener listener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				_contentAssistContext = null;
				data.put(dataKey, ((Text)event.widget).getText());
				doUpdateStatus();
			}
        };
        txtInputfield.addModifyListener(listener);
        		
        // content assist for type input field
		JavaTypeCompletionProcessor typeCompletionProcessor= new JavaTypeCompletionProcessor(false, false, true);		
		typeCompletionProcessor.setCompletionContextRequestor(new CompletionContextRequestor() {
			public StubTypeContext getStubTypeContext() {				
				return getContentAssistContext(dataKey.toString());
			}
		});
		
		ControlContentAssistHelper.createTextContentAssistant(txtInputfield, typeCompletionProcessor);
		TextFieldNavigationHandler.install(txtInputfield);

        Button _btnBrowseMapperClass = new Button(composite, SWT.PUSH);   
        _btnBrowseMapperClass.setVisible(!readonly);
        _btnBrowseMapperClass.setText(Messages.BASETEMPLATEWIZARDPAGE_BROWSE_LABEL);
        _btnBrowseMapperClass.setFont(composite.getFont());
        _btnBrowseMapperClass.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (!readonly) {
	        _btnBrowseMapperClass.addListener(SWT.Selection, new Listener() {
	        	public void handleEvent(Event event) {
	        		IType type = chooseType((String)data.get(dataKey));	        		
	        		if (type!=null) {
	        			// dialog was closed with OK
	        			txtInputfield.setText(type.getFullyQualifiedName());
	        		}	
	        	}
	        });
        }
        
        return txtInputfield;
	}

	private StubTypeContext getContentAssistContext(String currentType) {
		if (_contentAssistContext == null) {
			String typeName = currentType==null || currentType.isEmpty() ? JavaTypeCompletionProcessor.DUMMY_CLASS_NAME : currentType; 					
			_contentAssistContext= TypeContextChecker.createSuperClassStubTypeContext(typeName, getEnclosingType(), getPackageFragment());
		}
		return _contentAssistContext;
	}


	protected IType chooseType(String initialValue) {
		IJavaProject project= getJavaProject();
		if (project == null) {
			return null;
		}

		IJavaElement[] elements= new IJavaElement[] { project };
		IJavaSearchScope scope= SearchEngine.createJavaSearchScope(elements);

		FilteredTypesSelectionDialog dialog= new FilteredTypesSelectionDialog(getShell(), false,
			getWizard().getContainer(), scope, IJavaSearchConstants.CLASS);
		dialog.setTitle(Messages.BASETEMPLATEWIZARDPAGE_TYPE_TITLE);
		dialog.setMessage(Messages.BASETEMPLATEWIZARDPAGE_TYLE_DESC);
		if (initialValue!=null)
			dialog.setInitialPattern(initialValue);

		if (dialog.open() == Window.OK) {
			return (IType) dialog.getFirstResult();
		}
		return null;
	}

	protected IPackageFragmentRoot chooseContainer() {
		IJavaElement initElement= getPackageFragmentRoot();
		Class[] acceptedClasses= new Class[] { IPackageFragmentRoot.class, IJavaProject.class };
		TypedElementSelectionValidator validator= new TypedElementSelectionValidator(acceptedClasses, false) {
			public boolean isSelectedValid(Object element) {
				try {
					if (element instanceof IJavaProject) {
						IJavaProject jproject= (IJavaProject)element;
						IPath path= jproject.getProject().getFullPath();
						return (jproject.findPackageFragmentRoot(path) != null);
					} else if (element instanceof IPackageFragmentRoot) {
						return (((IPackageFragmentRoot)element).getKind() == IPackageFragmentRoot.K_SOURCE);
					}
					return true;
				} catch (JavaModelException e) {
					JavaPlugin.log(e.getStatus()); // just log, no UI in validation
				}
				return false;
			}
		};

		acceptedClasses= new Class[] { IJavaModel.class, IPackageFragmentRoot.class, IJavaProject.class };
		ViewerFilter filter= new TypedViewerFilter(acceptedClasses) {
			public boolean select(Viewer viewer, Object parent, Object element) {
				if (element instanceof IJavaProject) {
					try {
						// only show BI projects: check for BI nature 
						return ((IJavaProject)element).getProject().hasNature(ProjectNature.NATURE_ID);
					} catch (CoreException e) {
						return false;						
					}
				}
				else if (element instanceof IPackageFragmentRoot) {
					try {
						return (((IPackageFragmentRoot)element).getKind() == IPackageFragmentRoot.K_SOURCE);
					} catch (JavaModelException e) {
						JavaPlugin.log(e.getStatus()); // just log, no UI in validation
						return false;
					}
				}
				return super.select(viewer, parent, element);
			}
		};

		StandardJavaElementContentProvider provider= new StandardJavaElementContentProvider();
		ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(), labelProvider, provider);
		dialog.setValidator(validator);
		dialog.setComparator(new JavaElementComparator());
		dialog.setTitle(NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_title);
		dialog.setMessage(NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_description);
		dialog.addFilter(filter);
		dialog.setInput(JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()));
		dialog.setInitialSelection(initElement);
		dialog.setHelpAvailable(false);

		if (dialog.open() == Window.OK) {
			Object element= dialog.getFirstResult();
			if (element instanceof IJavaProject) {
				IJavaProject jproject= (IJavaProject)element;
				return jproject.getPackageFragmentRoot(jproject.getProject());
			} else if (element instanceof IPackageFragmentRoot) {
				return (IPackageFragmentRoot)element;
			}
			return null;
		}
		return null;
	}

}

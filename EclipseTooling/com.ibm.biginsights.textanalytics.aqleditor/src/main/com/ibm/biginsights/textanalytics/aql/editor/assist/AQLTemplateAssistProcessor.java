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
package com.ibm.biginsights.textanalytics.aql.editor.assist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.swt.graphics.Image;

import com.ibm.biginsights.textanalytics.aql.editor.Activator;


/**
 * 
 *  Babbar
 */
public class AQLTemplateAssistProcessor extends TemplateCompletionProcessor{



	
	
	private String context;

	protected Template[] getTemplates(String contextTypeId) {
		AQLTemplateManager manager = AQLTemplateManager.getInstance();
		return manager.getTemplateStore(context).getTemplates();
	}

	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		AQLTemplateManager manager = AQLTemplateManager.getInstance();
		return manager.getContextTypeRegistry(context).getContextType(context);
	}

	protected Image getImage(Template template) {
		return Activator.getDefault().getImageRegistry().get(Activator.ICON_TEMPLATE);
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

		//System.out.println("template generation");
		ITextSelection selection= (ITextSelection) viewer.getSelectionProvider().getSelection();

		// adjust offset to end of normalized selection
		if (selection.getOffset() == offset)
			offset= selection.getOffset() + selection.getLength();

		String prefix= extractPrefix(viewer, offset);
		Region region= new Region(offset - prefix.length(), prefix.length());
		TemplateContext context= createContext(viewer, region);
		if (context == null)
			return new ICompletionProposal[0];
		//System.out.println("selection: " + selection.getText());
		context.setVariable("selection", selection.getText()); // name of the selection variables {line, word}_selection //$NON-NLS-1$

		Template[] templates= getTemplates(context.getContextType().getId());

		List<ICompletionProposal> matches= new ArrayList<ICompletionProposal>();
		for (int i= 0; i < templates.length; i++) {
			Template template= templates[i];
			try {
				context.getContextType().validate(template.getPattern());
			} catch (TemplateException e) {
				continue;
			}
			if (template.getName().startsWith(prefix) && 
					template.matches(prefix, context.getContextType().getId()))
				matches.add(createProposal(template, context, (IRegion) region, getRelevance(template, prefix)));
		}

		return matches.toArray(new ICompletionProposal[matches.size()]);
	}

	public void initializeContext(String contextType) {
		this.context = contextType;
	}

}

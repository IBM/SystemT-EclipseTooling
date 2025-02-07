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
package com.ibm.biginsights.textanalytics.aql.editor.ui;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.Messages;
import com.ibm.biginsights.textanalytics.aql.editor.assist.AQLAssistProcessor;
import com.ibm.biginsights.textanalytics.aql.editor.hover.AQLDocHover;
import com.ibm.biginsights.textanalytics.aql.editor.hover.AQLHoverInformationControl;

/**
 * AQLConfiguration class holds the configuration details of an opened aql file in editor such as 
 * AQL editor, color manager, double click strategy, statement scanner etc. 
 * This configuration will be available as long as the file opened in editor. 
 * 
 */
public class AQLConfiguration extends SourceViewerConfiguration {



  private AQLDoubleClickStrategy doubleClickStrategy;

  private AQLStatementScanner statementScanner;

//  private AQLDefaultScanner scanner;
  private AQLEditor editor;
  private ColorManager colorManager;
  private InformationPresenter fInfoPresenter;

  public AQLConfiguration(ColorManager colorManager, AQLEditor editor) {
    super();
    this.editor = editor;
    this.colorManager = colorManager;
  }

  @Override
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
    return new String[] { IDocument.DEFAULT_CONTENT_TYPE, AQLPartitionScanner.AQL_COMMENT,
        AQLPartitionScanner.AQL_REGEX, AQLPartitionScanner.AQL_STRING };
  }

  @Override
  public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer,
      String contentType) {
    if (this.doubleClickStrategy == null) {
      this.doubleClickStrategy = new AQLDoubleClickStrategy();
    }
    return this.doubleClickStrategy;
  }

  // protected AQLDefaultScanner getAQLDefaultScanner() {
  // if (this.scanner == null) {
  // this.scanner = new AQLDefaultScanner(this.colorManager);
  // this.scanner.setDefaultReturnToken(new Token(new TextAttribute(this.colorManager
  // .getColor(IAQLColorConstants.DEFAULT))));
  // }
  // return this.scanner;
  // }

  public AQLStatementScanner getAQLStatementScanner() {
    if (this.statementScanner == null) {
      this.statementScanner = new AQLStatementScanner(this.colorManager);
      this.statementScanner.setDefaultReturnToken(new Token(new TextAttribute(this.colorManager
          .getColor(IAQLColorConstants.DEFAULT))));
    }
    return this.statementScanner;
  }

  @Override
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
    PresentationReconciler reconciler = new PresentationReconciler();

    // DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getAQLStatementScanner());
    // reconciler.setDamager(dr, AQLPartitionScanner.AQL_STATEMENT);
    // reconciler.setRepairer(dr, AQLPartitionScanner.AQL_STATEMENT);

    DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getAQLStatementScanner());
    reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setDamager(dr, AQLPartitionScanner.AQL_REGEX);
    reconciler.setRepairer(dr, AQLPartitionScanner.AQL_REGEX);
    reconciler.setDamager(dr, AQLPartitionScanner.AQL_STRING);
    reconciler.setRepairer(dr, AQLPartitionScanner.AQL_STRING);
    reconciler.setDamager(dr, AQLPartitionScanner.AQL_VIEW_NAME);
    reconciler.setRepairer(dr, AQLPartitionScanner.AQL_VIEW_NAME);

    // dr = new DefaultDamagerRepairer(getAQLDefaultScanner());
    // reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
    // reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

    NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(new TextAttribute(
        this.colorManager.getColor(IAQLColorConstants.AQL_COMMENT)));
    reconciler.setDamager(ndr, AQLPartitionScanner.AQL_COMMENT);
    reconciler.setRepairer(ndr, AQLPartitionScanner.AQL_COMMENT);

    return reconciler;
  }

  @Override
  public IReconciler getReconciler(ISourceViewer sourceViewer) {
    // Do we really need a reconciler? We can do a full build via the builder, and a file build
    // via a resource change.
    MonoReconciler reconciler = new MonoReconciler(new ReconcilingStrategy(this.editor), false);
    reconciler.install(sourceViewer);
    reconciler.setDelay(600);
    return reconciler;
  }

  @Override
   public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer){
	   return new DefaultAnnotationHover();
   }
   
   @Override 
   public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
	  
	    AQLAssistProcessor aqlAssist = new AQLAssistProcessor();
		ContentAssistant assistant = new ContentAssistant();
        assistant.setInformationControlCreator(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}});
        assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
        assistant.setContentAssistProcessor(aqlAssist, AQLPartitionScanner.AQL_STRING);
        assistant.setContentAssistProcessor(aqlAssist, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.install(sourceViewer);
        assistant.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
        assistant.setProposalSelectorBackground(colorManager.getColor(new RGB(255, 250, 240)));
        assistant.enableAutoActivation(true);
        assistant.setAutoActivationDelay(500);        
		return assistant;		
	}

   /** The DefaultInformationControl returned by this IInformationControlCreator will be the
   * InformationControl used by the AQLDocHover
   */
   public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
     return new IInformationControlCreator() {
     public IInformationControl createInformationControl(Shell parent) {
         return new DefaultInformationControl(parent,  Messages.AQLEditor_HOVER_TIP);
     }};
   }
   
   /** This InformationPresenter will be used to present the doc comment information when F2 is
   * pressed on an AQLElement in the AQLEditor. AbstractTextEditor will check for the InformationPresenter,
   * to display the doc comment information as a tooltip help.
   */
   public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
       if (fInfoPresenter == null) {
         IInformationControlCreator infoCreator = new IInformationControlCreator() {
             public IInformationControl createInformationControl(Shell parent) {
                 return new AQLHoverInformationControl (parent);
             }
         };
         //InformationPresenter is created with the InformationControlCreator that holds the InformationControl. InformationPresenter
         //requires an InformationControl to control the action of displaying the tooltip help.
        fInfoPresenter= new InformationPresenter (infoCreator) {
        protected Point computeInformationControlLocation (Rectangle subjectArea, Point controlSize)
        {
             int x =  subjectArea.x ;
             int y =  subjectArea.y + subjectArea.height; 
             return new Point ( x + 290, y+100);
        }
        };
        
       fInfoPresenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
       fInfoPresenter.setAnchor(AbstractInformationControlManager.ANCHOR_BOTTOM);
       ITextHover hover = getTextHover (sourceViewer, IDocument.DEFAULT_CONTENT_TYPE);
       //InformationPresenter requires an InformationProvider thats takes the responsibility of providing the necessary information
       //that will be presented by the InformationPresenter. ITextHover instance is passed to the InformationProvider to process the 
       // information that needs to be displayed.
       IInformationProvider providers = new InformationProvider (hover);
       fInfoPresenter.setInformationProvider(providers, IDocument.DEFAULT_CONTENT_TYPE);
       fInfoPresenter.setSizeConstraints(80, 10, false, true);
       fInfoPresenter.setFocus ((IWidgetTokenOwner) sourceViewer);
       fInfoPresenter.takesFocusWhenVisible (true);
     }
     return fInfoPresenter;
   }
   
   public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return new AQLDocHover();
   }
   
   public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
     return super.getTextHover(sourceViewer, contentType, stateMask);
   }
   /**
    * Disposes the AQL configuration objects such as editor, color manager, statement scanner etc 
    * before closing the file in editor.
    * 
    */
   public void dispose(){
	   this.colorManager.dispose();
	   this.doubleClickStrategy = null;
	   this.statementScanner = null;
	   this.editor = null;
   }
   
   /**
    * This class provides doc comment information to the InformationPresenter
    */
   class InformationProvider implements IInformationProvider {
     private ITextHover docHover;
     InformationProvider(ITextHover hover) {
       docHover = hover;
   }
  
    @Override
    public String getInformation (ITextViewer textViewer, IRegion region)
    {
      ITextHoverExtension2 textHover= (ITextHoverExtension2)docHover;
      String fHoverInfo1 = (String) textHover.getHoverInfo2 (textViewer, region);
      return fHoverInfo1;
    }

    @Override
    public IRegion getSubject (ITextViewer textViewer, int offSet)
    {
      return docHover.getHoverRegion (textViewer, offSet);
    }
   }
  
}

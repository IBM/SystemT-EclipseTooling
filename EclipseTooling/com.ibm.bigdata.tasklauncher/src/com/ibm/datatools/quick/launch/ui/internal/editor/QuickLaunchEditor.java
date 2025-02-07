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
package com.ibm.datatools.quick.launch.ui.internal.editor;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.ibm.datatools.quick.launch.ui.Activator;
import com.ibm.datatools.quick.launch.ui.Copyright;
import com.ibm.datatools.quick.launch.ui.QuickLaunchConstants;
import com.ibm.datatools.quick.launch.ui.i18n.IAManager;
import com.ibm.datatools.quick.launch.ui.i18n.IconManager;
import com.ibm.datatools.quick.launch.ui.internal.core.QuickLaunchSolutionManager;
import com.ibm.datatools.quick.launch.ui.internal.core.QuickLaunchSolutionManager.Solution;
import com.ibm.datatools.quick.launch.ui.internal.pref.QuickLaunchPreferences;

/**
 * Quick launch editor. Intended to be used internally by quick launch
 * 
 * 
 * @since 2010May05
 */
public class QuickLaunchEditor extends SharedHeaderFormEditor
{



    private IManagedForm m_form;

    private QuickLaunchEditorInput m_input;

    public static final String BODY_BG_COLOR = "0xB8D0F0"; //$NON-NLS-1$

    public static final String BANNER_BG_COLOR = "0x638ECE"; //$NON-NLS-1$

    public static final String TAB_BLUE_COLOR = "0x5888C8"; //$NON-NLS-1$
    
    public static final String TITLE_BLUE_COLOR = "0x0050AA"; //$NON-NLS-1$

    public static final String WHITE_COLOR = "0xFFFFFF"; //$NON-NLS-1$

    public static final String GROUP_BORDER_COLOR = "0x90B6DC"; //$NON-NLS-1$

    public static final String GROUP_BG_COLOR = "0xFAFCFC"; //$NON-NLS-1$

    public static final String UC_TITLE_COLOR = "0x0050AA"; //$NON-NLS-1$

    public static final String UC_DESC_COLOR = "0x4D4D4D"; //$NON-NLS-1$

    public static final String TAB_BEGIN = "   "; //$NON-NLS-1$

    public static final String TAB_END = "   "; //$NON-NLS-1$

    public static final String SEPARATOR = " | "; //$NON-NLS-1$

    private static int BOX_BORDER_WIDTH = 20;


    protected void createPages()
    {
        super.createPages();
        if ( getContainer() instanceof CTabFolder )
        {
            ((CTabFolder)getContainer()).setTabHeight( 0 );
        }

    }

    @Override
    protected void addPages()
    {
    	Boolean highContrast = Display.getDefault().getHighContrast();
        try
        {

            FormPage solutionPage = null;
            String solutionId = null;
            
            if (!highContrast) {
	            JFaceResources.getColorRegistry().put( BODY_BG_COLOR, new RGB( 184, 208, 240 ) ); 
	            JFaceResources.getColorRegistry().put( BANNER_BG_COLOR, new RGB( 99, 142, 206 ) );
	            JFaceResources.getColorRegistry().put( TAB_BLUE_COLOR, new RGB( 88, 136, 200 ) );  
	            JFaceResources.getColorRegistry().put( WHITE_COLOR, new RGB( 255, 255, 255 ) );
	            JFaceResources.getColorRegistry().put( TITLE_BLUE_COLOR, new RGB( 0, 80, 170 ) );
	            JFaceResources.getColorRegistry().put( GROUP_BORDER_COLOR, new RGB( 144, 182, 220 ) );
	            JFaceResources.getColorRegistry().put( GROUP_BG_COLOR, new RGB( 250, 252, 252 ) );
	            JFaceResources.getColorRegistry().put( UC_TITLE_COLOR, new RGB( 0, 80, 170 ) );
	            JFaceResources.getColorRegistry().put( UC_DESC_COLOR, new RGB( 77, 77, 77 ) );
            }

            Solution[] solutions = QuickLaunchSolutionManager.getTopSolutions();

            for ( QuickLaunchSolutionManager.Solution solution : solutions )
            {

                if ( solution == null )
                {
                    solutionPage = new FormPage( this, IAManager.QuickLaunchEditor_DEFAULT_FORM,
                            IAManager.QuickLaunchEditor_DEFAULT_FORM );
                    m_form = solutionPage.getManagedForm();
                    TableWrapLayout layout = new TableWrapLayout();
                    layout.numColumns = 2;
                    m_form.getForm().getBody().setLayout( layout );
                    m_form.reflow( true );

                    return;
                }

                if ( solution.getId().equals( m_input.getSolutionId() ) )
                {
                    solutionId = solution.getId();
                }

                solutionPage = new SolutionsPage( this, solution );

                addPage( solutionPage );                
            }

            QuickLaunchSolutionManager.setEditorSolutionId( solutionId );
            IFormPage activePage = setActivePage( QuickLaunchSolutionManager.getEditorSolutionId() );
            if (activePage!=null && activePage.getManagedForm()!=null)
            	activePage.getManagedForm().reflow(true);
        }
        catch ( PartInitException e )
        {
            Activator.log( e );
        }
    }

    @Override
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        super.init( site, input );
        if ( !(input instanceof QuickLaunchEditorInput) )
        {
            throw new PartInitException( "OptimSolutionsEditorInput required" ); //$NON-NLS-1$
        }
        m_input = ((QuickLaunchEditorInput)input);

    }

    @Override
    public void doSave( IProgressMonitor monitor )
    {
        // NOOP

    }

    @Override
    public void doSaveAs()
    {
        // NOOP

    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return true;
    }



    class SolutionsPage extends FormPage
    {

        QuickLaunchSolutionManager.Solution solution;

        ScrolledForm m_form;
        
    	Hyperlink[] solutionTabs;

        protected SolutionsPage( FormEditor editor, QuickLaunchSolutionManager.Solution solution )
        {
            super( editor, solution.getId(), null );
            this.solution = solution;
        }

        @Override
        protected void createFormContent( IManagedForm managedForm )
        {

            this.m_form = managedForm.getForm();
            TableWrapLayout layout = new TableWrapLayout();
            layout.numColumns = 1;
            layout.makeColumnsEqualWidth = true;
            layout.bottomMargin = layout.topMargin = layout.leftMargin = layout.rightMargin = 0;
            layout.horizontalSpacing = layout.verticalSpacing = 0;
            m_form.getBody().setLayout( layout );
            m_form.getBody().setBackground(
                    JFaceResources.getColorRegistry().get( BODY_BG_COLOR ) );

            createBanner( managedForm, this.solution );

            createBody( managedForm, this.solution );

            Rectangle rect = m_form.getParent().getBounds();
            m_form.setSize( rect.width, rect.height );
        }

        public void createBanner( IManagedForm headerForm,
                QuickLaunchSolutionManager.Solution p_activeSolution )
        {
            Color BANNER_COLOR = JFaceResources.getColorRegistry().get( BANNER_BG_COLOR );
            Color TAB_COLOR = JFaceResources.getColorRegistry().get( TAB_BLUE_COLOR );

            ScrolledForm form = headerForm.getForm();
            form.setShowFocusedControl(true); // accessibility: turn on focus control, otherwise tabbing through the items doesn't scroll the editor pane
            Composite bannerGroup = headerForm.getToolkit().createComposite( form.getBody() );
            TableWrapLayout bLayout = new TableWrapLayout();
            bLayout.numColumns = 1;//2;//1
            bLayout.makeColumnsEqualWidth = false;
            bLayout.horizontalSpacing = bLayout.verticalSpacing = 0;
            bLayout.bottomMargin = bLayout.topMargin = bLayout.leftMargin = bLayout.rightMargin = 0;
            bannerGroup.setLayout( bLayout );
            TableWrapData td = new TableWrapData( TableWrapData.FILL_GRAB );
            td.colspan = 1;
            bannerGroup.setLayoutData( td );
            bannerGroup.setBackground( BANNER_COLOR );

            Label banner = new Label( bannerGroup, SWT.TOP );
            Image img = IconManager.getImage( IconManager.OPTIM_BANNER );
            banner.setImage( img );
            TableWrapData td1 = new TableWrapData( TableWrapData.FILL_GRAB );
            td1.colspan = 1;
            banner.setLayoutData( td1 );
            banner.setBackground( BANNER_COLOR );

            Solution[] solutions = QuickLaunchSolutionManager.getTopSolutions();
            int numtabs = (solutions == null || solutions.length < 1)
                    ? 1
                    : solutions.length;
            int cells = (numtabs > 1)
                    ? numtabs * 2
                    : 1;

            
            Composite linksGroup = headerForm.getToolkit().createComposite(  bannerGroup );
            Composite tabGroup = headerForm.getToolkit().createComposite(  linksGroup );
            
            TableWrapLayout linkLayout = new TableWrapLayout();
            linkLayout.numColumns = 2;
            linkLayout.makeColumnsEqualWidth = false;
            linkLayout.horizontalSpacing = linkLayout.verticalSpacing = 0;
            linkLayout.bottomMargin = linkLayout.topMargin = linkLayout.leftMargin = linkLayout.rightMargin = 0;
            TableWrapData tdLinkGroup = new TableWrapData( TableWrapData.FILL_GRAB);
            tdLinkGroup.colspan = 1;
            linksGroup.setBackground( TAB_COLOR );
            linksGroup.setLayoutData(tdLinkGroup);
            linksGroup.setLayout( linkLayout );
            
            TableWrapLayout tLayout = new TableWrapLayout();
            tLayout.numColumns = cells ;
            tLayout.makeColumnsEqualWidth = false;
            tLayout.horizontalSpacing = bLayout.verticalSpacing = 0;
            tLayout.topMargin = tLayout.leftMargin = tLayout.rightMargin = 0;
            tLayout.bottomMargin = 0;
            tabGroup.setLayout( tLayout );
            TableWrapData tdtab = new TableWrapData( TableWrapData.LEFT);//TableWrapData.FILL_GRAB);
            tdtab.colspan = 1;
            tabGroup.setLayoutData( tdtab );
            tabGroup.setBackground( TAB_COLOR );

            // Thomas: even with one tab
            if ( cells > 0 )
            {
                solutionTabs = new Hyperlink[ numtabs ];
                int i = 0;
                Font bold = JFaceResources.getFontRegistry().getBold( JFaceResources.HEADER_FONT );
                String actionSolutionId = p_activeSolution.getId();
                for ( Solution solution : solutions )
                {
                    Hyperlink tab = new Hyperlink( tabGroup, SWT.LEFT );//NONE );
                    tab.setText( TAB_BEGIN + solution.getLabel() + TAB_END );
                    tab.setData( solution.getId() );
                    tab.setFont( bold );
                    tab.addHyperlinkListener( new HyperlinkAdapter()
                    {

                        @Override
                        public void linkActivated( HyperlinkEvent e )
                        {
                            String solutionId = (String)e.widget.getData();
                            if ( solutionId != null && solutionId.trim().length() > 0 )
                            {
                                QuickLaunchSolutionManager.setEditorSolutionId( solutionId );
                                IFormPage page = setActivePage( solutionId );  
                                if (page.getManagedForm()!=null)
                                	page.getManagedForm().reflow(true);
                                
                                if(page instanceof SolutionsPage) {
	                            	SolutionsPage p=(SolutionsPage)page;                            	                            	
	                            	(p.getSolutionTabs()[p.getIndex()]).setFocus(); 
                                }  
                            }
                        }

                    } );
                    tdtab = new TableWrapData();
                    tdtab.colspan = 1;
                    tab.setLayoutData( tdtab );
                    tab.setBackground( TAB_COLOR );

                    if ( actionSolutionId != null && actionSolutionId.equals( solution.getId() ) )
                    {
                        tab
                                .setBackground( JFaceResources.getColorRegistry().get(
                                		BODY_BG_COLOR ) );
                        tab
                                .setForeground( JFaceResources.getColorRegistry().get(
                                        TITLE_BLUE_COLOR ) );
                    }
                    else
                    {
                        tab.setBackground( JFaceResources.getColorRegistry().get( TAB_BLUE_COLOR ) );
                        tab.setForeground( JFaceResources.getColorRegistry().get( WHITE_COLOR ) );
                    }

                    solutionTabs[ i ] = tab;
                    ++i;

                    if ( i < solutions.length )
                    {
                        Label label = headerForm.getToolkit().createLabel( tabGroup, SEPARATOR );
                        label.setFont( bold );
                        label.setBackground( TAB_COLOR );
                        label.setForeground( JFaceResources.getColorRegistry().get( WHITE_COLOR ) );
                        TableWrapData tdlab = new TableWrapData(TableWrapData.FILL_GRAB);//);
                        tdlab.colspan = 1;
                        label.setLayoutData( tdlab );
                    }
                }
            }
            
            Hyperlink prefLink = new Hyperlink(linksGroup, SWT.BOTTOM );
            prefLink.setText(TAB_BEGIN+ IAManager.QuickLaunchEditor_Preferences +TAB_BEGIN);
//            Font bold = JFaceResources.getFontRegistry().getBold( JFaceResources.HEADER_FONT );
            Font bold = JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT );
            prefLink.setForeground( JFaceResources.getColorRegistry().get( WHITE_COLOR ) );
            prefLink.setBackground(TAB_COLOR);
            prefLink.setFont( bold );
            TableWrapData prefData = new TableWrapData( TableWrapData.RIGHT );
            prefData.grabHorizontal = true;
            prefData.valign = TableWrapData.BOTTOM;
            prefLink.setLayoutData(prefData);
            prefLink.addHyperlinkListener(new IHyperlinkListener() {

				public void linkActivated(HyperlinkEvent e) {
					IPreferencePage page = new QuickLaunchPreferences();
					page.setTitle(IAManager.QuickLaunchEditor_TaskLauncher);
					PreferenceManager mgr = new PreferenceManager();
					IPreferenceNode node = new PreferenceNode(QuickLaunchConstants.QUICK_LAUNCH_PREF_PAGE, page);
					mgr.addToRoot(node);
					PreferenceDialog dialog = new PreferenceDialog(m_form.getShell(), mgr);
					dialog.create();
					dialog.setMessage(page.getTitle());
					dialog.open();
					
				}

				public void linkEntered(HyperlinkEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void linkExited(HyperlinkEvent e) {
					// TODO Auto-generated method stub
					
				}
            });
            Rectangle rect = form.getParent().getBounds();
            form.setSize( rect.width, rect.height );
        }

        protected void createBody( IManagedForm headerForm,
                QuickLaunchSolutionManager.Solution p_solution )
        {

            ScrolledForm form = headerForm.getForm();
            Composite bodyGroup = headerForm.getToolkit().createComposite( form.getBody() );
            TableWrapLayout bLayout = new TableWrapLayout();
            bLayout.numColumns = 2;
            bLayout.makeColumnsEqualWidth = true;
            bLayout.horizontalSpacing = bLayout.verticalSpacing = BOX_BORDER_WIDTH;
            bLayout.bottomMargin = bLayout.topMargin = bLayout.leftMargin = bLayout.rightMargin = BOX_BORDER_WIDTH;
            bodyGroup.setLayout( bLayout );
            TableWrapData td = new TableWrapData( TableWrapData.FILL_GRAB );
            td.colspan = 1;
            bodyGroup.setLayoutData( td );
            bodyGroup.setBackground( JFaceResources.getColorRegistry().get( BODY_BG_COLOR ) );

            for ( QuickLaunchSolutionManager.Group group : p_solution.getGroups() )
            {
                addGroup( bodyGroup, p_solution, group );
            }

        }

        private void addGroup( Composite parent, QuickLaunchSolutionManager.Solution solution,
                QuickLaunchSolutionManager.Group group )
        {
            ArrayList<QuickLaunchSolutionManager.UseCase> usecases = solution.getUseCases( group
                    .getId() );

            if ( usecases != null )
            {
                Composite highlightBox = QuickLaunchEditor.this.getToolkit().createComposite(
                        parent );
                TableWrapData td = new TableWrapData();
                td.align = TableWrapData.FILL;
                td.rowspan = group.getRowspan();
                highlightBox.setLayoutData( td );
                TableWrapLayout layout = new TableWrapLayout();
                layout.bottomMargin = layout.topMargin = layout.leftMargin = layout.rightMargin = 10;
                layout.numColumns = 1;
                highlightBox.setLayout( layout );
                highlightBox.setBackground( JFaceResources.getColorRegistry().get( GROUP_BORDER_COLOR ) );

                Composite groupBox = QuickLaunchEditor.this.getToolkit().createComposite(
                        highlightBox );
                td = new TableWrapData( TableWrapData.FILL_GRAB );
                groupBox.setLayoutData( td );
                layout = new TableWrapLayout();
                layout.bottomMargin = layout.topMargin = BOX_BORDER_WIDTH-5;
                layout.leftMargin = layout.rightMargin = BOX_BORDER_WIDTH;
                layout.numColumns = 2;
                layout.verticalSpacing = group.getVerticalSpacing();
                groupBox.setLayout( layout );

                Color color = JFaceResources.getColorRegistry().get( GROUP_BG_COLOR );

                groupBox.setBackground( color );

                header( group.getLabel(), groupBox, color );
                int first=0;
                for ( QuickLaunchSolutionManager.UseCase usecase : usecases )
                {                	
                    usecase(first++==0? group.getLabel():"", usecase, groupBox, color );
                }

            }
        }

        private void header( String label, Composite learnbox, Color color )
        {
            Label header = QuickLaunchEditor.this.getToolkit().createLabel( learnbox, label );
            TableWrapData td = new TableWrapData( TableWrapData.FILL_GRAB );
            td.colspan = 2;
            header.setLayoutData( td );
            header.setFont( JFaceResources.getHeaderFont() );
            header.setBackground( color );
            header.setForeground( JFaceResources.getColorRegistry().get( TITLE_BLUE_COLOR ) );

        }

        private void usecase(String groupLabel, QuickLaunchSolutionManager.UseCase usecase, Composite groupbox,
                Color color )
        {

            Image img = IconManager.getImage( usecase.getImagePath() );

            final IAction action = usecase.getAction();

            HyperlinkAdapter hyperlinkAction = new HyperlinkAdapter()
            {
                public void linkActivated( HyperlinkEvent e )
                {
                    if ( action != null )
                    {
                        action.run();
                    }
                    else
                    {
                        System.out.println( "Link active: " + e.getHref() ); //$NON-NLS-1$
                    }
                }
            };

            ImageHyperlink image1 = new ImageHyperlink( groupbox, SWT.BOTTOM );
            TableWrapData td = new TableWrapData( TableWrapData.LEFT );
            td.colspan = 1;
            image1.setLayoutData( td );
            if ( img != null )
            {
                image1.setActiveImage( img );
                image1.setImage( img );
                //Accessibility workaround. set the first image label as the group label 
                final String lblName = groupLabel!=""?groupLabel:usecase.getLabel();
                image1.getAccessible().addAccessibleListener(new AccessibleAdapter() {
                	public void getName(AccessibleEvent event) {                		
                		event.result = lblName;
                	}
                });
            }
            image1.setBackground( color );
            image1.addHyperlinkListener( hyperlinkAction );

            Composite usecaseBox = QuickLaunchEditor.this.getToolkit().createComposite( groupbox );
            td = new TableWrapData( TableWrapData.FILL_GRAB );
            td.colspan = 1;
            usecaseBox.setLayoutData( td );
            TableWrapLayout layout = new TableWrapLayout();
            layout.bottomMargin = layout.topMargin = layout.leftMargin = layout.rightMargin = 0;
            layout.verticalSpacing = layout.horizontalSpacing = 0;
            usecaseBox.setLayout( layout );
            usecaseBox.setBackground( color );
            
            Hyperlink link = new Hyperlink( usecaseBox, SWT.WRAP );
            link.setText( usecase.getLabel() );
            link.addHyperlinkListener( hyperlinkAction );
            td = new TableWrapData( TableWrapData.FILL_GRAB );
            td.colspan = 1;
            link.setLayoutData( td );
            Font bold = JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT );
            link.setFont( bold );
            link.setBackground( color );
            link.setForeground( JFaceResources.getColorRegistry().get( UC_TITLE_COLOR ) );

            String description = usecase.getDescription();
            if(description!=null && description.trim().length()>0)
            {
	            Hyperlink descr = new Hyperlink( usecaseBox, SWT.WRAP );
	            descr.setText( description );
	            descr.addHyperlinkListener( hyperlinkAction );
	            td = new TableWrapData( TableWrapData.FILL );
	            td.colspan = 1;
	            descr.setLayoutData( td );
	            descr.setBackground( color );
	            link.setForeground( JFaceResources.getColorRegistry().get( UC_DESC_COLOR ) );
            }

        }
        
        private Hyperlink[] getSolutionTabs() {
        	return solutionTabs;
        }
    }

}

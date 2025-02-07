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
package com.ibm.biginsights.textanalytics.workflow.plan.models;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.dnd.ActionPLanTransfer;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.AQLNodeModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.LabelModel;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlGroupType;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlTypes;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.GroupType;

/**
 * defines a Tag node
 * 
 * 
 */
public class LabelNode extends NodesGroup
{



  /** the file where the content of this tag is located */
  protected LabelModel    labelModel;

  protected ExamplesFolderNode examples;
  protected LabelsFolderNode subTags, basicSubTags, candidateSubTags, refinementSubTags, finalSubTags;
  protected AqlGroup basics, candidates, refinements, finals;

  public static final String BF_GROUP_NAME = "BasicFeatures";         // $NON-NLS-1$
  public static final String CG_GROUP_NAME = "CandidateGeneration";   // $NON-NLS-1$
  public static final String FC_GROUP_NAME = "FilterConsolidate";     // $NON-NLS-1$
  public static final String Finals_GROUP_NAME = "Finals";            // $NON-NLS-1$

  public static final String EXAMPLES_FOLDER_NAME = "Examples";       // $NON-NLS-1$
  public static final String LABELS_FOLDER_NAME = "Labels";           // $NON-NLS-1$

  // TODO deprecate this constructor
  public LabelNode (String name, IProject project, boolean generateAQLs) throws CoreException, IOException
  {
    super (name, GroupType.TAG, null);
    if (generateAQLs)
      createAqlFiles (project);
  }

  public LabelNode (LabelModel model)
  {
    super (model.getName (), GroupType.TAG, null);
    this.labelModel = model;
    buildChildren ();
  }

  public LabelNode (LabelModel model, IProject project, boolean generateAQLs) throws CoreException, IOException
  {
    this (model);
    if (generateAQLs)
      createAqlFiles (project);
  }

  public void buildChildren ()
  {
    basics = new AqlGroup (BF_GROUP_NAME, GroupType.BASIC_FEATURES, this);
    basicSubTags = basics.getLabelsFolder ();

    candidates = new AqlGroup (CG_GROUP_NAME, GroupType.CONCEPTS, this);
    candidateSubTags = candidates.getLabelsFolder ();

    refinements = new AqlGroup (FC_GROUP_NAME, GroupType.REFINEMENT, this);
    refinementSubTags = refinements.getLabelsFolder ();

    finals = new AqlGroup (Finals_GROUP_NAME, GroupType.FINALS, this);
    finalSubTags = finals.getLabelsFolder ();

    examples = new ExamplesFolderNode ("Examples", this);
    subTags = new LabelsFolderNode ("Labels", this);
  }

  public LabelModel toModel ()
  {
    return labelModel;
  }

  public void beforeDelete ()
  {
    // if (isRoot) {
    // try {
    // AqlProjectUtils.removePathFromDataPath(AqlProjectUtils.getProject(projectName),
    // folder.getFullPath());
    //
    // AqlProjectUtils.removeIncludeToMainAql(projectName,
    // getBasicsfile().getProjectRelativePath().toString());
    //
    // AqlProjectUtils.removeIncludeToMainAql(projectName,
    // getConceptsfile().getProjectRelativePath().toString());
    //
    // AqlProjectUtils.removeIncludeToMainAql(projectName,
    // getRefinementsfile().getProjectRelativePath().toString());
    //
    // AqlProjectUtils.removeAqlFolder(folder);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
  }

  public void createAqlFiles (IProject project) throws CoreException, IOException
  {
    IFolder aql_folder = project.getFolder (AqlProjectUtils.MAIN_AQL_FOLDERNAME);

    if (aql_folder == null || !aql_folder.exists ()) {
      aql_folder = AqlProjectUtils.createAqlFolder (project, project.getFolder (AqlProjectUtils.MAIN_AQL_FOLDERNAME));
    }

    IFolder folder = AqlProjectUtils.createAqlFolder (project, aql_folder.getFolder (getLabel()));

    IFile bas_file = AqlProjectUtils.createAqlFile (project, folder, "basics.aql");
    setBasicsfile (Constants.WORKSPACE_RESOURCE_PREFIX + bas_file.getFullPath ().toString ());
    AqlProjectUtils.appendIncludeToMainAql (project, String.format ("%s/%s", label, bas_file.getName ()));

    IFile con_file = AqlProjectUtils.createAqlFile (project, folder, "concepts.aql");
    setConceptsfile (Constants.WORKSPACE_RESOURCE_PREFIX + con_file.getFullPath ().toString ());
    AqlProjectUtils.appendIncludeToMainAql (project, String.format ("%s/%s", label, con_file.getName ()));

    IFile ref_file = AqlProjectUtils.createAqlFile (project, folder, "refinement.aql");
    setRefinementsfile (Constants.WORKSPACE_RESOURCE_PREFIX + ref_file.getFullPath ().toString ());
    AqlProjectUtils.appendIncludeToMainAql (project, String.format ("%s/%s", label, ref_file.getName ()));
  }

  public void addAqlNode (AqlNode node, AqlTypes type)
  {
    switch (type) {
      case DICTIONARY:
      case REGEX:
      case PARTofSPEECH:
        addBasicFeature (node);
      break;

      case SELECT:
      case BLOCK:
      case PATTERN:
      case UNION:
        addConcept (node);
      break;

      case PREDICATEbasedFILTER:
      case SETbasedFILTER:
      case CONSOLIDATE:
        addRefinement (node);
      break;

    }
  }

  public List<String> getTaggedFiles ()
  {
    List<String> tagged = new LinkedList<String> ();

    // get my labels
    for (TreeObject obj : examples.getChildren ()) {
      if (obj instanceof ExampleNode) {
        String str = ((ExampleNode) obj).getFileLabel ();
        if (!tagged.contains (str)) tagged.add (str);
      }
    }
    // recurse my children's labels
    for (TreeObject obj : subTags.getChildren ()) {
      if (obj instanceof LabelNode) {
        List<String> subtagged = ((LabelNode) obj).getTaggedFiles ();
        for (String str : subtagged) {
          if (!tagged.contains (str)) tagged.add (str);
        }
      }
    }

    return tagged;
  }

  public boolean isDone ()
  {
    return (labelModel != null && labelModel.isDone ());
  }

  public void setDone (boolean done)
  {
    this.labelModel.setDone (done);
  }

  public IFile getBasicsfile ()
  {
    String basicsfile = labelModel.getBasicfilepath ();

    if ( basicsfile == null || basicsfile.isEmpty () )
      return null;

    return AqlProjectUtils.getWorkspaceRoot ().getFile ( (IPath) new Path (ProjectPreferencesUtil.getPath (basicsfile)) );
  }

  public void setBasicsfile (String basicsfile)
  {
    this.labelModel.setBasicfilepath (basicsfile);
  }

  public IFile getConceptsfile ()
  {
    String candidatesfile = labelModel.getConceptfilepath ();

    if (candidatesfile == null || candidatesfile.isEmpty ())
      return null;

    return AqlProjectUtils.getWorkspaceRoot ().getFile ( (IPath) new Path (ProjectPreferencesUtil.getPath (candidatesfile)) );
  }

  public void setConceptsfile (String conceptsfile)
  {
    this.labelModel.setConceptfilepath (conceptsfile);
  }

  public IFile getRefinementsfile ()
  {
    String refinementsfile = labelModel.getRefinementfilepath ();

    if (refinementsfile == null || refinementsfile.isEmpty ())
      return null;

    return AqlProjectUtils.getWorkspaceRoot ().getFile ( (IPath) new Path (ProjectPreferencesUtil.getPath (refinementsfile)) );
  }

  public void setRefinementsfile (String refinementsfile)
  {
    this.labelModel.setRefinementfilepath (refinementsfile);
  }

  public void addBasicFeature (AqlNode node)
  {
    basics.getAqlStatementsFolder ().addChild (node);
    this.labelModel.addAqlNodeModel (node.toModel (), AqlGroupType.BASIC);
  }

  public void removeBasicFeature (AqlNode node)
  {
    basics.getAqlStatementsFolder ().removeChild (node);
  }

  public void addConcept (AqlNode node)
  {
    candidates.getAqlStatementsFolder ().addChild (node);
    this.labelModel.addAqlNodeModel (node.toModel (), AqlGroupType.CONCEPT);
  }

  public void removeConcepts (AqlNode node)
  {
    candidates.getAqlStatementsFolder ().removeChild (node);
  }

  public void addRefinement (AqlNode node)
  {
    refinements.getAqlStatementsFolder ().addChild (node);
    this.labelModel.addAqlNodeModel (node.toModel (), AqlGroupType.REFINEMENT);
  }

  public void removeRefinement (AqlNode node)
  {
    refinements.getAqlStatementsFolder ().removeChild (node);
  }

  public void addFinals (AqlNode node)
  {
    finals.getAqlStatementsFolder ().addChild (node);
    this.labelModel.addAqlNodeModel (node.toModel (), AqlGroupType.FINALS);
  }

  public void removeFinals (AqlNode node)
  {
    finals.getAqlStatementsFolder ().removeChild (node);
  }

  public void addExample (ExampleNode example)
  {
    examples.addChild (example);
  }

  public void addExample2 (ExampleNode example)
  {
    examples.addChild (example);
    labelModel.addExample (example.toModel ());
  }

  public void removeExample (ExampleNode example)
  {
    examples.removeChild (example);
  }

  public void addSubLabel (LabelNode node, AqlGroupType aqlType)
  {
    if (aqlType == null) {
      subTags.addChild2 (node);
      return;
    }

    switch (aqlType) {
      case BASIC:
        basicSubTags.addChild2 (node);
        break;
      case CONCEPT:
        candidateSubTags.addChild2 (node);
        break;
      case REFINEMENT:
        refinementSubTags.addChild2 (node);
        break;
      case FINALS:
        break;
    }
  }

  public void removeSubLabel (LabelNode node)
  {
    if ( ! subTags.removeChild (node) )
      if ( ! basicSubTags.removeChild (node) )
        if ( ! candidateSubTags.removeChild (node) )
          if ( ! refinementSubTags.removeChild (node) )
            finalSubTags.removeChild (node);
  }

  public void writeOut (DataOutputStream writeOut) throws IOException
  {
    // write the label
    byte[] buffer = label.getBytes ();
    writeOut.writeInt (buffer.length);
    writeOut.write (buffer);

    // write the comment
    String comment = (getComment () != null) ? getComment () : "";
    buffer = comment.getBytes ();
    writeOut.writeInt (buffer.length);
    writeOut.write (buffer);

    // is done editing?
    writeOut.writeBoolean (labelModel.isDone ());

    // basics file
    buffer = labelModel.getBasicfilepath ().getBytes ();
    writeOut.writeInt (buffer.length);
    writeOut.write (buffer);

    // candidates file
    buffer = labelModel.getConceptfilepath ().getBytes ();
    writeOut.writeInt (buffer.length);
    writeOut.write (buffer);

    // refinements file
    buffer = labelModel.getRefinementfilepath ().getBytes ();
    writeOut.writeInt (buffer.length);
    writeOut.write (buffer);

    // write how many elements will be written from know
    int count = examples.getChildren ().size() +
                basics.getChildAqlNodes ().size() + candidates.getChildAqlNodes ().size() + refinements.getChildAqlNodes ().size() + finals.getChildAqlNodes ().size() +
                subTags.getChildren ().size() + basicSubTags.getChildren ().size () + candidateSubTags.getChildren ().size () + refinementSubTags.getChildren ().size () + finalSubTags.getChildren ().size ();
    writeOut.writeInt (count);

    // write examples
    for (TreeObject example : examples.getChildren ()) {
      if (example instanceof ExampleNode) {
        writeOut.writeInt (ActionPLanTransfer.EXAMPLE);
        ((ExampleNode) example).writeOut (writeOut);
      }
    }

    // write aql elements

    for (AqlNode aqlNode : basics.getChildAqlNodes ()) {
      writeOut.writeInt (ActionPLanTransfer.BASICS);
      aqlNode.writeOut (writeOut);
    }

    for (AqlNode aqlNode : candidates.getChildAqlNodes ()) {
      writeOut.writeInt (ActionPLanTransfer.CANDIDATES);
      aqlNode.writeOut (writeOut);
    }

    for (AqlNode aqlNode : refinements.getChildAqlNodes ()) {
      writeOut.writeInt (ActionPLanTransfer.REFINEMENT);
      aqlNode.writeOut (writeOut);
    }

    for (AqlNode aqlNode : finals.getChildAqlNodes ()) {
      writeOut.writeInt (ActionPLanTransfer.FINALS);
      aqlNode.writeOut (writeOut);
    }

    // write sub labels
    for (TreeObject sublabel : subTags.getChildren ()) {
      if (sublabel instanceof LabelNode) {
        writeOut.writeInt (ActionPLanTransfer.LABEL);
        ((LabelNode) sublabel).writeOut (writeOut);
      }
    }

    for (TreeObject sublabel : basicSubTags.getChildren ()) {
      if (sublabel instanceof LabelNode) {
        writeOut.writeInt (ActionPLanTransfer.LABEL_BASICS);
        ((LabelNode) sublabel).writeOut (writeOut);
      }
    }

    for (TreeObject sublabel : candidateSubTags.getChildren ()) {
      if (sublabel instanceof LabelNode) {
        writeOut.writeInt (ActionPLanTransfer.LABEL_CANDIDATES);
        ((LabelNode) sublabel).writeOut (writeOut);
      }
    }

    for (TreeObject sublabel : refinementSubTags.getChildren ()) {
      if (sublabel instanceof LabelNode) {
        writeOut.writeInt (ActionPLanTransfer.LABEL_REFINEMENTS);
        ((LabelNode) sublabel).writeOut (writeOut);
      }
    }

    for (TreeObject sublabel : finalSubTags.getChildren ()) {
      if (sublabel instanceof LabelNode) {
        writeOut.writeInt (ActionPLanTransfer.LABEL_FINALS);
        ((LabelNode) sublabel).writeOut (writeOut);
      }
    }
  }

  public static LabelNode readIn (DataInputStream readIn) throws IOException
  {
    String label = "";
    String comment = "";
    String basicspath = "";
    String candidatesfile = "";
    String refinementsfile = "";
    String finalsfile = "";
    boolean isDone = false;

    ArrayList<ExampleModel> examples = new ArrayList<ExampleModel> ();

    ArrayList<AQLNodeModel> basics = new ArrayList<AQLNodeModel> ();
    ArrayList<AQLNodeModel> candidates = new ArrayList<AQLNodeModel> ();
    ArrayList<AQLNodeModel> refinements = new ArrayList<AQLNodeModel> ();
    ArrayList<AQLNodeModel> finals = new ArrayList<AQLNodeModel> ();

    ArrayList<LabelModel> sublabels = new ArrayList<LabelModel> ();
    ArrayList<LabelModel> sublabels_basics = new ArrayList<LabelModel> ();
    ArrayList<LabelModel> sublabels_candidates = new ArrayList<LabelModel> ();
    ArrayList<LabelModel> sublabels_refinements = new ArrayList<LabelModel> ();
    ArrayList<LabelModel> sublabels_finals = new ArrayList<LabelModel> ();

    // read label
    int size = readIn.readInt ();
    byte[] name = new byte[size];
    readIn.read (name);
    label = new String (name);

    // read comment
    size = readIn.readInt ();
    name = new byte[size];
    readIn.read (name);
    comment = new String (name);

    // read editing state
    isDone = readIn.readBoolean ();

    // read basics file
    size = readIn.readInt ();
    name = new byte[size];
    readIn.read (name);
    basicspath = new String (name);

    // read candidates file
    size = readIn.readInt ();
    name = new byte[size];
    readIn.read (name);
    candidatesfile = new String (name);

    // read refinements file
    size = readIn.readInt ();
    name = new byte[size];
    readIn.read (name);
    refinementsfile = new String (name);

    // read number of elements to read
    int count = readIn.readInt ();
    for (int i = 0; i < count; i++) {
      int type = readIn.readInt ();
      switch (type) {
        case ActionPLanTransfer.EXAMPLE:
          examples.add (ExampleNode.readIn (readIn).toModel ());
        break;

        case ActionPLanTransfer.BASICS:
          basics.add (AqlNode.readIn (readIn).toModel ());
        break;

        case ActionPLanTransfer.CANDIDATES:
          candidates.add (AqlNode.readIn (readIn).toModel ());
        break;

        case ActionPLanTransfer.REFINEMENT:
          refinements.add (AqlNode.readIn (readIn).toModel ());
        break;

        case ActionPLanTransfer.FINALS:
          finals.add (AqlNode.readIn (readIn).toModel ());
        break;

        case ActionPLanTransfer.LABEL:
          sublabels.add (readIn (readIn).toModel ());
        break;

        case ActionPLanTransfer.LABEL_BASICS:
          sublabels_basics.add (readIn (readIn).toModel ());
        break;

        case ActionPLanTransfer.LABEL_CANDIDATES:
          sublabels_candidates.add (readIn (readIn).toModel ());
        break;

        case ActionPLanTransfer.LABEL_REFINEMENTS:
          sublabels_refinements.add (readIn (readIn).toModel ());
        break;

        case ActionPLanTransfer.LABEL_FINALS:
          sublabels_finals.add (readIn (readIn).toModel ());
        break;
      }
    }

    LabelModel model = new LabelModel (label, comment, basicspath, candidatesfile, refinementsfile, finalsfile, examples, basics,
      candidates, refinements, finals, sublabels, sublabels_basics, sublabels_candidates, sublabels_refinements, sublabels_finals, isDone);

    return new LabelNode (model);
  }

  public void setComment (String comment)
  {
    this.labelModel.setComment (comment);
  }

  public String getComment ()
  {
    return labelModel.getComment();
  }

  @Override
  public void setLabel (String label)
  {
    super.setLabel (label);
    labelModel.setName (label);
  }

  public AqlGroup getBasicsGroup ()
  {
    return basics;
  }

  public AqlGroup getCandidatesGroup ()
  {
    return candidates;
  }

  public AqlGroup getRefinementsGroup ()
  {
    return refinements;
  }

  public AqlGroup getFinalsGroup ()
  {
    return finals;
  }

  public AqlGroup getAqlGroup (AqlGroupType grpType)
  {
    if (grpType == AqlGroupType.BASIC)
      return getBasicsGroup ();
    else if (grpType == AqlGroupType.CONCEPT)
      return getCandidatesGroup ();
    else if (grpType == AqlGroupType.REFINEMENT)
      return getRefinementsGroup ();
    else if (grpType == AqlGroupType.FINALS)
      return getFinalsGroup ();
    else
      return null;
  }

  public LabelsFolderNode getSubLabelFolder()
  {
    return subTags;
  }

  public boolean isDisplayed(){
    return true;
  }

  public boolean isRootLabel() {
    return !(parent instanceof LabelsFolderNode);
  }

  public List<AqlNode> getAllAqlNodes ()
  {
    List<AqlNode> allAqlNodes = new ArrayList<AqlNode> ();

    // Get all direct aql child nodes
    for (TreeObject to : basics.getAqlStatementsFolder ().getChildren ()) {
      allAqlNodes.add ((AqlNode)to);
    }

    for (TreeObject to : candidates.getAqlStatementsFolder ().getChildren ()) {
      allAqlNodes.add ((AqlNode)to);
    }

    for (TreeObject to : refinements.getAqlStatementsFolder ().getChildren ()) {
      allAqlNodes.add ((AqlNode)to);
    }

    for (TreeObject to : finals.getAqlStatementsFolder ().getChildren ()) {
      allAqlNodes.add ((AqlNode)to);
    }

    // combine with all aql children of the child labels
    for (LabelNode labelNode : getAllFirstLevelLabelNodes ()) {
      allAqlNodes.addAll (labelNode.getAllAqlNodes ());
    }

    return allAqlNodes;
  }

  public String getGeneratedModuleName (AqlGroupType aqlGrpType)
  {
    switch (aqlGrpType) {
    case BASIC:
        return getLabel () + "_" + BF_GROUP_NAME;
    case CONCEPT:
      return getLabel () + "_" + CG_GROUP_NAME;
    case REFINEMENT:
      return getLabel () + "_" + FC_GROUP_NAME;
    case FINALS:
      return getLabel () + "_" + Finals_GROUP_NAME;
    }

    return "";
  }

  public List<LabelNode> getAllFirstLevelLabelNodes ()
  {
    List<LabelNode> allLabelNodes = new ArrayList<LabelNode> ();

    for (TreeObject to : subTags.getChildren ()) {
      allLabelNodes.add ((LabelNode)to);
    }

    for (TreeObject to : basicSubTags.getChildren ()) {
      allLabelNodes.add ((LabelNode)to);
    }

    for (TreeObject to : candidateSubTags.getChildren ()) {
      allLabelNodes.add ((LabelNode)to);
    }

    for (TreeObject to : refinementSubTags.getChildren ()) {
      allLabelNodes.add ((LabelNode)to);
    }

    for (TreeObject to : finalSubTags.getChildren ()) {
      allLabelNodes.add ((LabelNode)to);
    }

    return allLabelNodes;
  }

  public Object[] getDisplayedChildren() {

    //-------- Get displayed children normally
    if (!ActionPlanView.isSimplifiedView ())
      return super.getDisplayedChildren ();

    //-------- get children for simplified view -- only the AQL groups
    List<TreeObject> displayedChildren = new ArrayList<TreeObject> ();
    for (TreeObject to : getChildren()) {
      if (to instanceof AqlGroup && to.isDisplayed ())
        displayedChildren.add (to);
    }

    return displayedChildren.toArray ();
  }

  public AqlGroupType getAqlGroupType()
  {
    if (getParent () != null || getParent ().getParent () != null) {
      TreeObject grandParent = getParent ().getParent ();
      if (grandParent instanceof AqlGroup)
        return ((AqlGroup)grandParent).getAqlType ();
    }

    return null;
  }

}

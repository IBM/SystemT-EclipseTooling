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
package com.ibm.biginsights.textanalytics.workflow.plan.serialize;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlGroupType;

@XmlRootElement(name = "label")
public class LabelModel {



  private String name;
	private String comment;
	private String basicfilepath;
	private String conceptfilepath;
	private String refinementfilepath;
	private String finalsfilepath;
	private boolean done;

	private ArrayList<ExampleModel> examples;

	private ArrayList<AQLNodeModel> basics;
	private ArrayList<AQLNodeModel> concepts;
	private ArrayList<AQLNodeModel> refinements;
	private ArrayList<AQLNodeModel> finals;

	private ArrayList<LabelModel> subTags;
	private ArrayList<LabelModel> basicsubTags;
	private ArrayList<LabelModel> conceptsubTags;
	private ArrayList<LabelModel> refinementsubTags;
	private ArrayList<LabelModel> finalsubTags;

  public LabelModel() {
		this ( "", "", "", "", "", "",
		       new ArrayList<ExampleModel>(),
		       new ArrayList<AQLNodeModel>(), new ArrayList<AQLNodeModel>(), new ArrayList<AQLNodeModel>(), new ArrayList<AQLNodeModel>(),
		       new ArrayList<LabelModel>(),
		       new ArrayList<LabelModel>(), new ArrayList<LabelModel>(), new ArrayList<LabelModel>(), new ArrayList<LabelModel>(), false);
	}

  public LabelModel (String name)
  {
    this();
    setName (name);
  }

	public LabelModel ( String name, String comment,
	                    String basicfilepath, String conceptfilepath, String refinementfilepath, String finalsfilepath,
	                    ArrayList<ExampleModel> examples,
                      ArrayList<AQLNodeModel> basics, ArrayList<AQLNodeModel> concepts, ArrayList<AQLNodeModel> refinements, ArrayList<AQLNodeModel> finals,
                      ArrayList<LabelModel> subTags,
                      ArrayList<LabelModel> basicsubTags, ArrayList<LabelModel> conceptsubTags, ArrayList<LabelModel> refinementsubTags, ArrayList<LabelModel> finalsubTags,
                      boolean done )
  {
    super ();
    this.name = name;
    this.comment = comment;
    this.basicfilepath = basicfilepath;
    this.conceptfilepath = conceptfilepath;
    this.refinementfilepath = refinementfilepath;
    this.finalsfilepath = finalsfilepath;
    this.done = done;
    this.examples = examples;
    this.basics = basics;
    this.concepts = concepts;
    this.refinements = refinements;
    this.finals = finals;
    this.subTags = subTags;
    this.basicsubTags = basicsubTags;
    this.conceptsubTags = conceptsubTags;
    this.refinementsubTags = refinementsubTags;
    this.finalsubTags = finalsubTags;

    if (this.examples == null)
      this.examples = new ArrayList<ExampleModel> ();
    if (this.basics == null)
      this.basics = new ArrayList<AQLNodeModel> ();
    if (this.concepts == null)
      this.concepts = new ArrayList<AQLNodeModel> ();
    if (this.refinements == null)
      this.refinements = new ArrayList<AQLNodeModel> ();
    if (this.finals == null)
      this.finals = new ArrayList<AQLNodeModel> ();
    if (this.subTags == null)
      this.subTags = new ArrayList<LabelModel> ();
    if (this.basicsubTags == null)
      this.basicsubTags = new ArrayList<LabelModel> ();
    if (this.conceptsubTags == null)
      this.conceptsubTags = new ArrayList<LabelModel> ();
    if (this.refinementsubTags == null)
      this.refinementsubTags = new ArrayList<LabelModel> ();
    if (this.finalsubTags == null)
      this.finalsubTags = new ArrayList<LabelModel> ();

  }

  public String getName() {
		return name;
	}

  /**
   * Get the sub-labels of certain type -- basic feature, candidate generation, filter and consolidate, finals.<br>
   * If aqlGroupType is null, return the sub-labels without type.<br>
   * @param aqlGroupType
   * @return
   */
  public List<LabelModel> getSubLabels(AqlGroupType aqlGroupType) {
    if (aqlGroupType == null)
      return subTags;

    switch (aqlGroupType) {
      case BASIC:
        return basicsubTags;

      case CONCEPT:
        return conceptsubTags;

      case REFINEMENT:
        return refinementsubTags;

      case FINALS:
        return finalsubTags;

      default:
        return null;
    }
  }

  /**
   * Get the AQL views of certain type -- basic feature, candidate generation, filter and consolidate, finals.<br>
   * Return null if groupType is null or not one of those 4 types.<br>
   * @param aqlGroupType
   * @return
   */
  public List<AQLNodeModel> getAqlObjects(AqlGroupType aqlGroupType) {
    if (aqlGroupType == null)
      return null;

    switch (aqlGroupType) {
      case BASIC:
        return basics;

      case CONCEPT:
        return concepts;

      case REFINEMENT:
        return refinements;

      case FINALS:
        return finals;

      default:
        return null;
    }
  }

  /**
   * This method is here for converting 1.3 extraction plan to 1.5 only.<br>
   * In 1.3 each AQL group links to a file; in 1.5 the info about aql file is in each AqlNodeModel.
   */
  public String getAqlFilePath(AqlGroupType aqlGroupType) {
    if (aqlGroupType == null)
      return null;

    switch (aqlGroupType) {
      case BASIC:
        return basicfilepath;

      case CONCEPT:
        return conceptfilepath;

      case REFINEMENT:
        return refinementfilepath;

      case FINALS:
        return finalsfilepath;    // shouldn't reach here, 1.3 doesn't have the concept of 'finals'

      default:
        return null;
    }
  }

  public List<AQLNodeModel> getAllAqlObjects ()
  {
    List<AQLNodeModel> allSubLabels = new ArrayList<AQLNodeModel> (basics);
    allSubLabels.addAll (concepts);
    allSubLabels.addAll (refinements);
    allSubLabels.addAll (finals);

    return null;
  }

  public List<LabelModel> getAllSubLabels ()
  {
    List<LabelModel> allSubLabels = new ArrayList<LabelModel> (subTags);
    allSubLabels.addAll (basicsubTags);
    allSubLabels.addAll (conceptsubTags);
    allSubLabels.addAll (refinementsubTags);
    allSubLabels.addAll (finalsubTags);

    return allSubLabels;
  }

  public AqlGroupType getAqlType (AQLNodeModel aqlModel) {
    if (getBasics () != null && getBasics ().contains (aqlModel))
      return AqlGroupType.BASIC;
    else if (getConcepts () != null && getConcepts ().contains (aqlModel))
      return AqlGroupType.CONCEPT;
    else if (getRefinements () != null && getRefinements ().contains (aqlModel))
      return AqlGroupType.REFINEMENT;
    else if (getFinals () != null && getFinals ().contains (aqlModel))
      return AqlGroupType.FINALS;
    else
      return null;
  }

  public void addAqlNodeModel (AQLNodeModel aqlNodeModel, AqlGroupType aqlGroupType)
  {
    if (aqlNodeModel == null || aqlGroupType == null)
      return;

    switch (aqlGroupType) {
      case BASIC:
        if (!basics.contains (aqlNodeModel))
          basics.add (aqlNodeModel);
        break;
      case CONCEPT:
        if (!concepts.contains (aqlNodeModel))
          concepts.add (aqlNodeModel);
        break;
      case REFINEMENT:
        if (!refinements.contains (aqlNodeModel))
          refinements.add (aqlNodeModel);
        break;
      case FINALS:
        if (!finals.contains (aqlNodeModel))
          finals.add (aqlNodeModel);
        break;
    }

    if ( StringUtils.isEmpty (aqlNodeModel.getAqlfilepath ()) )
      aqlNodeModel.setAqlfilepath (getFilepath (aqlGroupType));    // TODO: temporarily set this for now for 1.3. Later we need to set module and file separately
  }

  public void addSubLabel (LabelModel labelModel, AqlGroupType aqlGroupType)
  {
    if (labelModel == null)       // Nothing to add
      return;

    if (aqlGroupType == null) {   // No aql group specified, add to the label directly.
      if (!subTags.contains (labelModel))
        subTags.add (labelModel);
      return;
    }

    switch (aqlGroupType) {       // Add to appropriate sublabel group.
      case BASIC:
        if (!basicsubTags.contains (labelModel))
          basicsubTags.add (labelModel);
        break;
      case CONCEPT:
        if (!conceptsubTags.contains (labelModel))
          conceptsubTags.add (labelModel);
        break;
      case REFINEMENT:
        if (!refinementsubTags.contains (labelModel))
          refinementsubTags.add (labelModel);
        break;
      case FINALS:
        if (!finalsubTags.contains (labelModel))
          finalsubTags.add (labelModel);
        break;
    }
  }

  public void addExample(ExampleModel exampleModel)
  {
    if (exampleModel != null && !examples.contains (exampleModel))
      examples.add (exampleModel);
  }

  public void setName(String name) {
		this.name = name;
	}

	public String getBasicfilepath() {
		return basicfilepath;
	}

	public void setBasicfilepath(String basifilepath) {
		this.basicfilepath = basifilepath;
	}

	public String getConceptfilepath() {
		return conceptfilepath;
	}

	public void setConceptfilepath(String conceptfilepath) {
		this.conceptfilepath = conceptfilepath;
	}

	public String getRefinementfilepath() {
		return refinementfilepath;
	}

	public void setRefinementfilepath(String refinementfilepath) {
		this.refinementfilepath = refinementfilepath;
	}

	public ArrayList<ExampleModel> getExamples() {
		return examples;
	}

	public void setExamples(ArrayList<ExampleModel> examples) {
		this.examples = examples;
	}

	public ArrayList<LabelModel> getSubTags() {
		return subTags;
	}

	public void setSubTags(ArrayList<LabelModel> subTags) {
		this.subTags = subTags;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean isDone() {
		return done;
	}

	public ArrayList<AQLNodeModel> getBasics() {
		return basics;
	}

	public void setBasics(ArrayList<AQLNodeModel> basics) {
		this.basics = basics;
	}

	public ArrayList<AQLNodeModel> getConcepts() {
		return concepts;
	}

	public void setConcepts(ArrayList<AQLNodeModel> concepts) {
		this.concepts = concepts;
	}

	public ArrayList<AQLNodeModel> getRefinements() {
		return refinements;
	}

	public void setRefinements(ArrayList<AQLNodeModel> refinements) {
		this.refinements = refinements;
	}

  public ArrayList<AQLNodeModel> getFinals () {
    return finals;
  }

  public void setFinals (ArrayList<AQLNodeModel> finals) {
    this.finals = finals;
  }

  public void setComment (String comment)
  {
    this.comment = comment;
  }

  public String getComment ()
  {
    return comment;
  }

  public String getFinalsfilepath ()
  {
    return finalsfilepath;
  }

  public void setFinalsfilepath (String finalsfilepath)
  {
    this.finalsfilepath = finalsfilepath;
  }

  public ArrayList<LabelModel> getBasicsubTags ()
  {
    return basicsubTags;
  }

  public void setBasicsubTags (ArrayList<LabelModel> basicsubTags)
  {
    this.basicsubTags = basicsubTags;
  }

  public ArrayList<LabelModel> getConceptsubTags ()
  {
    return conceptsubTags;
  }

  public void setConceptsubTags (ArrayList<LabelModel> conceptsubTags)
  {
    this.conceptsubTags = conceptsubTags;
  }

  public ArrayList<LabelModel> getRefinementsubTags ()
  {
    return refinementsubTags;
  }

  public void setRefinementsubTags (ArrayList<LabelModel> refinementsubTags)
  {
    this.refinementsubTags = refinementsubTags;
  }

  public ArrayList<LabelModel> getFinalsubTags ()
  {
    return finalsubTags;
  }

  public void setFinalsubTags (ArrayList<LabelModel> finalsubTags)
  {
    this.finalsubTags = finalsubTags;
  }

  public boolean removeExample(ExampleModel exampleModel)
  {
    return examples.remove (exampleModel);
  }

  public boolean removeAqlNodeModel (AQLNodeModel aqlNodeModel)
  {
    if (aqlNodeModel == null)
      return false;

    if (!basics.remove (aqlNodeModel))
      if (!concepts.remove (aqlNodeModel))
        if (!refinements.remove (aqlNodeModel))
          if (!finals.remove (aqlNodeModel))
            return false;

    return true;
  }

  public boolean removeSubLabel (LabelModel labelModel)
  {
    if (labelModel == null)       // Nothing to remove
      return false;

    if (!subTags.remove (labelModel))
      if (!basicsubTags.remove (labelModel))
        if (!conceptsubTags.remove (labelModel))
          if (!refinementsubTags.remove (labelModel))
            if (!finalsubTags.remove (labelModel))
              return false;

    return true;
  }

  public boolean removeChild (Object child)
  {
    if (child == null)
      return false;

    if (child instanceof LabelModel)
      return removeSubLabel ((LabelModel)child);
    else if (child instanceof AQLNodeModel)
      return removeAqlNodeModel ((AQLNodeModel)child);
    else if (child instanceof ExampleModel)
      return removeExample ((ExampleModel)child);
    else
      return false;
  }

  public String getFilepath(AqlGroupType aqlGroupType) {
    if (aqlGroupType != null) {
      switch (aqlGroupType) {
        case BASIC:
          return getBasicfilepath ();
        case CONCEPT:
          return getConceptfilepath ();
        case REFINEMENT:
          return getRefinementfilepath ();
        case FINALS:
          return getFinalsfilepath ();
      }
    }

    return "";
  }
}

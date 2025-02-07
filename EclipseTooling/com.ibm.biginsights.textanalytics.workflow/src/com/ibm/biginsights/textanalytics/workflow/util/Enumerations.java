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
package com.ibm.biginsights.textanalytics.workflow.util;

public class Enumerations {



	public static enum GroupType {
		TAG, EXAMPLES, TAG_FOLDER, AQL_FOLDER, BASIC_FEATURES, CONCEPTS, REFINEMENT, FINALS
	};
	
	public static enum AqlTypes {
		// Concept Definition Rules
		DICTIONARY, REGEX, PARTofSPEECH, SELECT, UNION, BLOCK, PATTERN, CONSOLIDATE, PREDICATEbasedFILTER, SETbasedFILTER, EXPORTDICTIONARY, EXPORTTABLE, EXPORTVIEW, EXPORTFUNCTION
	}

	public static enum AqlBasicsTypes {
		// Concept Definition Rules
		DICTIONARY, REGEX, PARTofSPEECH
	}

	public static enum AqlConceptTypes {
		SELECT, UNION, BLOCK, PATTERN
	}

  public static enum AqlFinalsTypes {
    EXPORTVIEW, EXPORTDICTIONARY, EXPORTTABLE, EXPORTFUNCTION
  }

	public static enum AqlRefinementTypes {
		// Concept Refinement Rules
		CONSOLIDATE, PREDICATEbasedFILTER, SETbasedFILTER
	}

	public static enum AqlGroupType {
		BASIC, CONCEPT, REFINEMENT, FINALS
	}

	public static AqlGroupType getGroupType(AqlTypes type) {
		switch (type) {
		case DICTIONARY:
		case REGEX:
		case PARTofSPEECH:
			return AqlGroupType.BASIC;

		case SELECT:
		case UNION:
		case BLOCK:
		case PATTERN:
			return AqlGroupType.CONCEPT;

		case CONSOLIDATE:
		case PREDICATEbasedFILTER:
		case SETbasedFILTER:
			return AqlGroupType.REFINEMENT;

    case EXPORTDICTIONARY:
    case EXPORTTABLE:
    case EXPORTVIEW:
    case EXPORTFUNCTION:
      return AqlGroupType.FINALS;
		}

		return null;
	}

	public static String getString(String str) {
		AqlTypes type = AqlTypes.valueOf(str);
		if (type == null)
			return "";
		return getString(type);
	}

  public static String getString(AqlGroupType type) {
    String str = "Unknown type";

    switch (type) {
    case BASIC:
      return "Basic Features";
    case REFINEMENT:
      return "Candidate Generation";
    case CONCEPT:
      return "Filter and Consolidate";
    case FINALS:
      return "Finals";
    }

    return str;
  }


	public static String getString(AqlTypes type) {
		String str = "Unknown type";

		switch (type) {
		case DICTIONARY:
			return "Dictionary";

		case REGEX:
			return "Regular expression";

		case PARTofSPEECH:
			return "Part of Speech";

		case SELECT:
			return "Select";

		case UNION:
			return "Union all";

		case BLOCK:
			return "Block";

		case PATTERN:
			return "Pattern";

		case CONSOLIDATE:
			return "Consolidate";

		case PREDICATEbasedFILTER:
			return "Predicate-based Filter";

		case SETbasedFILTER:
			return "Set-based Filter";

    case EXPORTDICTIONARY:
      return "Export dictionary";

    case EXPORTVIEW:
      return "Export view";

    case EXPORTTABLE:
      return "Export table";

    case EXPORTFUNCTION:
      return "Export function";
		}

		return str;
	}

	public static AqlTypes getType(String str) {
		for (AqlTypes type : AqlTypes.values()) {
			if (str.equalsIgnoreCase(getString(type)))
				return type;
		}
		return null;
	}
}

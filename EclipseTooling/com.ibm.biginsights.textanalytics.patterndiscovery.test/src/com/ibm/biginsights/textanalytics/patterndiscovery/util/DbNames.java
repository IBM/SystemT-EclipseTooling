package com.ibm.biginsights.textanalytics.patterndiscovery.util;

public class DbNames
{
  // === TABLES IN SEQUENCES DB ===

  public static final String SEQUENCES_FOLDER_NAME = "sequenceDB";

  public static final String SEQUENCES_DB_NAME = "SEQUENCES";
  public static final String SEQUENCES_DB_APP_SCHEMA_NAME = "APP.";

  public static final String DICTIONARY = SEQUENCES_DB_APP_SCHEMA_NAME + "DICTIONARY";
  public static final String SEQUENCE_INSTANCES = SEQUENCES_DB_APP_SCHEMA_NAME + "SEQUENCE_INSTANCES";
  public static final String SEQUENCE_NEW_CO_COUNT = SEQUENCES_DB_APP_SCHEMA_NAME + "SEQUENCE_NEW_CO_COUNT";
  public static final String SEQUENCE_NEW_SEQ_COUNT = SEQUENCES_DB_APP_SCHEMA_NAME + "SEQUENCE_NEW_SEQ_COUNT";
  public static final String SEQUENCE_MAP = SEQUENCES_DB_APP_SCHEMA_NAME + "SEQUENCEMAP";
  public static final String SEQUENCES = SEQUENCES_DB_APP_SCHEMA_NAME + "SEQUENCES";
  public static final String SEQUENCES_SUPPORT = SEQUENCES_DB_APP_SCHEMA_NAME + "SEQUENCES_SUPPORT";
  public static final String TOKEN_MAP = SEQUENCES_DB_APP_SCHEMA_NAME + "TOKEN_MAP";

  // === TABLES IN AOMDB DB ===

  public static final String AOM_FOLDER_NAME = "aomDB";

  public static final String AOMDB_DB_NAME = "AOMDB";
  public static final String AOMDB_DB_AOMDATA_SCHEMA_NAME = "AOMDATA.";
  public static final String AOMDB_DB_APP_SCHEMA_NAME = "APP.";

  // -- AOMDATA --
  public static final String TYPE_ = "TYPE_";

  // -- APP --
  public static final String ALLGROUPINGS = "ALLGROUPINGS";
  public static final String ALLRAWRESULTS = "ALLRAWRESULTS";
  public static final String FINALGROUP = "FINALGROUP";
  public static final String FINALOUTPUT = "FINALOUTPUT";
  public static final String GROUPBYTMP = "GROUPBYTMP";
  public static final String GROUPINGJACCARD_ = "GROUPINGJACCARD_";
  public static final String RULESHISTORY = "RULESHISTORY";
}

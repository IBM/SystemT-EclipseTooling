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
package com.ibm.biginsights.textanalytics.patterndiscovery.single;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;
import com.ibm.biginsights.textanalytics.patterndiscovery.util.DbNames;

public class SequenceMiningTest extends DbBasedTest
{
  public SequenceMiningTest (String expected_path, String actual_path)
  {
    super (expected_path, actual_path, DbNames.SEQUENCES_FOLDER_NAME);
    tables_to_test = new String[] { DbNames.DICTIONARY, DbNames.SEQUENCE_INSTANCES, DbNames.SEQUENCE_MAP,
      DbNames.SEQUENCE_NEW_CO_COUNT, DbNames.SEQUENCE_NEW_SEQ_COUNT, DbNames.SEQUENCES_SUPPORT, DbNames.SEQUENCES,
      DbNames.TOKEN_MAP };
  }

  @Override
  public void initLogger ()
  {
    logger = PDLogger.getLogger (SequenceMiningTest.class.getName ());
  }
}

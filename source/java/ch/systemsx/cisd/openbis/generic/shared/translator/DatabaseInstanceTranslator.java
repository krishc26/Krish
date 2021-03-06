/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.shared.translator;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;

/**
 * A {@link DatabaseInstance} &lt;---&gt; {@link DatabaseInstancePE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class DatabaseInstanceTranslator
{

    private DatabaseInstanceTranslator()
    {
        // Can not be instantiated.
    }

    public final static DatabaseInstance translate(final DatabaseInstancePE databaseInstance)
    {
        if (databaseInstance == null)
        {
            return null;
        }
        final DatabaseInstance result = new DatabaseInstance();
        result.setCode(StringEscapeUtils.escapeHtml(databaseInstance.getCode()));
        result.setUuid(StringEscapeUtils.escapeHtml(databaseInstance.getUuid()));
        result.setIdentifier(StringEscapeUtils.escapeHtml(IdentifierHelper
                .createDatabaseInstanceIdentifier(databaseInstance).toString()));
        return result;
    }

    public final static DatabaseInstancePE translate(final DatabaseInstance databaseInstance)
    {
        if (databaseInstance == null)
        {
            return null;
        }
        final DatabaseInstancePE result = new DatabaseInstancePE();
        result.setCode(databaseInstance.getCode());
        result.setUuid(databaseInstance.getUuid());
        return result;
    }

}

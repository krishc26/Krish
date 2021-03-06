/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;

/**
 * <i>Data Access Object</i> for {@link GroupPE}.
 * 
 * @author Christian Ribeaud
 */
public interface IGroupDAO extends IGenericDAO<GroupPE>
{

    /**
     * Returns a list of {@link GroupPE}s (independent of {@link DatabaseInstancePE} each group
     * belongs to).
     */
    public List<GroupPE> listGroups() throws DataAccessException;

    /** Lists all groups which belong to the specified database instance. */
    public List<GroupPE> listGroups(final DatabaseInstancePE databaseInstance)
            throws DataAccessException;

    /** Creates a new group in the database. */
    public void createGroup(final GroupPE groupDTO) throws DataAccessException;

    /**
     * Returns <code>GroupPE</code> identified by given <var>groupCode</var> and given
     * <var>databaseInstanceId</var> or <code>null</code> if such a group does not exist.
     */
    public GroupPE tryFindGroupByCodeAndDatabaseInstance(final String groupCode,
            final DatabaseInstancePE databaseInstance) throws DataAccessException;
}

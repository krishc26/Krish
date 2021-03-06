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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Manipulations on {@link EntityTypePE} subclasses.
 * 
 * @author Tomasz Pylak
 */
public final class EntityTypeBO extends AbstractBusinessObject implements IEntityTypeBO
{
    private EntityTypePE entityTypePE;

    private EntityKind entityKind;

    public EntityTypeBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    private static EntityTypePE convertGeneric(EntityType entityType, EntityKind entityKind,
            DatabaseInstancePE databaseInstance) throws UserFailureException
    {
        EntityTypePE entityTypePE = EntityTypePE.createEntityTypePE(entityKind);
        entityTypePE.setCode(entityType.getCode());
        entityTypePE.setDescription(entityType.getDescription());
        entityTypePE.setDatabaseInstance(databaseInstance);
        return entityTypePE;
    }

    public final void save() throws UserFailureException
    {
        assert entityTypePE != null : "Entity type not defined.";
        assert entityKind != null : "Entity kind not defined.";
        try
        {
            getEntityTypeDAO(entityKind).createOrUpdateEntityType(entityTypePE);
        } catch (final DataAccessException e)
        {
            throwException(e, String.format("Entity type '%s' ", entityTypePE.getCode()));
        }
    }

    public void define(SampleType entityType)
    {
        SampleTypePE sampleTypePE = new SampleTypePE();
        sampleTypePE.setCode(entityType.getCode());
        sampleTypePE.setDescription(entityType.getDescription());
        sampleTypePE.setContainerHierarchyDepth(entityType.getContainerHierarchyDepth());
        sampleTypePE.setGeneratedFromHierarchyDepth(entityType.getGeneratedFromHierarchyDepth());
        sampleTypePE.setListable(entityType.isListable());
        sampleTypePE.setDatabaseInstance(getHomeDatabaseInstance());

        this.entityKind = EntityKind.SAMPLE;
        this.entityTypePE = sampleTypePE;
    }

    public void define(MaterialType entityType)
    {
        this.entityKind = EntityKind.MATERIAL;
        this.entityTypePE = convertGeneric(entityType, entityKind, getHomeDatabaseInstance());
    }

    public void define(ExperimentType entityType)
    {
        this.entityKind = EntityKind.EXPERIMENT;
        this.entityTypePE = convertGeneric(entityType, entityKind, getHomeDatabaseInstance());
    }

    public void define(DataSetType entityType)
    {
        this.entityKind = EntityKind.DATA_SET;
        this.entityTypePE = convertGeneric(entityType, entityKind, getHomeDatabaseInstance());
    }

    public void load(EntityKind kind, String code)
    {
        this.entityKind = kind;
        this.entityTypePE = getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(code);
        if (entityTypePE == null)
        {
            throw new UserFailureException(String.format("'%s' not found.", code));
        }
    }

    public void delete()
    {
        assert entityKind != null;
        assert entityTypePE != null : "Type not loaded";
        try
        {
            getEntityTypeDAO(entityKind).deleteEntityType(entityTypePE);
        } catch (final DataAccessException e)
        {
            throwException(e, String.format("'%s'", entityTypePE.getCode()));
        }
    }
}

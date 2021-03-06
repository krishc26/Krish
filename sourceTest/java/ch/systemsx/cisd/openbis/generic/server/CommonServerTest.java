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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IDataStoreBaseURLProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleSetCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.GroupTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;

/**
 * Test cases for corresponding {@link CommonServer} class.
 * 
 * @author    Franz-Josef Elmer
 */
public final class CommonServerTest extends AbstractServerTestCase
{

    private final static String DATA_STORE_BASE_URL = "dataStoreBaseURL";

    private final static String BASE_INDEX_URL = "baseIndexURL";

    private static final class MockExperimentTypePropertyType extends ExperimentTypePropertyTypePE
    {
        private static final long serialVersionUID = 1L;

        MockExperimentTypePropertyType()
        {
            propertyValues = new LinkedHashSet<EntityPropertyPE>();
            propertyValues.add(new ExperimentPropertyPE());
        }
    }

    private ICommonBusinessObjectFactory commonBusinessObjectFactory;

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    private final ICommonServer createServer()
    {
        CommonServer server =
                new CommonServer(authenticationService, sessionManager, daoFactory,
                        commonBusinessObjectFactory, new LastModificationState());
        server.setSampleTypeSlaveServerPlugin(sampleTypeSlaveServerPlugin);
        server.setDataSetTypeSlaveServerPlugin(dataSetTypeSlaveServerPlugin);
        server.dataStoreBaseURLProvider = new IDataStoreBaseURLProvider()
            {
                public String getDataStoreBaseURL()
                {
                    return DATA_STORE_BASE_URL;
                }
            };
        server.setBaseIndexURL(SESSION_TOKEN, BASE_INDEX_URL);
        return server;
    }

    private final static PersonPE createSystemUser()
    {
        final PersonPE systemPerson = new PersonPE();
        systemPerson.setUserId(PersonPE.SYSTEM_USER_ID);
        return systemPerson;
    }

    //
    // AbstractServerTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        commonBusinessObjectFactory = context.mock(ICommonBusinessObjectFactory.class);
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
    }

    @Test
    public void testLogout()
    {
        final Session session = createSession();
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));
                    one(sessionManager).closeSession(SESSION_TOKEN);
                }
            });
        createServer().logout(SESSION_TOKEN);

        context.assertIsSatisfied();
    }

    @Test
    public void testTryToAuthenticateWhichFailed()
    {
        final String user = "user";
        final String password = "password";
        final Session session = createSession();
        context.checking(new Expectations()
            {
                {
                    // Artefact of our test code
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));

                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(null));
                }
            });

        assertEquals(null, createServer().tryToAuthenticate(user, password));

        context.assertIsSatisfied();
    }

    @Test
    public void testFirstTryToAuthenticate()
    {
        final String user = "user";
        final String password = "password";
        final Session session = createSession();
        final PersonPE systemPerson = createSystemUser();
        final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
        final RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setDatabaseInstance(homeDatabaseInstance);
        roleAssignment.setRegistrator(systemPerson);
        roleAssignment.setRole(RoleCode.ADMIN);
        person.addRoleAssignment(roleAssignment);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));

                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(systemPerson))); // only 'system' in database

                    one(personDAO).tryFindPersonByUserId(user); // first login
                    will(returnValue(null));

                    one(personDAO).createPerson(person);
                    one(personDAO).updatePerson(person);
                }
            });

        final Session s = createServer().tryToAuthenticate(user, password);

        assertEquals(person, s.tryGetPerson());
        assertEquals(roleAssignment, s.tryGetPerson().getRoleAssignments().iterator().next());

        context.assertIsSatisfied();
    }

    @Test
    public void testFirstTryToAuthenticateButNotFirstUser()
    {
        final String user = "user";
        final String password = "password";
        final Session session = createSession();
        final PersonPE systemPerson = createSystemUser();
        final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));

                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(systemPerson, person)));

                    one(personDAO).tryFindPersonByUserId(user); // first login
                    will(returnValue(null));

                    one(personDAO).createPerson(person);
                }
            });

        final Session s = createServer().tryToAuthenticate(user, password);

        assertEquals(person, s.tryGetPerson());

        context.assertIsSatisfied();
    }

    @Test
    public void testTryToAuthenticate()
    {
        final String user = "user";
        final String password = "password";
        final Session session = createSession();
        final PersonPE systemPerson = createSystemUser();
        final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));

                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(systemPerson, person)));

                    one(personDAO).tryFindPersonByUserId(user);
                    will(returnValue(person));
                }
            });
        assertEquals(null, session.tryGetPerson());

        final Session s = createServer().tryToAuthenticate(user, password);

        assertSame(session, s);
        assertEquals(person, s.tryGetPerson());

        context.assertIsSatisfied();
    }

    @Test
    public void testListGroups()
    {
        final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
        final DatabaseInstanceIdentifier identifier = DatabaseInstanceIdentifier.createHome();
        final GroupPE g1 = CommonTestUtils.createGroup("g1", homeDatabaseInstance);
        final GroupPE g2 = CommonTestUtils.createGroup("g2", homeDatabaseInstance);
        final Session session = createSession();
        session.setPerson(person);
        person.setHomeGroup(g1);
        g1.setId(42L);
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));
                    one(groupDAO).listGroups(homeDatabaseInstance);
                    will(returnValue(Arrays.asList(g1, g2)));
                }
            });

        final List<Group> groups = createServer().listGroups(SESSION_TOKEN, identifier);

        assertEquals(GroupTranslator.translate(g1), groups.get(0));
        assertEquals(GroupTranslator.translate(g2), groups.get(1));
        assertEquals(2, groups.size());
        assertEquals(true, g1.isHome().booleanValue());
        assertEquals(false, g2.isHome().booleanValue());

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterGroup()
    {
        prepareGetSession();
        final String groupCode = "group";
        final String description = "description";
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createGroupBO(SESSION);
                    will(returnValue(groupBO));

                    one(groupBO).define(groupCode, description);
                    one(groupBO).save();
                }
            });

        createServer().registerGroup(SESSION_TOKEN, groupCode, description);

        context.assertIsSatisfied();
    }

    @Test
    public void testListPersons()
    {
        final PersonPE personPE = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
        final Person person = PersonTranslator.translate(personPE);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(personPE)));
                }
            });

        final List<Person> persons = createServer().listPersons(SESSION_TOKEN);

        assertEquals(person.getUserId(), persons.get(0).getUserId());
        assertEquals(person.getFirstName(), persons.get(0).getFirstName());
        assertEquals(person.getLastName(), persons.get(0).getLastName());
        assertEquals(person.getEmail(), persons.get(0).getEmail());
        assertEquals(1, persons.size());

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterPerson()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).listByCodes(Arrays.asList(CommonTestUtils.USER_ID));
                    will(returnValue(new ArrayList<PersonPE>()));

                    final String applicationToken = "application-token";
                    one(authenticationService).authenticateApplication();
                    will(returnValue(applicationToken));

                    one(authenticationService).getPrincipal(applicationToken,
                            CommonTestUtils.USER_ID);
                    will(returnValue(PRINCIPAL));

                    final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
                    one(personDAO).createPerson(person);
                }
            });

        createServer().registerPerson(SESSION_TOKEN, CommonTestUtils.USER_ID);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterExistingPerson()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {

                    one(personDAO).listByCodes(Arrays.asList(CommonTestUtils.USER_ID));
                    will(returnValue(Arrays.asList(CommonTestUtils
                            .createPersonFromPrincipal(PRINCIPAL))));
                }
            });

        try
        {
            createServer().registerPerson(SESSION_TOKEN, CommonTestUtils.USER_ID);
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Following persons already exist: [" + CommonTestUtils.USER_ID + "]", e
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterUnknownPerson()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).listByCodes(Arrays.asList(CommonTestUtils.USER_ID));
                    will(returnValue(new ArrayList<PersonPE>()));

                    final String applicationToken = "application-token";
                    one(authenticationService).authenticateApplication();
                    will(returnValue(applicationToken));

                    one(authenticationService).getPrincipal(applicationToken,
                            CommonTestUtils.USER_ID);
                    will(throwException(new IllegalArgumentException()));
                }
            });

        try
        {
            createServer().registerPerson(SESSION_TOKEN, CommonTestUtils.USER_ID);
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Following persons unknown by the authentication service: ["
                    + CommonTestUtils.USER_ID + "]", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testListRoles()
    {
        prepareGetSession();
        final RoleAssignmentPE rolePE = new RoleAssignmentPE();
        rolePE.setRole(RoleCode.ETL_SERVER);
        rolePE.setDatabaseInstance(new DatabaseInstancePE());
        context.checking(new Expectations()
            {
                {
                    one(roleAssignmentDAO).listRoleAssignments();
                    will(returnValue(Arrays.asList(rolePE)));
                }
            });

        final List<RoleAssignment> roles = createServer().listRoleAssignments(SESSION_TOKEN);

        assertEquals(RoleSetCode.INSTANCE_ETL_SERVER, roles.get(0).getRoleSetCode());
        assertEquals(1, roles.size());

        context.assertIsSatisfied();
    }

    @Test
    public final void testListSampleExternalData()
    {
        final TechId sampleId = CommonTestUtils.TECH_ID;
        final ExternalDataPE externalDataPE = new ExternalDataPE();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        fileFormatType.setCode("FFT");
        externalDataPE.setFileFormatType(fileFormatType);
        final LocatorTypePE locatorType = new LocatorTypePE();
        locatorType.setCode("LT");
        externalDataPE.setLocatorType(locatorType);
        final DataStorePE dataStorePE = new DataStorePE();
        dataStorePE.setCode("DST");
        externalDataPE.setDataStore(dataStorePE);
        final ExternalData externalData =
                ExternalDataTranslator.translate(externalDataPE, DATA_STORE_BASE_URL,
                        BASE_INDEX_URL);
        prepareGetSession();
        final boolean showOnlyDirectlyConnected = true;
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createDatasetLister(SESSION,
                            DATA_STORE_BASE_URL);
                    will(returnValue(datasetLister));

                    one(datasetLister).listBySampleTechId(sampleId, showOnlyDirectlyConnected);
                    will(returnValue(Arrays.asList(externalData)));
                }
            });

        final List<ExternalData> list =
                createServer().listSampleExternalData(SESSION_TOKEN, sampleId,
                        showOnlyDirectlyConnected);

        assertEquals(1, list.size());
        assertTrue(equals(externalData, list.get(0)));

        context.assertIsSatisfied();
    }

    @Test
    public void testListExperimentExternalData()
    {
        final TechId experimentId = CommonTestUtils.TECH_ID;
        final ExternalDataPE externalDataPE = new ExternalDataPE();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        fileFormatType.setCode("FFT");
        externalDataPE.setFileFormatType(fileFormatType);
        final LocatorTypePE locatorType = new LocatorTypePE();
        locatorType.setCode("LT");
        externalDataPE.setLocatorType(locatorType);
        final DataStorePE dataStorePE = new DataStorePE();
        dataStorePE.setCode("DST");
        externalDataPE.setDataStore(dataStorePE);
        final ExternalData externalData =
                ExternalDataTranslator.translate(externalDataPE, DATA_STORE_BASE_URL,
                        BASE_INDEX_URL);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createDatasetLister(SESSION,
                            DATA_STORE_BASE_URL);
                    will(returnValue(datasetLister));

                    one(datasetLister).listByExperimentTechId(experimentId);
                    will(returnValue(Arrays.asList(externalData)));
                }
            });

        final List<ExternalData> list =
                createServer().listExperimentExternalData(SESSION_TOKEN, experimentId);

        assertEquals(1, list.size());
        assertTrue(equals(externalData, list.get(0)));

        context.assertIsSatisfied();
    }

    private boolean equals(ExternalData data1, ExternalData data2)
    {
        return EqualsBuilder.reflectionEquals(data1, data2);
    }

    @Test
    public void testListExperiments()
    {
        final ProjectIdentifier projectIdentifier = CommonTestUtils.createProjectIdentifier();
        final ExperimentType experimentType =
                ExperimentTranslator.translate(CommonTestUtils.createExperimentType());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExperimentTable(SESSION);
                    will(returnValue(experimentTable));

                    one(experimentTable).load(experimentType.getCode(), projectIdentifier);

                    one(experimentTable).getExperiments();
                    will(returnValue(new ArrayList<ExperimentPE>()));
                }
            });
        createServer().listExperiments(SESSION_TOKEN, experimentType, projectIdentifier);
        context.assertIsSatisfied();
    }

    @Test
    public void testListExperimentTypes()
    {
        prepareGetSession();
        final List<EntityTypePE> types = new ArrayList<EntityTypePE>();
        final ExperimentTypePE experimentTypePE = CommonTestUtils.createExperimentType();
        final ExperimentType experimentType = ExperimentTranslator.translate(experimentTypePE);
        types.add(experimentTypePE);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).listEntityTypes();
                    will(returnValue(types));
                }
            });
        final List<ExperimentType> experimentTypes =
                createServer().listExperimentTypes(SESSION_TOKEN);
        assertEquals(1, experimentTypes.size());
        assertEquals(experimentType, experimentTypes.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testListProjects()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));

                    one(projectDAO).listProjects();
                    will(returnValue(new ArrayList<ProjectPE>()));
                }
            });
        createServer().listProjects(SESSION_TOKEN);
        context.assertIsSatisfied();
    }

    @Test
    public void testListPropertyTypes()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createPropertyTypeTable(SESSION);
                    will(returnValue(propertyTypeTable));

                    one(propertyTypeTable).loadWithRelations();

                    one(propertyTypeTable).getPropertyTypes();
                    will(returnValue(new ArrayList<PropertyTypePE>()));
                }
            });
        createServer().listPropertyTypes(SESSION_TOKEN, true);
        context.assertIsSatisfied();
    }

    @Test
    public final void testListDataTypes()
    {
        prepareGetSession();
        final DataTypePE dataTypePE = new DataTypePE();
        dataTypePE.setCode(EntityDataType.HYPERLINK);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getPropertyTypeDAO();
                    will(returnValue(propertyTypeDAO));

                    one(propertyTypeDAO).listDataTypes();
                    will(returnValue(Collections.singletonList(dataTypePE)));
                }
            });
        final List<DataType> dataTypes = createServer().listDataTypes(SESSION_TOKEN);
        assertEquals(1, dataTypes.size());
        assertEquals(DataTypeCode.HYPERLINK, dataTypes.get(0).getCode());
        context.assertIsSatisfied();
    }

    @Test
    public final void testFileFormatTypes()
    {
        prepareGetSession();
        final FileFormatTypePE fileFormatTypePE = new FileFormatTypePE();
        fileFormatTypePE.setCode("FFT");
        final List<FileFormatTypePE> list = Collections.singletonList(fileFormatTypePE);
        context.checking(new Expectations()
            {
                {

                    one(fileFormatDAO).listFileFormatTypes();
                    will(returnValue(list));
                }
            });

        List<FileFormatType> types = createServer().listFileFormatTypes(SESSION_TOKEN);

        assertEquals(1, types.size());
        assertEquals(fileFormatTypePE.getCode(), types.get(0).getCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileFormatsWithNoCodesSpecified() throws Exception
    {
        List<String> codes = new ArrayList<String>();
        prepareGetSession();
        createServer().deleteFileFormatTypes(SESSION_TOKEN, codes);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileFormatsWithCodesSpecified() throws Exception
    {
        final List<String> codes = Arrays.asList("code1", "code2");
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    for (String code : codes)
                    {
                        one(fileFormatDAO).tryToFindFileFormatTypeByCode(code);
                        FileFormatTypePE type = createFileFormatType(code);
                        will(returnValue(type));
                        one(fileFormatDAO).delete(type);
                    }
                }
            });
        createServer().deleteFileFormatTypes(SESSION_TOKEN, codes);
        context.assertIsSatisfied();
    }

    private static final FileFormatTypePE createFileFormatType(String code)
    {
        FileFormatTypePE result = new FileFormatTypePE();
        result.setCode(code);
        result.setDatabaseInstance(new DatabaseInstancePE());
        return result;
    }

    @Test
    public void testDeleteUnknownFileFormat() throws Exception
    {
        final String code = "unknown-type";
        final List<String> codes = Arrays.asList(code);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(fileFormatDAO).tryToFindFileFormatTypeByCode(code);
                    will(returnValue(null));
                }
            });
        boolean exceptionThrown = false;
        try
        {
            createServer().deleteFileFormatTypes(SESSION_TOKEN, codes);
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileFormatWithDataSets() throws Exception
    {
        final String code = "used-type";
        final List<String> codes = Arrays.asList(code);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(fileFormatDAO).tryToFindFileFormatTypeByCode(code);
                    FileFormatTypePE type = createFileFormatType(code);
                    will(returnValue(type));

                    one(fileFormatDAO).delete(type);
                    will(throwException(new DataIntegrityViolationException("")));
                }
            });
        boolean exceptionThrown = false;
        try
        {
            createServer().deleteFileFormatTypes(SESSION_TOKEN, codes);
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterFileFormatType()
    {
        prepareGetSession();
        final FileFormatTypePE fileFormatTypePE = new FileFormatTypePE();
        fileFormatTypePE.setCode("my-type");
        fileFormatTypePE.setDescription("my description");
        context.checking(new Expectations()
            {
                {
                    one(fileFormatDAO).createOrUpdate(fileFormatTypePE);
                }
            });
        FileFormatType fileFormatType = new FileFormatType();
        fileFormatType.setCode(fileFormatTypePE.getCode());
        fileFormatType.setDescription(fileFormatTypePE.getDescription());

        createServer().registerFileFormatType(SESSION_TOKEN, fileFormatType);

        context.assertIsSatisfied();
    }

    @Test
    public final void testListVocabularies()
    {
        prepareGetSession();
        final boolean excludeInternal = true;
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getVocabularyDAO();
                    will(returnValue(vocabularyDAO));

                    one(vocabularyDAO).listVocabularies(excludeInternal);
                    will(returnValue(Collections.emptyList()));
                }
            });
        final List<Vocabulary> vocabularies =
                createServer().listVocabularies(SESSION_TOKEN, true, excludeInternal);
        assertEquals(0, vocabularies.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterPropertyType()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createPropertyTypeBO(SESSION);
                    will(returnValue(propertyTypeBO));

                    one(propertyTypeBO).define(with(aNonNull(PropertyType.class)));
                    one(propertyTypeBO).save();
                }
            });
        createServer().registerPropertyType(SESSION_TOKEN, new PropertyType());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterVocabulary()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createVocabularyBO(SESSION);
                    will(returnValue(vocabularyBO));

                    one(vocabularyBO).define(with(aNonNull(NewVocabulary.class)));
                    one(vocabularyBO).save();
                }
            });
        createServer().registerVocabulary(SESSION_TOKEN, new NewVocabulary());
        context.assertIsSatisfied();
    }

    @Test
    public void testAddVocabularyTerms()
    {
        final List<String> terms = Arrays.asList("a", "b");
        final TechId vocabularyId = CommonTestUtils.TECH_ID;
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createVocabularyBO(SESSION);
                    will(returnValue(vocabularyBO));

                    one(vocabularyBO).loadDataByTechId(vocabularyId);
                    one(vocabularyBO).addNewTerms(terms);
                    one(vocabularyBO).save();
                }
            });

        createServer().addVocabularyTerms(SESSION_TOKEN, vocabularyId, terms);

        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteVocabularyTerms()
    {
        final TechId vocabularyId = CommonTestUtils.TECH_ID;
        final List<VocabularyTerm> termToBeDeleted = Arrays.asList(new VocabularyTerm());
        final List<VocabularyTermReplacement> termsToBeReplaced =
                Arrays.asList(new VocabularyTermReplacement());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createVocabularyBO(SESSION);
                    will(returnValue(vocabularyBO));

                    one(vocabularyBO).loadDataByTechId(vocabularyId);
                    one(vocabularyBO).delete(termToBeDeleted, termsToBeReplaced);
                    one(vocabularyBO).save();
                }
            });

        createServer().deleteVocabularyTerms(SESSION_TOKEN, vocabularyId, termToBeDeleted,
                termsToBeReplaced);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterMaterialType()
    {
        final MaterialType type = new MaterialType();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypeBO(SESSION);
                    will(returnValue(entityTypeBO));

                    one(entityTypeBO).define(type);
                    one(entityTypeBO).save();
                }
            });

        createServer().registerMaterialType(SESSION_TOKEN, type);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateMaterialType()
    {
        final MaterialType type = new MaterialType();
        type.setCode("my-type");
        type.setDescription("my description");
        final MaterialTypePE typePE = new MaterialTypePE();
        typePE.setCode(type.getCode());
        typePE.setDatabaseInstance(new DatabaseInstancePE());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(type.getCode());
                    will(returnValue(typePE));

                    one(entityTypeDAO).createOrUpdateEntityType(typePE);
                }
            });

        createServer().updateMaterialType(SESSION_TOKEN, type);

        assertEquals(type.getDescription(), typePE.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterSampleType()
    {
        final SampleType type = new SampleType();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypeBO(SESSION);
                    will(returnValue(entityTypeBO));

                    one(entityTypeBO).define(type);
                    one(entityTypeBO).save();
                }
            });

        createServer().registerSampleType(SESSION_TOKEN, type);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateSampleType()
    {
        final SampleType type = new SampleType();
        type.setCode("my-type");
        type.setDescription("my description");
        final SampleTypePE typePE = new SampleTypePE();
        typePE.setCode(type.getCode());
        typePE.setDatabaseInstance(new DatabaseInstancePE());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.SAMPLE);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(type.getCode());
                    will(returnValue(typePE));

                    one(entityTypeDAO).createOrUpdateEntityType(typePE);
                }
            });

        createServer().updateSampleType(SESSION_TOKEN, type);

        assertEquals(type.getDescription(), typePE.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterExperimentType()
    {
        final ExperimentType type = new ExperimentType();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypeBO(SESSION);
                    will(returnValue(entityTypeBO));

                    one(entityTypeBO).define(type);
                    one(entityTypeBO).save();
                }
            });

        createServer().registerExperimentType(SESSION_TOKEN, type);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateExperimentType()
    {
        final ExperimentType type = new ExperimentType();
        type.setCode("my-type");
        type.setDescription("my description");
        final ExperimentTypePE typePE = new ExperimentTypePE();
        typePE.setCode(type.getCode());
        typePE.setDatabaseInstance(new DatabaseInstancePE());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(type.getCode());
                    will(returnValue(typePE));

                    one(entityTypeDAO).createOrUpdateEntityType(typePE);
                }
            });

        createServer().updateExperimentType(SESSION_TOKEN, type);

        assertEquals(type.getDescription(), typePE.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterDataSetType()
    {
        final DataSetType type = new DataSetType();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypeBO(SESSION);
                    will(returnValue(entityTypeBO));

                    one(entityTypeBO).define(type);
                    one(entityTypeBO).save();
                }
            });

        createServer().registerDataSetType(SESSION_TOKEN, type);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateDataSetType()
    {
        final DataSetType type = new DataSetType();
        type.setCode("my-type");
        type.setDescription("my description");
        final DataSetTypePE typePE = new DataSetTypePE();
        typePE.setCode(type.getCode());
        typePE.setDatabaseInstance(new DatabaseInstancePE());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.DATA_SET);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(type.getCode());
                    will(returnValue(typePE));

                    one(entityTypeDAO).createOrUpdateEntityType(typePE);
                }
            });

        createServer().updateDataSetType(SESSION_TOKEN, type);

        assertEquals(type.getDescription(), typePE.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public final void testAssignPropertyType()
    {
        prepareGetSession();
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final String propertyTypeCode = "USER.DISTANCE";
        final String entityTypeCode = "ARCHERY";
        final boolean mandatory = true;
        final String value = "50m";
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypePropertyTypeBO(SESSION,
                            DtoConverters.convertEntityKind(entityKind));
                    will(returnValue(entityTypePropertyTypeBO));

                    one(entityTypePropertyTypeBO).createAssignment(propertyTypeCode,
                            entityTypeCode, mandatory, value);
                }
            });
        assertEquals(
                "Mandatory property type 'USER.DISTANCE' successfully assigned to experiment type 'ARCHERY'",
                createServer().assignPropertyType(SESSION_TOKEN, entityKind, propertyTypeCode,
                        entityTypeCode, mandatory, value));
        context.assertIsSatisfied();
    }

    @Test
    public void testUassignPropertyType()
    {
        prepareGetSession();
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final String propertyTypeCode = "USER.DISTANCE";
        final String entityTypeCode = "ARCHERY";
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypePropertyTypeBO(SESSION,
                            DtoConverters.convertEntityKind(entityKind));
                    will(returnValue(entityTypePropertyTypeBO));

                    one(entityTypePropertyTypeBO).loadAssignment(propertyTypeCode, entityTypeCode);
                    one(entityTypePropertyTypeBO).deleteLoadedAssignment();
                }
            });

        createServer().unassignPropertyType(SESSION_TOKEN, entityKind, propertyTypeCode,
                entityTypeCode);

        context.assertIsSatisfied();
    }

    @Test
    public void testCountPropertyTypedEntities()
    {
        prepareGetSession();
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final String propertyTypeCode = "USER.DISTANCE";
        final String entityTypeCode = "ARCHERY";
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypePropertyTypeBO(SESSION,
                            DtoConverters.convertEntityKind(entityKind));
                    will(returnValue(entityTypePropertyTypeBO));

                    one(entityTypePropertyTypeBO).loadAssignment(propertyTypeCode, entityTypeCode);
                    one(entityTypePropertyTypeBO).getLoadedAssignment();
                    ExperimentTypePropertyTypePE experimentTypePropertyTypePE =
                            new MockExperimentTypePropertyType();
                    will(returnValue(experimentTypePropertyTypePE));
                }
            });

        int count =
                createServer().countPropertyTypedEntities(SESSION_TOKEN, entityKind,
                        propertyTypeCode, entityTypeCode);

        assertEquals(1, count);
        context.assertIsSatisfied();
    }

    @Test
    public void testListMaterials()
    {
        prepareGetSession();
        final MaterialType materialType =
                MaterialTypeTranslator.translate(CommonTestUtils.createMaterialType(), null);
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createMaterialTable(SESSION);
                    will(returnValue(materialTable));

                    one(materialTable).load(materialType.getCode());

                    one(materialTable).getMaterials();
                    will(returnValue(new ArrayList<MaterialTypePE>()));
                }
            });
        createServer().listMaterials(SESSION_TOKEN, materialType);
        context.assertIsSatisfied();
    }

    @Test
    public void testListMaterialTypes()
    {
        prepareGetSession();
        final List<EntityTypePE> types = new ArrayList<EntityTypePE>();
        types.add(CommonTestUtils.createMaterialType());
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).listEntityTypes();
                    will(returnValue(types));
                }
            });
        final List<MaterialType> materialTypes = createServer().listMaterialTypes(SESSION_TOKEN);
        assertEquals(1, materialTypes.size());
        assertEquals(types.get(0).getCode(), materialTypes.get(0).getCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteDataSets()
    {
        prepareGetSession();
        final List<String> dataSetCodes = Arrays.asList("ds1", "ds2", "ds3");
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExternalDataTable(SESSION);
                    will(returnValue(externalDataTable));

                    one(externalDataTable).loadByDataSetCodes(dataSetCodes);
                    one(externalDataTable).getExternalData();
                    ExternalDataPE ds1 = createDataSet("ds1", "type1");
                    ExternalDataPE ds2 = createDataSet("ds2", "type1");
                    ExternalDataPE ds3 = createDataSet("ds3", "type2");
                    will(returnValue(Arrays.asList(ds1, ds2, ds3)));

                    one(dataSetTypeSlaveServerPlugin).deleteDataSets(SESSION,
                            Arrays.asList(ds1, ds2), "reason");
                    one(dataSetTypeSlaveServerPlugin).deleteDataSets(SESSION, Arrays.asList(ds3),
                            "reason");
                }
            });

        createServer().deleteDataSets(SESSION_TOKEN, dataSetCodes, "reason");

        context.assertIsSatisfied();
    }

    private ExternalDataPE createDataSet(String code, String type)
    {
        ExternalDataPE externalData = new ExternalDataPE();
        externalData.setCode(code);
        DataSetTypePE dataSetType = new DataSetTypePE();
        dataSetType.setCode(type);
        dataSetType.setDatabaseInstance(homeDatabaseInstance);
        externalData.setDataSetType(dataSetType);
        return externalData;
    }

    @Test
    public void testUploadDataSets()
    {
        prepareGetSession();
        final List<String> dataSetCodes = Arrays.asList("a", "b");
        final DataSetUploadContext uploadContext = new DataSetUploadContext();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExternalDataTable(SESSION);
                    will(returnValue(externalDataTable));

                    one(externalDataTable).loadByDataSetCodes(dataSetCodes);
                    one(externalDataTable).uploadLoadedDataSetsToCIFEX(uploadContext);
                }
            });

        createServer().uploadDataSets(SESSION_TOKEN, dataSetCodes, uploadContext);

        context.assertIsSatisfied();
    }

    @Test
    public void testSaveDisplaySettings()
    {
        final DisplaySettings displaySettings = new DisplaySettings();
        final PersonPE person = new PersonPE();
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    Session session = createSession();
                    session.setPerson(person);
                    will(returnValue(session));

                    one(personDAO).updatePerson(person);
                }
            });

        createServer().saveDisplaySettings(SESSION_TOKEN, displaySettings);

        assertSame(displaySettings, person.getDisplaySettings());

        context.assertIsSatisfied();
    }

    @Test
    public void testChangeUserHomeGroup()
    {
        final TechId groupId = CommonTestUtils.TECH_ID;
        final GroupPE group = new GroupPE();
        group.setId(groupId.getId());
        final PersonPE person = new PersonPE();
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    Session session = createSession();
                    session.setPerson(person);
                    will(returnValue(session));

                    one(groupDAO).getByTechId(groupId);
                    will(returnValue(group));
                }
            });

        createServer().changeUserHomeGroup(SESSION_TOKEN, groupId);

        assertSame(group, person.getHomeGroup());
        assertSame(groupId.getId(), person.getHomeGroup().getId());

        context.assertIsSatisfied();
    }

    @Test
    public void testChangeUserHomeGroupToNull()
    {
        final TechId groupId = CommonTestUtils.TECH_ID;
        final GroupPE group = new GroupPE();
        group.setId(groupId.getId());
        final PersonPE person = new PersonPE();
        person.setHomeGroup(group);
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    Session session = createSession();
                    session.setPerson(person);
                    will(returnValue(session));
                }
            });

        createServer().changeUserHomeGroup(SESSION_TOKEN, null);

        assertNull(person.getHomeGroup());

        context.assertIsSatisfied();
    }
}

/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SimpleDataSetHelper;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.DatabaseInstanceTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author    Franz-Josef Elmer
 */
public class ETLService extends AbstractServer<IETLService> implements IETLService
{
    private final ISessionManager<Session> sessionManager;

    private final IDAOFactory daoFactory;

    private final ICommonBusinessObjectFactory boFactory;

    private final IDataStoreServiceFactory dssFactory;

    public ETLService(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory boFactory, IDataStoreServiceFactory dssFactory)
    {
        super(sessionManager, daoFactory);
        this.sessionManager = sessionManager;
        this.daoFactory = daoFactory;
        this.boFactory = boFactory;
        this.dssFactory = dssFactory;
    }

    public IETLService createLogger(final boolean invocationSuccessful, final long elapsedTime)
    {
        return new ETLServiceLogger(getSessionManager(), invocationSuccessful, elapsedTime);
    }

    @Override
    public int getVersion()
    {
        return IServer.VERSION;
    }

    public DatabaseInstance getHomeDatabaseInstance(String sessionToken)
    {
        return DatabaseInstanceTranslator.translate(getHomeDatabaseInstance());
    }

    private DatabaseInstancePE getHomeDatabaseInstance()
    {
        return daoFactory.getHomeDatabaseInstance();
    }

    public void registerDataStoreServer(String sessionToken, DataStoreServerInfo info)
    {
        Session session = sessionManager.getSession(sessionToken);

        String dssSessionToken = info.getSessionToken();
        String remoteHost = session.getRemoteHost();
        int port = info.getPort();
        final String dssURL = "https://" + remoteHost + ":" + port;
        checkVersion(dssSessionToken, dssURL);
        IDataStoreDAO dataStoreDAO = daoFactory.getDataStoreDAO();
        DataStorePE dataStore = dataStoreDAO.tryToFindDataStoreByCode(info.getDataStoreCode());
        if (dataStore == null)
        {
            dataStore = new DataStorePE();
            dataStore.setDatabaseInstance(getHomeDatabaseInstance());
        }
        dataStore.setCode(info.getDataStoreCode());
        dataStore.setDownloadUrl(info.getDownloadUrl());
        dataStore.setRemoteUrl(dssURL);
        dataStore.setSessionToken(dssSessionToken);
        setServices(dataStore, info.getServicesDescriptions(), dataStoreDAO);
        dataStoreDAO.createOrUpdateDataStore(dataStore);
    }

    private void checkVersion(String dssSessionToken, final String dssURL)
    {
        final IDataStoreService service = dssFactory.create(dssURL);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Obtain version of Data Store Server at " + dssURL);
        }
        int dssVersion = service.getVersion(dssSessionToken);
        if (IDataStoreService.VERSION != dssVersion)
        {
            String msg =
                    "Data Store Server version is " + dssVersion + " instead of "
                            + IDataStoreService.VERSION;
            notificationLog.error(msg);
            throw new ConfigurationFailureException(msg);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Data Store Server (version " + dssVersion + ") registered for "
                    + dssURL);
        }
    }

    private void setServices(DataStorePE dataStore, DatastoreServiceDescriptions serviceDescs,
            IDataStoreDAO dataStoreDAO)
    {
        // Clean services first and save the result.
        // In general it should happen automatically, because services are annotated with
        // "DELETE_ORPHANS".
        // But hibernate does the orphans deletion at the flush time, and insertion of new services
        // is performed before.
        // So if it happens that services with the same keys are registered, we have a unique
        // constraint violation. This is a recognized hibernate bug HHH-2421.
        dataStore.setServices(new HashSet<DataStoreServicePE>());
        dataStoreDAO.createOrUpdateDataStore(dataStore);

        Set<DataStoreServicePE> dataStoreServices = createDataStoreServices(serviceDescs);
        dataStore.setServices(dataStoreServices);
    }

    private Set<DataStoreServicePE> createDataStoreServices(
            DatastoreServiceDescriptions serviceDescriptions)
    {
        Set<DataStoreServicePE> services = new HashSet<DataStoreServicePE>();

        Set<DataStoreServicePE> processing =
                createDataStoreServices(serviceDescriptions.getProcessingServiceDescriptions(),
                        DataStoreServiceKind.PROCESSING);
        services.addAll(processing);

        Set<DataStoreServicePE> queries =
                createDataStoreServices(serviceDescriptions.getReportingServiceDescriptions(),
                        DataStoreServiceKind.QUERIES);
        services.addAll(queries);

        return services;
    }

    private Set<DataStoreServicePE> createDataStoreServices(
            List<DatastoreServiceDescription> serviceDescriptions, DataStoreServiceKind serviceKind)
    {
        Set<DataStoreServicePE> services = new HashSet<DataStoreServicePE>();
        for (DatastoreServiceDescription desc : serviceDescriptions)
        {
            DataStoreServicePE service = new DataStoreServicePE();
            service.setKey(desc.getKey());
            service.setLabel(desc.getLabel());
            service.setKind(serviceKind);
            Set<DataSetTypePE> datasetTypes = extractDatasetTypes(desc.getDatasetTypeCodes(), desc);
            service.setDatasetTypes(datasetTypes);
            services.add(service);
        }
        return services;
    }

    private Set<DataSetTypePE> extractDatasetTypes(String[] datasetTypeCodes,
            DatastoreServiceDescription serviceDescription)
    {
        Set<DataSetTypePE> datasetTypes = new HashSet<DataSetTypePE>();
        Set<String> missingCodes = new HashSet<String>();
        IDataSetTypeDAO dataSetTypeDAO = daoFactory.getDataSetTypeDAO();
        for (String datasetTypeCode : datasetTypeCodes)
        {
            DataSetTypePE datasetType = dataSetTypeDAO.tryToFindDataSetTypeByCode(datasetTypeCode);
            if (datasetType == null)
            {
                missingCodes.add(datasetTypeCode);
            } else
            {
                datasetTypes.add(datasetType);
            }
        }
        if (missingCodes.size() > 0)
        {
            notifyDataStoreServerMisconfiguration(missingCodes, serviceDescription);
        }
        return datasetTypes;
    }

    private void notifyDataStoreServerMisconfiguration(Set<String> missingCodes,
            DatastoreServiceDescription serviceDescription)
    {
        String missingCodesText = CollectionUtils.abbreviate(missingCodes, -1);
        notificationLog.warn(String.format("The Datastore Server Plugin '%s' is misconfigured. "
                + "It refers to the dataset types which do not exist in openBIS: %s",
                serviceDescription.toString(), missingCodesText));
    }

    public String createDataSetCode(String sessionToken) throws UserFailureException
    {
        sessionManager.getSession(sessionToken); // throws exception if invalid sessionToken
        return daoFactory.getPermIdDAO().createPermId();
    }

    public Experiment tryToGetExperiment(String sessionToken,
            ExperimentIdentifier experimentIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert experimentIdentifier != null : "Unspecified experiment identifier.";

        final Session session = sessionManager.getSession(sessionToken);
        ExperimentPE experiment = tryToLoadExperimentByIdentifier(session, experimentIdentifier);
        if (experiment == null)
        {
            return null;
        }
        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                LoadableFields.PROPERTIES);
    }

    public Sample tryGetSampleWithExperiment(String sessionToken, SampleIdentifier sampleIdentifier)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = sessionManager.getSession(sessionToken);
        SamplePE sample = tryLoadSample(session, sampleIdentifier);
        if (sample != null)
        {
            HibernateUtils.initialize(sample.getProperties());
            enrichWithProperties(sample.getExperiment());
        }
        return SampleTranslator.translate(sample, session.getBaseIndexURL());
    }

    private ExperimentPE tryLoadExperimentBySampleIdentifier(final Session session,
            SampleIdentifier sampleIdentifier)
    {
        final SamplePE sample = tryLoadSample(session, sampleIdentifier);
        return sample == null ? null : sample.getExperiment();
    }

    private ExperimentPE tryToLoadExperimentByIdentifier(final Session session,
            ExperimentIdentifier experimentIdentifier)
    {
        final IExperimentBO experimentBO = boFactory.createExperimentBO(session);
        experimentBO.loadByExperimentIdentifier(experimentIdentifier);
        return experimentBO.getExperiment();
    }

    private SamplePE tryLoadSample(final Session session, SampleIdentifier sampleIdentifier)
    {
        final ISampleBO sampleBO = boFactory.createSampleBO(session);
        sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);
        final SamplePE sample = sampleBO.tryToGetSample();
        return sample;
    }

    private void enrichWithProperties(ExperimentPE experiment)
    {
        if (experiment == null)
        {
            return;
        }
        HibernateUtils.initialize(experiment.getProperties());
    }

    public IEntityProperty[] tryToGetPropertiesOfTopSampleRegisteredFor(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = sessionManager.getSession(sessionToken);
        final ISampleBO sampleBO = boFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        SamplePE sample = sampleBO.getSample();
        if (sample == null)
        {
            return null;
        }
        SamplePE top = sample.getTop();
        if (top == null)
        {
            return new IEntityProperty[0];
        }
        HibernateUtils.initialize(top.getProperties());
        return EntityPropertyTranslator.translate(top.getProperties().toArray(
                new SamplePropertyPE[0]), new HashMap<PropertyTypePE, PropertyType>());
    }

    public void registerDataSet(String sessionToken, SampleIdentifier sampleIdentifier,
            NewExternalData externalData) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = sessionManager.getSession(sessionToken);
        ExperimentPE experiment = tryLoadExperimentBySampleIdentifier(session, sampleIdentifier);
        if (experiment == null)
        {
            throw new UserFailureException("No experiment found for sample " + sampleIdentifier);
        }
        if (experiment.getInvalidation() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is invalid.");
        }
        final ISampleBO sampleBO = boFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        final SamplePE cellPlate = sampleBO.getSample();
        final IExternalDataBO externalDataBO = boFactory.createExternalDataBO(session);
        SourceType sourceType =
                externalData.isMeasured() ? SourceType.MEASUREMENT : SourceType.DERIVED;
        externalDataBO.define(externalData, cellPlate, sourceType);
        externalDataBO.save();
        final String dataSetCode = externalDataBO.getExternalData().getCode();
        assert dataSetCode != null : "Data set code not specified.";
    }

    public void registerDataSet(String sessionToken, ExperimentIdentifier experimentIdentifier,
            NewExternalData externalData) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert experimentIdentifier != null : "Unspecified experiment identifier.";

        final Session session = sessionManager.getSession(sessionToken);
        ExperimentPE experiment = tryToLoadExperimentByIdentifier(session, experimentIdentifier);
        if (experiment.getInvalidation() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is invalid.");
        }
        final IExternalDataBO externalDataBO = boFactory.createExternalDataBO(session);
        SourceType sourceType =
                externalData.isMeasured() ? SourceType.MEASUREMENT : SourceType.DERIVED;
        externalDataBO.define(externalData, experiment, sourceType);
        externalDataBO.save();
        final String dataSetCode = externalDataBO.getExternalData().getCode();
        assert dataSetCode != null : "Data set code not specified.";
    }

    public ExternalData tryGetDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert dataSetCode != null : "Unspecified data set code.";

        Session session = sessionManager.getSession(sessionToken); // assert authenticated

        IExternalDataBO externalDataBO = boFactory.createExternalDataBO(session);
        externalDataBO.loadByCode(dataSetCode);
        externalDataBO.enrichWithParentsAndExperiment();
        return ExternalDataTranslator.translate(externalDataBO.getExternalData(),
                dataStoreBaseURLProvider.getDataStoreBaseURL(), session.getBaseIndexURL());
    }

    public List<Sample> listSamplesByCriteria(String sessionToken,
            ListSamplesByPropertyCriteria criteria) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert criteria != null : "Unspecified criteria.";

        Session session = sessionManager.getSession(sessionToken);
        ISampleTable sampleTable = boFactory.createSampleTable(session);
        sampleTable.loadSamplesByCriteria(criteria);
        return SampleTranslator.translate(sampleTable.getSamples(), "");
    }

    public List<SimpleDataSetInformationDTO> listDataSets(String sessionToken, String dataStoreCode)
            throws UserFailureException
    {
        Session session = sessionManager.getSession(sessionToken);
        DataStorePE dataStore =
                getDAOFactory().getDataStoreDAO().tryToFindDataStoreByCode(dataStoreCode);
        if (dataStore == null)
        {
            throw new UserFailureException(String.format("Unknown data store '%s'", dataStoreCode));
        }
        IExternalDataTable dataSetTable = boFactory.createExternalDataTable(session);
        dataSetTable.loadByDataStore(dataStore);
        return SimpleDataSetHelper.translate(dataSetTable.getExternalData());
    }

    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull)
    {
        sessionManager.getSession(sessionToken);
        return getDAOFactory().getEventDAO().listDeletedDataSets(lastSeenDeletionEventIdOrNull);
    }

}

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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.DataIterator;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.QueryStrategyChooser;

/**
 * A fallback implementation of {@link IDatasetSetListingQuery} for database engines who don't
 * support querying for identifier sets.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { IDatasetListingQuery.class })
class DatasetSetListingQueryFallback implements IDatasetSetListingQuery
{
    private final IDatasetSetListingQuery oneByOneDelegate;

    private final IDatasetSetListingQuery fullTableScanDelegate;

    private final QueryStrategyChooser strategyChooser;

    public DatasetSetListingQueryFallback(final IDatasetListingQuery query,
            QueryStrategyChooser strategyChooser, long databaseInstanceId)
    {
        this.strategyChooser = strategyChooser;
        this.oneByOneDelegate = new DatasetSetListingQueryOneByOne(query);
        this.fullTableScanDelegate =
                new DatasetSetListingQueryFullTableScan(query, databaseInstanceId);
    }

    public Iterable<DatasetRecord> getDatasets(final LongSet sampleIds)
    {
        if (strategyChooser.useFullTableScan(sampleIds))
        {
            return fullTableScanDelegate.getDatasets(sampleIds);
        } else
        {
            return oneByOneDelegate.getDatasets(sampleIds);
        }
    }

    // TODO 2009-09-01, Tomasz Pylak: implement me! (h2)
    public DataIterator<Long> getDatasetChildrenIds(LongSet entityIds)
    {
        throw new NotImplementedException();
    }

}

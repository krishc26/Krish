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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Common method(s) all Business Objects and Tables have to implement.
 * 
 * @author Franz-Josef Elmer
 */
public interface IBusinessObject
{
    /**
     * Writes changed or added data to the Data Access Layers.
     * 
     * @throws UnsupportedOperationException if saving isn't supported because it is a read-only
     *             object.
     */
    public void save() throws UserFailureException;

}

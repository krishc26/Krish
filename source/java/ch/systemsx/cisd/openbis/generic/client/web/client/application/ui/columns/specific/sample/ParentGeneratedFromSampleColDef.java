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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

public class ParentGeneratedFromSampleColDef extends AbstractParentSampleColDef
{
    private static final String PARENT_PREFIX = "generatedFromParent";

    // GWT only
    public ParentGeneratedFromSampleColDef()
    {
        super(0, null);
    }

    public ParentGeneratedFromSampleColDef(int level, String headerText)
    {
        super(level, headerText);
    }

    @Override
    protected String getIdentifierPrefix()
    {
        return PARENT_PREFIX;
    }

    @Override
    protected Sample tryGetParent(Sample sample)
    {
        return sample.getGeneratedFrom();
    }
}
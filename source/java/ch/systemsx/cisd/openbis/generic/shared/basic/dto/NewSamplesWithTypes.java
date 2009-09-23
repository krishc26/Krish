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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.List;

/**
 * Contains a list of new samples and their type.
 * 
 * @author Izabela Adamczyk
 */
public class NewSamplesWithTypes
{
    SampleType sampleType;

    List<NewSample> newSamples;

    public NewSamplesWithTypes()
    {
    }

    public NewSamplesWithTypes(SampleType sampleType, List<NewSample> newSamples)
    {
        setSampleType(sampleType);
        setNewSamples(newSamples);
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public List<NewSample> getNewSamples()
    {
        return newSamples;
    }

    public void setNewSamples(List<NewSample> newSamples)
    {
        this.newSamples = newSamples;
    }
}
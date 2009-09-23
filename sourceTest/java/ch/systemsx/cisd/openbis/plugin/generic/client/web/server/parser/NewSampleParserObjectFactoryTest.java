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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.fail;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.parser.DefaultPropertyMapper;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Test cases for corresponding {@link NewSampleParserObjectFactory} class.
 * 
 * @author Christian Ribeaud
 */
public final class NewSampleParserObjectFactoryTest
{

    private final static NewSampleParserObjectFactory createNewSampleParserObjectFactory(
            IPropertyMapper propertyMapper, boolean expectIdentifierInFile)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode("SAMPLE_TYPE");
        final NewSampleParserObjectFactory parserObjectFactory =
                new NewSampleParserObjectFactory(sampleType, propertyMapper,
                        expectIdentifierInFile, true);
        return parserObjectFactory;
    }

    private final static IPropertyMapper createPropertyMapper()
    {
        final String[] properties = new String[]
            { "identifier", "container", "parent", "prop1", "prop2" };
        final DefaultPropertyMapper propertyMapper = new DefaultPropertyMapper(properties);
        return propertyMapper;
    }

    private final static IPropertyMapper createPropertyMapperWithoutIdentifier()
    {
        final String[] properties = new String[]
            { "prop1", "prop2" };
        final DefaultPropertyMapper propertyMapper = new DefaultPropertyMapper(properties);
        return propertyMapper;
    }

    @Test
    public final void testCreateObjectWithNullOrNotEnoughColumns()
    {
        final NewSampleParserObjectFactory parserObjectFactory =
                createNewSampleParserObjectFactory(createPropertyMapper(), true);
        boolean fail = true;
        try
        {
            parserObjectFactory.createObject(null);
        } catch (AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        try
        {
            parserObjectFactory.createObject(ArrayUtils.EMPTY_STRING_ARRAY);
            fail(String.format("'%s' expected.",
                    ch.systemsx.cisd.common.parser.IndexOutOfBoundsException.class));
        } catch (final ch.systemsx.cisd.common.parser.IndexOutOfBoundsException ex)
        {
            // Nothing to do here.
        }
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] getLineTokensForAutoGeneratedCodes()
    {
        return new Object[][]
            {
                { new String[]
                    { "", "" }, 0 },
                { new String[]
                    { null, null }, 0 },
                { new String[]
                    { "1", "hello" }, 2 },

            };
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] getLineTokens()
    {
        return new Object[][]
            {
                { new String[]
                    { "", "", "", "", "" }, 0 },
                { new String[]
                    { "id1", "cont1", "par1", "1", "hello" }, 2 },

            };
    }

    @Test(dataProvider = "getLineTokens")
    public final void testFailCreateObjectForAutoGeneratedCodesWithIdentifierSpecified(
            final String[] lineTokens, final int numberOfProperties)
    {
        final NewSampleParserObjectFactory parserObjectFactory =
                createNewSampleParserObjectFactory(createPropertyMapper(), false);
        boolean exceptionThrown = false;
        try
        {
            parserObjectFactory.createObject(lineTokens);
        } catch (ParserException e)
        {
            exceptionThrown = true;
            AssertJUnit
                    .assertTrue(e
                            .getMessage()
                            .contains(
                                    "Requested automatical generation of codes. Column 'identifier' should be removed from the file."));
        }
        AssertJUnit.assertTrue(exceptionThrown);
    }

    @Test(dataProvider = "getLineTokensForAutoGeneratedCodes")
    public final void testFailCreateObjectWithIdentifierUnspecified(final String[] lineTokens,
            final int numberOfProperties)
    {
        final NewSampleParserObjectFactory parserObjectFactory =
                createNewSampleParserObjectFactory(createPropertyMapperWithoutIdentifier(), true);
        boolean exceptionThrown = false;
        try
        {
            parserObjectFactory.createObject(lineTokens);
        } catch (ParserException e)
        {
            exceptionThrown = true;
            AssertJUnit.assertTrue(e.getMessage().contains(
                    "Mandatory column 'identifier' is missing."));
        }
        AssertJUnit.assertTrue(exceptionThrown);
    }

    @Test(dataProvider = "getLineTokensForAutoGeneratedCodes")
    public final void testCreateObjectForAutoGeneratedCodes(final String[] lineTokens,
            final int numberOfProperties)
    {
        final NewSampleParserObjectFactory parserObjectFactory =
                createNewSampleParserObjectFactory(createPropertyMapperWithoutIdentifier(), false);
        final NewSample objectCreated = parserObjectFactory.createObject(lineTokens);
        AssertJUnit.assertNull(objectCreated.getIdentifier());
        final IEntityProperty[] properties = objectCreated.getProperties();
        assertEquals(numberOfProperties, properties.length);
        int index = 0;
        for (final IEntityProperty sampleProperty : properties)
        {
            sampleProperty.getValue().equals(lineTokens[index++]);
        }
    }

    @Test(dataProvider = "getLineTokens")
    public final void testCreateObject(final String[] lineTokens, final int numberOfProperties)
    {
        final NewSampleParserObjectFactory parserObjectFactory =
                createNewSampleParserObjectFactory(createPropertyMapper(), true);
        final NewSample objectCreated = parserObjectFactory.createObject(lineTokens);
        assertEquals(objectCreated.getIdentifier(), lineTokens[0]);
        assertEquals(objectCreated.getContainerIdentifier(),
                StringUtils.isEmpty(lineTokens[1]) ? null : lineTokens[1]);
        assertEquals(objectCreated.getParentIdentifier(), StringUtils.isEmpty(lineTokens[2]) ? null
                : lineTokens[2]);
        final IEntityProperty[] properties = objectCreated.getProperties();
        assertEquals(numberOfProperties, properties.length);
        int index = 3;
        for (final IEntityProperty sampleProperty : properties)
        {
            sampleProperty.getValue().equals(lineTokens[index++]);
        }
    }
}
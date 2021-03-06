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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = LuceneQueryBuilder.class)
public class LuceneQueryBuilderTest extends AssertJUnit
{
    @Test
    public void testReplaceWordSeparators()
    {
        char[] wordSeparators = new char[]
            { '.', ',', '-', '_' };
        String result = LuceneQueryBuilder.replaceWordSeparators("a.b-c_d,e", wordSeparators);
        assertEquals("(a AND b AND c AND d AND e)", result);

        result = LuceneQueryBuilder.replaceWordSeparators("..,,a.b,,..", wordSeparators);
        assertEquals("(a AND b)", result);

        result = LuceneQueryBuilder.replaceWordSeparators(".a", wordSeparators);
        assertEquals("a", result);

    }

    @DataProvider(name = "queryEscaping")
    protected Object[][] getQueriesToTest()
    {
        return new Object[][]
            {
                { "abc", "abc" },
                { "code:CP registrator:Joe", "code\\:CP registrator\\:Joe" },
                { "::", "\\:\\:" } };
    }

    @Test(dataProvider = "queryEscaping")
    public final void testDisableAdvancedSearch(String unescapedQuery, String escapedQuery)
    {
        String query = LuceneQueryBuilder.adaptQuery(unescapedQuery);
        assertEquals(escapedQuery, query);
    }

}

/*
 *  Copyright (C) 2020 Royal Observatory, University of Edinburgh, UK
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.roe.wfau.enteucha.hsqldb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.roe.wfau.enteucha.api.AbstractTestCase;
import uk.ac.roe.wfau.enteucha.api.Matcher;
import uk.ac.roe.wfau.enteucha.hsqldb.HsqlZoneMatcherImpl.IndexingShape;

/**
 * 
 * 
 */
@Component
@RunWith(
        SpringJUnit4ClassRunner.class
        )
@ContextConfiguration(
    locations = {
        "classpath:component-config.xml"
        }
    )
public class HsqlZoneMatcherTestCase
extends AbstractTestCase
    {

    /**
     * Public constructor.
     * 
     */
    public HsqlZoneMatcherTestCase()
        {
        super();
        }

    /**
     * Test finding things.
     * 
     */
    @Test
    public void test()
        {
        IndexingShape[] shapes =
            {
            IndexingShape.SEPARATE,
            //IndexingShape.COMBINED,
            //IndexingShape.COMPLEX
            };
        for (IndexingShape indexshape : shapes)
            {
            final IndexingShape indexing = indexshape ;
            outerloop(
                new Matcher.Factory()
                    {
                    @Override
                    public Matcher create()
                        {
                        return new HsqlZoneMatcherImpl(
                            indexing,
                            zoneval
                            );
                        }
                    }
                );
            }
        }

    /**
     * Public main() method.
     *
     */
    public static void main(String[] args)
        {
        testmain(
            HsqlZoneMatcherTestCase.class
            );
        }
    }

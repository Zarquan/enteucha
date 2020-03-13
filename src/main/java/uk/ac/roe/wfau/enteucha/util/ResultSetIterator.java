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
package uk.ac.roe.wfau.enteucha.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * Wrap a SQL {@ResultSet} with an {@link Iterator}.
 * Returns an 
 *
 */
@Slf4j
public abstract class ResultSetIterator<T>
implements Iterator<T>
    {
    public ResultSetIterator(final ResultSet results)
        {
        this.results = results;
        this.step();
        }
    
    private ResultSet results ;

    private T next ;

    private void step()
        {
        try {
            if (results.next())
                {
                this.next = build(
                    results
                    );
                }
            else {
                this.next = null ;
                }
            }
        catch (SQLException ouch)
            {
            log.error("SQLException reading ResultSet [{}]", ouch.getMessage());
            }
        }
    
    @Override
    public boolean hasNext()
        {
        return (this.next != null);
        }

    @Override
    public T next()
        {
        T temp = this.next ;
        this.step();
        return temp;
        }

    /**
     * Build a type <T> object from a {@link ResultSet}.
     * @param results The ResultSet to read.
     * @return A type <T> object. 
     * @throws SQLException
     * 
     */
    abstract protected T build(final ResultSet results)
    throws SQLException;
    
    }

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

import uk.ac.roe.wfau.enteucha.api.Position;
import uk.ac.roe.wfau.enteucha.api.PositionImpl;

/**
 * Wrap a {@ResultSet} with an {@link Iterator}.
 *
 */
public class PositionResultSetIterator
extends ResultSetIterator<Position>
implements Iterator<Position>
    {
    public PositionResultSetIterator(ResultSet results)
        {
        super(results);
        }

    protected Position build(final ResultSet results)
    throws SQLException 
        {
        return new PositionImpl(
            results.getLong(1),                      
            results.getDouble(2),                        
            results.getDouble(3),                        
            results.getDouble(4),                        
            results.getDouble(5),                        
            results.getDouble(6)                        
            );
        }
    }

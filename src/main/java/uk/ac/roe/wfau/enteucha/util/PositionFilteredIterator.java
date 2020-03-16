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

import java.util.Iterator;

import org.apache.commons.math3.util.FastMath;

import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.Position;

/**
 * A filtered {@link Iterator} for {@link Position}s.
 *
 */
@Slf4j
public abstract class PositionFilteredIterator
implements Iterator<Position>
    {
    /**
     * Public constructor.
     * 
     */
    public PositionFilteredIterator(final Iterator<Position> positions, final Position target, final Double radius)
        {
        this.positions = positions ;
        this.target    = target ;
        this.radius    = radius ;
        this.next = this.step();
        }
    
    protected Iterator<Position> positions;
    protected Position target;
    protected Double radius;
    protected Position next;

    /**
     * Get the next candidate and check if it is within range.
     *
     */
    protected Position step()
        {
        if (positions != null)
            {
            while (positions.hasNext())
                {
                final Position position = positions.next();
                if (check(position))
                    {
                    return position ;
                    }
                }
            }
        return null ;
        }

    @Override
    public boolean hasNext()
        {
        return (next != null);
        }

    @Override
    public Position next()
        {
        final Position temp = next ;
        next = step();
        return temp;
        }

    /**
     * Check if a {@link Position} is within the search radius of a target {@link Position}.
     *
     */
    protected abstract boolean check(final Position position);

    }

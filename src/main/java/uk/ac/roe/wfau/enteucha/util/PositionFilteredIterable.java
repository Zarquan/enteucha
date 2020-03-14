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

import uk.ac.roe.wfau.enteucha.api.Position;

/**
 * A proximity filter for an {@link Iterable} of {@link Position}s.  
 * 
 */
public class PositionFilteredIterable implements Iterable<Position>
    {

    /**
     * Public constructor.
     * 
     */
    public PositionFilteredIterable(final Iterable<Position> positions, final Position target, final Double radius)
        {
        this.positions = positions ;
        this.target = target ;
        this.radius = radius ;
        }
    
    private Iterable<Position> positions ;
    private Position target ;
    private Double radius;
    
    @Override
    public Iterator<Position> iterator()
        {
        return new PositionFilteredIterator(
            positions.iterator(),
            target,
            radius
            );
        }
    }

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
 * A proximity filter based on the minimum and maximum ra and dec values..  
 *
 */
@Slf4j
public class MinMaxRangeFilter
extends PositionFilter
implements Iterator<Position>
    {
    /**
     * Small offset to avoid divide by zero.
     * 
     */
    protected static final double epsilon = 10E-6;
    
    /**
     * Public constructor.
     * 
     */
    public MinMaxRangeFilter(final Iterator<Position> positions, final Position target, final Double radius)
        {
        super(
            positions,
            target,
            radius
            );
        }

    /**
     * Check if a {@link Position} is within the search radius of a target {@link Position}.
     *
     */
    @Override
    protected boolean check(final Position position)
        {
        double factor = radius / (FastMath.abs(FastMath.cos(FastMath.toRadians(target.dec()))) + epsilon);
        double minra = (target.ra() - factor);
        double maxra = (target.ra() + factor);

        double mindec = (target.dec() - radius) ; 
        double maxdec = (target.dec() + radius) ; 
        
        boolean match =
            ((position.ra()  >= minra)  && (position.ra()  <= maxra))
            &&
            ((position.dec() >= mindec) && (position.dec() <= maxdec))   
            ;
                
        //log.debug("[{}] [{}][{}]", (match ? "+++" : "---"), position.ra(), position.dec());
        return match ;
        }
    }

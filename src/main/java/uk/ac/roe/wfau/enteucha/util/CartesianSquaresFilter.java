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
 * A proximity filter based on the sum of the squares of the cartesian coordinates.  
 *
 */
@Slf4j
public class CartesianSquaresFilter
extends PositionFilteredIterator
implements Iterator<Position>
    {
    /**
     * Public constructor.
     * 
     */
    public CartesianSquaresFilter(final Iterator<Position> positions, final Position target, final Double radius)
        {
        super(
            positions,
            target,
            radius
            );
        }

    /**
     * Check if a {@link Position} is within the search radius of a target {@link Position}
     * by calculating the distance between the cartesian coordinates. 
     *
     */
    @Override
    protected boolean check(final Position position)
        {
        double squares =
            FastMath.pow(
                position.cx() - target.cx(),
                2
                ) 
          + FastMath.pow(
                position.cy() - target.cy(),
                2
                ) 
          + FastMath.pow(
                position.cz() - target.cz(),
                2
                );
        double squaresin = 4 * (
            FastMath.pow(
                FastMath.sin(
                    FastMath.toRadians(
                        radius
                        )/2
                    ),
                2)
            );
        boolean match = (squaresin > squares);
        log.debug("[{}] [{}][{}]", (match ? "+++" : "---"), position.ra(), position.dec());
        return match ;
        }

    /*
     * Target [120][120] radius [0.125]
     * 
     * [---] [119.75][119.75] 
     * [+++] [119.75][120.0] <-- Why (0.25) ? 
     * [---] [119.75][120.25] 
     * 
     * [---] [120.0][119.75] <-- Why not ?
     * [+++] [120.0][120.0] 
     * [---] [120.0][120.25] <-- Why not ? 
     * 
     * [---] [120.25][119.75] 
     * [+++] [120.25][120.0] <-- Why (0.25) ?
     * [---] [120.25][120.25] 
     * 
     */
    }

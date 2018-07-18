/*
 *  Copyright (C) 2018 Royal Observatory, University of Edinburgh, UK
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

package uk.ac.roe.wfau.enteucha.dataset;

import uk.ac.roe.wfau.enteucha.dataset.Position.Matcher;

/**
 * Public interface for a declination stripe.
 * 
 */
public interface Zone
extends Matcher
    {

    /**
     * Public interface for a set of {@link Zone}s.
     * 
     *
     */
    public static interface ZoneSet
    extends Matcher
        {
        /**
         * Select a set of {@link Zone}s that contain {@link Position}s within a radius around a target {@link Position}.
         * 
         */
        public Iterable<Zone> contains(final Position target, final Double radius);

        
        }
    
    /**
     * The zone identifier.
     * 
     */
    public int ident();

    }
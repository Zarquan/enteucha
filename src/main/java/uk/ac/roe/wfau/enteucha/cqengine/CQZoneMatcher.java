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

package uk.ac.roe.wfau.enteucha.cqengine;

import uk.ac.roe.wfau.enteucha.api.Matcher;
import uk.ac.roe.wfau.enteucha.api.Position;

/**
 * Public interface for a declination stripe.
 * 
 */
public interface CQZoneMatcher
extends Matcher
    {

    /**
     * Public interface for a set of {@link CQZone}s.
     * 
     *
     */
    public static interface Zone
    extends Matcher
        {
        /**
         * The zone identifier.
         * 
         */
        public int ident();

        }

    /**
     * Select a set of {@link Zone}s that contain {@link Position}s within a radius around a target {@link Position}.
     * 
     */
    public Iterable<Zone> contains(final Position target, final Double radius);

    /**
     * Enumeration of the available indexing schemes.
     * 
     */
    public enum IndexingShape
        {
        SEPARATE_SIMPLE(),
        COMBINED_SIMPLE(),
        SEPARATE_QUANTIZED();
        };

    /**
     * The {@link IndexingShape} for this {@link CQZoneMatcher}.
     * 
     */
    public IndexingShape indexing();

    }

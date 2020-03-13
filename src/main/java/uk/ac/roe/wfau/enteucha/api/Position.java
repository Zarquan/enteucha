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

package uk.ac.roe.wfau.enteucha.api;

/**
 * Public interface for a {@link Position} in the sky. 
 * 
 */
public interface Position
    {

    /**
     * Public interface for a {@link Position} factory.
     * 
     */
    public static interface Factory
        {
        /**
         * Create a new {@link Position}.
         * 
         */
        public Position create(final Double ra, final Double dec) ;
        }
    
    /**
     * The zone or HTM identifier this position is in.
     * 
     */
    public Long ident();

    /**
     * The equatorial coordinate right ascension.
     * 
     */
    public Double ra();
    /**
     * The equatorial coordinate declination.
     * 
     */
    public Double dec();

    /**
     * Cartesian coordinate.
     * 
     */
    public Double cx();
    /**
     * Cartesian coordinate.
     * 
     */
    public Double cy();
    /**
     * Cartesian coordinate.
     * 
     */
    public Double cz();
    
    }

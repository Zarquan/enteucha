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
 * A simple POJO implementation of {@link Position}.
 * 
 */
public class PositionImpl implements Position
    {

    /**
     * A simple implementation of {@link Position.Factory}.
     *  
     */
    public static class Factory
    implements Position.Factory
        {
        @Override
        public Position create(final Double ra, final Double dec)
            {
            return new PositionImpl(ra, dec);
            }
        }

    /**
     * Protected constructor.
     *  
     */
    public PositionImpl(final Double ra, final Double dec)
        {
        this.ra  = ra ;
        this.dec = dec;

        double radra  = Math.toRadians(ra);
        double raddec = Math.toRadians(dec);
        double cosdec = Math.cos(raddec);

        this.cx = Math.cos(radra) * cosdec ;
        this.cy = Math.sin(radra) * cosdec ;
        this.cz = Math.sin(raddec) ;
        
        }

    /**
     * Protected constructor.
     *  
     */
    public PositionImpl(final Double ra, final Double dec, final Double cx, final Double cy, final Double cz)
        {
        this(
            null,
            ra,
            dec,
            cx,
            cy,
            cz
            );
        }

    /**
     * Protected constructor.
     *  
     */
    public PositionImpl(final Long ident, final Double ra, final Double dec, final Double cx, final Double cy, final Double cz)
        {
        this.ra  = ra ;
        this.dec = dec;
        this.cx  = cx ;
        this.cy  = cy ;
        this.cz  = cz ;
        }

    private Long ident;
    @Override
    public Long ident()
        {
        return this.ident;
        }

    private Double ra;
    @Override
    public Double ra()
        {
        return this.ra;
        }

    private Double dec;
    @Override
    public Double dec()
        {
        return this.dec;
        }

    private Double cx;
    @Override
    public Double cx()
        {
        return this.cx;
        }

    private Double cy;
    @Override
    public Double cy()
        {
        return this.cy;
        }

    private Double cz;
    @Override
    public Double cz()
        {
        return this.cz;
        }
    }

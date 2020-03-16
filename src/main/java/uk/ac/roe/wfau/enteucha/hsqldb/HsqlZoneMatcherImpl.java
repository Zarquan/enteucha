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

package uk.ac.roe.wfau.enteucha.hsqldb;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.commons.math3.util.FastMath;

import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.Matcher;
import uk.ac.roe.wfau.enteucha.api.Position;
import uk.ac.roe.wfau.enteucha.util.CartesianSquaresFilter;
import uk.ac.roe.wfau.enteucha.util.MinMaxRangeFilter;
import uk.ac.roe.wfau.enteucha.util.PositionResultSetIterator;

/**
 * 
 * 
 */
@Slf4j
public class HsqlZoneMatcherImpl
extends HsqlMatcherBase
implements Matcher
    {
    /**
     * Public constructor.
     * 
     */
    public HsqlZoneMatcherImpl(int count)
        {
        this(
            IndexingShape.SEPARATE,
            count
            ) ;
        }
    
    /**
     * Public constructor.
     * 
     */
    public HsqlZoneMatcherImpl(final IndexingShape indexing, double height)
        {
        this.indexing = indexing;
        this.height = height ;
        this.init();
        }

    /**
     * Indexing shape.
     * 
     */
    public enum IndexingShape
        {
        SEPARATE(),
        COMBINED(),
        COMPLEX();
        };

    /**
     * The {@link IndexingShape} for this {@link Matcher}.
     * 
     */
    private IndexingShape indexing ;

    /**
     * The {@link IndexingShape} for this {@link Matcher}.
     * 
     */
    public IndexingShape indexing()
        {
        return this.indexing;
        }

    /**
     * The height of each zone slice.
     * 
     */
    private double height ;

    /**
     * The height of each zone slice.
     * 
     */
    public double height()
        {
        return this.height;
        }
    
    /**
     * Small offset to avoid divide by zero.
     * 
     */
    protected static final Double epsilon = 10E-6;

    /**
     * Initialise our database connection.
     * 
     */
    public void init()
        {
        try {
            this.connect();

            this.connection().createStatement().executeUpdate(
                    "DROP TABLE zones IF EXISTS"
                    );
            this.connection().createStatement().executeUpdate(
                "CREATE TABLE zones ("
                + "zoneid BIGINT NOT NULL, "
                + "ra  DOUBLE NOT NULL, "
                + "dec DOUBLE NOT NULL, "
                + "cx  DOUBLE NOT NULL, "
                + "cy  DOUBLE NOT NULL, "
                + "cz  DOUBLE NOT NULL  "
                + ")"
                );
            switch (this.indexing)
                {
                case SEPARATE:
                    this.connection().createStatement().executeUpdate(
                        "CREATE INDEX zoneindex "
                        + " ON zones ("
                        + "    zoneid"
                        + ")"
                        );
                    this.connection().createStatement().executeUpdate(
                        "CREATE INDEX raindex "
                        + " ON zones ("
                        + "    ra"
                        + ")"
                        );
                    this.connection().createStatement().executeUpdate(
                        "CREATE INDEX decindex"
                        + " ON zones ("
                        + "    dec"
                        + ")"
                        );
                    break ;

                case COMBINED:
                    this.connection().createStatement().executeUpdate(
                        "CREATE INDEX zoneindex "
                        + " ON zones ("
                        + "    zoneid"
                        + ")"
                        );
                    this.connection().createStatement().executeUpdate(
                        "CREATE INDEX radecindex"
                        + " ON zones ("
                        + "    ra,"
                        + "    dec"
                        + ")"
                        );
                    break ;

                case COMPLEX:
                    this.connection().createStatement().executeUpdate(
                        "CREATE INDEX complexindex"
                        + " ON zones ("
                        + "    zoneid,"
                        + "    ra,"
                        + "    dec"
                        + ")"
                        );
                break ;
                    
                default :
                    throw new IllegalArgumentException(
                        "Unknown indexing shape [{" + this.indexing.name() + "}]"
                        ); 
                }
            }
        catch (final SQLException ouch)
            {
            log.error("SQLException [{}]", ouch);
            throw new RuntimeException(
                ouch
                );
            }
        }

    @Override
    public Iterator<Position> matches(final Position target, final Double radius)
        {
        //log.trace("matches [{}][{}] [{}]", target.ra(), target.dec(), radius);
        //log.debug("radius [{}]", radius);
        //log.debug("height [{}]", height);

        //log.debug("ra  [{}]", target.ra());
        //log.debug("dec [{}]", target.dec());

        //log.debug("cx  [{}]", target.cx());
        //log.debug("cy  [{}]", target.cy());
        //log.debug("cz  [{}]", target.cz());

        final String template = "SELECT "
            + "    zoneid, "
            + "    ra, "
            + "    dec,"
            + "    cx, "
            + "    cy, "
            + "    cz  "
            + " FROM "
            + "    zones "
            + " WHERE "
            + "    zoneid BETWEEN "
            + "        ? "
            + "    AND "
            + "        ? "
            ;
/*
 * 
            + " AND "
            + "    ra BETWEEN "
            + "        ? "
            + "    AND "
            + "        ? "
            + " AND "
            + "    dec BETWEEN "
            + "        ? "
            + "    AND "
            + "        ? "
            ;
 * 
 */

/*
 * TODO Make this configurable. 
            + "    AND "
            + "        ? "
            + "    AND  "
            + "        ? > (power((cx - ?), 2) + power((cy - ?), 2) + power(cz - ?, 2)) ";
 *             
 */

        final int minzone = (int) FastMath.floor(((target.dec() + 90) - radius) / this.height) ;
        final int maxzone = (int) FastMath.floor(((target.dec() + 90) + radius) / this.height) ;
/*
 *
        double factor = radius / (FastMath.abs(FastMath.cos(FastMath.toRadians(target.dec()))) + epsilon);
        double minra = (target.ra() - factor);
        double maxra = (target.ra() + factor);
 *
 */
        double mindec = target.dec() - radius ; 
        double maxdec = target.dec() + radius ; 

        /*
         * 
        double squaresin = 4 * (
                FastMath.pow(
                    FastMath.sin(
                        FastMath.toRadians(
                            radius
                            )/2
                        ),
                    2)
                );
         * 
         */

        //log.debug("min/max zone [{}][{}]", minzone, maxzone);
        //log.debug("min/max ra   [{}][{}]", minra, maxra);
        //log.debug("min/max dec  [{}][{}]", mindec, maxdec);
        
        try {
            final PreparedStatement statement = connection().prepareStatement(template);

            statement.setInt(1, minzone);
            statement.setInt(2, maxzone);            
/*            
            statement.setDouble(3, minra);
            statement.setDouble(4, maxra);

            statement.setDouble(5, mindec);
            statement.setDouble(6, maxdec);
 */
            return new MinMaxRangeFilter(
                new CartesianSquaresFilter(
                    new PositionResultSetIterator(
                        statement.executeQuery()
                        ),
                    target,
                    radius
                    ),
                target,
                radius
                );
            }
        catch (SQLException ouch)
            {
            log.error("SQLException [{}]", ouch.getMessage());
            throw new RuntimeException(
                ouch
                );
            }
        }

    @Override
    public void insert(Position position)
        {
        String template = "INSERT INTO "
            + "    zones ( "
            + "        zoneid, "
            + "        ra, "
            + "        dec, "
            + "        cx, "
            + "        cy, "
            + "        cz "
            + "        ) "
            + "    VALUES( "
            + "        ?, "
            + "        ?, "
            + "        ?, "
            + "        ?, "
            + "        ?, "
            + "        ?"
            + "        ) ";

        final Integer zone = (int) FastMath.floor((position.dec() + 90) / this.height);
        //log.trace("insert [{}] [{}][{}]", zone, position.ra(), position.dec());

        try {
            final PreparedStatement statement = connection().prepareStatement(template);
            statement.setInt(1, zone);
            statement.setDouble(2, position.ra());
            statement.setDouble(3, position.dec());
            statement.setDouble(4, position.cx());
            statement.setDouble(5, position.cy());
            statement.setDouble(6, position.cz());
            statement.execute();
            total++;
            }
        catch (SQLException ouch)
            {
            log.error("SQLException during insert [{}]", ouch);
            throw new RuntimeException(
                ouch
                );
            }
        }

    private long total = 0;

    @Override
    public long total()
        {
        return total;
        }

    @Override
    public String info()
        {
        final StringBuilder builder = new StringBuilder(); 
        builder.append("Class [");
        builder.append(this.getClass().getSimpleName());
        builder.append("] ");
        builder.append("Indexing [");
        builder.append(this.indexing.name());
        builder.append("] ");
        builder.append("Total rows [");
        builder.append(String.format("%,d", this.total()));
        builder.append("] ");
        builder.append("Zone height [");
        builder.append(this.height);
        builder.append("] ");
        builder.append("URL [");
        builder.append(this.url());
        builder.append("]");
        return builder.toString();
        }
    }

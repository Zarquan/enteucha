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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.jhu.htm.core.Domain;
import edu.jhu.htm.core.HTMException;
import edu.jhu.htm.core.HTMindex;
import edu.jhu.htm.core.HTMindexImp;
import edu.jhu.htm.core.HTMrange;
import edu.jhu.htm.core.HTMrangeIterator;
import edu.jhu.htm.geometry.Circle;
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
public class HsqlHtmidMatcherImpl
extends HsqlMatcherBase
implements Matcher
    {
    /**
     * Default depth.
     *
     */
    public static final int DEFAULT_DEPTH = 10;
    public static final int DEFAULT_BUILD = 10;

    /**
     * Public constructor.
     *
     */
    public HsqlHtmidMatcherImpl()
        {
        this(
            DEFAULT_DEPTH,
            DEFAULT_BUILD
           );
        }

    /**
     * Public constructor.
     *
     */
    public HsqlHtmidMatcherImpl(final int depth, final int build)
        {
        this.depth = depth;
        this.build = build;
//      this.index = new HTMindexImp(depth, build);
        this.init();
        }

    private long total;

    @Override
    public long total()
        {
        return this.total;
        }

    /**
     * The depth of our index.
     *
     */
    protected final int depth ;
    protected final int build ;

    /**
     * The depth of our index.
     *
     */
    protected final int depth()
        {
        return this.depth;
        }

    /**
     * Our HTM index.
     *
     */
    protected final HTMindex index ;

    /**
     * Invalid HTMID, {@value}.
     *
     */
    public static final Long INVALID_HTMID = -1L ;

    /**
     * Get the HTM triangle ID that contains a point.
     * @param pos The point position.
     *
     */
    public Long htmid(Position pos)
        {
        return htmid(pos.ra(), pos.dec());
        }

    /**
     * Get the HTM triangle ID that contains a point.
     * @param ra  The point position.
     * @param dec The point position.
     *
     */
    public Long htmid(double ra, double dec)
        {
        try {
            return index.lookupId(
                ra,
                dec
                );
            }
        catch (final HTMException ouch)
            {
            log.error("HTMException checking HTMID for position [{}]", ra, dec, ouch.getMessage());
            throw new RuntimeException(
                ouch
                );
            }
        }

    /**
     * Get a list of HTM triangles that intersect a circle.
     * @param ra  The circle position.
     * @param dec The circle position.
     * @param radius The circle radius.
     *
     */
    public Collection<Long> circle(double ra, double dec, double radius)
        {
        //log.debug("circle [{}][{}][{}]", ra, dec, radius);
        final ArrayList<Long> list = new ArrayList<Long>();

        try {
            final HTMrange range = new HTMrange();
            // constructor specifying (ra, dec) and radius in arcmin.
            final Circle circle = new Circle(
                ra,
                dec,
                (radius * 160)
                );
            final Domain domain = circle.getDomain();
            domain.setOlevel(depth);

            //log.debug("intersect - start");
            domain.intersect(
                (HTMindexImp) index,
                range,
                false
                );
            //log.debug("intersect - done");

            @SuppressWarnings("unchecked")
            final Iterator<Long> iter = new HTMrangeIterator(
                range,
                false
                );
            while (iter.hasNext())
                {
                list.add(
                    iter.next()
                    );
                }
            }
        catch (HTMException ouch)
            {
            //log.error("HTMException [{}]", ouch.getMessage());
            throw new RuntimeException(
                ouch
                );
            };
        return list ;
        }

    @Override
    public void init()
        {
        this.index = new HTMindexImp(
            this.depth,
            this.build
            );

        try {
            this.connect();

            this.connection().createStatement().executeUpdate(
                    "DROP TABLE htmsources IF EXISTS"
                    );
            this.connection().createStatement().executeUpdate(
                "CREATE TABLE htmsources ("
                + "htmid BIGINT NOT NULL, "
                + "ra  DOUBLE NOT NULL, "
                + "dec DOUBLE NOT NULL, "
                + "cx  DOUBLE NOT NULL, "
                + "cy  DOUBLE NOT NULL, "
                + "cz  DOUBLE NOT NULL  "
                + ")"
                );
            this.connection().createStatement().executeUpdate(
                "CREATE INDEX htmidindex "
                + " ON htmsources ("
                + "    htmid"
                + ")"
                );
            }
        catch (final SQLException ouch)
            {
            log.debug("SQLException during init() [{}]", ouch.getMessage());
            throw new RuntimeException(
                ouch
                );
            }
        }

    @Override
    public void insert(final Position position)
        {
        String template = "INSERT INTO "
            + "    htmsources ( "
            + "        htmid, "
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

        final Long htmid = htmid(position);
        //log.trace("insert [{}] [{}][{}]", htmid, position.ra(), position.dec());

        try {
            final PreparedStatement statement = connection().prepareStatement(template);
            statement.setLong(1, htmid);
            statement.setDouble(2, position.ra());
            statement.setDouble(3, position.dec());
            statement.setDouble(4, position.cx());
            statement.setDouble(5, position.cy());
            statement.setDouble(6, position.cz());
            statement.execute();
            this.total++;
            }
        catch (SQLException ouch)
            {
            log.error("SQLException during insert [{}]", ouch);
            throw new RuntimeException(
                ouch
                );
            }
        }

    @Override
    public Iterator<Position> matches(Position target, Double radius)
        {
        //log.trace("matches [{}][{}] [{}]", target.ra(), target.dec(), radius);
        final String template = "SELECT "
                + "    htmid, "
                + "    ra, "
                + "    dec, "
                + "    cx, "
                + "    cy, "
                + "    cz  "
                + " FROM "
                + "    htmsources "
                + " WHERE "
                + "    htmid IN (?)"
                + "";

        try {
            final Statement statement = connection().createStatement();

            final Collection<Long> htmids = circle(
                    target.ra(),
                    target.dec(),
                    radius
                    );

            final StringBuilder builder = new StringBuilder();

            Iterator<Long> iter = htmids.iterator();
            while (iter.hasNext())
                {
                builder.append(
                    iter.next()
                    );
                if (iter.hasNext())
                    {
                    builder.append(
                        ","
                        );
                    }
                }

            final String query = template.replace("?", builder.toString());
            //log.trace("--- [{}]", query);

            return new MinMaxRangeFilter(
                new CartesianSquaresFilter(
                    new PositionResultSetIterator(
                        statement.executeQuery(
                            query
                            )
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
    public String type()
        {
        return this.getClass().getSimpleName();
        }

    @Override
    public String info()
        {
        final StringBuilder builder = new StringBuilder();
        builder.append("Class [");
        builder.append(this.getClass().getSimpleName());
        builder.append("] ");
        builder.append("Total rows [");
        builder.append(String.format("%,d", this.total()));
        builder.append("] ");
        builder.append("Depth [");
        builder.append(this.depth);
        builder.append("] ");
        builder.append("URL [");
        builder.append(this.url());
        builder.append("]");
        return builder.toString();
        }
    }

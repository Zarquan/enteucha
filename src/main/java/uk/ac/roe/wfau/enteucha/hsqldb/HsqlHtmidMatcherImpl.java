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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
import uk.ac.roe.wfau.enteucha.api.PositionImpl;

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
    public static final int DEFAULT_DEPTH = 20 ;

    /**
     * Public constructor.
     * 
     */
    public HsqlHtmidMatcherImpl()
        {
        this(
            DEFAULT_DEPTH
           );
        }

    /**
     * Public constructor.
     * 
     */
    public HsqlHtmidMatcherImpl(final int depth)
        {
        this.depth = depth;
        this.index = new HTMindexImp(depth);
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
        //log.debug("htmid [{}][{}]", ra, dec);
        try {
            return index.lookupId(
                ra,
                dec
                );
            }
        catch (final HTMException cause)
            {
            log.error("HTMException [{}]", ra, dec, cause.getMessage());
            return INVALID_HTMID ;
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
            final Circle circle = new Circle(
                ra,
                dec,
                radius
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
            log.error("HTMException [{}]", ouch.getMessage());
            };
        return list ;
        }
    
    @Override
    public void init()
        {
        //log.debug("init()");
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
            }
        }
    
    @Override
    public void insert(final Position position)
        {
        //log.trace("insert() [{}][{}]", position.ra(), position.dec());
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
            }
        }
    
    @Override
    public Iterable<Position> matches(Position target, Double radius)
        {
        final List<Position> results = new ArrayList<Position>();
        
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
            //log.debug("preparing");
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
            //log.trace("query [{}]", query);

            //log.debug("executing");
            final ResultSet resultset = statement.executeQuery(query);
            while (resultset.next())
                {
                results.add(
                    new PositionImpl(
                        resultset.getDouble(2),                        
                        resultset.getDouble(3),                        
                        resultset.getDouble(4),                        
                        resultset.getDouble(5),                        
                        resultset.getDouble(6)                        
                        )
                    );
                }

            // TODO - Do the cz, cy, cz filtering in java not in the database. 
        
            }
        catch (SQLException ouch)
            {
            log.error("SQLException [{}]", ouch);
            }
        //log.debug("done");
        return results;
        }

    @Override
    public String info()
        {
        final StringBuilder builder = new StringBuilder(); 
        builder.append("Class [");
        builder.append(this.getClass().getSimpleName());
        builder.append("] ");
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
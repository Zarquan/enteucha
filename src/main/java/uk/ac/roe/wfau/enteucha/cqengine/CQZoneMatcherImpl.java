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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.compound.CompoundIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import com.googlecode.cqengine.quantizer.DoubleQuantizer;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;

import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.Position;
import uk.ac.roe.wfau.enteucha.api.PositionImpl;
import uk.ac.roe.wfau.enteucha.util.GenericIterable;
import uk.ac.roe.wfau.enteucha.util.IterableIteratorList;
import uk.ac.roe.wfau.enteucha.util.IteratorIterable;
import uk.ac.roe.wfau.enteucha.util.PositionFilteredIterable;
import uk.ac.roe.wfau.enteucha.util.PositionFilteredIterator;

/**
 * A CQEngine based implementation of {@link CQZoneMatcher}
 * TODO Add validation for out of range values.
 * TODO Add +360 and -360 copies for positions within margins of ra = 0 or 360.
 *  
 */
@Slf4j
public class CQZoneMatcherImpl
implements CQZoneMatcher
    {

    /**
     * Small offset to avoid divide by zero.
     * 
     */
    protected static final double epsilon = 10E-6;

    /**
     * The height in degrees of the {@link Zone}s in this set.
     * 
     */
    private double zoneheight ;

    /**
     * The height in degrees of the {@link Zone}s in this set.
     * 
     */
    public double height()
        {
        return this.zoneheight;
        }
    
    /**
     * The {@link IndexingShape} for this {@link CQZoneMatcher}.
     * 
     */
    protected IndexingShape indexing ;

    /**
     * The {@link IndexingShape} for this {@link CQZoneMatcher}.
     * 
     */
    @Override
    public IndexingShape indexing()
        {
        return this.indexing;
        }


    /**
     * Initialise this {@link CQZoneMatcher}.
     * 
     */
    public void init()
        {
        }

    /**
     * Public constructor.
     * 
     */
    public CQZoneMatcherImpl(final IndexingShape indexing, double zoneheight)
        {
        this.indexing = indexing ;
        this.zoneheight = zoneheight ;
        }

    //long zonetotal = 0 ;
    //long zonecount = 0 ;

    //long mathtotal = 0 ;
    //long mathcount = 0 ;

    //long radectotal = 0 ;
    //long radeccount = 0 ;
    
    @Override
    public Iterator<Position> matches(final Position target, final Double radius)
        {
        log.trace("matches() [{}][{}][{}]", target.ra(), target.dec(), radius);

        final IterableIteratorList<Position> list = new IterableIteratorList<Position>();  
        for (Zone zone : contains(target, radius))
            {
            list.add(
                zone.matches(
                    target,
                    radius
                    )
                );
            }
        return list.iterator();
        
        /*
         * 
        this.zonetotal = 0 ;
        this.zonecount = 0 ;

        this.mathtotal = 0 ;
        this.mathcount = 0 ;

        this.radectotal = 0 ;
        this.radeccount = 0 ;
        
        final List<Position> list = new ArrayList<Position>(100);  
        for (Zone zone : contains(target, radius))
            {
            log.trace("Checking zone [{}][{}]", zone.ident(), zone.total());
            zonecount++;
            Iterable<Position> iter = new IteratorIterable<Position>(zone.matches(target, radius));
            for (Position match : iter)
                {
                log.trace("Found match [{}][{}]", match.ra(), match.dec());
                list.add(match);
                }
            }

        //log.trace("Math cx/cy/zc compare [{}] took [{}µs][{}ns] avg [{}µs][{}ns]", (this.mathcount),  (this.mathtotal/1000),  (this.mathtotal),  (this.mathtotal/(this.mathcount * 1000)),   (this.mathtotal/this.mathcount));
        //log.trace("Zone ra/dec/pos query [{}] took [{}µs][{}ns] avg [{}µs][{}ns]", (this.radeccount), (this.radectotal/1000), (this.radectotal), (this.radectotal/(this.radeccount * 1000)), (this.radectotal/this.radeccount));
        //log.trace("Zone between found [{}] from [{}] took [{}µs][{}ns]", (zonecount), zones.size(), ((zonetotal)/1000), (zonetotal) );
        
        return list.iterator();
         * 
         */
        }
        
    @Override
    public Iterable<Zone> contains(final Position target, final Double radius)
        {
        log.trace("contains() [{}][{}][{}]", target.ra(), target.dec(), radius);
        log.trace("height [{}]",  this.zoneheight);

        final Integer min = (int) FastMath.floor(((target.dec() + 90) - radius) / this.zoneheight) ;
        final Integer max = (int) FastMath.floor(((target.dec() + 90) + radius) / this.zoneheight) ;

        log.trace("min [{}]", min);
        log.trace("max [{}]", max);

        return new GenericIterable<Zone, ZoneImpl>(
            between(
                min,
                max
                )
            );
        }
        
    /**
     * Select a {@link ResultSet} of {@link ZoneImpl}s between an upper and lower bound.  
     * 
     */
    protected ResultSet<ZoneImpl> between(final Integer min, final Integer max)
        {
        log.trace("between() [{}][{}]", min, max);
        long zonestart = System.nanoTime();
        final ResultSet<ZoneImpl> results = zones.retrieve(
            QueryFactory.between(
                CQZoneMatcherImpl.ZONE_ID,
                min,
                true,
                max,
                true
                )
            );

        long zonedone = System.nanoTime();
        long zonediff = zonedone - zonestart;
        log.trace("Zone between took [{}µs][{}ns]", ((zonediff)/1000), (zonediff) );
        return results ; 
        }

    /**
     * Select a {@link ZoneImpl} based on the {@link Zone} identifier.
     * @todo Does creating a new Zone need to be ThreadSafe ?
     * @todo Add a create flag
     *
     */
    protected Zone select(final Integer ident)
        {
        //log.trace("select() [{}]", ident);
        final Iterator<ZoneImpl> iter = zones.retrieve(
            QueryFactory.equal(
                CQZoneMatcherImpl.ZONE_ID,
                ident
                )
            ).iterator();
        if (iter.hasNext())
            {
            final ZoneImpl found = iter.next();
            return found ;
            }
        else {
            final ZoneImpl created = new ZoneImpl(
                ident
                ) ;
            zones.add(
                created
                );
            //log.trace("New zone [{}][{}]", created.ident(), zones.size());
            return created ;
            }
        }

    @Override
    public void insert(final Position position)
        {
        //log.debug("insert() [{}][{}]", position.ra(), position.dec());
        final Zone zone = select(
            (int) FastMath.floor((position.dec() + 90) / this.zoneheight)
            );
        //log.debug("Zone [{}]", zone.ident());
        zone.insert(
            position
            );
        this.total++;
        //log.debug("Added [{}][{}]", zone.total(), total());
        }

    /**
     * The total count of {@link Position}s inserted into this {@link CQZoneMatcher}.
     *
     */
    private long total = 0 ;

    /**
     * The total count of {@link Position}s inserted into this {@link CQZoneMatcher}.
     *
     */
    public long total()
        {
        return this.total;
        }

    /**
     * Our collection of {@link Zone}s, indexed by {@link ZoneImpl.ZONE_ID}. 
     * 
     */
    private final IndexedCollection<ZoneImpl> zones = new ConcurrentIndexedCollection<ZoneImpl>(
        OnHeapPersistence.onPrimaryKey(
            CQZoneMatcherImpl.ZONE_ID
            )
        );

    /**
     * The CQEngine {@link Attribute} for a {@link Zone} identifier.
     * 
     */
    public static final SimpleAttribute<ZoneImpl, Integer> ZONE_ID = new SimpleAttribute<ZoneImpl, Integer>("zone.id")
        {
        @Override
        public Integer getValue(final ZoneImpl zone, final QueryOptions options)
            {
            return zone.ident();
            }
        };

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
        builder.append("Zone height [");
        builder.append(this.zoneheight);
        builder.append("] ");

        long subcount = 0 ;
        long subtotal = 0 ;
        long maxtotal = 0 ;
        long mintotal = this.total ;
        for (Zone zone : zones)
            {
            //builder.append("Zone [");
            //builder.append(zone.ident());
            //builder.append("] ");
            subcount++;
            subtotal += zone.total();
            if (zone.total() > maxtotal)
                {
                maxtotal = zone.total();
                }
            if (zone.total() < mintotal)
                {
                mintotal = zone.total();
                }
            }
        builder.append("Zone count [");
        builder.append(subcount);
        builder.append("] ");
        builder.append("Zone size avg [");
        builder.append((subtotal/subcount));
        builder.append("]");
        builder.append(" min [");
        builder.append((mintotal));
        builder.append("]");
        builder.append(" max [");
        builder.append((maxtotal));
        builder.append("]");
        
        return builder.toString();
        }

    /**
     * A CQEngine based implementation of {@link CQZoneMatcher.Zone}
     * 
     */
    public class ZoneImpl
    implements CQZoneMatcher.Zone
        {
    
        /**
         * Protected constructor.
         * 
         */
        protected ZoneImpl(int ident)
            {
            this.ident = ident;
            this.init();
            }

        private int ident;
        @Override
        public int ident()
            {
            return this.ident;
            }

        @Override
        public Iterator<Position> matches(final Position target, final Double radius)
            {
            log.trace("Zone.matches() [{}][{}] [{}]", target.ra(), target.dec(), radius);
            Iterable<Position> iteratable = query(
                target,
                radius
                );

            Iterator<Position> iterator = iteratable.iterator();
            
            return new PositionFilteredIterator(
                iterator,
                target,
                radius
                );
            }
        
        /**
         * Query our CQEngine collection for {@link Position}s within a search radius of a target {@link Position}.
         *
         */
        protected ResultSet<Position> query(final Position target, final Double radius)
            {
            log.trace("zone [{}] query [{}][{}][{}]", this.ident, target.ra(), target.dec(), radius);

            double factor = radius / (FastMath.abs(FastMath.cos(FastMath.toRadians(target.dec()))) + epsilon);
            double minra = (target.ra() - factor);
            double maxra = (target.ra() + factor);

            double mindec = (target.dec() - radius) ; 
            double maxdec = (target.dec() + radius) ; 

            log.trace("min/max ra  [{}][{}]", minra,  maxra) ;
            log.trace("min/max dec [{}][{}]", mindec, maxdec);
/*
 *
 * TODO Make this configurable.
            return positions.retrieve(
                QueryFactory.between(
                    ZoneMatcherImpl.POS_RA,
                    minra,
                    true,
                    maxra,
                    true
                    )
                );
 *
 */        
            //long radecstart = System.nanoTime();

            final ResultSet<Position> results = positions.retrieve(
                QueryFactory.and(
                    QueryFactory.between(
                        CQZoneMatcherImpl.POS_DEC,
                        mindec,
                        true,
                        maxdec,
                        true
                        ),
                    QueryFactory.between(
                        CQZoneMatcherImpl.POS_RA,
                        minra,
                        true,
                        maxra,
                        true
                        )
                    )
                );

            //long radecdone = System.nanoTime();
            //long radecdiff = radecdone -radecstart;
            //log.trace("Found [{}]", results.size());
            //radectotal += radecdiff;
            //radeccount++;

            /*
             * 
            log.trace("--- --- --- ---");
            for(Position pos : positions)
                {
                if (
                    (pos.ra() >= minra) && (pos.ra() <= maxra)
                    &&
                    (pos.dec() >= mindec) && (pos.dec() <= maxdec)
                    )
                    {
                    log.trace("+++ [{}][{}]", pos.ra(), pos.dec());
                    }
                else {
                    log.trace("--- [{}][{}]", pos.ra(), pos.dec());
                    }
                }
            log.trace("--- --- --- ---");
             *             
             */
            
            //log.trace("Zone ra/dec query took [{}µs][{}ns]", (radecdiff/1000), (radecdiff) );
            return results;
            }

        @Override
        public void insert(final Position position)
            {
            //log.trace("insert() [{}][{}]", position.ra(), position.dec());
            if (position instanceof PositionImpl)
                {
                positions.add(
                    (PositionImpl) position
                    );
                }
            else {
                throw new IllegalArgumentException(
                    "PositionImpl expected [" + position.getClass().getName() + "]"
                    );
                }
            }
    
        /**
         * Our collection of {@link Position}s. 
         * 
         */
        private final IndexedCollection<Position> positions = new ConcurrentIndexedCollection<Position>();

        @Override
        public long total()
            {
            return positions.size();
            }

        @Override
        @SuppressWarnings("unchecked")
        public void init()
            {
            switch(CQZoneMatcherImpl.this.indexing)
                {
                case SEPARATE_SIMPLE:
                    positions.addIndex(
                        NavigableIndex.onAttribute(
                            CQZoneMatcherImpl.POS_RA
                            )
                        );
                    positions.addIndex(
                        NavigableIndex.onAttribute(
                            CQZoneMatcherImpl.POS_DEC
                            )
                        );
                    break ;

                case SEPARATE_QUANTIZED:
                    positions.addIndex(
                        NavigableIndex.withQuantizerOnAttribute(
                            DoubleQuantizer.withCompressionFactor(
                                5
                                ),
                            CQZoneMatcherImpl.POS_RA
                            )
                        );
                    positions.addIndex(
                        NavigableIndex.onAttribute(
                            CQZoneMatcherImpl.POS_DEC
                            )
                        );
                break ;
                
                case COMBINED_SIMPLE:
                    {
                    positions.addIndex(
                        CompoundIndex.onAttributes(
                            CQZoneMatcherImpl.POS_RA,
                            CQZoneMatcherImpl.POS_DEC
                            )
                        );
                    }
                    break ;
                default:
                    throw new IllegalArgumentException(
                        "Unknown indexing [{" + CQZoneMatcherImpl.this.indexing.name() + "}]"
                        ); 
                }
            }

        @Override
        public String info()
            {
            final StringBuilder builder = new StringBuilder(); 
            builder.append("Class [");
            builder.append(this.getClass().getSimpleName());
            builder.append("] ");
            builder.append("Indexing [");
            builder.append(CQZoneMatcherImpl.this.indexing.name());
            builder.append("]");
            builder.append("Total rows [");
            builder.append(String.format("%,d", this.total()));
            builder.append("]");
            return builder.toString();
            }
        }

    /**
     * The CQEngine {@link Attribute} for a {@link PositionImpl} right ascension.
     * 
     */
    public static final SimpleAttribute<Position, Double> POS_RA = new SimpleAttribute<Position, Double>("pos.ra")
        {
        @Override
        public Double getValue(final Position position, final QueryOptions options)
            {
            return position.ra();
            }
        };

    /**
     * The CQEngine {@link Attribute} for a {@link PositionImpl} declination.
     * 
     */
    public static final SimpleAttribute<Position, Double> POS_DEC = new SimpleAttribute<Position, Double>("pos.dec")
        {
        @Override
        public Double getValue(final Position position, final QueryOptions options)
            {
            return position.dec();
            }
        };
    }


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

import java.util.Iterator;

import org.apache.commons.math3.util.FastMath;
import org.springframework.beans.factory.annotation.Value;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 */
@Slf4j
public abstract class AbstractTestCase
extends TestCase
    {

    /**
     * 
     */
    public AbstractTestCase()
        {
        }

    @Value("${enteucha.test.loop:1000}")
    protected int looprepeat;    

    @Value("${enteucha.test.range.min:0}")
    int rangemin;
    @Value("${enteucha.test.range.max:1}")
    int rangemax;

    @Value("${enteucha.test.count.min:9}")
    int countmin;
    @Value("${enteucha.test.count.max:10}")
    int countmax;

    @Value("${enteucha.test.zone.min:6}")
    int zonemin;
    @Value("${enteucha.test.zone.max:9}")
    int zonemax;

    @Value("${enteucha.test.radius.min:6}")
    int radiusmin;
    @Value("${enteucha.test.radius.max:9}")
    int radiusmax;

    final Runtime runtime = Runtime.getRuntime();

    /**
     * Flag to prevent clean on the first call to outer loop.
     *  
     */
    boolean first = true ;

    /**
     * Test finding things.
     * 
     */
    public void outerloop(final Matcher.Factory factory)
        {
        if (first)
            {
            first = false;
            }
        else {
            clean();
            }
        final Position target = new PositionImpl(
            120.0,
            120.0
            );
        log.info("Target [{}][{}]", target.ra(), target.dec());

        for (int rangeexp = this.rangemax ; rangeexp >= this.rangemin ; rangeexp--)
            {
            double rangeval = FastMath.pow(2.0, rangeexp);
            //log.info("Data spread [{}]", spreadval);
            
            for (int zoneexp = this.zonemin ; zoneexp <= this.zonemax ; zoneexp++ )
                {
                double zoneheight = FastMath.pow(2.0, -zoneexp);
                final Matcher matcher = factory.create(
                    zoneheight
                    );
                matcher.insert(
                    target
                    );
                log.info("---- ----");
                for (int insertexp = 0 ; insertexp <= countmax ; insertexp++)
                    {
                    double insertnum = FastMath.pow(2.0, insertexp);
                    double traceexp = insertexp + 1 ;
                    double tracenum = FastMath.pow(2.0, traceexp)+1;
                    double tracesum = FastMath.pow(tracenum, 2.0);
                    //log.info("Insert range [2^{} = {}]",     rangeexp,  rangeval);
                    //log.info("Insert count [2^({}+1) = {}]", insertexp, insertnum);
                    log.info("Insert [{}][{}][{}]",
                        insertexp,
                        String.format("%,.0f", tracenum),
                        String.format("%,.0f", tracesum)
                        );
                    //log.info("Memory [{}][{}][{}]", humanSize(runtime.totalMemory()), humanSize(runtime.freeMemory()), humanSize(runtime.maxMemory()));
                    for (double c = -insertnum ; c <= insertnum ; c++)
                        {
                        for (double d = -insertnum ; d <= insertnum ; d++)
                            {
                            if ((((long)c) % 2) == 0)
                                {
                                if ((((long) d) % 2) == 0)
                                    {
                                    continue;
                                    }
                                }
                            matcher.insert(
                                new PositionImpl(
                                    (target.ra()  + (rangeval * (-c/insertnum))),
                                    (target.dec() + (rangeval * (+d/insertnum)))
                                    )
                                );
                            }
                        }
                    if (insertexp >= countmin)
                        {
                        log.info(
                            "Total  [{}][(2^({}+1))+1 = {}] => [{}^2 = {}]",
                            insertexp,
                            insertexp,
                            String.format("%.0f",  tracenum),
                            String.format("%.0f",  tracenum),
                            String.format("%.0f", tracesum)
                            );
                        log.info(
                            "Range  [{}][{}/{} = {}] ",
                            rangeval,
                            rangeval,
                            String.format("%.0f", tracesum),
                            String.format("%.8f", (rangeval / tracesum))
                            );
                        log.info(
                            "Zone   [{}][2^(-{}) = {}]",
                            zoneexp,
                            zoneexp,
                            zoneheight
                            );
                        log.info(
                            "Memory [{}][{}][{}]",
                            humanSize(runtime.totalMemory()),
                            humanSize(runtime.freeMemory()),
                            humanSize(runtime.maxMemory())
                            );
                        log.info(">>>>");
                        innerloop(
                            matcher,
                            target 
                            );
                        log.info("<<<<");
                        }
                    }
                }
            }
        }

    public void innerloop(final Matcher matcher, final Position target)
        {
        for (int radiusexp = this.radiusmin ; radiusexp <= this.radiusmax ; radiusexp++ )
            {
            double radiusval = FastMath.pow(2.0, -radiusexp);
            log.info("---- ----");
            log.info(
                "Radius [{}][2^(-{}) = {}]",
                radiusexp,
                radiusexp,
                radiusval
                );
            
            long looptime  = 0 ;
            long loopcount = 0 ;
            for(int loop = 0 ; loop < this.looprepeat ; loop++)
                {
                //log.debug("---- ---- ---- ----");
                //log.debug("Starting crossmatch");
                long innerstart = System.nanoTime();
                Iterator<Position> iter = matcher.matches(
                    target,
                    radiusval
                    );
                while (iter.hasNext())
                    {
                    Position pos = iter.next();
                    loopcount++;
                    }
                long innerend = System.nanoTime();
                long innertime = innerend - innerstart;
                looptime += innertime ;
                //log.debug("---- ---- ---- ----");
                //log.debug("Finished crossmatch");
                //log.debug("Found [{}] in [{}µs {}ns][{}µs {}ns][{}µs {}ns]", matchcount, (innerone/1000), innerone, (innertwo/1000), innertwo, (innertime/1000), innertime);
                //log.debug("Found [{}] in [{}ms][{}µs][{}ns]", matchcount, (innertime/1000000), (innertime/1000), innertime);
                }
            
            double average = looptime/this.looprepeat; 

            log.info(
                matcher.info()
                );
            log.info(
                "Searched [{}] radius [{}] found [{}] in [{}] loops, total [{}s][{}ms], average [{}ms][{}µs][{}ns] {}",
                String.format("%,d", matcher.total()),
                radiusval,
                (loopcount/this.looprepeat),
                this.looprepeat,
                
                String.format("%,d", (looptime/1000000000)),
                String.format("%,d", (looptime/1000000)),
                
                String.format("%,.3f", (average/1000000)),
                String.format("%,.3f", (average/1000)),
                String.format("%,.0f", (average)),
                
                (((average) < 1000000) ? "PASS" : "FAIL")
                );
            }
        }

    /**
     * Format a data size as a human readable String.
     * https://programming.guide/java/formatting-byte-size-to-human-readable-format.html 
     * 
     */
    public static String humanSize(long bytes)
        {
        return humanSize(bytes, false);
        }
    
    /**
     * Format a number as a human readable String.
     * https://programming.guide/java/formatting-byte-size-to-human-readable-format.html 
     * 
     */
    public static String humanSize(long value, boolean si)
        {
        int unit = (si) ? 1000 : 1024;
        if (value < unit) return value + " B";
        int exponent = (int) (Math.log(value) / Math.log(unit));
        final String prefix = (si ? "kMGTPE" : "KMGTPE").charAt(exponent-1) + (si ? "" : "i");
        return String.format("%.1f %sB", value / Math.pow(unit, exponent), prefix);
        }

    
    /**
     * Run finalise and gc.
     *  
     */
    public void clean()
        {
        try {
            log.info("---- Finalize");
            System.runFinalization();
            Thread.sleep(10000);
            }
        catch (final InterruptedException ouch)
            {
            log.debug("InterruptedException [{}]", ouch);
            }
        try {
            log.info("---- Running gc");
            System.gc();
            Thread.sleep(10000);
            }
        catch (final InterruptedException ouch)
            {
            log.debug("InterruptedException [{}]", ouch);
            }
        }
    }

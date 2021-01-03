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
import java.util.Random;

import org.apache.commons.math3.util.FastMath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.hsqldb.HsqlZoneMatcherTestCase;

/**
 *
 *
 */
@Slf4j
@Component
public abstract class AbstractTestCase
extends TestCase
    {
    /**
     * Public constructor.
     *
     */
    public AbstractTestCase()
        {
        }

    /**
     * The target loop count.
     *
     */
    @Value("${enteucha.loop:1000}")
    protected int looprepeat;

    /**
     * The minimum range exponent.
     *
     */
    @Value("${enteucha.range.min:0}")
    protected int rangemin;

    /**
     * The maximum range exponent.
     *
     */
    @Value("${enteucha.range.max:1}")
    protected int rangemax;

    /**
     * The range exponent, ({@link rangemax} .. {@link rangemin}}).
     *
     */
    protected int rangeexp;

    /**
     * The range value, 2^(-{@link rangeexp}).
     *
     */
    protected double rangeval;

    @Value("${enteucha.select.min:9}")
    protected int selectmin;

    @Value("${enteucha.insert.max:10}")
    protected int selectmax;

    /**
     * The minimum zone exponent.
     *
     */
    @Value("${enteucha.zone.min:6}")
    protected int zonemin;

    /**
     * The maximum zone exponent.
     *
     */
    @Value("${enteucha.zone.max:9}")
    protected int zonemax;

    /**
     * The zone exponent, ({@link zonemin} .. {@link zonemax}}).
     *
     */
    protected int zoneexp;

    /**
     * The zone size, 2^(-{@link zoneexp}).
     *
     */
    protected double zoneval;

    /**
     * The minimum radius exponent.
     *
     */
    @Value("${enteucha.radius.min:6}")
    protected int radiusmin;

    /**
     * The maximum radius exponent.
     *
     */
    @Value("${enteucha.radius.max:9}")
    protected int radiusmax;

    /**
     * The radius exponent, ({@link rangemax} .. {@link rangemin}}).
     *
     */
    protected int radiusexp;

    /**
     * The radius, 2^(-{@link radiusexp}).
     *
     */
    protected double radiusval;


    final Runtime runtime = Runtime.getRuntime();

    /**
     * Flag to prevent clean on the first call to outer loop.
     *
     */
    boolean first = true ;

    /**
     * The target ra in degrees.
     *
     */
    @Value("${enteucha.target.ra:120}")
    protected double targetra;

    /**
     * The target decin degrees.
     *
     */
    @Value("${enteucha.target.dec:-10}")
    protected double targetdec;

    /**
     * The target position we are trying to crossmacth.
     *
     */
    private Position target = null;

    /**
     * Our random offset generator.
     *
     */
    private Random random = new Random();

    /**
     * Generate a random offset +/- the jitter value.
     *
     */
    protected double random(double jitter)
        {
        return (this.random.nextDouble() - 0.5) * jitter ;
        }


    /**
     * Initialse our test.
     *
     */
    @Before
    public void init()
        {
        this.target = new PositionImpl(
            targetra,
            targetdec
            );
        }

    /**
     * Run our test.
     *
     */
    public abstract void test();

    /**
     * Test finding things.
     *
     */
    public void outerloop(final Matcher.Factory factory)
        {
/*
        if (first)
            {
            startup(factory);
            first = false;
            }
 */
        clean();
        log.info("Target [{}][{}]", target.ra(), target.dec());
        log.info("<outerloop target='{},{}'>", target.ra(), target.dec());
        // TODO Move the range loop inside the matrix insert plugin.
        for (this.rangeexp = this.rangemax ; this.rangeexp >= this.rangemin ; this.rangeexp--)
            {
            this.rangeval = FastMath.pow(2.0, this.rangeexp);

            //TODO Move the zone loop to ZoneMatcherTestBase
            for (this.zoneexp = this.zonemin ; this.zoneexp <= this.zonemax ; this.zoneexp++ )
                {
                this.zoneval = FastMath.pow(2.0, -this.zoneexp );
                Matcher matcher = startup(factory);
                log.info("---- ----");
                //TODO Move the insert loop to a plugin (matrix, votable ..)
                for (int insertexp = 0 ; insertexp <= selectmax ; insertexp++)
                    {
                    double insertnum = FastMath.pow(2.0, insertexp);
                    double traceexp = insertexp + 1 ;
                    double tracenum = FastMath.pow(2.0, traceexp)+1;
                    double tracesum = FastMath.pow(tracenum, 2.0);
                    double jitter   = rangeval/tracenum;

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
                                    (target.ra()  + (rangeval * (-c/insertnum)) + this.random(jitter)),
                                    (target.dec() + (rangeval * (+d/insertnum)) + this.random(jitter))
                                    )
                                );
                            }
                        }
                    if (insertexp >= selectmin)
                        {
                        log.info(
                            "Total  [{}][(2^({}+1))+1 = {}] => [{}^2 = {}]",
                            insertexp,
                            insertexp,
                            String.format("%.0f", tracenum),
                            String.format("%.0f", tracenum),
                            String.format("%.0f", tracesum)
                            );
                        log.info(
                            "Range  [{}][2^{} = {}] [{}/{} = {}] ",
                            this.rangeexp,
                            this.rangeexp,
                            this.rangeval,
                            this.rangeval,
                            String.format("%.0f", tracenum),
                            String.format("%.8f", (rangeval / tracenum))
                            );
                        log.info(
                            "Zone   [{}][2^(-{}) = {}]",
                            this.zoneexp,
                            this.zoneexp,
                            this.zoneval
                            );
                        log.info(
                            "span/zone [{} / {}] = [{}]",
                            String.format("%.4f", (rangeval / tracenum)),
                            String.format("%.4f", this.zoneval),
                            String.format("%.4f", ((rangeval / tracenum) / this.zoneval))
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
        log.info("</outerloop>");
        }

    public void innerloop(final Matcher matcher, final Position target)
        {
        for (this.radiusexp = this.radiusmin ; radiusexp <= this.radiusmax ; radiusexp++ )
            {
            this.radiusval = FastMath.pow(2.0, -radiusexp);
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
                    //log.debug("-- [{}][{}][{}]", loopcount, pos.ra(), pos.dec());
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

//
// TODO Checksum the ra and dec values to compare between different runs ?
// TODO Make pass/fail configurable.
// TODO Table output for analysis in TopCat
// TODO Concurrent searches on a static dataset.
//

            log.info(
                matcher.info()
                );
            log.info(
              //"Searched [{}] radius [{}] found [{}] in [{}] loops, total [{}s][{}ms], average [{}ms][{}µs] {}",
                "Searched [{}] range [{}] zone [{}] radius [{}] found [{}] in [{}] loops, total [{}s][{}ms], average [{}ms][{}us] {}",
                String.format("%,d", matcher.total()),
                rangeval,
                zoneval,
                radiusval,
                String.format("%,d", (loopcount/this.looprepeat)),
                String.format("%,d", this.looprepeat),

                String.format("%,d", (looptime/1000000000)),
                String.format("%,d", (looptime/1000000)),

                String.format("%,.3f", (average/1000000)),
                String.format("%,.3f", (average/1000)),

                (((average) < 1000000) ? "PASS" : "FAIL")
                );

            log.info(
                  "<data-row><matcher>{}</matcher><index>{}</index><points>{}</points><range>{}</range><zone>{}</zone><radius>{}</radius><found>{}</found><repeat>{}</repeat><timesum>{}</timesum><timeavg>{}</timeavg></data-row>",
                  matcher.type(),
                  matcher.index(),
                  String.format("%d", matcher.total()),
                  rangeval,
                  zoneval,
                  radiusval,
                  String.format("%d", (loopcount/this.looprepeat)),
                  String.format("%d", this.looprepeat),
                  String.format("%d", (looptime/1000000)),
                  String.format("%.3f", (average/1000000))
                  );
            }
        }

    protected Matcher startup(final Matcher.Factory factory)
        {
        log.debug("startup ----");
        Matcher matcher = factory.create();
        matcher.insert(
            this.target
            );
        Iterator<Position> iter = matcher.matches(
            this.target,
            0.0
            );
        while (iter.hasNext())
            {
            Position pos = iter.next();
            log.debug("-- [{}][{}][{}]", pos.ra(), pos.dec());
            }
        return matcher;
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
        return String.format("%.1f%sB", value / Math.pow(unit, exponent), prefix);
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
            Thread.sleep(100);
            }
        catch (final InterruptedException ouch)
            {
            log.debug("InterruptedException [{}]", ouch);
            }
        try {
            log.info("---- Running gc");
            System.gc();
            Thread.sleep(100);
            }
        catch (final InterruptedException ouch)
            {
            log.debug("InterruptedException [{}]", ouch);
            }
        }

    /**
     * Public testmain() method.
     *
     */
    public static void testmain(final Class<?> clazz)
        {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("component-config.xml");

        AbstractTestCase target = (AbstractTestCase) context.getBean(clazz);
        target.init();
        target.test();

        context.close();
        }
    }

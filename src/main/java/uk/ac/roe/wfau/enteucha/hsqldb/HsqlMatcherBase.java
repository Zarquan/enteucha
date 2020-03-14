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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 */
@Slf4j
public class HsqlMatcherBase
    {
    /**
     * Public constructor.
     * 
     */
    public HsqlMatcherBase()
        {
        }
    
    /**
     * The matcher database type.
     * 
     */
    protected String databasetype = "mem";
    protected String databasetype()
        {
        return this.databasetype.trim();
        }

    /**
     * The matcher database host.
     * 
     */
    protected String databasehost = "localhost";
    protected String databasehost()
        {
        return this.databasehost.trim();
        }

    /**
     * The matcher database port.
     * 
     */
    protected String databaseport = "9001" ;
    protected String databaseport()
        {
        return this.databaseport.trim();
        }
    
    /**
     * The matcher database name.
     * 
     */
    protected String databasename = "zonematch";
    protected String databasename()
        {
        return this.databasename.trim();
        }

    /**
     * The matcher database user name.
     * 
    @Value("${databaseuser:}")
    protected String databaseuser;
    protected String databaseuser()
        {
        return this.databaseuser.trim();
        }
     */

    /**
     * The matcher database password.
     * 
    @Value("${databasepass:}")
    protected String databasepass;
    protected String databasepass()
        {
        return this.databasepass.trim();
        }
     */

    /**
     * Generate our database connection url.
     * 
     */
    public String url()
        {
        final StringBuilder builder = new StringBuilder(
            "jdbc:hsqldb"
            ); 

        if ("mem".equals(this.databasetype()))
            {
            builder.append(":mem:");
            builder.append(this.databasename());
            }
        else {
            log.error("Unknown database type [{}]",
                this.databasetype()
                );
            throw new UnsupportedOperationException(
                "Unknown database type [" + this.databasetype() + "]"
                );
            }
        log.trace("url() [{}]", builder.toString());
        return builder.toString();
        }

    /**
     * Our JDBC {@link DataSource}.
     *
     */
    private DataSource source ;

    /**
     * Our JDBC {@link Driver}.
     *
     */
    protected Driver driver()
        {
        return new org.hsqldb.jdbc.JDBCDriver();
        }

    /**
     * Connect our {@link DataSource}.
     * 
     */
    protected DataSource source()
        {
        log.trace("source()");
        log.trace(" databasehost [{}]", databasehost());
        log.trace(" databaseport [{}]", databaseport());
        log.trace(" databasename [{}]", databasename());
        //log.debug(" databaseuser [{}]", databaseuser());
        //log.debug(" databasepass [{}]", databasepass());
        if (null == this.source)
            {
            this.source = new SimpleDriverDataSource(
                this.driver(),
                this.url()//,
                //this.databaseuser(),
                //this.databasepass()
                );            
            }
        return this.source;
        }

    /**
     * Our database connection.
     * 
     */
    private Connection connection ;

    /**
     * Our database connection.
     * 
     */
    protected Connection connection()
        {
        return this.connection;
        }

    /**
     * Connect our {@link DataSource}.
     * @throws SQLException 
     * 
     */
    protected void connect()
    throws SQLException
        {
        try {
            if (null == this.connection)
                {
                this.connection = this.source().getConnection();
                }
            }
        catch (SQLException ouch)
            {
            log.error("SQLException closing connection [{}]", ouch);
            throw ouch;
            }
        }

    /**
     * Shutdown our database connection.
     * @throws SQLException 
     * 
     */
    public void close()
    throws SQLException
        {
        try {
            if (this.connection != null)
                {
                this.connection.close();
                }
            }
        catch (SQLException ouch)
            {
            log.error("SQLException closing connection [{}]", ouch);
            throw ouch;
            }
        finally {
            this.connection = null ;
            }
        }
    }

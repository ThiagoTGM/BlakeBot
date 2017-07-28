/*
 * This file is part of BlakeBot.
 *
 * BlakeBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlakeBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BlakeBot. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.thiagotgm.blakebot.module.status;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.ResumedEvent;

/**
 * Keeps track of uptime and downtime stats for the bot.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-07-28
 */
public class UptimeTracker {
    
    private static final Logger LOG = LoggerFactory.getLogger( UptimeTracker.class );
    
    private static final File UPTIME_FILE = Paths.get( "uptimes.log" ).toFile();
    private static final File DOWNTIME_FILE = Paths.get( "downtimes.log" ).toFile();
    private static final File CONNECTION_FILE = Paths.get( "connection.log" ).toFile();
    private static final String CONNECTION_LOG_SEPARATOR = " : ";
    
    private static final long INITIAL_DISCONNECT_TIME = -2;
    private static final long NO_TIME = -1;

    private long connectTime;
    private final TimeData uptimes;
    private long disconnectTime;
    private final TimeData downtimes;
    
    private final Writer connectionOutput;
    
    private static UptimeTracker instance;
    
    /**
     * Initializes a new instance.
     */
    private UptimeTracker() {
        
        connectTime = NO_TIME;
        disconnectTime = INITIAL_DISCONNECT_TIME;
        
        boolean logUptimes = true; // TODO: Make this a setting
        boolean logDowntimes = true;
        boolean logConnection = true;
        
        uptimes = new TimeData( "uptime", ( logUptimes ) ? UPTIME_FILE : null );
        downtimes = new TimeData( "downtime", ( logDowntimes ) ? DOWNTIME_FILE : null );
        
        if ( logConnection ) { // Should log connections.
            Writer output = null;
            LOG.info( "Logging connection events." );
            try { // Try to get output file.
                output = new FileWriter( CONNECTION_FILE );
            } catch ( IOException e ) {
                LOG.error( "Could not open connection log file.", e );
                output = null;
            } finally {
                connectionOutput = output;
            }
        } else { // Do not log.
            connectionOutput = null;
        }
        
    }
    
    /**
     * Returns the running instance of this class.
     * <p>
     * If there isn't one, creates one.
     *
     * @return The class instance.
     */
    public synchronized static UptimeTracker getInstance() {
        
        if ( instance == null ) {
            instance = new UptimeTracker();
        }
        return instance;
        
    }
    
    /**
     * Logs a connection event, if an output was specified on construction.
     *
     * @param event The event to be logged.
     * @param time The time that it happened.
     */
    private void logConnectionEvent( Event event, long time ) {
        
        if ( connectionOutput != null ) {
            StringBuilder builder = new StringBuilder();
            builder.append( time );
            builder.append( CONNECTION_LOG_SEPARATOR );
            if ( event instanceof DisconnectedEvent ) {
                builder.append( "Disconnected" );
                builder.append( CONNECTION_LOG_SEPARATOR );
                builder.append( ( (DisconnectedEvent) event ).getReason() );
            } else if ( event instanceof ReadyEvent ) {
                builder.append( "Connected" );
            } else if ( event instanceof ResumedEvent ) {
                builder.append( "Reconnected" );
            } else {
                builder.append( "<?>" );
            }
            try {
                connectionOutput.write( builder.toString() );
            } catch ( IOException e ) {
                LOG.error( "Could not write to connection log file." );
            }
        }
        
    }
    
    /**
     * Detects that the bot was disconnected.
     *
     * @param event The event fired.
     */
    @EventSubscriber
    public synchronized void disconnected( DisconnectedEvent event ) {
        
        disconnectTime = System.currentTimeMillis();
        LOG.debug( "Disconnected by {}.", event.getReason() );
        logConnectionEvent( event, disconnectTime );
        
        if ( connectTime == NO_TIME ) { // Was not connected.
            LOG.warn( "Disconnected without being connected." );
        } else { // Was currently connected.
            long uptime = disconnectTime - connectTime;
            
            if ( LOG.isInfoEnabled() ) {
                LOG.info( "Disconnected after " + new Time( uptime ).toString( false ) );
            }
            
            uptimes.recordTime( uptime );
            
            connectTime = NO_TIME;
        }
        
    }
    
    /**
     * Detects that the bot was connected.
     *
     * @param event The event fired.
     */
    @EventSubscriber
    public synchronized void connected( ReadyEvent event ) {
        
        connectTime = System.currentTimeMillis();
        logConnectionEvent( event, connectTime );
        
        if ( disconnectTime == NO_TIME ) { // Was not disconnected.
            LOG.warn( "Connected without being disconnected." );
        } else if ( disconnectTime == INITIAL_DISCONNECT_TIME ) {
            LOG.debug( "Connecting for the first time." );
            disconnectTime = NO_TIME;
        } else { // Was currently disconnected.
            long downtime = connectTime - disconnectTime;
            
            if ( LOG.isInfoEnabled() ) {
                LOG.info( "Connected after " + new Time( downtime ).toString( false ) );
            }
            
            downtimes.recordTime( downtime );
            
            disconnectTime = NO_TIME;
        }
        
    }
    
    /**
     * Detects that the bot was reconnected.
     *
     * @param event The event fired.
     */
    @EventSubscriber
    public synchronized void resumed( ResumedEvent event ) {
        
        connectTime = System.currentTimeMillis();
        logConnectionEvent( event, connectTime );
        
        if ( disconnectTime == NO_TIME ) { // Was not disconnected.
            LOG.warn( "Connected without being disconnected." );
        } else { // Was currently disconnected.
            long downtime = connectTime - disconnectTime;
            
            if ( LOG.isInfoEnabled() ) {
                LOG.info( "Reconnected after " + new Time( downtime ).toString( false ) );
            }
            
            downtimes.recordTime( downtime );
            
            disconnectTime = NO_TIME;
        }
        
    }
    
    /* Methods for retrieving uptimes */
    
    /**
     * Calculates the current uptime of the bot.
     *
     * @return The current uptime, in milliseconds. If not currently connected, returns 0.
     */
    private long currentUptime() {
        
        return ( connectTime >= 0 ) ? System.currentTimeMillis() - connectTime : 0;
        
    }
    
    /**
     * Retrieves the current uptime of the bot.
     *
     * @return The current uptime. If not currently connected, returns a time interval of 0.
     */
    public synchronized Time getCurrentUptime() {
        
        return new Time( currentUptime() );
        
    }
    
    /**
     * Retrieves the total uptime of the bot.
     *
     * @return The total uptime (including the current one, if any).
     */
    public synchronized Time getTotalUptime() {
        
        return new Time( uptimes.getTotal() + currentUptime() );
        
    }
    
    /**
     * Retrieves the smallest uptime of the bot.
     *
     * @return The smallest uptime.
     */
    public synchronized Time getMinimumUptime() {
        
        return uptimes.getMinimum();
        
    }
    
    /**
     * Retrieves the largest uptime of the bot.
     *
     * @return The largest uptime.
     */
    public synchronized Time getMaximumUptime() {
        
        return uptimes.getMaximum();
        
    }
    
    /**
     * Retrieves the mean uptime of the bot.
     *
     * @return The mean uptime.
     */
    public synchronized Time getMeanUptime() {
        
        return uptimes.getMean();
        
    }
    
    /**
     * Retrieves the median uptime of the bot.
     *
     * @return The median uptime.
     */
    public synchronized Time getMedianUptime() {
        
        return uptimes.getMedian();
        
    }
    
    /* Methods for retrieving downtimes */
    
    /**
     * Calculates the current downtime of the bot.
     *
     * @return The current downtime, in milliseconds. If not currently disconnected, returns 0.
     */
    private long currentDowntime() {
        
        return ( disconnectTime >= 0 ) ? System.currentTimeMillis() - disconnectTime : 0;
        
    }
    
    /**
     * Retrieves the current downtime of the bot.
     *
     * @return The current downtime. If not currently disconnected, returns a time interval of 0.
     */
    public synchronized Time getCurrentDowntime() {
        
        return new Time( currentDowntime() );
        
    }
    
    /**
     * Retrieves the total downtime of the bot.
     *
     * @return The total downtime (including the current one, if any).
     */
    public synchronized Time getTotalDowntime() {
        
        return new Time( downtimes.getTotal() + currentDowntime() );
        
    }
    
    /**
     * Retrieves the smallest downtime of the bot.
     *
     * @return The smallest downtime.
     */
    public synchronized Time getMinimumDowntime() {
        
        return downtimes.getMinimum();
        
    }
    
    /**
     * Retrieves the largest downtime of the bot.
     *
     * @return The largest downtime.
     */
    public synchronized Time getMaximumDowntime() {
        
        return downtimes.getMaximum();
        
    }
    
    /**
     * Retrieves the mean downtime of the bot.
     *
     * @return The mean downtime.
     */
    public synchronized Time getMeanDowntime() {
        
        return downtimes.getMean();
        
    }
    
    /**
     * Retrieves the median downtime of the bot.
     *
     * @return The median downtime.
     */
    public synchronized Time getMedianDowntime() {
        
        return downtimes.getMedian();
        
    }
    
    /* Other stuff */
    
    /**
     * Retrieves the amount of times that the bot lost connection.
     *
     * @return The amount of disconnects.
     */
    public int getDisconnectAmount() {
        
        return uptimes.getAmount();
        
    }
    
    /**
     * Class that keeps track of a set of time intervals.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-07-28
     */
    private class TimeData {
        
        private final String errorString;
        private final Writer output;
        
        private final List<Long> sortedTimes;
        private long total;
        
        /**
         * Constructs a data set with the given name, that logs data to the given output file.
         *
         * @param name The name of the data set.
         * @param outputFile The file to log data to. If null, will not be logged.
         */
        public TimeData( String name, File outputFile ) {
            
            this.errorString = "Could not write to " + name + " log file.";

            if ( outputFile != null ) { // Log to a file.
                Writer output = null;
                LOG.info( "Logging {}.", name );
                try { // Try to get output file.
                    output = new FileWriter( outputFile );
                } catch ( IOException e ) {
                    LOG.error( "Could not open " + name + " log file.", e );
                    output = null;
                } finally {
                    this.output = output;
                }
            } else { // No file to log to.
                this.output = null;
            }
            
            sortedTimes = new ArrayList<>();
            total = 0;
            
        }
        
        /**
         * Records a time interval into this data set.
         *
         * @param time The time interval to be recorded.
         */
        public void recordTime( long time ) {
            
            /* Output to file if necessary */
            if ( output != null ) {
                try {
                    output.write( String.valueOf( time ) );
                } catch ( IOException e ) {
                    LOG.error( errorString, e );
                }
            }
            
            /* Add to sorted time list */
            int i = 0;
            while ( ( i < sortedTimes.size() ) && ( time > sortedTimes.get( i ) ) ) {
                
                i++; // Find position for the time being inserted.
                    
            }
            sortedTimes.add( i, time );
            
            /* Add to total time */
            total += time;
            
        }
        
        /**
         * Retrieves the total amount of time recorded in this data set.
         *
         * @return The total time, in milliseconds.
         */
        public long getTotal() {
            
            return total;
            
        }
        
        /**
         * Retrieves the smallest time interval recorded.
         *
         * @return The smallest time. If there is no recorded time, returns the time interval 0.
         */
        public Time getMinimum() {
            
            if ( sortedTimes.size() == 0 ) {
                return new Time( 0 ); // No recorded time.
            }
            return new Time( sortedTimes.get( 0 ) );
            
        }
        
        /**
         * Retrieves the largest time interval recorded.
         *
         * @return The largest time. If there is no recorded time, returns the time interval 0.
         */
        public Time getMaximum() {
            
            if ( sortedTimes.size() == 0 ) {
                return new Time( 0 ); // No recorded time.
            }
            return new Time( sortedTimes.get( sortedTimes.size() - 1 ) );
            
        }
        
        /**
         * Retrieves the mean time interval of this data set.
         *
         * @return The mean. If there is no recorded time, returns the time interval 0.
         */
        public Time getMean() {
            
            if ( sortedTimes.size() == 0 ) {
                return new Time( 0 ); // No recorded time.
            }
            return new Time( total / sortedTimes.size() );
            
        }
        
        /**
         * Retrieves the median time interval of this data set.
         *
         * @return The median. If there is no recorded time, returns the time interval 0.
         */
        public Time getMedian() {
            
            if ( sortedTimes.size() == 0 ) {
                return new Time( 0 ); // No recorded time.
            }
            int middle = sortedTimes.size() / 2;
            long median;
            if ( ( sortedTimes.size() % 2 ) == 1 ) { // Odd number of times.
                median = sortedTimes.get( middle );
            } else { // Even number of times.
                median = ( sortedTimes.get( middle - 1 ) + sortedTimes.get( middle ) ) / 2;
            }
            return new Time( median );
            
        }
        
        /**
         * Retrieves the amount of time intervals stored in this data set.
         *
         * @return The amount of time intervals.
         */
        public int getAmount() {
            
            return sortedTimes.size();
            
        }
        
    }

}
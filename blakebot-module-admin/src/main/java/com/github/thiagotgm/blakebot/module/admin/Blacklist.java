package com.github.thiagotgm.blakebot.module.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Class that keeps record of the blacklist for all servers.<br>
 * Uses a Singleton pattern.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-02-07
 */
public class Blacklist {
    
    private static final String FILENAME = "Blacklist.xml";
    private static final String FILEPATH = Paths.get( "data" ).toString();
    private static final String PATH = Paths.get( FILEPATH, FILENAME ).toString();
    private static final String ROOT_TAG = "blacklist";
    private static final String RESTRICTION_TAG = "restriction";
    private static final String GUILD_TAG = "guild";
    private static final String CHANNEL_TAG = "channel";
    private static final String USER_TAG = "user";
    private static final String NAME_ATTRIBUTE = "name";
    
    private static final Logger log = LoggerFactory.getLogger( Blacklist.class );
    
    private final Document document;
    private final Element root;
    
    private static Blacklist instance;
    
    /**
     * Creates a new instance using data loaded from the blacklist file.<br>
     * If the file doesn't exist, starts a new document.
     */
    private Blacklist() {
        
        Document document = loadDocument();
        this.document = ( document == null ) ? newDocument() : document;
        this.root = this.document.getRootElement(); 
        
    }
    
    /**
     * Returns the current instance. If there isn't one, creates it.
     *
     * @return The Blacklist instance.
     */
    public static Blacklist getInstance() {
        
        if ( instance == null ) {
            instance = new Blacklist();
        }
        return instance;
        
    }
    
    /**
     * Creates a new document to represent the blacklist.
     *
     * @return The newly created document.
     */
    private Document newDocument() {
        
        Document document = DocumentHelper.createDocument();
        document.addElement( ROOT_TAG );
        return document;
        
    }
    
    /**
     * Loads an existing blacklist document.
     *
     * @return The loaded document if it exists, or null if it doesn't exist or
     *         could not be read.
     */
    private synchronized Document loadDocument() {
        
        File inputFile = new File( PATH );
        if ( !inputFile.exists() ) {
            return null;
        }
        SAXReader reader = new SAXReader();
        try {
            return reader.read( inputFile );
        } catch ( DocumentException e ) {
            log.error( "Failed to read blacklist document.", e );
            return null;
        }
        
    }
    
    /**
     * Writes blacklist document to file.
     */
    private synchronized void saveDocument() {
        
        try {
            FileOutputStream  output = new FileOutputStream( new File( PATH ) );
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter( output, format );
            writer.write( document );
        } catch ( FileNotFoundException e ) {
            log.error( "Could not create or open blacklist file.", e );
        } catch ( UnsupportedEncodingException e ) {
            log.error( "Could not create XML writer.", e );
        } catch ( IOException e ) {
            log.error( "Could not write to blacklist file.", e );
        }
        
    }
    
    /**
     * Retrieves the child of a given element that has a given tag (name) and has
     * a given "name" attribute. If the child doesn't exist, returns the parent.
     *
     * @param parent Parent of the desired element.
     * @param childTag Tag (name) of the desired Element.
     * @param childId "name" attribute of the desired Element.
     * @return The child of parent with specified id/name and "name" attribute.
     *         If it doesn't exist, returns parent.
     */
    private Element getChild( Element parent, String childTag, String childId ) {
        
        Element found = parent;
        for ( Element candidate : parent.elements( childTag ) ) {
            
            if ( childId.equals( candidate.attributeValue( NAME_ATTRIBUTE ) ) ) {
                found = candidate;
                break;
            }
            
        }
        return found;
        
    }
    
    /**
     * Recursively obtains a list of all the restrictions that applies to a given
     * element (eg all restrictions under that element and its parents).
     *
     * @param element The element to get restrictions for.
     * @return The list of all restrictions that apply for that element,
     *         or an empty list if the element is the root element.
     */
    private List<String> getRestrictions( Element element ) {
        
        if ( element == root ) {
            return new LinkedList<>();
        }
        
        List<String> restrictions = getRestrictions( element.getParent() );
        for ( Element restriction : element.elements( RESTRICTION_TAG ) ) {
            
            restrictions.add( restriction.getText() );
            
        }
        
        return restrictions;
        
    }
    
    /**
     * Retrieves the Element that corresponds to a given Guild, or the root element
     * if it doesn't exist.
     *
     * @param guild Desired Guild.
     * @return The Element that corresponds to that Guild, or root if it doesn't
     *         exist.
     */
    private Element getElement( IGuild guild ) {
        
        return getChild( root, GUILD_TAG, guild.getID() );
        
    }
    
    /**
     * Retrieves all the restrictions that apply for a given Guild.
     *
     * @param guild Desired Guild.
     * @return The restrictions that apply for that Guild.
     */
    public List<String> getRestrictions( IGuild guild ) {
        
        return getRestrictions( getElement( guild ) );
        
    }
    
    /**
     * Retrieves the Element that corresponds to a given Channel, or the closest
     * element (Guild or root) if it doesn't exist.
     *
     * @param channel Desired Channel.
     * @return the Element that corresponds to that Channel, or the closest
     *         element (Guild or root) if it doesn't exist.
     */
    private Element getElement( IChannel channel ) {
        
        return getChild( getElement( channel.getGuild() ), CHANNEL_TAG, channel.getID() );
        
    }
    
    /**
     * Retrieves all the restrictions that apply for a given Channel
     * (including Guild-wide restrictions).
     *
     * @param channel Desired Channel.
     * @return The restrictions that apply for that Channel.
     */
    public List<String> getRestrictions( IChannel channel ) {
        
        return getRestrictions( getElement( channel ) );
        
    }
    
    /**
     * Retrieves the Element that corresponds to a given User in a given Channel,
     * or the closest element (Channel, Guild or root) if it doesn't exist.
     *
     * @param user Desired User.
     * @param channel Channel the User is in.
     * @return the Element that corresponds to that User in that Channel, or the
     *         closest element (Channel, Guild or root) if it doesn't exist.
     */
    private Element getElement( IUser user, IChannel channel ) {
        
        return getChild( getElement( channel ), USER_TAG, user.getID() );
        
    }
    
    /**
     * Retrieves all the restrictions that apply for a given User, in a given
     * channel (including Channel and Guild-wide restrictions).
     *
     * @param user Desired User.
     * @param channel Channel the user is in.
     * @return The restrictions that apply for that User.
     */
    public List<String> getRestrictions( IUser user, IChannel channel ) {
        
        return getRestrictions( getElement( user, channel ) );
        
    }
    
    /**
     * Retrieves the child of a given element that has a given tag (name) and has
     * a given "name" attribute. If the child doesn't exist, creates it.
     *
     * @param parent Parent of the desired element.
     * @param childTag Tag (name) of the desired Element.
     * @param childId "name" attribute of the desired Element.
     * @return The child of parent with specified id/name and "name" attribute.
     */
    private Element getOrCreateChild( Element parent, String childTag, String childId ) {
        
        Element child = getChild( parent, childTag, childId );
        if ( child == parent ) {
            child = parent.addElement( childTag );
            child.addAttribute( NAME_ATTRIBUTE, childId );
        }
        return child;
        
    }
    
    /**
     * Adds a restriction to a given element, if it does not contain that restriction yet.
     *
     * @param restriction Restriction to be added.
     * @param element Element to add the restriction to.
     * @return True if the restriction was added successfully. False if the restriction was
     *         already present in that element.
     */
    private boolean addRestriction( String restriction, Element element ) {
        
        for ( Element existent : element.elements( RESTRICTION_TAG ) ) {
            if ( existent.getText().equals( restriction ) ) {
                return false;
            }
        }
        element.addElement( RESTRICTION_TAG ).setText( restriction );
        saveDocument();
        return true;
        
    }
    
    /**
     * Retrieves the Element that corresponds to a given Guild. Creates it if it doesn't exist.
     *
     * @param guild Desired Guild.
     * @return The Element that corresponds to that Guild.
     */
    private Element getOrCreateElement( IGuild guild ) {
        
        return getOrCreateChild( root, GUILD_TAG, guild.getID() );
        
    }
    
    /**
     * Adds a restriction to the given Guild.
     *
     * @param restriction Restriction to be added.
     * @param guild Guild to add the restriction to.
     * @return True if the restriction was added successfully. False if the restriction was
     *         already present for that Guild.
     */
    public boolean addRestriction( String restriction, IGuild guild ) {
        
        return addRestriction( restriction, getOrCreateElement( guild ) );
        
    }
    
    /**
     * Retrieves the Element that corresponds to a given Channel. Creates it if it doesn't exist.
     *
     * @param channel Desired Channel.
     * @return The Element that corresponds to that Channel.
     */
    private Element getOrCreateElement( IChannel channel ) {
        
        return getOrCreateChild( getElement( channel.getGuild() ), CHANNEL_TAG, channel.getID() );
        
    }
    
    /**
     * Adds a restriction to the given Channel.
     *
     * @param restriction Restriction to be added.
     * @param channel Channel to add the restriction to.
     * @return True if the restriction was added successfully. False if the restriction was
     *         already present for that Channel.
     */
    public boolean addRestriction( String restriction, IChannel channel ) {
        
        return addRestriction( restriction, getOrCreateElement( channel ) );
        
    }
    
    /**
     * Retrieves the Element that corresponds to a given User in a given Channel. Creates it if
     * it doesn't exist.
     *
     * @param user Desired User.
     * @param channel Channel the user is in.
     * @return the Element that corresponds to that User in that Channel.
     */
    private Element getOrCreateElement( IUser user, IChannel channel ) {
        
        return getOrCreateChild( getElement( channel ), USER_TAG, user.getID() );
        
    }
    
    /**
     * Adds a restriction to the given User in a given Channel.
     *
     * @param restriction Restriction to be added.
     * @param user User to add the restriction to.
     * @param channel Channel the user is in.
     * @return True if the restriction was added successfully. False if the restriction was
     *         already present for that User in that Channel.
     */
    public boolean addRestriction( String restriction, IUser user, IChannel channel ) {
        
        return addRestriction( restriction, getOrCreateElement( user, channel ) );
        
    }
    
    /**
     * Removes a given restriction from a given Element. Trims any Elements that become
     * childless due to this operation.
     *
     * @param restriction Restriction to be removed.
     * @param element Element where the restriction should be removed from.
     * @return true if the restriction was successfully removed. false if the restriction
     *         was not found on the given Element.
     */
    private boolean removeRestriction( String restriction, Element element ) {
        
        for ( Element existent : element.elements( RESTRICTION_TAG ) ) {
            
            if ( existent.getText().equals( restriction ) ) {
                element.remove( existent );
                while ( ( element != root ) && ( element.elements().isEmpty() ) ) {
                    // Trims childless elements.
                    Element parent = element.getParent();
                    parent.remove( element );
                    element = parent;
                    
                }
                return true;
            }
            
        }
        return false;
        
    }
    
    /**
     * Removes a given restriction from a given Guild.
     *
     * @param restriction Restriction to be removed.
     * @param guild Guild where the restriction should be removed from.
     * @return true if the restriction was successfully removed. false if the restriction
     *         was not found on the given Guild.
     */
    public boolean removeRestriction( String restriction, IGuild guild ) {
        
        return removeRestriction( restriction, getElement( guild ) );
        
    }
    
    /**
     * Removes a given restriction from a given Channel.
     *
     * @param restriction Restriction to be removed.
     * @param channel Channel where the restriction should be removed from.
     * @return true if the restriction was successfully removed. false if the restriction
     *         was not found on the given Channel.
     */
    public boolean removeRestriction( String restriction, IChannel channel ) {
        
        return removeRestriction( restriction, getElement( channel ) );
        
    }
    
    /**
     * Removes a given restriction from a given User in a given Channel.
     *
     * @param restriction Restriction to be removed.
     * @param user User where the restriction should be removed from.
     * @param channel Channel the user is in.
     * @return true if the restriction was successfully removed. false if the restriction
     *         was not found on the given User.
     */
    public boolean removeRestriction( String restriction, IUser user, IChannel channel ) {
        
        return removeRestriction( restriction, getElement( user, channel ) );
        
    }

}

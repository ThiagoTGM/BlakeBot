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

package com.github.thiagotgm.blakebot.common.utils;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Helper methods for testing XMLElements.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-11
 */
public class XMLTestHelper {

    /**
     * Writes an XMLElement to a byte array.<br>
     * Uses {@link XMLElement#write(XMLStreamWriter)} with an output that directs to a 
     * ByteArrayOutputStream, returning the bytes that were written.
     *
     * @param elem Element to write.
     * @return The byte array
     * @throws XMLStreamException if an error happened.
     * @throws IOException if an error happened.
     */
    public static byte[] toByteArray( XMLElement elem ) throws XMLStreamException, IOException {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Utils.writeXMLDocument( out, elem );
        out.close();
        return out.toByteArray();
        
    }
    
    /**
     * Reads an XMLElement from a byte array.<br>
     * Uses {@link XMLElement#read(XMLStreamReader)} with an input that directs from a
     * ByteArrayInputStream, reading the given bytes.
     *
     * @param elem The element to read.
     * @param data The data bytes to read from.
     * @throws XMLStreamException if an error happened.
     * @throws IOException if an error happened.
     */
    public static void fromByteArray( XMLElement elem, byte[] data ) throws XMLStreamException, IOException {
        
        InputStream in = new ByteArrayInputStream( data );
        Utils.readXMLDocument( in, elem );
        in.close();
        
    }
    
    /**
     * Attempts to write then read an XMLElement, asserting that the read element matches
     * the original element.
     * <p>
     * That is, writes <tt>expected</tt> to a stream, then has <tt>target</tt> read the resulting
     * data, and then checks if <tt>target</tt> matches <tt>expected</tt>.
     *
     * @param <T> The type of element being tested.
     * @param expected The original element that should be written.
     * @param target The element that should read the data written by <tt>expected</tt>.
     * @throws XMLStreamException if an error happened.
     * @throws IOException if an error happened.
     */
    public static <T extends XMLElement> void testReadWrite( T expected, T target )
            throws XMLStreamException, IOException {
        
        byte[] data = toByteArray( expected );
        fromByteArray( target, data );
        assertEquals( "Read element is not correct.", expected, target );
        
    }

}
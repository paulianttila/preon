/*
 * Copyright (C) 2008 Wilfred Springer
 * 
 * This file is part of Preon.
 * 
 * Preon is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * 
 * Preon is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Preon; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package nl.flotsam.preon.codec;

import java.lang.reflect.AnnotatedElement;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

import nl.flotsam.preon.Codec;
import nl.flotsam.preon.DecodingException;
import nl.flotsam.preon.annotation.BoundNumber;
import nl.flotsam.preon.buffer.BitBuffer;
import nl.flotsam.preon.buffer.ByteOrder;
import nl.flotsam.preon.buffer.DefaultBitBuffer;
import nl.flotsam.preon.codec.EnumCodecFactory;

import org.easymock.EasyMock;


public class EnumCodecFactoryTest extends TestCase {

    private AnnotatedElement metadata;

    private BoundNumber boundNumber;

    public void setUp() {
        metadata = EasyMock.createMock(AnnotatedElement.class);
        boundNumber = EasyMock.createMock(BoundNumber.class);
    }

    public void testHappyPath() throws DecodingException {
        // Pre-play behaviour
        EasyMock.expect(metadata.isAnnotationPresent(BoundNumber.class)).andReturn(true);
        EasyMock.expect(metadata.getAnnotation(BoundNumber.class)).andReturn(
                boundNumber);
        EasyMock.expect(boundNumber.size()).andReturn("8");
        EasyMock.expect(boundNumber.endian()).andReturn(ByteOrder.LittleEndian);

        // Replay
        EasyMock.replay(metadata, boundNumber);
        EnumCodecFactory factory = new EnumCodecFactory();
        BitBuffer buffer = new DefaultBitBuffer(ByteBuffer.wrap(new byte[] { 0,
                1 }));
        Codec<Direction> codec = factory.create(metadata, Direction.class, null);
        assertNotNull(codec);
        assertEquals(Direction.Left, codec.decode(buffer, null, null));
        
        // Verify
        EasyMock.verify(metadata, boundNumber);
    }

    enum Direction {
        Left, Right
    }

}
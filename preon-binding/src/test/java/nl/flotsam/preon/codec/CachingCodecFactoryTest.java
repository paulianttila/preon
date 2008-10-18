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
import java.util.Date;

import nl.flotsam.preon.Codec;
import nl.flotsam.preon.CodecFactory;
import nl.flotsam.preon.codec.CachingCodecFactory;

import org.easymock.EasyMock;


import junit.framework.TestCase;

public class CachingCodecFactoryTest extends TestCase {

    private CodecFactory delegate;

    private Codec<String> codec1;

    private Codec<Date> codec2;

    private AnnotatedElement metadata;

    @SuppressWarnings("unchecked")
    public void setUp() {
        delegate = EasyMock.createMock(CodecFactory.class);
        codec1 = EasyMock.createMock(Codec.class);
        codec2 = EasyMock.createMock(Codec.class);
        metadata = EasyMock.createMock(AnnotatedElement.class);
    }

    /**
     * Tests if the {@link CachingCodecFactory} correctly returns the same codec
     * for identical requests.
     */
    public void testCachingStrategy() {
        EasyMock.expect(delegate.create(metadata, String.class, null)).andReturn(
                codec1);
        EasyMock.expect(delegate.create(metadata, Date.class, null))
                .andReturn(codec2);
        EasyMock.replay(delegate, codec1, codec2);
        CachingCodecFactory factory = new CachingCodecFactory(delegate);
        assertEquals(codec1, factory.create(metadata, String.class, null));
        assertEquals(codec2, factory.create(metadata, Date.class, null));
        Codec<String> cacheRef = factory.create(metadata, String.class, null);
        assertNotNull(cacheRef);
        EasyMock.verify(delegate, codec1, codec2);
    }

}
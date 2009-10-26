/**
 * Copyright (C) 2009 Wilfred Springer
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

import nl.flotsam.limbo.Expression;
import nl.flotsam.preon.Resolver;
import nl.flotsam.preon.annotation.BoundString;
import nl.flotsam.preon.channel.BitChannel;
import nl.flotsam.preon.channel.OutputStreamBitChannel;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class FixedLengthStringCodecTest {

    @Mock
    private Resolver resolver;

    @Mock
    private Expression<Integer, Resolver> sizeExpr;

    @Test
    public void shouldEncodeCorrectly() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BitChannel channel = new OutputStreamBitChannel(out);
        when(sizeExpr.eval(Matchers.any(Resolver.class))).thenReturn(4);
        FixedLengthStringCodec codec =
                new FixedLengthStringCodec(BoundString.Encoding.ASCII, sizeExpr, null, new BoundString.NullConverter());
        codec.encode("Whatever", channel, resolver);
        out.flush();
        byte[] result = out.toByteArray();
        assertThat(result.length, is(4));
        assertThat(new String(result, "US-ASCII"), is("What"));
    }

}

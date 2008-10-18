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

package nl.flotsam.preon;

import nl.flotsam.limbo.Expression;
import nl.flotsam.preon.buffer.BitBuffer;


/**
 * The interface to be implemented by objects that are able to decode/encode
 * objects.
 * 
 * @author Wilfred Springer
 * 
 * @param <T>
 *            The type of object the {@link Codec} is able to decode/encode.
 */
public interface Codec<T> {

    /**
     * Decodes a value from the {@link BitBuffer}.
     * 
     * @param buffer
     *            The {@link BitBuffer} containing the data from which a value
     *            will be decoded.
     * @param resolver
     *            The object capable of resolving variable references, when
     *            required.
     * @param builder
     *            The object responsible for creating default instances of
     *            objects, when needed. (In reality, this is most likely going
     *            to be important to {@link ObjectCodecFactory
     *            ObjectCodecFactories} only, but in order to make sure the
     *            {@link Builder} arrives there, we need to have the ability to
     *            pass it in.
     * @return The decoded value.
     * @throws DecodingException
     *             If the {@link Codec} fails to decode the value.
     */
    T decode(BitBuffer buffer, Resolver resolver, Builder builder) throws DecodingException;

    /**
     * Attempts to determine the size (in bits) of the data encoded/decoded. If
     * the {@link Codec} cannot guarantee that the number of bits required for
     * encoding/decoding is a fixed number, then it is expected to return a
     * negative number.
     * 
     * <p>
     * Note that this operation might be deprecated in the future, because of
     * {@link #getSize()}.
     * </p>
     * 
     * @param resolver
     *            An object that will be used to determine the size of the
     *            object if it is a function of variables in the context
     *            provided by the environment. (This is tricky. Might need to
     *            change that.)
     * @return The size of the encoded/decoded data in number of bits.
     */
    int getSize(Resolver resolver);

    /**
     * Returns the number of bits occupied by objects created by this Codec, as
     * a function of the context to which variables will be resolved.
     * 
     * @return A Limbo {@link Expression}, expressing the number of bits
     *         occupied by instance loaded and stored by this Codec.
     */
    Expression<Integer, Resolver> getSize();

    /**
     * Returns an object that is capable of rendering a description of the data
     * structure encoded/decoded by this Codec.
     * 
     * @return An object capable of describing the {@link Codec}.
     */
    CodecDescriptor getCodecDescriptor();

    /**
     * Returns an array of types constructed potentially by this Codec.
     * 
     * @return An array of types constructed potentially by this codec.
     */
    Class<?>[] getTypes();

}
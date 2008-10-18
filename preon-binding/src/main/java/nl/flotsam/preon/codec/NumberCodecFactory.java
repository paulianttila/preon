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
import java.util.HashMap;
import java.util.Map;

import nl.flotsam.limbo.Expression;
import nl.flotsam.limbo.Expressions;
import nl.flotsam.limbo.util.StringBuilderDocument;
import nl.flotsam.pecia.Contents;
import nl.flotsam.pecia.ParaContents;
import nl.flotsam.preon.Builder;
import nl.flotsam.preon.Codec;
import nl.flotsam.preon.CodecDescriptor;
import nl.flotsam.preon.CodecFactory;
import nl.flotsam.preon.DecodingException;
import nl.flotsam.preon.Resolver;
import nl.flotsam.preon.ResolverContext;
import nl.flotsam.preon.annotation.Bound;
import nl.flotsam.preon.annotation.BoundNumber;
import nl.flotsam.preon.buffer.BitBuffer;
import nl.flotsam.preon.buffer.ByteOrder;


/**
 * A {@link CodecFactory} generating {@link Codec Codecs} capable of decoding
 * numbers from the {@link BitBuffer}. Note that the {@link Codec Codecs}
 * created by this class are capable to decode Longs, Integers, Shorts, Bytes,
 * longs, ints, shorts and bytes.
 * 
 * @author Wilfred Springer
 * 
 */
public class NumberCodecFactory implements CodecFactory {

    private enum NumericType {

        Float {

            public int getDefaultSize() {
                return 32;
            }

            public Object decode(BitBuffer buffer, int size, ByteOrder endian) {
                int value = buffer.readAsInt(size, endian);
                return java.lang.Float.intBitsToFloat(value);
            }

            public Class<?> getType() {
                return java.lang.Float.class;
            }

        },

        Double {

            public int getDefaultSize() {
                return 64;
            }

            public Object decode(BitBuffer buffer, int size, ByteOrder endian) {
                return java.lang.Double.longBitsToDouble(buffer.readAsLong(size, endian));
            }

            public Class<?> getType() {
                return java.lang.Double.class;
            }

        },

        Integer {
            public int getDefaultSize() {
                return 32;
            }

            public Object decode(BitBuffer buffer, int size, ByteOrder endian) {
                return buffer.readAsInt(size, endian);
            }

            public Class<?> getType() {
                return Integer.class;
            }
        },

        Long {
            public int getDefaultSize() {
                return 64;
            }

            public Object decode(BitBuffer buffer, int size, ByteOrder endian) {
                return buffer.readAsLong(size, endian);
            }

            public Class<?> getType() {
                return Long.class;
            }
        },

        Short {
            public int getDefaultSize() {
                return 16;
            }

            public Object decode(BitBuffer buffer, int size, ByteOrder endian) {
                return buffer.readAsShort(size, endian);
            }

            public Class<?> getType() {
                return Short.class;
            }
        },

        Byte {
            public int getDefaultSize() {
                return 8;
            }

            public Object decode(BitBuffer buffer, int size, ByteOrder endian) {
                return buffer.readAsByte(size, endian);
            }

            public Class<?> getType() {
                return Byte.class;
            }
        };

        public abstract int getDefaultSize();

        public abstract Object decode(BitBuffer buffer, int size, ByteOrder endian);

        public abstract Class<?> getType();

    }

    private static Map<Class<?>, NumericType> NUMERIC_TYPES = new HashMap<Class<?>, NumericType>(8);

    static {
        NUMERIC_TYPES.put(Integer.class, NumericType.Integer);
        NUMERIC_TYPES.put(Long.class, NumericType.Long);
        NUMERIC_TYPES.put(Short.class, NumericType.Short);
        NUMERIC_TYPES.put(Byte.class, NumericType.Byte);
        NUMERIC_TYPES.put(int.class, NumericType.Integer);
        NUMERIC_TYPES.put(long.class, NumericType.Long);
        NUMERIC_TYPES.put(short.class, NumericType.Short);
        NUMERIC_TYPES.put(byte.class, NumericType.Byte);
        NUMERIC_TYPES.put(float.class, NumericType.Float);
        NUMERIC_TYPES.put(Float.class, NumericType.Float);
        NUMERIC_TYPES.put(double.class, NumericType.Double);
        NUMERIC_TYPES.put(Double.class, NumericType.Double);
    }

    @SuppressWarnings( { "unchecked" })
    public <T> Codec<T> create(AnnotatedElement overrides, Class<T> type, ResolverContext context) {
        if (NUMERIC_TYPES.keySet().contains(type)) {
            NumericType numericType = NUMERIC_TYPES.get(type);
            if (overrides == null || overrides.isAnnotationPresent(Bound.class)) {
                ByteOrder endian = ByteOrder.LittleEndian;
                int size = numericType.getDefaultSize();
                Expression<Integer, Resolver> sizeExpr = Expressions.createInteger(context, Integer
                        .toString(size));
                return (Codec<T>) new NumericCodec(sizeExpr, endian, numericType);
            }
            if (overrides != null && overrides.isAnnotationPresent(BoundNumber.class)) {
                BoundNumber numericMetadata = overrides.getAnnotation(BoundNumber.class);
                ByteOrder endian = numericMetadata.endian();
                String size = numericMetadata.size();
                if (size.length() == 0) {
                    size = Integer.toString(numericType.getDefaultSize());
                }
                Expression<Integer, Resolver> sizeExpr = Expressions.createInteger(context, size);
                return (Codec<T>) new NumericCodec(sizeExpr, endian, numericType);
            }
        }
        return null;
    }

    private static class NumericCodec implements Codec<Object> {

        protected Expression<Integer, Resolver> size;

        protected ByteOrder endian;

        protected NumericType type;

        public NumericCodec(Expression<Integer, Resolver> sizeExpr, ByteOrder endian,
                NumericType type) {
            this.size = sizeExpr;
            this.endian = endian;
            this.type = type;
        }

        public Object decode(BitBuffer buffer, Resolver resolver, Builder builder)
                throws DecodingException {
            return type.decode(buffer, size.eval(resolver), endian);
        }

        public int getSize(Resolver resolver) {
            if (resolver != null) {
                return size.eval(resolver);
            } else {
                return -1;
            }
        }

        public CodecDescriptor getCodecDescriptor() {
            return new CodecDescriptor() {

                public String getLabel() {
                    StringBuilder builder = new StringBuilder();
                    Expression<Integer, Resolver> sizeExpr = NumericCodec.this.getSize();
                    if (sizeExpr != null && !sizeExpr.isParameterized()) {
                        builder.append(Integer.toString(sizeExpr.eval(null)));
                        builder.append("-bit ");
                    }
                    switch (type) {
                        case Integer: {
                            builder.append("integer");
                            break;
                        }
                        case Long: {
                            builder.append("long");
                            break;
                        }
                        case Byte: {
                            builder.append("byte");
                            break;
                        }
                        case Short: {
                            builder.append("short");
                            break;
                        }
                        case Float: {
                            builder.append("float");
                        }
                        case Double: {
                            builder.append("double");
                        }
                    }
                    return builder.toString();
                }

                public boolean hasFullDescription() {
                    return false;
                }

                public <T> Contents<T> putFullDescription(Contents<T> contents) {
                    return null;
                }

                public <T, V extends ParaContents<T>> V putOneLiner(V para) {
                    para.text("A ").text(getLabel());
                    return para;
                }

                public String getSize() {
                    StringBuilder builder = new StringBuilder();
                    size.document(new StringBuilderDocument(builder));
                    return builder.toString();
                }

                public <T> void writeReference(ParaContents<T> contents) {
                    contents.text(getLabel());
                }

            };
        }

        public Class<?>[] getTypes() {
            return new Class[] { type.getType() };
        }

        public Expression<Integer, Resolver> getSize() {
            return size;
        }

    }

}
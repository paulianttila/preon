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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.AnnotatedElement;

import nl.flotsam.limbo.Expression;
import nl.flotsam.limbo.Expressions;
import nl.flotsam.limbo.util.StringBuilderDocument;
import nl.flotsam.pecia.Contents;
import nl.flotsam.pecia.ParaContents;
import nl.flotsam.preon.Builder;
import nl.flotsam.preon.Codec;
import nl.flotsam.preon.CodecConstructionException;
import nl.flotsam.preon.CodecDescriptor;
import nl.flotsam.preon.CodecFactory;
import nl.flotsam.preon.Codecs;
import nl.flotsam.preon.DecodingException;
import nl.flotsam.preon.Resolver;
import nl.flotsam.preon.ResolverContext;
import nl.flotsam.preon.annotation.BoundString;
import nl.flotsam.preon.annotation.BoundString.ByteConverter;
import nl.flotsam.preon.annotation.BoundString.Encoding;
import nl.flotsam.preon.buffer.BitBuffer;


/**
 * A {@link CodecFactory} generating {@link Codecs} capable of generating String
 * from {@link BitBuffer} content.
 * 
 * @author Wilfred Springer
 * 
 */
public class StringCodecFactory implements CodecFactory {

    @SuppressWarnings("unchecked")
    public <T> Codec<T> create(AnnotatedElement metadata, Class<T> type, ResolverContext context) {
        if (metadata == null) {
            return null;
        }
        BoundString settings = metadata.getAnnotation(BoundString.class);
        if (String.class.equals(type) && settings != null) {
            try {
                if (settings.size().length() > 0) {
                    Expression<Integer, Resolver> expr;
                    expr = Expressions.createInteger(context, settings.size());
                    return (Codec<T>) new FixedLengthStringCodec(settings.encoding(), expr,
                            settings.match(), settings.converter().newInstance());

                } else {
                    return (Codec<T>) new NullTerminatedStringCodec(settings.encoding(), settings
                            .match(), settings.converter().newInstance());
                }
            } catch (InstantiationException e) {
                throw new CodecConstructionException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new CodecConstructionException(e.getMessage());
            }
        } else {
            return null;
        }
    }

    public static class NullTerminatedStringCodec implements Codec<String> {

        private Encoding encoding;

        private String match;

        private ByteConverter byteConverter;

        public NullTerminatedStringCodec(Encoding encoding, String match,
                ByteConverter byteConverter) {
            this.encoding = encoding;
            this.match = match;
            this.byteConverter = byteConverter;
        }

        public String decode(BitBuffer buffer, Resolver resolver, Builder builder)
                throws DecodingException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte value;
            while ((value = buffer.readAsByte(8)) != 0x00) {
                out.write(byteConverter.convert(value));
            }
            String charset = null;
            switch (encoding) {
                case ASCII: {
                    charset = "US-ASCII";
                    break;
                }
                case ISO_8859_1: {
                    charset = "ISO-8859-1";
                    break;
                }
            }
            try {
                return new String(out.toByteArray(), charset);
            } catch (UnsupportedEncodingException uee) {
                throw new DecodingException(uee);
            }
        }

        public CodecDescriptor getCodecDescriptor() {
            return new CodecDescriptor() {

                public String getLabel() {
                    StringBuilder builder = new StringBuilder();
                    builder.append("null-terminated string");
                    builder.append(" encoded in ");
                    builder.append(encoding);
                    if (!"".equals(match)) {
                        builder.append(" matching \"");
                        builder.append(match);
                        builder.append("\"");
                    }
                    String conversion = byteConverter.getDescription();
                    if (conversion != null && !"".equals(conversion)) {
                        builder.append("(");
                        builder.append(conversion);
                        builder.append(")");
                    }
                    return builder.toString();
                }

                public String getSize() {
                    return "unknown";
                }

                public boolean hasFullDescription() {
                    return false;
                }

                public <T> Contents<T> putFullDescription(Contents<T> contents) {
                    return contents;
                }

                public <T, V extends ParaContents<T>> V putOneLiner(V para) {
                    para.text(getLabel());
                    return para;
                }

                public <T> void writeReference(ParaContents<T> contents) {
                    contents.text(getLabel());
                }

            };
        }

        public int getSize(Resolver resolver) {
            return -1;
        }

        public Class<?>[] getTypes() {
            return new Class[] { String.class };
        }

        public Expression<Integer, Resolver> getSize() {
            return null;
        }

    }

    public static class FixedLengthStringCodec implements Codec<String> {

        private Encoding encoding;

        private Expression<Integer, Resolver> sizeExpr;

        private String match;

        private ByteConverter byteConverter;

        public FixedLengthStringCodec(Encoding encoding, Expression<Integer, Resolver> sizeExpr,
                String match, ByteConverter byteConverter) {
            this.encoding = encoding;
            this.sizeExpr = sizeExpr;
            this.match = match;
            this.byteConverter = byteConverter;
        }

        public String decode(BitBuffer buffer, Resolver resolver, Builder builder)
                throws DecodingException {
            int size = sizeExpr.eval(resolver);
            byte[] bytes = new byte[size];
            for (int i = 0; i < size; i++) {
                bytes[i] = byteConverter.convert(buffer.readAsByte(8));
            }
            String result;
            try {
                result = encoding.decode(bytes);
            } catch (UnsupportedEncodingException e) {
                throw new DecodingException(e);
            }
            if (match.length() > 0) {
                if (!match.equals(result)) {
                    throw new DecodingException(new IllegalStateException("Expected \"" + match
                            + "\", but got \"" + result + "\"."));
                }
            }
            return result;
        }

        public CodecDescriptor getCodecDescriptor() {
            return new CodecDescriptor() {

                public String getLabel() {
                    StringBuilder builder = new StringBuilder();
                    builder.append("A number of characters (");
                    sizeExpr.document(new StringBuilderDocument(builder));
                    builder.append(") encoded in ");
                    builder.append(encoding);
                    if (!"".equals(match)) {
                        builder.append(" matching \"");
                        builder.append(match);
                        builder.append("\"");
                    }
                    String conversion = byteConverter.getDescription();
                    if (conversion != null && !"".equals(conversion)) {
                        builder.append("(");
                        builder.append(conversion);
                        builder.append(")");
                    }
                    return builder.toString();
                }

                public String getSize() {
                    StringBuilder builder = new StringBuilder();
                    sizeExpr.document(new StringBuilderDocument(builder));
                    builder.append(" times 8");
                    return builder.toString();
                }

                public boolean hasFullDescription() {
                    return false;
                }

                public <T> Contents<T> putFullDescription(Contents<T> contents) {
                    return null;
                }

                public <T, V extends ParaContents<T>> V putOneLiner(V para) {
                    para.text(getLabel());
                    return para;
                }

                public <T> void writeReference(ParaContents<T> contents) {
                    contents.text(getLabel());
                }

            };
        }

        public int getSize(Resolver resolver) {
            return sizeExpr.eval(resolver);
        }

        public Class<?>[] getTypes() {
            return new Class[] { String.class };
        }

        public Expression<Integer, Resolver> getSize() {
            return Expressions.multiply(Expressions.createInteger(8, Resolver.class), sizeExpr);
        }

    }

}
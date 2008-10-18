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

package nl.flotsam.preon.sample.bytecode;

import java.io.UnsupportedEncodingException;

import nl.flotsam.preon.annotation.BoundList;
import nl.flotsam.preon.annotation.BoundNumber;
import nl.flotsam.preon.annotation.TypePrefix;
import nl.flotsam.preon.buffer.ByteOrder;


public class ClassFile {

    @BoundNumber(size = "32")
    private long magic;

    @BoundNumber(size = "16")
    private int minorVersion;

    @BoundNumber(size = "16")
    private int majorVersion;

    @BoundNumber(size = "16")
    private int constantPoolCount;

    @BoundList(size = "constantPoolCount-1", types = { ClassCpInfo.class,
            DoubleCpInfo.class, FieldRefCpInfo.class, FloatCpInfo.class,
            IntegerCpInfo.class, InterfaceMethodRefCpInfo.class,
            MethodRefCpInfo.class, NameAndTypeCpInfo.class, StringCpInfo.class,
            Utf8CpInfo.class })
    private CpInfo[] constantPool;

    @BoundNumber(size = "16")
    private int accessFlags;

    @BoundNumber(size = "16")
    private int thisClass;

    @BoundNumber(size = "16")
    private int superClass;

    @BoundNumber(size = "16")
    private int interfacesCount;

    @BoundList(size = "interfacesCount")
    private int[] interfaces;

    @BoundNumber(size = "16")
    private int fieldCount;

    @BoundList(size = "fieldCount")
    private FieldInfo[] fields;

    @BoundNumber(size = "16")
    private int methodCount;

    @BoundList(size = "methodCount")
    private MethodInfo[] methods;

    @BoundNumber(size = "16")
    private int attributeCount;

    @BoundList(size = "attributesCount")
    private AttributeInfo[] attributes;

    @TypePrefix(value = "7", size = 8)
    private class ClassCpInfo extends CpInfo {

        @BoundNumber(size = "16")
        private int nameIndex;

        public String getName() {
            return constantPool[nameIndex].toString();
        }

    }

    @TypePrefix(value = "1")
    private class Utf8CpInfo extends CpInfo {

        @BoundNumber(size = "16")
        private int length;

        @BoundList(size = "length")
        private byte[] bytes;

        public String getStringValue() {
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(
                        "Expecting UTF-8, but got something else.");
            }
        }

    }

    @TypePrefix(value = "9")
    private class FieldRefCpInfo extends CpInfo {

        @BoundNumber(size = "16")
        private int classIndex;

        @BoundNumber(size = "16")
        private int nameAndTypeIndex;

    }

    @TypePrefix(value = "10")
    private class MethodRefCpInfo extends CpInfo {

        @BoundNumber(size = "16")
        private int classIndex;

        @BoundNumber(size = "16")
        private int nameAndTypeIndex;

    }

    @TypePrefix(value = "11")
    private class InterfaceMethodRefCpInfo extends CpInfo {

        @BoundNumber(size = "16")
        private int classIndex;

        @BoundNumber(size = "16")
        private int nameAndTypeIndex;

    }

    @TypePrefix(value = "8")
    private class StringCpInfo extends CpInfo {

        @BoundNumber(size = "16")
        private int stringIndex;

    }

    @TypePrefix(value = "3")
    private class IntegerCpInfo extends CpInfo {

        @BoundNumber(endian = ByteOrder.BigEndian)
        private int value;

    }

    @TypePrefix(value = "4")
    private class FloatCpInfo extends CpInfo {

        @BoundNumber(endian = ByteOrder.BigEndian)
        private float value;

    }

    @TypePrefix(value = "6")
    private class DoubleCpInfo extends CpInfo {

        @BoundNumber(endian = ByteOrder.BigEndian)
        private double value;

    }

    @TypePrefix(value = "5")
    private class LongCpInfo extends CpInfo {

        @BoundNumber(endian = ByteOrder.BigEndian)
        private long value;

    }

    @TypePrefix(value = "12")
    private class NameAndTypeCpInfo extends CpInfo {

        @BoundNumber(size = "16")
        private int nameIndex;

        @BoundNumber(size = "16")
        private int descriptorIndex;

    }

    private class MethodInfo {

        @BoundNumber(size = "16")
        private int accessFlags;

        @BoundNumber(size = "16")
        private int nameIndex;

        @BoundNumber(size = "16")
        private int descriptorIndex;

        @BoundNumber(size = "16")
        private int attributesCount;

        @BoundList(size = "attributesCount")
        private AttributeInfo[] attributes;

        public String getName() {
            return ((Utf8CpInfo) ClassFile.this.constantPool[nameIndex])
                    .getStringValue();
        }

        public String getDescriptor() {
            return ((Utf8CpInfo) ClassFile.this.constantPool[descriptorIndex])
                    .getStringValue();
        }

    }
    
    private abstract class AttributeInfo {
        
        @BoundNumber(size="16")
        private int attributeNameIndex;
        
        @BoundNumber(size="32")
        private long attributeLength;
                
    }

}
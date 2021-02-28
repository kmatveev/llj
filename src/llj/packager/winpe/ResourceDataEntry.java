package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static llj.util.BinIOTools.getInt;
import static llj.util.BinIOTools.getLong;
import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.putInt;
import static llj.util.BinIOTools.putUnsignedInt;

public class ResourceDataEntry extends FieldSequenceFormat {

    public static final int SIZE = 16;

    public static enum Field implements FieldSequenceFormat.Field<ResourceDataEntry> {
        
        DATA_RVA {
            @Override
            public void read(ByteBuffer source, ResourceDataEntry dest) {
                dest.dataRva = getUnsignedInt(source);
            }

            @Override
            public void write(ResourceDataEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.dataRva);
            }

            @Override
            public int size() {
                return DWORD;
            }

            @Override
            public Optional<String> getStringValue(ResourceDataEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.dataRva, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        SIZE {
            @Override
            public void read(ByteBuffer source, ResourceDataEntry dest) {
                dest.size = getUnsignedInt(source);
            }

            @Override
            public void write(ResourceDataEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.size);
            }

            @Override
            public int size() {
                return DWORD;
            }

            @Override
            public Optional<String> getStringValue(ResourceDataEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.size, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        CODEPAGE {
            @Override
            public void read(ByteBuffer source, ResourceDataEntry dest) {
                dest.codepage = getUnsignedInt(source);
            }

            @Override
            public void write(ResourceDataEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.codepage);
            }

            @Override
            public int size() {
                return DWORD;
            }

            @Override
            public Optional<String> getStringValue(ResourceDataEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.codepage, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        RESERVED {
            @Override
            public void read(ByteBuffer source, ResourceDataEntry dest) {
                dest.reserved = getInt(source);
            }

            @Override
            public void write(ResourceDataEntry source, ByteBuffer dest) {
                putInt(dest, source.reserved);
            }

            @Override
            public int size() {
                return DWORD;
            }

            @Override
            public Optional<String> getStringValue(ResourceDataEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.reserved, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        }
        
    }
    
    public long dataRva;
    public long size;
    public long codepage;
    public int reserved;

    @Override
    public Collection<? extends FieldSequenceFormat.Field> fields() {
        return Arrays.asList(Field.values());
    }

    @Override
    public boolean isDisplayFormatSupported(String fieldName, DisplayFormat format) {
        return false;
    }

    @Override
    public void setStringValue(String fieldName, DisplayFormat format) {

    }

    @Override
    public int getSize() {
        return SIZE;
    }

    @Override
    public String getStringValue() {
        return "ResourceDataEntry";
    }

    @Override
    public Object readFieldsFrom(ByteBuffer readBuffer) {
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return super.readFieldsFrom(readBuffer);
    }
}

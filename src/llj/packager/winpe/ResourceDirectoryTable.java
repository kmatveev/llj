package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;
import llj.util.BinIOTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static llj.util.BinIOTools.getInt;
import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;

public class ResourceDirectoryTable extends FieldSequenceFormat {

    public static final int SIZE = 16;

    public static enum Field implements FieldSequenceFormat.Field<ResourceDirectoryTable> {
        
        CHARACTERISTICS {
            @Override
            public void read(ByteBuffer source, ResourceDirectoryTable dest) {
                dest.characteristics = getInt(source);
            }

            @Override
            public void write(ResourceDirectoryTable source, ByteBuffer dest) {
                BinIOTools.putInt(dest, source.characteristics);
            }

            @Override
            public int size() {
                return DWORD;
            }

            @Override
            public Optional<String> getStringValue(ResourceDirectoryTable format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.characteristics, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        TIME_DATE_STAMP {
            @Override
            public void read(ByteBuffer source, ResourceDirectoryTable dest) {
                dest.timedatestamp = getInt(source);
            }

            @Override
            public void write(ResourceDirectoryTable source, ByteBuffer dest) {
                BinIOTools.putInt(dest, source.timedatestamp);
            }

            @Override
            public int size() {
                return DWORD;
            }

            @Override
            public Optional<String> getStringValue(ResourceDirectoryTable format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.timedatestamp, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        MAJOR_VERSION {
            @Override
            public void read(ByteBuffer source, ResourceDirectoryTable dest) {
                dest.majorVersion = getUnsignedShort(source);
            }

            @Override
            public void write(ResourceDirectoryTable source, ByteBuffer dest) {
                BinIOTools.putUnsignedShort(dest, source.majorVersion);
            }

            @Override
            public int size() {
                return WORD;
            }

            @Override
            public Optional<String> getStringValue(ResourceDirectoryTable format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.majorVersion, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        MINOR_VERSION {
            @Override
            public void read(ByteBuffer source, ResourceDirectoryTable dest) {
                dest.minorVersion = getUnsignedShort(source);
            }

            @Override
            public void write(ResourceDirectoryTable source, ByteBuffer dest) {
                BinIOTools.putUnsignedShort(dest, source.minorVersion);
            }

            @Override
            public int size() {
                return WORD;
            }

            @Override
            public Optional<String> getStringValue(ResourceDirectoryTable format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.minorVersion, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        NUMBER_OF_NAME_ENTRIES {
            @Override
            public void read(ByteBuffer source, ResourceDirectoryTable dest) {
                dest.numberOfNameEntries = getUnsignedShort(source);
            }

            @Override
            public void write(ResourceDirectoryTable source, ByteBuffer dest) {
                BinIOTools.putUnsignedShort(dest, source.numberOfNameEntries);
            }

            @Override
            public int size() {
                return WORD;
            }

            @Override
            public Optional<String> getStringValue(ResourceDirectoryTable format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.numberOfNameEntries, size(), ByteOrder.LITTLE_ENDIAN);
            }

        },
        NUMBER_OF_ID_ENTRIES {
            @Override
            public void read(ByteBuffer source, ResourceDirectoryTable dest) {
                dest.numberOfIdEntries = getUnsignedShort(source);
            }

            @Override
            public void write(ResourceDirectoryTable source, ByteBuffer dest) {
                BinIOTools.putUnsignedShort(dest, source.numberOfIdEntries);
            }

            @Override
            public int size() {
                return WORD;
            }

            @Override
            public Optional<String> getStringValue(ResourceDirectoryTable format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.numberOfIdEntries, size(), ByteOrder.LITTLE_ENDIAN);
            }

        }
        
    }
    public int characteristics;
    public int timedatestamp;
    public int majorVersion, minorVersion;
    public int numberOfNameEntries, numberOfIdEntries;

    @Override
    public Object readFieldsFrom(ByteBuffer readBuffer) {
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return super.readFieldsFrom(readBuffer);
    }

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
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSize() {
        return  SIZE;
    }

    @Override
    public String getStringValue() {
        return "ResourceDirectoryTable";
    }
}

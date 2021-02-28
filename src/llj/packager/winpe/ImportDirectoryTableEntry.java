package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.putUnsignedInt;

public class ImportDirectoryTableEntry extends FieldSequenceFormat {

    public static final int SIZE = 20;

    public static enum ImportDirectoryTableField implements FieldSequenceFormat.Field<ImportDirectoryTableEntry> {
        
        IMPORT_LOOKUP_TABLE_RVA {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, ImportDirectoryTableEntry dest) {
                dest.importLookupTableRva = getUnsignedInt(source);
            }

            public void write(ImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.importLookupTableRva);
            }

            @Override
            public Optional<String> getStringValue(ImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.importLookupTableRva, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        TIME_DATE_STAMP {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, ImportDirectoryTableEntry dest) {
                dest.timeDateStamp = getUnsignedInt(source);
            }

            public void write(ImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.timeDateStamp);
            }

            @Override
            public Optional<String> getStringValue(ImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.timeDateStamp, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        FORWARDER_CHAIN {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, ImportDirectoryTableEntry dest) {
                dest.forwarderChain = getUnsignedInt(source);
            }

            public void write(ImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.forwarderChain);
            }

            @Override
            public Optional<String> getStringValue(ImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.forwarderChain, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        NAME_RVA {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, ImportDirectoryTableEntry dest) {
                dest.nameRva = getUnsignedInt(source);
            }

            public void write(ImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.nameRva);
            }

            @Override
            public Optional<String> getStringValue(ImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.nameRva, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        IMPORT_ADDRESS_TABLE_RVA {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, ImportDirectoryTableEntry dest) {
                dest.importAddressTableRva = getUnsignedInt(source);
            }

            public void write(ImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.importAddressTableRva);
            }

            @Override
            public Optional<String> getStringValue(ImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.importAddressTableRva, size(), ByteOrder.LITTLE_ENDIAN);
            }

        }
    }
    
    public long importLookupTableRva;
    public long timeDateStamp;
    public long forwarderChain;
    public long nameRva;
    public long importAddressTableRva;

    @Override
    public Collection<? extends Field> fields() {
        return Arrays.asList(ImportDirectoryTableEntry.ImportDirectoryTableField.values());
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
        return SIZE;
    }

    @Override
    public String getStringValue() {
        return "ImportDirectoryTableEntry";
    }
    
    public boolean allEmpty() {
        return (importLookupTableRva == 0) && (timeDateStamp == 0) && (forwarderChain == 0) && (nameRva == 0) && (importAddressTableRva == 0);
    }

    @Override
    public Object readFieldsFrom(ByteBuffer readBuffer) {
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return super.readFieldsFrom(readBuffer);
    }
}

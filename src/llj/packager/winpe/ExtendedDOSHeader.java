package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;
import llj.packager.Format;
import llj.packager.dosexe.DOSExeFormat;
import llj.packager.dosexe.DOSHeader;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;

public class ExtendedDOSHeader extends DOSHeader<ExtendedDOSHeader.ExtensionField> {

    public static final int HEADER_EXTENSION_MIN_SIZE = 36;

    public static enum ExtensionField implements FieldSequenceFormat.Field<ExtendedDOSHeader> {

        RES1 {
            @Override
            public int size() {
                return 4 * 2;
            }

            public void read(ByteBuffer source, ExtendedDOSHeader dest) {
                for (int i = 0; i < dest.res1.length; i++)
                    dest.res1[i] = getUnsignedShort(source);
            }

            public void write(ExtendedDOSHeader source, ByteBuffer dest) {
                for (int i = 0; i < source.res1.length; i++)
                    putUnsignedShort(dest, source.res1[i]);
            }

            @Override
            public Optional<String> getStringValue(ExtendedDOSHeader format, DisplayFormat displayFormat) {
                return Optional.of(Arrays.toString(format.res1));
            }
        },
        OEMID {
            @Override
            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, ExtendedDOSHeader dest) {
                dest.oemid = getUnsignedShort(source);
            }

            public void write(ExtendedDOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.oemid);
            }

            @Override
            public Optional<String> getStringValue(ExtendedDOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.oemid, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        OEMINFO {
            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, ExtendedDOSHeader dest) {
                dest.oeminfo = getUnsignedShort(source);
            }

            public void write(ExtendedDOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.oeminfo);
            }

            @Override
            public Optional<String> getStringValue(ExtendedDOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.oeminfo, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        RES2 {
            public int size() {
                return 20;
            }

            public void read(ByteBuffer source, ExtendedDOSHeader dest) {
                for (int i = 0; i < dest.res2.length; i++) {
                    dest.res2[i] = getUnsignedShort(source);
                }
            }

            public void write(ExtendedDOSHeader source, ByteBuffer dest) {
                for (int i = 0; i < source.res2.length; i++)
                    putUnsignedShort(dest, source.res2[i]);
            }
            
            @Override
            public Optional<String> getStringValue(ExtendedDOSHeader format, DisplayFormat displayFormat) {
                return Optional.of(Arrays.toString(format.res2));
            }
            
        },
        NEW_POS {
            public int size() {
                return DWORD;
            }

            public void read(ByteBuffer source, ExtendedDOSHeader dest) {
                dest.newPos = getUnsignedInt(source);
            }

            public void write(ExtendedDOSHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.newPos);
            }

            @Override
            public Optional<String> getStringValue(ExtendedDOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.newPos, size(), ByteOrder.LITTLE_ENDIAN);
            }
        };

        public abstract int size();

        public abstract void read(ByteBuffer source, ExtendedDOSHeader dest);

        public abstract void write(ExtendedDOSHeader source, ByteBuffer dest);

    }

    // These fields are used by Windows executables packed in DOS executables
    public final int[] res1 = new int[4];        // Reserved words

    public int oemid;             // OEM identifier (for oeminfo)
    public int oeminfo;           // OEM information; oemid specific
    public final int[] res2 = new int[10];      // Reserved words
    public long newPos;           // File address of new exe header
    public int headerExtensionSize;
    
    public ExtendedDOSHeader() {
        // create empty instance
    }
    
    public static ExtendedDOSHeader extend(DOSHeader<Object> base) {
        ExtendedDOSHeader extendedDOSHeader = new ExtendedDOSHeader();

        extendedDOSHeader.signature = base.signature;
        extendedDOSHeader.lastSize = base.lastSize;
        extendedDOSHeader.nBlocks = base.nBlocks;
        extendedDOSHeader.nRelocs = base.nRelocs;
        extendedDOSHeader.headerSize = base.headerSize;
        extendedDOSHeader.minalloc = base.minalloc;
        extendedDOSHeader.maxalloc = base.maxalloc;
        extendedDOSHeader.ss = base.ss;
        extendedDOSHeader.sp = base.sp;
        extendedDOSHeader.checksum = base.checksum;
        extendedDOSHeader.ip = base.ip;
        extendedDOSHeader.cs = base.cs;
        extendedDOSHeader.relocPos = base.relocPos;
        extendedDOSHeader.nOverlay = base.nOverlay;
        extendedDOSHeader.checksum = base.checksum;
        
        ByteBuffer extension = ByteBuffer.allocate(base.headerExtension.getSize());
        base.headerExtension.writeTo(extension);
        extension.flip();
        Location extensionLocation = extendedDOSHeader.createHeaderExtensionsFrom(extension, true);
        if (extensionLocation != null) {
            throw new RuntimeException();
        }
        
        extendedDOSHeader.relocations = base.relocations;
        extendedDOSHeader.freeSpace = base.freeSpace;
        
        return extendedDOSHeader;
    }
    
    @Override
    public int getHeaderExtensionsSize() {
        return headerExtensionSize;
    }

    public Optional<String> getStringValue(ExtensionField field, DisplayFormat displayFormat) {
        return field.getStringValue(this, displayFormat);
    }

    @Override
    public Location createHeaderExtensionsFrom(ByteBuffer readBuffer, boolean canTakeBuffer) {
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        headerExtensionSize = readBuffer.remaining();
        for (ExtensionField field : ExtensionField.values()) {
            try {
                field.read(readBuffer, this);
            } catch (BufferUnderflowException e) {
                return Location.atExtension(field);
            }
        }
        return null;
    }

    @Override
    public void writeHeaderExtensionsTo(ByteBuffer writeBuffer) {

        int initialPos = writeBuffer.position();
        for (ExtensionField field : ExtensionField.values()) {
            field.write(this, writeBuffer);
        }
        int currentPos = writeBuffer.position();
        int remaining = getHeaderExtensionsSize() - (currentPos - initialPos);
        for (int i = 0; i < remaining; i++) {
            writeBuffer.put((byte) 0);
        }
    }

    @Override
    public boolean isWellFormed() {
        boolean standardWellFormed = super.isWellFormed();
        boolean extSizeOk = getHeaderExtensionsSize() >= HEADER_EXTENSION_MIN_SIZE;
        return standardWellFormed && extSizeOk;
    }

    @Override
    public void makeWellFormed() {
        headerExtensionSize = HEADER_EXTENSION_MIN_SIZE;
        super.makeWellFormed();
    }

    public List<String> getExtensionNames() {
        List<String> extensionFields = new ArrayList<String>();
        for (ExtensionField field : ExtensionField.values()) {
            extensionFields.add(field.name());
        }
        return extensionFields;
    }

    @Override
    public Optional<String> getStringValue(String fieldName, DisplayFormat displayFormat) {
        for (ExtensionField field : ExtensionField.values()) {
            if (field.name().equals(fieldName)) {
                return getStringValue(field, displayFormat);
            }
        }
        return super.getStringValue(fieldName, displayFormat);
    }

    @Override
    public int getSize(String fieldName) {
        for (ExtensionField field : ExtensionField.values()) {
            if (field.name().equals(fieldName)) {
                return field.size();
            }
        }
        return super.getSize(fieldName);
    }

    @Override
    public int getOffset(String fieldName) {
        int extensionOffset = 0;
        boolean extensionRequested = false;
        for (ExtensionField field : ExtensionField.values()) {
            if (field.name().equals(fieldName)) {
                extensionRequested = true;
                break;
            } else {
                extensionOffset += field.size();
            }
        }
        if (extensionRequested) {
            return super.getOffset(EXTENSION) + extensionOffset;
        }
        return super.getOffset(fieldName);
    }

}

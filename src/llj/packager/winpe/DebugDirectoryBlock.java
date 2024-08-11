package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;
import llj.packager.Format;
import llj.util.BinIOTools;

import java.nio.ByteBuffer;
import java.util.*;

public class DebugDirectoryBlock implements Format {

    public static class DebugDirectoryEntry extends FieldSequenceFormat {

        public static final int SIZE = 28;

        public static enum Field implements FieldSequenceFormat.Field<DebugDirectoryEntry> {

            Characteristics {
                @Override
                public void read(ByteBuffer source, DebugDirectoryEntry dest) {
                    dest.characteristics = BinIOTools.getUnsignedInt(source);
                }

                @Override
                public void write(DebugDirectoryEntry source, ByteBuffer dest) {
                    BinIOTools.putUnsignedInt(dest,source.characteristics);
                }

                @Override
                public int size() {
                    return DWORD;
                }

                @Override
                public Optional<String> getStringValue(DebugDirectoryEntry format, DisplayFormat displayFormat) {
                    return Optional.empty();
                }
            },
            TimeDateStamp {
                @Override
                public void read(ByteBuffer source, DebugDirectoryEntry dest) {
                    dest.timedatestamp = BinIOTools.getUnsignedInt(source);
                }

                @Override
                public void write(DebugDirectoryEntry source, ByteBuffer dest) {
                    BinIOTools.putUnsignedInt(dest,source.timedatestamp);
                }

                @Override
                public int size() {
                    return DWORD;
                }

                @Override
                public Optional<String> getStringValue(DebugDirectoryEntry format, DisplayFormat displayFormat) {
                    return Optional.empty();
                }
            },
            MajorVersion {
                @Override
                public void read(ByteBuffer source, DebugDirectoryEntry dest) {
                    dest.majorVersion = BinIOTools.getUnsignedShort(source);
                }

                @Override
                public void write(DebugDirectoryEntry source, ByteBuffer dest) {
                    BinIOTools.putUnsignedShort(dest,source.majorVersion);
                }

                @Override
                public int size() {
                    return WORD;
                }

                @Override
                public Optional<String> getStringValue(DebugDirectoryEntry format, DisplayFormat displayFormat) {
                    return Optional.empty();
                }
            },
            MinorVersion {
                @Override
                public void read(ByteBuffer source, DebugDirectoryEntry dest) {
                    dest.minorVersion = BinIOTools.getUnsignedShort(source);
                }

                @Override
                public void write(DebugDirectoryEntry source, ByteBuffer dest) {
                    BinIOTools.putUnsignedShort(dest,source.minorVersion);
                }

                @Override
                public int size() {
                    return WORD;
                }

                @Override
                public Optional<String> getStringValue(DebugDirectoryEntry format, DisplayFormat displayFormat) {
                    return Optional.empty();
                }
            },
            Type {

                public static final int IMAGE_DEBUG_TYPE_UNKNOWN = 0;
                public static final int IMAGE_DEBUG_TYPE_COFF = 1;
                public static final int IMAGE_DEBUG_TYPE_CODEVIEW = 2;
                public static final int IMAGE_DEBUG_TYPE_FPO = 3;
                public static final int IMAGE_DEBUG_TYPE_MISC = 4;
                public static final int IMAGE_DEBUG_TYPE_EXCEPTION = 5;
                public static final int IMAGE_DEBUG_TYPE_FIXUP = 6;
                public static final int IMAGE_DEBUG_TYPE_OMAP_TO_SRC = 7;
                public static final int IMAGE_DEBUG_TYPE_OMAP_FROM_SRC = 8;
                public static final int IMAGE_DEBUG_TYPE_BORLAND = 9;
                public static final int IMAGE_DEBUG_TYPE_RESERVED10 = 10;
                public static final int IMAGE_DEBUG_TYPE_CLSID = 11;
                public static final int IMAGE_DEBUG_TYPE_VC_FEATURE = 12;
                public static final int IMAGE_DEBUG_TYPE_POGO = 13;
                public static final int IMAGE_DEBUG_TYPE_ILTCG = 14;
                public static final int IMAGE_DEBUG_TYPE_MPX = 15;
                public static final int IMAGE_DEBUG_TYPE_REPRO = 16;
                public static final int EMBEDDED = 17;
                public static final int HASH = 19;
                public static final int IMAGE_DEBUG_TYPE_EX_DLLCHARACTERISTICS = 20;

                @Override
                public void read(ByteBuffer source, DebugDirectoryEntry dest) {
                    dest.type = BinIOTools.getUnsignedInt(source);
                }

                @Override
                public void write(DebugDirectoryEntry source, ByteBuffer dest) {
                    BinIOTools.putUnsignedInt(dest,source.type);
                }

                @Override
                public int size() {
                    return DWORD;
                }

                @Override
                public Optional<String> getStringValue(DebugDirectoryEntry format, DisplayFormat displayFormat) {
                    String result = null;
                    switch ((int)format.type) {
                        case IMAGE_DEBUG_TYPE_UNKNOWN: result = "Unknown"; break;
                        case IMAGE_DEBUG_TYPE_COFF: result = "COFF"; break;
                        case IMAGE_DEBUG_TYPE_CODEVIEW: result = "CodeView"; break;
                        case IMAGE_DEBUG_TYPE_FPO: result = "FPO"; break;
                        case IMAGE_DEBUG_TYPE_MISC: result = "Misc"; break;
                        case IMAGE_DEBUG_TYPE_EXCEPTION: result = "Exception"; break;
                        case IMAGE_DEBUG_TYPE_FIXUP: result = "Fixup"; break;
                        case IMAGE_DEBUG_TYPE_OMAP_TO_SRC: result = "OMAP to src"; break;
                        case IMAGE_DEBUG_TYPE_OMAP_FROM_SRC: result = "OMAP from src"; break;
                        case IMAGE_DEBUG_TYPE_BORLAND: result = "Borland"; break;
                        case IMAGE_DEBUG_TYPE_RESERVED10: result = "Reserved10"; break;
                        case IMAGE_DEBUG_TYPE_CLSID: result = "CLSID"; break;
                        case IMAGE_DEBUG_TYPE_VC_FEATURE: result = "VC feature"; break;
                        case IMAGE_DEBUG_TYPE_POGO: result = "POGO"; break;
                        case IMAGE_DEBUG_TYPE_ILTCG: result = "ILTCG"; break;
                        case IMAGE_DEBUG_TYPE_MPX: result = "MPX"; break;
                        case IMAGE_DEBUG_TYPE_REPRO: result = "REPRO"; break;
                        case EMBEDDED: result = "Embedded"; break;
                        case HASH: result = "Hash"; break;
                        case IMAGE_DEBUG_TYPE_EX_DLLCHARACTERISTICS: result = "Extended DLL characteristics"; break;
                        default: result = null;
                    }
                    return Optional.ofNullable(result);
                }
            },
            SizeOfData {
                @Override
                public void read(ByteBuffer source, DebugDirectoryEntry dest) {
                    dest.sizeOfData = BinIOTools.getUnsignedInt(source);
                }

                @Override
                public void write(DebugDirectoryEntry source, ByteBuffer dest) {
                    BinIOTools.putUnsignedInt(dest,source.sizeOfData);
                }

                @Override
                public int size() {
                    return DWORD;
                }

                @Override
                public Optional<String> getStringValue(DebugDirectoryEntry format, DisplayFormat displayFormat) {
                    return Optional.empty();
                }
            },
            AddressOfRawData {
                @Override
                public void read(ByteBuffer source, DebugDirectoryEntry dest) {
                    dest.addressOfRawData = BinIOTools.getUnsignedInt(source);
                }

                @Override
                public void write(DebugDirectoryEntry source, ByteBuffer dest) {
                    BinIOTools.putUnsignedInt(dest,source.addressOfRawData);
                }

                @Override
                public int size() {
                    return DWORD;
                }

                @Override
                public Optional<String> getStringValue(DebugDirectoryEntry format, DisplayFormat displayFormat) {
                    return Optional.empty();
                }
            },
            PointerToRawData {
                @Override
                public void read(ByteBuffer source, DebugDirectoryEntry dest) {
                    dest.pointerToRawData = BinIOTools.getUnsignedInt(source);
                }

                @Override
                public void write(DebugDirectoryEntry source, ByteBuffer dest) {
                    BinIOTools.putUnsignedInt(dest,source.pointerToRawData);
                }

                @Override
                public int size() {
                    return DWORD;
                }

                @Override
                public Optional<String> getStringValue(DebugDirectoryEntry format, DisplayFormat displayFormat) {
                    return Optional.empty();
                }
            },

        }

        public long characteristics;
        public long timedatestamp;
        public int majorVersion, minorVersion;
        public long type;
        public long sizeOfData;
        public long addressOfRawData;
        public long pointerToRawData;

        @Override
        public Collection<? extends FieldSequenceFormat.Field> fields() {
            return Arrays.asList(DebugDirectoryEntry.Field.values());
        }

        @Override
        public int getSize() {
            return SIZE;
        }

        @Override
        public String getStringValue() {
            return "DebugDirectoryEntry";
        }

        @Override
        public boolean isDisplayFormatSupported(String fieldName, DisplayFormat format) {
            return false;
        }

        @Override
        public void setStringValue(String fieldName, DisplayFormat format) {

        }
    }

    public List<DebugDirectoryEntry> entries = new ArrayList<>();

    @Override
    public void writeTo(ByteBuffer out) {

    }

    public void readFrom(ByteBuffer bb, long size) {
        entries = new ArrayList<>();
        long remainingSize = size;
        while (remainingSize > 0) {
            DebugDirectoryEntry entry = new DebugDirectoryEntry();
            entry.readFrom(bb);
            entries.add(entry);
            remainingSize -= entry.getSize();
        }
    }

    @Override
    public int getSize() {
        return entries.size() * DebugDirectoryEntry.SIZE;
    }

    @Override
    public String getStringValue() {
        return "DebugDirectoryBlock";
    }
}

package llj.packager.winpe;

import llj.packager.Format;
import llj.util.BinIOTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class BaseRelocationsBlock implements Format {
    
    public long pageRva;
    public long blockSize;
    
    public static class Entry {
        
        public enum Type {
            IMAGE_REL_BASED_ABSOLUTE, 
            IMAGE_REL_BASED_HIGH,
            IMAGE_REL_BASED_LOW,
            IMAGE_REL_BASED_HIGHLOW,
            IMAGE_REL_BASED_HIGHADJ,
            IMAGE_REL_BASED_MIPS_JMPADDR,
            RESERVED,
            IMAGE_REL_BASED_THUMB_MOV32,
            IMAGE_REL_BASED_RISCV_LOW12S,
            IMAGE_REL_BASED_MIPS_JMPADDR16,
            IMAGE_REL_BASED_DIR64
        }
        
        public Type type;
        public long offset;
        
        public static final int SIZE = WORD;
    }
    
    public List<Entry> entries;
    
    public int calculateSize() {
        return (entries.size() * Entry.SIZE) + DWORD + DWORD;
    }

    public void readFrom(ByteBuffer readBuffer) {
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        pageRva = BinIOTools.getUnsignedInt(readBuffer);
        blockSize = BinIOTools.getUnsignedInt(readBuffer);
        long expectedNumEntries = (blockSize - DWORD - DWORD) / Entry.SIZE;
        entries = new ArrayList<>((int)expectedNumEntries);

            for (int i = 0; i < expectedNumEntries; i++) {
                Entry entry = new Entry();
                long val = BinIOTools.getUnsignedShort(readBuffer);
                entry.offset = (int)(val & ((2 << 13) - 1));
                int typeC = (int)(val >> 12);
                for (Entry.Type typeE : Entry.Type.values()) {
                    if (typeE.ordinal() == typeC) {
                        entry.type = typeE;
                    }
                }
                entries.add(entry);
            }

    }
    

    @Override
    public void writeTo(ByteBuffer out) {
        
    }

    @Override
    public int getSize() {
        return (int) blockSize;
    }

    @Override
    public String getStringValue() {
        return null;
    }
}

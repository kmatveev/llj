package llj.packager.objcoff;

import llj.packager.Format;
import llj.packager.coff.COFFHeader;
import llj.packager.coff.RelocationEntry;
import llj.packager.coff.Section;
import llj.packager.coff.SymbolTableEntry;
import llj.packager.winpe.PEFormatException;
import llj.packager.coff.SectionHeader;
import llj.util.BinIOTools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OBJCOFFFormat implements Format {

    public final COFFHeader coffHeader = new COFFHeader();
    public final List<Section> sections = new ArrayList<Section>();
    public final List<SymbolTableEntry> symbolTable = new ArrayList<SymbolTableEntry>();

    @Override
    public void writeTo(ByteBuffer out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStringValue() {
        throw new UnsupportedOperationException();
    }

    public void readFrom(SeekableByteChannel in) throws PEFormatException, IOException {

        ByteBuffer readBuffer = ByteBuffer.allocate(500);
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // read COFF header
        {
            Object lastField = coffHeader.readFrom(in, readBuffer);

            if (lastField != null) {
                throw new PEFormatException("Unable to read OBJ file, COFF header is shorter than expected size");
            }

        }

//        // read PE optional COFF header
//        {
//            // no need to read more bytes, since we will not parse them
//            int peOptionalHeaderReadSize = Math.max(coffHeader.sizeOfOptionalHeader, PEFormat.COFFOptionalHeaderPE32.SIZE);
//
//            Object lastField = coffOptionalHeaderPE.readFrom(in, readBuffer, peOptionalHeaderReadSize);
//
//            if (lastField != null) {
//                throw new PEFormatException("Unable to read PE file, PE optional header is shorter than expected size");
//            }
//
//        }

        for (int i = 0; i < coffHeader.numberOfSections; i++) {
            SectionHeader sectionHeader = new SectionHeader();
            sectionHeader.readFrom(in, readBuffer);
            sections.add(new Section(sectionHeader));
        }

        for (Section section : sections) {
            in.position(section.sectionHeader.pointerToRelocations);
            for (int i = 0; i < section.sectionHeader.numberOfRelocations; i++) {
                RelocationEntry relocationEntry = new RelocationEntry();
                relocationEntry.readFrom(in, readBuffer);
                section.relocations.add(relocationEntry);
            }
        }

        in.position(coffHeader.pointerToSymbolTable);
        for (int i = 0; i < coffHeader.numberOfSymbols; i++) {
            SymbolTableEntry symbolTableEntry = new SymbolTableEntry();
            symbolTableEntry.readFrom(in, readBuffer);
            symbolTable.add(symbolTableEntry);
        }

        in.position(getStringsOffset());
        // long numOfStrings = BinIOTools.getUnsignedInt(in);

    }

    private long getStringsOffset() {
        return coffHeader.pointerToSymbolTable + (coffHeader.numberOfSymbols * SymbolTableEntry.SIZE);
    }

    public int getCOFFHeadersTotalSize() {
        return COFFHeader.SIZE + coffHeader.sizeOfOptionalHeader;
    }

    public long getSectionHeadersOffset() {
        return (getCOFFHeadersTotalSize());
    }

    public long getSectionHeadersSize() {
        return (sections.size() * SectionHeader.SIZE);
    }


}

package llj.packager.objcoff;

import llj.packager.Format;
import llj.packager.coff.*;
import llj.util.BinIOTools;
import llj.util.BinTools;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.*;

public class OBJCOFFFormat implements Format {

    public final COFFHeader coffHeader = new COFFHeader();
    public final List<Section> sections = new ArrayList<Section>();
    public final List<Symbol> symbols = new ArrayList<Symbol>();
    public long sizeOfStringArea; // this size includes 4 bytes for size itself
    public final List<COFFStringEntry> coffStrings = new ArrayList<>();
    public final Map<COFFStringEntry, List<String>> coffStringsUsage = new HashMap<>();

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

    public void readFrom(SeekableByteChannel in) throws OBJCOFFFormatException, IOException {

        ByteBuffer readBuffer = ByteBuffer.allocate(500);
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // read COFF header
        {
            Object lastField = coffHeader.readFrom(in, readBuffer);

            if (lastField != null) {
                throw new OBJCOFFFormatException("Unable to read OBJ file, COFF header is shorter than expected size");
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
        int auxCount = 0;
        for (int i = 0; i < coffHeader.numberOfSymbols; i++) {
            long symbolOffset = in.position() - coffHeader.pointerToSymbolTable;
            SymbolTableEntry symbolTableEntry = new SymbolTableEntry();
            symbolTableEntry.readFrom(in, readBuffer);
            if (auxCount == 0) {
                symbols.add(new Symbol(symbolOffset, symbolTableEntry, false));
                auxCount = symbolTableEntry.auxCount;
            } else {
                symbols.get(symbols.size() - 1).auxSymbols.add(new Symbol(symbolOffset, symbolTableEntry, true));
                auxCount--;
            }
        }

        in.position(getStringsOffset());
        try {
            sizeOfStringArea = BinIOTools.getUnsignedInt(in);
            ByteBuffer stringsContent = ByteBuffer.allocate((int) (sizeOfStringArea - 4)); // sizeOfStringArea includes 4 bytes for size itself
            BinIOTools.readIntoBuffer(in, stringsContent, stringsContent.capacity());
            while (stringsContent.hasRemaining()) {
                int stringOffset = stringsContent.position() + 4;
                String strVal = BinTools.readZeroTerminatedAsciiString(stringsContent);
                coffStrings.add(new COFFStringEntry(stringOffset, strVal));
            }
        } catch (ReadException e) {
            throw new OBJCOFFFormatException("Unable to read number of strings", e);
        }

        for (Symbol symbol : symbols) {

            NameOrStringTablePointer symbolName = symbol.symbolTableEntry.name;
            String resolvedName;
            if (symbolName.type == NameOrStringTablePointer.Type.NAME) {
                resolvedName = new String(symbolName.name);
            } else if (symbolName.type == NameOrStringTablePointer.Type.STRING_TABLE_POINTER) {
                COFFStringEntry coffString = findByOffset(symbolName.stringTablePointer, "Symbol at " + symbol.offsetInSymbolsArea);
                resolvedName = coffString == null ? "" : coffString.value;
            } else {
                throw new RuntimeException();
            }
            symbol.resolvedName = resolvedName;

            if (symbol.symbolTableEntry.sectionNumber > 0 &&  symbol.symbolTableEntry.sectionNumber < 0x8FFF) {
                symbol.resolvedSection = sections.get(symbol.symbolTableEntry.sectionNumber - 1);
            }

        }

    }

    public long getStringsOffset() {
        return coffHeader.pointerToSymbolTable + (coffHeader.numberOfSymbols * SymbolTableEntry.SIZE);
    }

    public COFFStringEntry findByOffset(long offset, String usage) throws OBJCOFFFormatException {
        if (offset <= 0) return null;
        for(COFFStringEntry stringEntry : coffStrings) {
            if (stringEntry.offsetInStringsArea == offset) {
                COFFStringEntry result = stringEntry;
                coffStringsUsage.merge(stringEntry, Arrays.asList(usage), (o, n) -> {List<String> r = new ArrayList<String>(o); r.addAll(n); return r;});
                return result;
            }
        }
        if (offset > sizeOfStringArea) throw new OBJCOFFFormatException("string offset is too large");
        // TODO we should also support pointers to non-first symbols of strings. In this case we should produce a temp string and return it
        throw new OBJCOFFFormatException("Pointers to substrings are not supported");
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

package llj.packager.winpe;

import llj.packager.Format;
import llj.packager.RawFormat;
import llj.packager.coff.COFFHeader;
import llj.packager.coff.COFFOptionalHeaderStandard;
import llj.packager.coff.RelocationEntry;
import llj.packager.coff.Section;
import llj.packager.coff.SectionHeader;
import llj.packager.coff.SymbolTableEntry;
import llj.packager.dosexe.DOSHeader;
import llj.util.BinIOTools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static llj.util.BinIOTools.*;

public class PEFormat implements Format {

    public static final String[] DIRECTORY_ENTRY_NAMES = new String[]{"Export table", "Import table", "Resource table", "Exception table", "Certificate table", "Base relocation table", "Debug", "Architecture", "Global ptr", "TLS table", "Load config table", "Bound import", "IAT", "Delay import descriptor", "CLR Runtime header", "Reserved"};
    public static final int EXPORTS_INDEX = 0, IMPORTS_INDEX = 1, RESOURCES_INDEX = 2, RELOCS_INDEX = 5;
    public final ExtendedDOSHeader dosHeader = new ExtendedDOSHeader();
    public final COFFHeader coffHeader = new COFFHeader();
    
    public COFFOptionalHeaderStandard<Object> coffOptionalHeaderStandard = null;
    public COFFOptionalHeaderPE32 coffOptionalHeaderPE32 = null;
    public COFFOptionalHeaderPE32Plus coffOptionalHeaderPE32Plus = null;
    
    public ExportBlock exports;
    public List<ImportBlock> imports;
    public List<BaseRelocationsBlock> baseRelocations;
    public ResourceDirectory resourceRoot;
    
    public final List<Section> sections = new ArrayList<Section>();
    public final List<SymbolTableEntry> symbolTable = new ArrayList<SymbolTableEntry>();
    
    public final RawFormat dosStub = new RawFormat(null);
    public boolean peSignatureInPlace;

    public static int setBit(int bit) {
        return 1 << (bit);
    }

    public static long setBit(long bit) {
        return 1 << (bit);
    }

    int getPEHeadersTotalSize() {
        return COFFHeader.SIZE + coffHeader.sizeOfOptionalHeader;
    }

    long getPEOffset() {
        return dosHeader.newPos;
    }
    
    long getCOFFHeaderOffset() {
        return getPEOffset() + 4;
    }

    long getSectionHeadersOffset() {
        return (getCOFFHeaderOffset() + getPEHeadersTotalSize());
    }

    long getSectionHeadersSize() {
        return (sections.size() * SectionHeader.SIZE);
    }

    public void writeTo(WritableByteChannel out) {
        throw new UnsupportedOperationException();
    }

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
        return null;
    }

    public void readFrom(SeekableByteChannel in) throws PEFormatException, IOException {

        ByteBuffer readBuffer = ByteBuffer.allocate(500);
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);

        {
            Object lastField = dosHeader.readFrom(in, readBuffer);

            if (lastField != null) {
                throw new PEFormatException("Unable to read PE file, it is shorter than expected size of DOS EXE header");
            }
        }

        if (!Arrays.equals(dosHeader.signature, DOSHeader.MAGIC)) {
            throw new PEFormatException("Unable to read PE file, there are no DOS EXE signature in place");
        }

        long peHeaderPos = dosHeader.newPos;
        in.position(peHeaderPos);

        try {
            peSignatureInPlace = readPESignature(in, readBuffer);
        } catch (IOException e) {
            throw new PEFormatException("Unable to read PE file, got I/O error trying to read PE signature, most likely it is beyond file size: " + peHeaderPos);
        }
        if (!peSignatureInPlace) {
            throw new PEFormatException("Unable to read PE file, there are no PE signature in place");
        }

        // read COFF header
        {
            Object lastField = coffHeader.readFrom(in, readBuffer);

            if (lastField != null) {
                throw new PEFormatException("Unable to read PE file, COFF header is shorter than expected size");
            }

        }

        if (coffHeader.sizeOfOptionalHeader == 0) {
            throw new PEFormatException("Unable to read PE file, COFF optional header is absent");
        }

        // read PE optional COFF header
        {
            COFFOptionalHeaderStandard<Object> coffOptionalHeaderStandard = new COFFOptionalHeaderStandard<Object>();
            
            // no need to read more bytes, since we will not parse them
            int peOptionalHeaderReadSize = coffHeader.sizeOfOptionalHeader;

            Object lastField = coffOptionalHeaderStandard.readFrom(in, readBuffer, peOptionalHeaderReadSize);
            
            if (COFFOptionalHeaderPE32.canExtend(coffOptionalHeaderStandard)) {
                coffOptionalHeaderPE32 = COFFOptionalHeaderPE32.extend(coffOptionalHeaderStandard);
            } else if (COFFOptionalHeaderPE32Plus.canExtend(coffOptionalHeaderStandard)) {
                coffOptionalHeaderPE32Plus = COFFOptionalHeaderPE32Plus.extend(coffOptionalHeaderStandard);
            } else {
                this.coffOptionalHeaderStandard = coffOptionalHeaderStandard; 
            }

            if (lastField != null) {
                throw new PEFormatException("Unable to read PE file, PE optional header is shorter than expected size");
            }

        }

        for (int i = 0; i < coffHeader.numberOfSections; i++) {
            SectionHeader sectionHeader = new SectionHeader();
            sectionHeader.readFrom(in, readBuffer);
            Section section = new Section(sectionHeader);
            sections.add(section);
        }
        
        // now we've completed sequential reading of total COFF header (including optional header and section headers)
        // so from this point we can use in.position to move file read pointer
        
        for (Section section : sections) {
            section.readFrom(in);
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

        
        if (coffOptionalHeaderPE32 != null) {
            if ((coffOptionalHeaderPE32.dataDirectory.size() > EXPORTS_INDEX) && coffOptionalHeaderPE32.dataDirectory.get(EXPORTS_INDEX).VirtualAddress > 0) {
                long exportDirTableRva = coffOptionalHeaderPE32.dataDirectory.get(EXPORTS_INDEX).VirtualAddress;
                readExportDirectoryTable(exportDirTableRva);
            }
            if ((coffOptionalHeaderPE32.dataDirectory.size() > IMPORTS_INDEX) && coffOptionalHeaderPE32.dataDirectory.get(IMPORTS_INDEX).VirtualAddress > 0) {
                imports = new ArrayList<>();
                long importDirTableRva = coffOptionalHeaderPE32.dataDirectory.get(IMPORTS_INDEX).VirtualAddress;
                readImportDirectoryTable(importDirTableRva, false);
            }
            if ((coffOptionalHeaderPE32.dataDirectory.size() > RESOURCES_INDEX) && coffOptionalHeaderPE32.dataDirectory.get(RESOURCES_INDEX).VirtualAddress > 0) {
                long resourceRootRva = coffOptionalHeaderPE32.dataDirectory.get(RESOURCES_INDEX).VirtualAddress;
                Section section = findSectionByRVA(resourceRootRva);
                ByteBuffer bb = section.getByVirtualAddress(resourceRootRva);
                resourceRoot = new ResourceDirectory();
                List<Section.Usage> usages = resourceRoot.resolve(bb, "/Root");
                section.usages.addAll(usages);
            }
            if ((coffOptionalHeaderPE32.dataDirectory.size() > RELOCS_INDEX) && coffOptionalHeaderPE32.dataDirectory.get(5).VirtualAddress > RELOCS_INDEX) {
                long relocBlocksRva = coffOptionalHeaderPE32.dataDirectory.get(RELOCS_INDEX).VirtualAddress;
                Section section = findSectionByRVA(relocBlocksRva);
                ByteBuffer bb = section.getByVirtualAddress(relocBlocksRva);
                long relocBlocksSize = coffOptionalHeaderPE32.dataDirectory.get(RELOCS_INDEX).Size;
                readBaseRelocations(bb, relocBlocksSize);
                
            }
            
        } else if (coffOptionalHeaderPE32Plus != null) {
            if ((coffOptionalHeaderPE32Plus.dataDirectory.size() > EXPORTS_INDEX) && coffOptionalHeaderPE32Plus.dataDirectory.get(EXPORTS_INDEX).VirtualAddress > 0) {
                long exportDirTableRva = coffOptionalHeaderPE32Plus.dataDirectory.get(EXPORTS_INDEX).VirtualAddress;
                readExportDirectoryTable(exportDirTableRva);
            }
            if ((coffOptionalHeaderPE32Plus.dataDirectory.size() > IMPORTS_INDEX) && coffOptionalHeaderPE32Plus.dataDirectory.get(IMPORTS_INDEX).VirtualAddress > 0) {
                imports = new ArrayList<>();
                long importDirTableRva = coffOptionalHeaderPE32Plus.dataDirectory.get(IMPORTS_INDEX).VirtualAddress;
                readImportDirectoryTable(importDirTableRva, true);
            }
            if ((coffOptionalHeaderPE32Plus.dataDirectory.size() > RESOURCES_INDEX) && coffOptionalHeaderPE32Plus.dataDirectory.get(RESOURCES_INDEX).VirtualAddress > 0) {
                long resourceRootRva = coffOptionalHeaderPE32Plus.dataDirectory.get(RESOURCES_INDEX).VirtualAddress;
                Section section = findSectionByRVA(resourceRootRva);
                ByteBuffer bb = section.getByVirtualAddress(resourceRootRva);
                resourceRoot = new ResourceDirectory();
                List<Section.Usage> usages = resourceRoot.resolve(bb, "/Root");
                section.usages.addAll(usages);
            }
            if ((coffOptionalHeaderPE32Plus.dataDirectory.size() > RELOCS_INDEX) && coffOptionalHeaderPE32Plus.dataDirectory.get(RELOCS_INDEX).VirtualAddress > 0) {
                long relocBlocksRva = coffOptionalHeaderPE32Plus.dataDirectory.get(RELOCS_INDEX).VirtualAddress;
                Section section = findSectionByRVA(relocBlocksRva);
                ByteBuffer bb = section.getByVirtualAddress(relocBlocksRva);
                long relocBlocksSize = coffOptionalHeaderPE32Plus.dataDirectory.get(RELOCS_INDEX).Size;
                readBaseRelocations(bb, relocBlocksSize);
            }
            
        }

        

    }
    
    public DirectoryEntry getDirectoryEntry(int index) {
        if (coffOptionalHeaderPE32 != null) {
            return coffOptionalHeaderPE32.dataDirectory.get(index);
        } else if (coffOptionalHeaderPE32Plus != null) {
            return coffOptionalHeaderPE32Plus.dataDirectory.get(index);
        } else {
            return null;
        }
    }

    public void readExportDirectoryTable(long exportDirTableRva) {
        Section section = findSectionByRVA(exportDirTableRva);
        ByteBuffer bb = section.getByVirtualAddress(exportDirTableRva);
        int count = 0;
        int size = 0;

        ExportDirectoryTableEntry entry = new ExportDirectoryTableEntry();
        entry.readFrom(bb);
        size += entry.getSize();
        exports = new ExportBlock(entry);
        Section exportsNameSection = findSectionByRVA(entry.nameRva);
        exports.name = exportsNameSection.getStringByVirtualAddress(entry.nameRva);
        addSectionUsage(exportsNameSection, entry.nameRva, exports.name.length() + 1, "ExportDirectoryTableEntry.Name");

        if (entry.numEntries > 0) {
            Section exportAddressTableSection = findSectionByRVA(entry.exportAddressTableRva);
            ByteBuffer exportAddressTableRaw = exportAddressTableSection.getByVirtualAddress(entry.exportAddressTableRva);
            addSectionUsage(exportAddressTableSection, entry.exportAddressTableRva, entry.numEntries * ExportAddressTableEntry.SIZE, "ExportAddressTable");
            for (int i = 0; i < entry.numEntries; i++) {
                ExportAddressTableEntry addressTableEntry = new ExportAddressTableEntry(BinIOTools.getUnsignedInt(exportAddressTableRaw));
                exports.exportedFunctions.add(addressTableEntry);
            }
        }

        if (entry.numNamePointers > 0) {
            Section exportNameTableSection = findSectionByRVA(entry.namePointerRva);
            ByteBuffer exportNamesTableRaw = exportNameTableSection.getByVirtualAddress(entry.namePointerRva);
            addSectionUsage(exportNameTableSection, entry.namePointerRva, entry.numNamePointers * 4, "ExportNamesTable");

            Section exportOrdinalTableSection = findSectionByRVA(entry.ordinalTableRva);
            ByteBuffer exportOrdinalTableRaw = exportOrdinalTableSection.getByVirtualAddress(entry.ordinalTableRva);
            addSectionUsage(exportOrdinalTableSection, entry.ordinalTableRva, entry.numNamePointers * 2, "ExportOrdinalsTable");

            for (int i = 0; i < entry.numNamePointers; i++) {
                long nameRva = BinIOTools.getUnsignedInt(exportNamesTableRaw);
                Section exportedNameSection = findSectionByRVA(nameRva);
                String exportedName = exportedNameSection.getStringByVirtualAddress(nameRva);
                exports.exportedFunctionNames.add(exportedName);
                addSectionUsage(exportedNameSection, nameRva, exportedName.length() + 1, "ExportedName#" + i);

                int addressTableIndexBiased = BinIOTools.getUnsignedShort(exportOrdinalTableRaw);
                exports.exportedFunctionOrdinalIndexes.add((int) (addressTableIndexBiased - entry.ordinalBase + 1));

            }
        }

        addSectionUsage(section, exportDirTableRva, size, "ExportDirectoryTable");
    }

    public static boolean addSectionUsage(Section section, long nameRva, long length, String comment) {
        return section.usages.add(new Section.Usage(nameRva - section.sectionHeader.virtualAddress, length, comment));
    }

    public void readImportDirectoryTable(long importDirTableRva, boolean is64Bit) {
        Section section = findSectionByRVA(importDirTableRva);
        ByteBuffer bb = section.getByVirtualAddress(importDirTableRva);
        int count = 0;
        int size = 0;
        while (true) {
            ImportDirectoryTableEntry entry = new ImportDirectoryTableEntry();
            entry.readFrom(bb);
            size += entry.getSize();
            if (entry.allEmpty()) {
                break;
            } else {
                ImportBlock importBlock = new ImportBlock(entry, is64Bit);
                imports.add(importBlock);
                Section importBlockSection = findSectionByRVA(entry.nameRva);
                importBlock.name = importBlockSection.getStringByVirtualAddress(entry.nameRva);
                addSectionUsage(importBlockSection, entry.nameRva, importBlock.name.length() + 1, "ImportDirectoryTableEntry" + String.valueOf(count) + ".Name");
                Section importLookupTableSection = findSectionByRVA(entry.importLookupTableRva);
                ByteBuffer importLookupEntryRaw = importLookupTableSection.getByVirtualAddress(entry.importLookupTableRva);
                Section importAddressTableSection = findSectionByRVA(entry.importAddressTableRva);
                ByteBuffer importAddressEntryRaw = importAddressTableSection.getByVirtualAddress(entry.importAddressTableRva);
                for (int i = 0; true; i++) {
                    if (is64Bit) {
                        ImportLookupEntryPE32Plus lookupEntry = new ImportLookupEntryPE32Plus(BinIOTools.getLong(importLookupEntryRaw));
                        addSectionUsage(importLookupTableSection, entry.importLookupTableRva + i * ImportLookupEntryPE32Plus.SIZE, ImportLookupEntryPE32Plus.SIZE, "ImportLookupEntryPE32Plus#" + i);
                        ImportLookupEntryPE32Plus addressEntry = new ImportLookupEntryPE32Plus(BinIOTools.getLong(importAddressEntryRaw));
                        addSectionUsage(importAddressTableSection, entry.importAddressTableRva + i * ImportLookupEntryPE32Plus.SIZE, ImportLookupEntryPE32Plus.SIZE, "ImportAddressEntryPE32Plus#" + i);
                        if (lookupEntry.isEmpty()) {
                            break;
                        } else {
                            importBlock.importedFunctions64.add(lookupEntry);
                            if (!lookupEntry.isOrdinal()) {
                                Section hintNameSection = findSectionByRVA(lookupEntry.getHintNameRva());
                                HintNameEntry hintNameEntry = HintNameEntry.readFrom(hintNameSection.getByVirtualAddress(lookupEntry.getHintNameRva()));
                                addSectionUsage(hintNameSection, lookupEntry.getHintNameRva(), hintNameEntry.getSize(), "HintNameEntry");
                                String name = hintNameEntry.value;
                                importBlock.resolvedImportedFunctions.add(name);
                            } else {
                                importBlock.resolvedImportedFunctions.add(String.valueOf(lookupEntry.getOrdinal()));
                            }
                        }
                    } else {
                        ImportLookupEntryPE32 lookupEntry = new ImportLookupEntryPE32(BinIOTools.getInt(importLookupEntryRaw));
                        addSectionUsage(importLookupTableSection, entry.importLookupTableRva + i * ImportLookupEntryPE32.SIZE, ImportLookupEntryPE32.SIZE, "ImportLookupEntryPE32#" + i);
                        ImportLookupEntryPE32 addressEntry = new ImportLookupEntryPE32(BinIOTools.getInt(importAddressEntryRaw));
                        addSectionUsage(importAddressTableSection, entry.importAddressTableRva + i * ImportLookupEntryPE32.SIZE, ImportLookupEntryPE32.SIZE, "ImportAddressEntryPE32#" + i);
                        if (lookupEntry.isEmpty()) {
                            break;
                        } else {
                            importBlock.importedFunctions.add(lookupEntry);
                            if (!lookupEntry.isOrdinal()) {
                                Section hintNameSection = findSectionByRVA(lookupEntry.getHintNameRva());
                                HintNameEntry hintNameEntry = HintNameEntry.readFrom(hintNameSection.getByVirtualAddress(lookupEntry.getHintNameRva()));
                                addSectionUsage(hintNameSection, lookupEntry.getHintNameRva(), hintNameEntry.getSize(), "HintNameEntry");
                                String name = hintNameEntry.value;
                                importBlock.resolvedImportedFunctions.add(name);
                            } else {
                                importBlock.resolvedImportedFunctions.add(String.valueOf(lookupEntry.getOrdinal()));
                            }
                        }
                    }
                }
                
            }
            count += 1;
        }
        addSectionUsage(section, importDirTableRva, size, "ImportDirectoryTable");
    }

    public void readBaseRelocations(ByteBuffer bb, long size) {
        baseRelocations = new ArrayList<>();
        long remainingSize = size;
        while (remainingSize > 0) {
            BaseRelocationsBlock block = new BaseRelocationsBlock();
            block.readFrom(bb);
            baseRelocations.add(block);
            remainingSize -= block.getSize();
        }
    }

    public static boolean readPESignature(ReadableByteChannel channel, ByteBuffer bb) throws IOException {
        readIntoBuffer(channel, bb, 2);
        if (bb.get() == 0x50 && bb.get() == 0x45) {
            readIntoBuffer(channel, bb, 2);
            return true;
        } else {
            return false;
        }
    }

    public static void writePESignature(WritableByteChannel channel) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(new byte[]{0x50, 0x45, 0, 0});
        channel.write(bb);
    }

    public Section findSectionByRVA(long rva) {
        for (Section section : sections) {
            // in PE file sections contain RVA in field "Virtual address"
            if (section.sectionHeader.containsVirtualAddress(rva)) {
                return section;
            }
        }
        return null;
    }

    public long getImageBaseVA() {
        long imageBaseVA;
        if (coffOptionalHeaderPE32 != null) {
            imageBaseVA = coffOptionalHeaderPE32.imageBase;
        } else if (coffOptionalHeaderPE32Plus != null) {
            imageBaseVA = coffOptionalHeaderPE32Plus.imageBase;
        } else {
            throw new IllegalStateException("Can't find imageBaseVA");
        }
        return imageBaseVA;
    }

    // accepts full virtual address
    public Section findSectionByVA(long virtualAddress) {
        for (Section section : sections) {
            // in PE file sections contain RVA in field "Virtual address"
            if (section.sectionHeader.containsVirtualAddress(virtualAddress - getImageBaseVA())) {
                return section;
            }
        }
        return null;
    }
    
    public static String directoryNameByIndex(int idx) {
        return DIRECTORY_ENTRY_NAMES[idx];
    }

}

package llj.packager.winpe;

import llj.packager.Format;
import llj.packager.RawFormat;
import llj.packager.coff.*;
import llj.packager.dosexe.DOSHeader;
import llj.util.BinIOTools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static llj.util.BinIOTools.*;

public class PEFormat extends COFFBasedFormat<PEFormatException> implements Format {

    public static final String[] DIRECTORY_ENTRY_NAMES = new String[]{"Export table", "Import table", "Resource table", "Exception table", "Certificate table", "Base relocation table", "Debug", "Architecture", "Global ptr", "TLS table", "Load config table", "Bound import", "IAT", "Delay import descriptor", "CLR Runtime header", "Reserved"};
    public static final int EXPORTS_INDEX = 0, IMPORTS_INDEX = 1, RESOURCES_INDEX = 2, RELOCS_INDEX = 5, DELAY_IMPORTS_INDEX = 13;
    public final ExtendedDOSHeader dosHeader = new ExtendedDOSHeader();

    public COFFOptionalHeaderStandard<Object> coffOptionalHeaderStandard = null;
    public COFFOptionalHeaderPE32 coffOptionalHeaderPE32 = null;
    public COFFOptionalHeaderPE32Plus coffOptionalHeaderPE32Plus = null;
    
    public ExportBlock exports;
    public List<ImportBlock> imports, delayImports;
    public List<BaseRelocationsBlock> baseRelocations;
    public ResourceDirectory resourceRoot;
    
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

        try {
            super.readCOFFFormat(in, readBuffer);
        } catch (COFFFormatException e) {
            throw new PEFormatException(e);
        }

        if (coffOptionalHeaderPE32 != null) {
            if ((coffOptionalHeaderPE32.dataDirectory.size() > EXPORTS_INDEX) && coffOptionalHeaderPE32.dataDirectory.get(EXPORTS_INDEX).VirtualAddress > 0) {
                long exportDirTableRva = coffOptionalHeaderPE32.dataDirectory.get(EXPORTS_INDEX).VirtualAddress;
                readExportDirectoryTable(exportDirTableRva);
            }
            if ((coffOptionalHeaderPE32.dataDirectory.size() > IMPORTS_INDEX) && coffOptionalHeaderPE32.dataDirectory.get(IMPORTS_INDEX).VirtualAddress > 0) {
                imports = new ArrayList<>();
                DirectoryEntry directoryEntry = coffOptionalHeaderPE32.dataDirectory.get(IMPORTS_INDEX);
                readImportDirectoryTable(directoryEntry.VirtualAddress, false, directoryEntry.Size);
            }
            if ((coffOptionalHeaderPE32.dataDirectory.size() > RESOURCES_INDEX) && coffOptionalHeaderPE32.dataDirectory.get(RESOURCES_INDEX).VirtualAddress > 0) {
                long resourceRootRva = coffOptionalHeaderPE32.dataDirectory.get(RESOURCES_INDEX).VirtualAddress;
                Section section = findSectionByRVA(resourceRootRva);
                ByteBuffer bb = section.getByVirtualAddress(resourceRootRva);
                resourceRoot = new ResourceDirectory();
                List<Section.Usage> usages = resourceRoot.resolve(bb, "/Root");
                section.usages.addAll(usages);
            }
            if ((coffOptionalHeaderPE32.dataDirectory.size() > RELOCS_INDEX) && coffOptionalHeaderPE32.dataDirectory.get(RELOCS_INDEX).VirtualAddress > 0) {
                long relocBlocksRva = coffOptionalHeaderPE32.dataDirectory.get(RELOCS_INDEX).VirtualAddress;
                Section section = findSectionByRVA(relocBlocksRva);
                ByteBuffer bb = section.getByVirtualAddress(relocBlocksRva);
                long relocBlocksSize = coffOptionalHeaderPE32.dataDirectory.get(RELOCS_INDEX).Size;
                readBaseRelocations(bb, relocBlocksSize);
                
            }
            if ((coffOptionalHeaderPE32.dataDirectory.size() > DELAY_IMPORTS_INDEX) && coffOptionalHeaderPE32.dataDirectory.get(DELAY_IMPORTS_INDEX).VirtualAddress > 0) {
                delayImports = new ArrayList<>();
                DirectoryEntry directoryEntry = coffOptionalHeaderPE32.dataDirectory.get(DELAY_IMPORTS_INDEX);
                readDelayImportDirectoryTable(directoryEntry.VirtualAddress, true, directoryEntry.Size);
            }


            
        } else if (coffOptionalHeaderPE32Plus != null) {
            if ((coffOptionalHeaderPE32Plus.dataDirectory.size() > EXPORTS_INDEX) && coffOptionalHeaderPE32Plus.dataDirectory.get(EXPORTS_INDEX).VirtualAddress > 0) {
                long exportDirTableRva = coffOptionalHeaderPE32Plus.dataDirectory.get(EXPORTS_INDEX).VirtualAddress;
                readExportDirectoryTable(exportDirTableRva);
            }
            if ((coffOptionalHeaderPE32Plus.dataDirectory.size() > IMPORTS_INDEX) && coffOptionalHeaderPE32Plus.dataDirectory.get(IMPORTS_INDEX).VirtualAddress > 0) {
                imports = new ArrayList<>();
                DirectoryEntry directoryEntry = coffOptionalHeaderPE32Plus.dataDirectory.get(IMPORTS_INDEX);
                readImportDirectoryTable(directoryEntry.VirtualAddress, true, directoryEntry.Size);
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
            if ((coffOptionalHeaderPE32Plus.dataDirectory.size() > DELAY_IMPORTS_INDEX) && coffOptionalHeaderPE32Plus.dataDirectory.get(DELAY_IMPORTS_INDEX).VirtualAddress > 0) {
                delayImports = new ArrayList<>();
                DirectoryEntry directoryEntry = coffOptionalHeaderPE32Plus.dataDirectory.get(DELAY_IMPORTS_INDEX);
                readDelayImportDirectoryTable(directoryEntry.VirtualAddress, true, directoryEntry.Size);
            }

            
        }

        

    }

    public void readCOFFOptionalHeader(SeekableByteChannel in, ByteBuffer readBuffer) throws IOException, PEFormatException {

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
        exportsNameSection.addUsage(entry.nameRva, exports.name.length() + 1, "ExportDirectoryTableEntry.Name");

        if (entry.numEntries > 0) {
            Section exportAddressTableSection = findSectionByRVA(entry.exportAddressTableRva);
            ByteBuffer exportAddressTableRaw = exportAddressTableSection.getByVirtualAddress(entry.exportAddressTableRva);
            exportAddressTableSection.addUsage(entry.exportAddressTableRva, entry.numEntries * ExportAddressTableEntry.SIZE, "ExportAddressTable");
            for (int i = 0; i < entry.numEntries; i++) {
                ExportAddressTableEntry addressTableEntry = new ExportAddressTableEntry(BinIOTools.getUnsignedInt(exportAddressTableRaw));
                exports.exportedFunctions.add(addressTableEntry);
                Section exportedDataSection = findSectionByRVA(addressTableEntry.exportRvaOrForwarderRva);
                if (exportedDataSection == section) {
                    // if exportRvaOrForwarderRva points to exports section, then it is a forwarder value, which we can read and cache
                    String forwarderValue = exportedDataSection.getStringByVirtualAddress(addressTableEntry.exportRvaOrForwarderRva);
                    addressTableEntry.forwarderValue = forwarderValue;
                }
            }
        }

        if (entry.numNamePointers > 0) {
            Section exportNameTableSection = findSectionByRVA(entry.namePointerRva);
            ByteBuffer exportNamesTableRaw = exportNameTableSection.getByVirtualAddress(entry.namePointerRva);
            exportNameTableSection.addUsage(entry.namePointerRva, entry.numNamePointers * 4, "ExportNamesTable");

            Section exportOrdinalTableSection = findSectionByRVA(entry.ordinalTableRva);
            ByteBuffer exportOrdinalTableRaw = exportOrdinalTableSection.getByVirtualAddress(entry.ordinalTableRva);
            exportOrdinalTableSection.addUsage(entry.ordinalTableRva, entry.numNamePointers * 2, "ExportOrdinalsTable");

            for (int i = 0; i < entry.numNamePointers; i++) {
                long nameRva = BinIOTools.getUnsignedInt(exportNamesTableRaw);
                Section exportedNameSection = findSectionByRVA(nameRva);
                String exportedName = exportedNameSection.getStringByVirtualAddress(nameRva);
                exports.exportedFunctionNames.add(exportedName);
                exportedNameSection.addUsage(nameRva, exportedName.length() + 1, "ExportedName#" + i);

                int addressTableIndexBiased = BinIOTools.getUnsignedShort(exportOrdinalTableRaw);
                exports.exportedFunctionOrdinalIndexes.add((int) (addressTableIndexBiased - entry.ordinalBase + 1));

            }
        }

        section.addUsage(exportDirTableRva, size, "ExportDirectoryTable");
    }

    public void readImportDirectoryTable(long importDirTableRva, boolean is64Bit, long declaredSize) {
        Section section = findSectionByRVA(importDirTableRva);
        ByteBuffer bb = section.getByVirtualAddress(importDirTableRva, declaredSize);
        int count = 0;
        int size = 0;
        while (true) {
            ImportDirectoryTableEntry entry = new ImportDirectoryTableEntry();
            entry.readFrom(bb);
            size += entry.getSize();
            if (entry.allEmpty()) {
                break;
            } else {
                ImportBlock importBlock = ImportBlock.createFrom(entry, is64Bit);
                imports.add(importBlock);
                importBlock.processImportBlock(this, count);
            }
            count += 1;
        }
        section.addUsage(importDirTableRva, size, "ImportDirectoryTable");
    }

    public void readDelayImportDirectoryTable(long importDirTableRva, boolean is64Bit, long declaredSize) {
        Section section = findSectionByRVA(importDirTableRva);
        ByteBuffer bb = section.getByVirtualAddress(importDirTableRva, declaredSize);
        int count = 0;
        int size = 0;
        while (true) {
            DelayImportDirectoryTableEntry entry = new DelayImportDirectoryTableEntry();
            entry.readFrom(bb);
            size += entry.getSize();
            if (entry.allEmpty()) {
                break;
            } else {
                ImportBlock importBlock = ImportBlock.createFrom(entry, is64Bit);
                delayImports.add(importBlock);
                importBlock.processImportBlock(this, count);
            }
            count += 1;
        }
        section.addUsage(importDirTableRva, size, "DelayImportDirectoryTable");
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

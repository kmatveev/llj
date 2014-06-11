package llj.packager.winpe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import static llj.util.BinIOTools.*;

public class PEFormat {

    public final DOSHeader dosHeader = new DOSHeader();
    public final PEFileHeader peHeader = new PEFileHeader();
    public final PEOptionalHeader peOptionalHeader = new PEOptionalHeader();
    public final List<SectionHeader> sections = new ArrayList<SectionHeader>();

    int getPEHeadersTotalSize() {
        return PEFileHeader.SIZE + PEOptionalHeader.SIZE;
    }

    long getPEFileHeaderOffset() {
        return dosHeader.e_lfanew;
    }

    long getSectionHeadersOffset() {
        return (getPEFileHeaderOffset() + getPEHeadersTotalSize());
    }

    long getSectionHeadersSize() {
        return (sections.size() * SectionHeader.SIZE);
    }

    public void writeTo(WritableByteChannel out) {

    }

    public void readFrom(SeekableByteChannel in) throws Exception {

        ByteBuffer readBuffer = ByteBuffer.allocate(500);
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);

        dosHeader.readFrom(in, readBuffer);

        in.position(dosHeader.e_lfanew);

        peHeader.readSignature(in, readBuffer);
        peHeader.readFrom(in, readBuffer);

        peOptionalHeader.readFrom(in, readBuffer);

        for (int i = 0; i < peHeader.NumberOfSections; i++) {
            SectionHeader sectionHeader = new SectionHeader();
            sectionHeader.readFrom(in, readBuffer);
            sections.add(sectionHeader);
        }
    }

    public static class DOSHeader {

        public static final int SIZE = 64;
        public static final int MAGIC = 0x5A4D;

        public static enum Field {
            E_MAGIC {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_magic = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_magic); }
            },
            E_CBLP {
                public void read(ByteBuffer source, DOSHeader dest) { dest.e_cblp = getUnsignedShort(source);}
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_cblp); }
            },
            E_CP {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_cp = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_cp); }
            },
            E_CRLC {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_crlc = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_crlc); }
            },
            E_CPARHDR {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_cparhdr = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_cparhdr); }
            },
            E_MINALLOC {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_minalloc = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_minalloc); }
            },
            E_MAXALLOC {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_maxalloc = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_maxalloc); }
            },
            E_SS {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_ss = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_ss); }
            },
            E_SP {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_sp = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_sp); }
            },
            E_CSUM {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_csum = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_csum); }
            },
            E_IP {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_ip = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_ip); }
            },
            E_CS {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_cs = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_cs); }
            },
            E_LFARLC {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_lfarlc = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) {  putUnsignedShort(dest, source.e_lfarlc); }
            },
            E_OVNO {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_ovno = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_ovno); }
            },
            E_RES {
                public void read(ByteBuffer source, DOSHeader dest) {
                    for (int i = 0; i < dest.e_res.length; i++)
                        dest.e_res[i] = getUnsignedShort(source);
                }
                public void write(DOSHeader source, ByteBuffer dest) {
                    for (int i = 0; i < source.e_res.length; i++)
                        putUnsignedShort(dest, source.e_res[i]);
                }
            },
            E_OEMID {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_oemid = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_oemid);  }
            },
            E_OEMINFO {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_oeminfo = getUnsignedShort(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.e_oeminfo);  }
            },
            E_RES2 {
                public void read(ByteBuffer source, DOSHeader dest) {
                    for (int i = 0; i < dest.e_res2.length; i++) {
                        dest.e_res2[i] = getUnsignedShort(source);
                    }
                }
                public void write(DOSHeader source, ByteBuffer dest) {
                    for (int i = 0; i < source.e_res2.length; i++)
                        putUnsignedShort(dest, source.e_res2[i]);
                }
            },
            E_LFANEW {
                public void read(ByteBuffer source, DOSHeader dest) {dest.e_lfanew = getUnsignedInt(source); }
                public void write(DOSHeader source, ByteBuffer dest) { putUnsignedInt(dest, source.e_lfanew); }
            };
            public abstract void read(ByteBuffer source, DOSHeader dest);

            public abstract void write(DOSHeader source, ByteBuffer dest);

        }

        int e_magic = MAGIC;  // Magic number
        int e_cblp;          // Bytes on last page of file
        int e_cp;            // Pages in file
        int e_crlc;          // Relocations
        int e_cparhdr;       // Size of header in paragraphs
        int e_minalloc;      // Minimum extra paragraphs needed
        int e_maxalloc;      // Maximum extra paragraphs needed
        int e_ss;            // Initial (relative) SS value
        int e_sp;            // Initial SP value
        int e_csum;          // Checksum
        int e_ip;            // Initial IP value
        int e_cs;            // Initial (relative) CS value
        int e_lfarlc;        // File address of relocation table
        int e_ovno;          // Overlay number
        final int[] e_res = new int[4];        // Reserved words
        int e_oemid;         // OEM identifier (for e_oeminfo)
        int e_oeminfo;       // OEM information; e_oemid specific
        final int[] e_res2 = new int[10];      // Reserved words
        long e_lfanew;        // File address of new exe header

        public void readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws Exception {
            readIntoBuffer(in, readBuffer, SIZE);
            readFrom(readBuffer);
        }

        public void readFrom(ByteBuffer readBuffer) {
            for (DOSHeader.Field field : DOSHeader.Field.values()) {
                field.read(readBuffer, this);
            }
        }

        public void writeTo(ByteBuffer writeBuffer) {
            for (DOSHeader.Field field : DOSHeader.Field.values()) {
                field.write(this, writeBuffer);
            }
        }
    }

    public static class PEFileHeader {

        public static final int SIZE = 20;

        public static enum Field {
            MACHINE {
                public void read(ByteBuffer source, PEFileHeader dest) {dest.Machine = getUnsignedShort(source); }
                public void write(PEFileHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.Machine); }
            },
            NUMBER_OF_SECTIONS {
                public void read(ByteBuffer source, PEFileHeader dest) {dest.NumberOfSections = getUnsignedShort(source); }
                public void write(PEFileHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.NumberOfSections); }
            },
            TIME_DATE_STAMP {
                public void read(ByteBuffer source, PEFileHeader dest) {dest.TimeDateStamp = getUnsignedInt(source); }
                public void write(PEFileHeader source, ByteBuffer dest) { putUnsignedInt(dest, source.TimeDateStamp); }
            },
            POINTER_TO_SYMBOL_TABLE {
                public void read(ByteBuffer source, PEFileHeader dest) {dest.PointerToSymbolTable = getUnsignedInt(source); }
                public void write(PEFileHeader source, ByteBuffer dest) { putUnsignedInt(dest, source.PointerToSymbolTable); }
            },
            NUMBER_OF_SYMBOLS {
                public void read(ByteBuffer source, PEFileHeader dest) {dest.NumberOfSymbols = getUnsignedInt(source); }
                public void write(PEFileHeader source, ByteBuffer dest) { putUnsignedInt(dest, source.NumberOfSymbols); }
            },
            SIZE_OF_OPTIONAL_HEADER {
                public void read(ByteBuffer source, PEFileHeader dest) {dest.SizeOfOptionalHeader = getUnsignedShort(source); }
                public void write(PEFileHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.SizeOfOptionalHeader); }
            },
            CHARACTERISTICS {
                public void read(ByteBuffer source, PEFileHeader dest) {dest.Characteristics = getUnsignedShort(source); }
                public void write(PEFileHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.Characteristics); }
            };

            public abstract void read(ByteBuffer source, PEFileHeader dest);

            public abstract void write(PEFileHeader source, ByteBuffer dest);

        }

        int  Machine;
        int  NumberOfSections = 0;
        long TimeDateStamp;
        long PointerToSymbolTable;
        long NumberOfSymbols;
        int  SizeOfOptionalHeader = PEOptionalHeader.SIZE;
        int  Characteristics;

        public boolean readSignature(ReadableByteChannel channel, ByteBuffer bb) throws Exception {
            readIntoBuffer(channel, bb, 2);
            if (bb.get() == 0x50 && bb.get() == 0x45) {
                readIntoBuffer(channel, bb, 2);
                return true;
            } else {
                return false;
            }
        }

        public void writeSignature(WritableByteChannel channel) throws IOException {
            ByteBuffer bb = ByteBuffer.wrap(new byte[] {0x50, 0x45, 0, 0});
            channel.write(bb);
        }

        public void readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws Exception {
            readIntoBuffer(in, readBuffer, SIZE);
            readFrom(readBuffer);
        }

        public void readFrom(ByteBuffer readBuffer) {
            for (PEFileHeader.Field field : PEFileHeader.Field.values()) {
                field.read(readBuffer, this);
            }
        }

        public void writeTo(ByteBuffer writeBuffer) {
            for (PEFileHeader.Field field : PEFileHeader.Field.values()) {
                field.write(this, writeBuffer);
            }
        }

    }

    public static class PEOptionalHeader {

        public static final int SIZE = 224 ;
        public static final int MAGIC = 0x010b;

        public static enum Field {
            MAGIC {
                public void read(ByteBuffer source, PEOptionalHeader dest) { dest.Magic = getUnsignedShort(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.Magic);}
            },
            MAJOR_LINKER_VERSION {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.MajorLinkerVersion = getUnsignedByte(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedByte(dest, source.MajorLinkerVersion);}
            },
            MINOR_LINKER_VERSION {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.MinorLinkerVersion = getUnsignedByte(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedByte(dest, source.MinorLinkerVersion);}
            },
            SIZE_OF_CODE {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.SizeOfCode = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.SizeOfCode);}
            },
            SIZE_OF_INITIALIZED_DATA {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.SizeOfInitializedData = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.SizeOfInitializedData);}
            },
            SIZE_OF_UNINITIALIZED_DATA {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.SizeOfUninitializedData = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.SizeOfUninitializedData);}
            },
            ADDRESS_OF_ENTRY_POINT {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.AddressOfEntryPoint = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.AddressOfEntryPoint);}
            },
            BASE_OF_CODE {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.BaseOfCode = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.BaseOfCode);}
            },
            BASE_OF_DATA {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.BaseOfData = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) { putUnsignedInt(dest, source.BaseOfData);}
            },
            IMAGE_BASE {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.ImageBase = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.ImageBase);}
            },
            SECTION_ALIGNMENT {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.SectionAlignment = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.SectionAlignment);}
            },
            FILE_ALIGNMENT {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.FileAlignment = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) { putUnsignedInt(dest, source.FileAlignment);}
            },
            MAJOR_OS_VERSION {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.MajorOperatingSystemVersion = getUnsignedShort(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedShort(dest, source.MajorOperatingSystemVersion);}
            },
            MINOR_OS_VERSION {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.MinorOperatingSystemVersion = getUnsignedShort(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedShort(dest, source.MajorOperatingSystemVersion);}
            },
            MAJOR_IMAGE_VERSION {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.MajorImageVersion = getUnsignedShort(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedShort(dest, source.MajorImageVersion);}
            },
            MINOR_IMAGE_VERSION {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.MinorImageVersion = getUnsignedShort(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedShort(dest, source.MinorImageVersion);}
            },
            MAJOR_SUBSYSTEM_VERSION {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.MajorSubsystemVersion = getUnsignedShort(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedShort(dest, source.MajorSubsystemVersion);}
            },
            MINOR_SUBSYSTEM_VERSION {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.MinorSubsystemVersion = getUnsignedShort(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedShort(dest, source.MinorSubsystemVersion);}
            },
            RESERVED1 {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.Reserved1 = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.Reserved1);}
            },
            SIZE_OF_IMAGE {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.SizeOfImage = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.SizeOfImage);}
            },
            SIZE_OF_HEADERS {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.SizeOfHeaders = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.SizeOfHeaders);}
            },
            CHECKSUM {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.CheckSum = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.CheckSum);}
            },
            SUBSYSTEM {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.Subsystem = getUnsignedShort(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.Subsystem);}
            },
            DLL_CHARACTERISTICS {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.DllCharacteristics = getUnsignedShort(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedShort(dest, source.DllCharacteristics);}
            },
            SIZE_OF_STACK_RESERVE {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.SizeOfStackReserve = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.SizeOfStackReserve);}
            },
            SIZE_OF_STACK_COMMIT {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.SizeOfStackCommit = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.SizeOfStackCommit);}
            },
            SIZE_OF_HEAP_RESERVE {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.SizeOfHeapReserve = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.SizeOfHeapReserve);}
            },
            SIZE_OF_HEP_COMMIT {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.SizeOfHeapCommit = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.SizeOfHeapCommit);}
            },
            LOADER_FLAGS {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.LoaderFlags = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.LoaderFlags);}
            },
            NUMBER_OF_RVA_AND_SIZES {
                public void read(ByteBuffer source, PEOptionalHeader dest) {dest.NumberOfRvaAndSizes = getUnsignedInt(source);}
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.NumberOfRvaAndSizes);}
            },
            DATA_DIRECTORY {
                public void read(ByteBuffer source, PEOptionalHeader dest) {
                    for (DataDirectory directory : dest.DataDirectory) {
                        directory.readFrom(source);
                    }
                }
                public void write(PEOptionalHeader source, ByteBuffer dest) {
                    for (DataDirectory directory : source.DataDirectory) {
                        directory.writeTo(dest);
                    }
                }
            };

            public abstract void read(ByteBuffer source, PEOptionalHeader dest);

            public abstract void write(PEOptionalHeader source, ByteBuffer dest);

        }

        int  Magic = MAGIC;
        short   MajorLinkerVersion;
        short   MinorLinkerVersion;
        long   SizeOfCode;
        long   SizeOfInitializedData;
        long   SizeOfUninitializedData;
        long   AddressOfEntryPoint;
        long   BaseOfCode;
        long   BaseOfData;
        long   ImageBase;
        long   SectionAlignment;
        long   FileAlignment;
        int  MajorOperatingSystemVersion;
        int  MinorOperatingSystemVersion;
        int  MajorImageVersion;
        int  MinorImageVersion;
        int  MajorSubsystemVersion;
        int  MinorSubsystemVersion;
        long   Reserved1;
        long   SizeOfImage;
        long   SizeOfHeaders;
        long   CheckSum;
        int  Subsystem;
        int  DllCharacteristics;
        long   SizeOfStackReserve;
        long   SizeOfStackCommit;
        long   SizeOfHeapReserve;
        long   SizeOfHeapCommit;
        long   LoaderFlags;
        long   NumberOfRvaAndSizes;
        final DataDirectory[] DataDirectory = new DataDirectory[16];

        public PEOptionalHeader() {
            for (int i = 0; i < DataDirectory.length; i++) DataDirectory[i] = new DataDirectory();
        }

        public void readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws Exception {
            readIntoBuffer(in, readBuffer, SIZE);
            readFrom(readBuffer);
        }

        public void readFrom(ByteBuffer readBuffer) {
            for (PEOptionalHeader.Field field : PEOptionalHeader.Field.values()) {
                field.read(readBuffer, this);
            }
        }

        public void writeTo(ByteBuffer writeBuffer) {
            for (PEOptionalHeader.Field field : PEOptionalHeader.Field.values()) {
                field.write(this, writeBuffer);
            }
        }

    }

    public static class DataDirectory {
        long VirtualAddress;
        long Size;

        public void readFrom(ByteBuffer readBuffer) {
            VirtualAddress = getUnsignedInt(readBuffer);
            Size = getUnsignedInt(readBuffer);
        }

        public void writeTo(ByteBuffer writeBuffer) {
            putUnsignedInt(writeBuffer, VirtualAddress);
            putUnsignedInt(writeBuffer, Size);
        }

    }

    public static class SectionHeader {

        public static final int SIZE = 40;

        public static enum Field {
            NAME {
                public void read(ByteBuffer source, SectionHeader dest) {
                    for (int i = 0; i < dest.Name.length; i++) dest.Name[i] = getUnsignedChar(source);
                }
                public void write(SectionHeader source, ByteBuffer dest) {
                    for (int i = 0; i <source.Name.length; i++) putUnsignedChar(dest, source.Name[i]);
                }
            },
            PHYSICAL_ADDRESS_OR_VIRTUAL_SIZE {
                public void read(ByteBuffer source, SectionHeader dest) { dest.PhysicalAddressOrVirtualSize = getUnsignedInt(source); }
                public void write(SectionHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.PhysicalAddressOrVirtualSize); }
            },
            VIRTUAL_ADDRESS {
                public void read(ByteBuffer source, SectionHeader dest) { dest.VirtualAddress = getUnsignedInt(source); }
                public void write(SectionHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.VirtualAddress); }
            },
            SIZE_OF_RAW_DATA {
                public void read(ByteBuffer source, SectionHeader dest) { dest.SizeOfRawData = getUnsignedInt(source); }
                public void write(SectionHeader source, ByteBuffer dest) {  putUnsignedInt(dest, source.SizeOfRawData);}
            },
            POINTER_TO_RAW_DATA {
                public void read(ByteBuffer source, SectionHeader dest) {  dest.PointerToRawData = getUnsignedInt(source); }
                public void write(SectionHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.PointerToRawData); }
            },
            POINTER_TO_RELOCATIONS {
                public void read(ByteBuffer source, SectionHeader dest) { dest.PointerToRelocations = getUnsignedInt(source); }
                public void write(SectionHeader source, ByteBuffer dest) { putUnsignedInt(dest, source.PointerToRelocations);}
            },
            POINTER_TO_LINE_NUMBERS {
                public void read(ByteBuffer source, SectionHeader dest) { dest.PointerToLinenumbers = getUnsignedInt(source); }
                public void write(SectionHeader source, ByteBuffer dest) {
                    putUnsignedInt(dest, source.PointerToLinenumbers);  }
            },
            NUMBER_OF_RELOCATIONS {
                public void read(ByteBuffer source, SectionHeader dest) {  dest.NumberOfRelocations = getUnsignedShort(source); }
                public void write(SectionHeader source, ByteBuffer dest) { putUnsignedShort(dest, source.NumberOfRelocations);}
            },
            NUMBER_OF_LINE_NUMBERS {
                public void read(ByteBuffer source, SectionHeader dest) { dest.NumberOfLinenumbers = getUnsignedShort(source); }
                public void write(SectionHeader source, ByteBuffer dest) {
                    putUnsignedShort(dest, source.NumberOfLinenumbers); }
            },
            CHARACTERISTICS {
                public void read(ByteBuffer source, SectionHeader dest) { dest.Characteristics = getUnsignedInt(source); }
                public void write(SectionHeader source, ByteBuffer dest) { putUnsignedInt(dest, source.Characteristics);}
            };
            public abstract void read(ByteBuffer source, SectionHeader dest);

            public abstract void write(SectionHeader source, ByteBuffer dest);

            }

        final char[] Name = new char[8];
        long   PhysicalAddressOrVirtualSize;
        long   VirtualAddress;
        long   SizeOfRawData;
        long   PointerToRawData;
        long   PointerToRelocations;
        long   PointerToLinenumbers;
        int  NumberOfRelocations;
        int  NumberOfLinenumbers;
        long   Characteristics;

        public void readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws Exception {
            readIntoBuffer(in, readBuffer, SIZE);
            readFrom(readBuffer);
        }

        public void readFrom(ByteBuffer readBuffer) {
            for (SectionHeader.Field field : SectionHeader.Field.values()) {
                field.read(readBuffer, this);
            }
        }

        public void writeTo(ByteBuffer writeBuffer) {
            for (SectionHeader.Field field : SectionHeader.Field.values()) {
                field.write(this, writeBuffer);
            }
        }

    }

}

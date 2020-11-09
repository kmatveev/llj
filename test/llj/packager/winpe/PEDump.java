package llj.packager.winpe;

import llj.packager.coff.COFFHeader;
import llj.packager.coff.Section;
import llj.packager.coff.SectionHeader;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PEDump {

    public static void main(String[] args) {
        try {
            dump("c:\\windows\\system32\\calc.exe");
            // dump("c:\\windows\\system.ini");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dump(String file) throws Exception {

        Path path = FileSystems.getDefault().getPath(file);
        Set<OpenOption> opts = new HashSet<OpenOption>();
        opts.add(StandardOpenOption.READ);
        FileChannel fileChannel = FileChannel.open(path, opts);

        PEFormat peFormat = new PEFormat();
        try {
            peFormat.readFrom(fileChannel);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        long peHeaderOffset = peFormat.getCOFFHeaderOffset();

        System.out.println("File: '" + file + "'; Size: " + fileChannel.size());

        {
            int x = (peFormat.dosHeader.getDeclaredHeaderSize());
            long y = peHeaderOffset;
            System.out.println("DOS Header size: " + x);
            System.out.println("  DOS Header standard area size: " + peFormat.dosHeader.FIXED_HEADER_SIZE +
                               "; extension area size: " + peFormat.dosHeader.getHeaderExtensionsSize() +
                               "; relocations area size:" + peFormat.dosHeader.getRelocationsSize() +
                               "; free space:" + peFormat.dosHeader.getFreeSpaceSize());
            System.out.println("  DOS Header -> COFF Header offset: " + y + "; diff: " + (y - x));
        }

        {
            long x = peHeaderOffset;
            int y = peFormat.getPEHeadersTotalSize();
            System.out.println("COFF Header offset: " + x + "; COFF header and optional header total size: " + y + "; COFF Header ends at: " + (x + y));

            System.out.println("Machine is: " + COFFHeader.Machine.valueOf(peFormat.coffHeader.machine));

            List<COFFHeader.CharacteristicsField> fields = COFFHeader.CharacteristicsField.getAllSetInValue(peFormat.coffHeader.characteristics);
            System.out.println("Characteristics are: " + fields);
        }

        {
            long x = peFormat.getSectionHeadersOffset();
            int n = peFormat.coffHeader.numberOfSections;
            long y = peFormat.getSectionHeadersSize();
            System.out.println("PE Section headers start at: " + x + "; PE section headers total size: " + y + "; PE Section headers end at: " + (x + y));
        }

        List<Section> sections = peFormat.sections;
        System.out.println("Number of sections:" + sections.size());
        for (Section section : sections) {
            SectionHeader sectionHeader = section.sectionHeader;
            List<SectionHeader.CharacteristicsField> fields = SectionHeader.CharacteristicsField.getAllSetInValue(sectionHeader.characteristics);
            System.out.println("Section name: \"" + new String(sectionHeader.name.name) + "\"; raw data at: " + sectionHeader.pointerToRawData + "; raw data size: " + sectionHeader.sizeOfRawData + "; raw data ends at: " + (sectionHeader.pointerToRawData + sectionHeader.sizeOfRawData));
            System.out.println("  "  + "characteristics are: " + fields);
        }

        System.out.println("PE raw data start at:" + peFormat.coffOptionalHeaderPE32Plus.sizeOfHeaders);

//        List<String> verificationErrors = PEVerifier.validate(peFormat);
//        System.out.println("Verification errors: " + verificationErrors.size());
    }

}

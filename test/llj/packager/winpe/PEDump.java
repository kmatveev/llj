package llj.packager.winpe;

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
        peFormat.readFrom(fileChannel);

        long peHeaderOffset = peFormat.getPEFileHeaderOffset();

        System.out.println("File: '" + file + "'; Size: " + fileChannel.size());

        {
            int x = PEFormat.DOSHeader.SIZE;
            long y = peHeaderOffset;
            System.out.println("DOS Header size: " + x + "; PE Header offset: " + y + "; diff: " + (y - x));
        }

        {
            long x = peHeaderOffset;
            int y = peFormat.getPEHeadersTotalSize();
            System.out.println("PE Header offset: " + x + "; PE header and optional header total size: " + y + "; PE Header ends at:" + (x + y));
        }

        {
            long x = peFormat.getSectionHeadersOffset();
            long y = peFormat.getSectionHeadersSize();
            System.out.println("PE Section headers start at: " + x + "; PE section headers total size: " + y + "; PE Section headers end at:" + (x + y));
        }

        System.out.println("Raw data start at:" + peFormat.peOptionalHeader.SizeOfHeaders);

        List<PEFormat.SectionHeader> sections = peFormat.sections;
        System.out.println("Number of sections:" + sections.size());
        for (PEFormat.SectionHeader sectionHeader : sections) {
            System.out.println("Section name: " + new String(sectionHeader.Name) + "; raw data at: " + sectionHeader.PointerToRawData + "; raw data size: " + sectionHeader.SizeOfRawData + "; raw data ends at: " + (sectionHeader.PointerToRawData + sectionHeader.SizeOfRawData));
        }

        List<String> verificationErrors = PEVerifier.validate(peFormat);
        System.out.println("Verification errors: " + verificationErrors.size());
    }

}

package llj.packager.winpe;

import llj.packager.coff.COFFHeader;
import llj.packager.coff.Section;
import llj.packager.coff.SectionHeader;
import llj.packager.objcoff.OBJCOFFFormat;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OBJDump {

    public static void main(String[] args) {
        try {
            dump("c:\\users\\matvkon\\appdata\\local\\projects\\my-projects\\windows-master\\masm-tests\\assume\\test2.obj");
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


        OBJCOFFFormat objFormat = new OBJCOFFFormat();
        try {
            objFormat.readFrom(fileChannel);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("File: '" + file + "'; Size: " + fileChannel.size());


        {
            System.out.println("Machine is: " + COFFHeader.Machine.valueOf(objFormat.coffHeader.machine));

            List<COFFHeader.CharacteristicsField> fields = COFFHeader.CharacteristicsField.getAllSetInValue(objFormat.coffHeader.characteristics);
            System.out.println("Characteristics are: " + fields);
        }

        {
            long x = objFormat.getSectionHeadersOffset();
            int n = objFormat.coffHeader.numberOfSections;
            long y = objFormat.getSectionHeadersSize();
            System.out.println("COFF Section headers start at: " + x + "; COFF section headers total size: " + y + "; COFF Section headers end at: " + (x + y));
        }

        List<Section> sections = objFormat.sections;
        System.out.println("Number of sections:" + sections.size());
        for (Section section : sections) {
            SectionHeader sectionHeader = section.sectionHeader;
            List<SectionHeader.CharacteristicsField> fields = SectionHeader.CharacteristicsField.getAllSetInValue(sectionHeader.characteristics);

            System.out.println("Section name: \"" + new String(sectionHeader.name.name) + "\"; raw data at: " + sectionHeader.pointerToRawData + "; raw data size: " + sectionHeader.sizeOfRawData + "; raw data ends at: " + (sectionHeader.pointerToRawData + sectionHeader.sizeOfRawData));
            System.out.println("  "  + "number of relocations: " + sectionHeader.numberOfRelocations);
            System.out.println("  "  + "characteristics are: " + fields);
        }

        System.out.println("Number of symbols:" + objFormat.symbols.size());



    }

}

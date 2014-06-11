package llj.packager.winpe;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestPEPackager {

    public static void main(String[] args) {
        try {
            testPEFormatRead1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testPEFormatRead1() throws Exception {

        Path path = FileSystems.getDefault().getPath("c:\\windows\\system32\\calc.exe");
        Set<OpenOption> opts = new HashSet<OpenOption>();
        opts.add(StandardOpenOption.READ);
        FileChannel fileChannel = FileChannel.open(path, opts);

        PEFormat peFormat = new PEFormat();
        peFormat.readFrom(fileChannel);

        System.out.println(peFormat.peOptionalHeader.Magic + ", Should be: " + 267);
        List<PEFormat.SectionHeader> sections = peFormat.sections;
        System.out.println(sections.size() + ", Should be: " + 3);
        for (PEFormat.SectionHeader sectionHeader : sections) {
            System.out.println(new String(sectionHeader.Name));
        }

        List<String> verificationErrors = PEVerifier.validate(peFormat);
        System.out.println("Verification errors: " + verificationErrors.size());
    }

    public static void testPEFormatWrite1() {

        PEFormat peFormat = new PEFormat();

        ByteBuffer bb = ByteBuffer.allocate(500);
        peFormat.peOptionalHeader.writeTo(bb);
        if (bb.position() != PEFormat.PEOptionalHeader.SIZE) throw new RuntimeException("Optional header size is wrong");

    }


}

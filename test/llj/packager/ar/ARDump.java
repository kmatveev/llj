package llj.packager.ar;

import llj.util.BinIOTools;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ARDump {

    public static void main(String[] args) throws Exception {
        dump(new File("C:\\Users\\MatveevKV\\AppData\\Local\\tools\\asts-bridge-distr\\4.4.2\\embedded\\win64\\mtesrl64.lib"));
    }

    public static void dump(File arFile) throws Exception {
        Path path = arFile.toPath();
        Set<OpenOption> opts = new HashSet<OpenOption>();
        opts.add(StandardOpenOption.READ);
        FileChannel fileChannel = FileChannel.open(path, opts);

        ARFormat format = new ARFormat();
        format.readFrom(fileChannel);

        System.out.println("Archive contains " + format.itemHeaders.size() + " entries ");

        ARFormat.LookupTable lt = new ARFormat.LookupTable();
        {
            int lookupTableIdx = format.findLookupTable();

            long dataOffset = format.itemDataOffsets.get(lookupTableIdx);
            fileChannel.position(dataOffset);
            ARFormat.FileHeader lookupTableHeader = format.itemHeaders.get(lookupTableIdx);
            ByteBuffer fileData = ByteBuffer.allocate(lookupTableHeader.fileSize);

            fileChannel.read(fileData);
            fileData.flip();

            lt.parse(fileData);

        }


        int filenameTableInd = format.findFilenameTable();
        fileChannel.position(format.itemDataOffsets.get(filenameTableInd));
        byte[] rawFilenameTable = BinIOTools.getBytes(fileChannel, format.itemHeaders.get(filenameTableInd).fileSize);
        List<String> filenameTable = format.parseFilenameTable(rawFilenameTable);

        System.out.println("Archive's filename table contains " + filenameTable.size() + " entries ");
    }
}

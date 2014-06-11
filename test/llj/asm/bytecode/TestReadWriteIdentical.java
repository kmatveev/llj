package llj.asm.bytecode;

import llj.packager.jclass.ClassFileFormat;
import llj.util.DebugChannel;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestReadWriteIdentical {

    public static void main(String[] args) {
        try {
            // String in = "C:\\projects\\my\\udp-client\\out\\production\\udp-client\\Client.class";
            String in = "C:\\projects\\my\\minecraft\\minecraft-reveng\\stage2-1\\com\\mojang\\authlib\\yggdrasil\\YggdrasilUserAuthentication.class";
            String out = in + ".out";
            readThenWrite(in, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readThenWrite(String from, String to) throws Exception {



        FileChannel inChannel = getFileChannel(from, StandardOpenOption.READ);
        ClassFileFormat classFormat = ClassFileFormat.readFrom(new DebugChannel(inChannel));
        // printStats(statistics);
        inChannel.close();

        Map<String, Integer> statistics = new HashMap<String, Integer>();
        // statistics.clear();
        Set<OpenOption> opts = new HashSet<OpenOption>();
        opts.add(StandardOpenOption.WRITE);
        opts.add(StandardOpenOption.CREATE_NEW);
        FileChannel outChannel = getFileChannel(to, opts);
        classFormat.writeTo(outChannel, statistics);
        printStats(statistics);
        outChannel.close();
    }

    private static void printStats(Map<String, Integer> statistics) {
        for (Map.Entry<String, Integer> statEntry : statistics.entrySet()) {
            System.out.println(statEntry.getKey() + "=" + statEntry.getValue() + "; ");
        }
        System.out.println("");
    }

    private static FileChannel getFileChannel(String from, StandardOpenOption read) throws IOException {
        Set<OpenOption> opts = new HashSet<OpenOption>();
        opts.add(read);
        return getFileChannel(from, opts);
    }

    private static FileChannel getFileChannel(String from, Set<OpenOption> opts) throws IOException {
        FileChannel inChannel;
        Path path = FileSystems.getDefault().getPath(from);
        inChannel = FileChannel.open(path, opts);
        return inChannel;
    }


}

package llj.packager.winpe;

import llj.packager.coff.Section;
import llj.util.BinIOTools;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ResourceEntry {
    ResourceDirectoryEntry directoryEntry;
    String resolvedName;
    ResourceDirectory resolvedSubDirectory;
    ResourceDataEntry dataEntry;

    public ResourceEntry(ResourceDirectoryEntry directoryEntry) {
        this.directoryEntry = directoryEntry;
    }

    public List<Section.Usage> resolve(ByteBuffer src, String ref) {
        List<Section.Usage> usages = new ArrayList<>();
        if (directoryEntry.isName) {
            src.position(directoryEntry.nameOffset);
            resolvedName = readResourceString(src);
            usages.add(new Section.Usage(directoryEntry.nameOffset, src.position() - directoryEntry.nameOffset, "ResourceDirectoryEntry_Name" + "/" + ref));
        }
        if (directoryEntry.isDataEntry) {
            src.position(directoryEntry.valueOffset);
            dataEntry = new ResourceDataEntry();
            dataEntry.readFrom(src);
            usages.add(new Section.Usage(directoryEntry.valueOffset, dataEntry.SIZE, "ResourceDataEntry" + "/" + ref));
            // TODO move outside, since we can't maybeResolve RVA here
            // usages.add(new Section.Usage(dataEntry.dataRva, dataEntry.size, "ResourceData" + "/" + ref));
        } else {
            src.position(directoryEntry.valueOffset);
            resolvedSubDirectory = new ResourceDirectory();
            usages.addAll(resolvedSubDirectory.resolve(src, ref));
        }
        return usages;
    }

    // TODO lots of short-living objects here, optimize
    public static String readResourceString(ByteBuffer bb) {
        int length = BinIOTools.getUnsignedShort(bb);
        byte[] data = new byte[length * 2]; // length is a number of 2-byte characters
        bb.get(data);
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_16LE.newDecoder();
            String result = decoder.decode(ByteBuffer.wrap(data)).toString();
            return result;
        } catch (CharacterCodingException ex) {
            throw new RuntimeException(ex);
        }

    }
}

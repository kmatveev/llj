package llj.packager.winpe;

import llj.packager.coff.Section;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ResourceDirectory {
    
    public ResourceDirectoryTable header;
    public List<ResourceEntry> entries = new ArrayList<>();
    
    public static enum ResourceType {
        CURSOR(1), BITMAP(2), ICON(3), MENU(4), DIALOG(5), STRING(6), FONTDIR(7), FONT(8), ACCELERATOR(9), RCDATA(10), MESSAGETABLE(11), VERSION(16), DLGINCLUDE(17), PLUGPLAY(19), VXD(20), ANICURSOR(21), ANIICON(22), HTML(23), MANIFEST(24);

        ResourceType(int id) {
            this.id = id;
        }
        
        public static ResourceType findById(int id) {
            for (ResourceType resType : values()) {
                if (resType.id == id) {
                    return resType;
                }
            }
            return null;
        }

        public final int id;
    }
    
    public ResourceDirectory() {
    }
    
    public List<Section.Usage> resolve(ByteBuffer src, String ref) {
        this.header = new ResourceDirectoryTable();
        int offset = src.position();
        header.readFrom(src);
        List<Section.Usage> usages = new ArrayList<>();
        usages.add(new Section.Usage(offset, header.getSize(), "ResourceDirectoryTable:" + ref));
        for (int i = 0; i < header.numberOfNameEntries; i++) {
            ResourceDirectoryEntry entry = new ResourceDirectoryEntry();
            entry.isName = true;
            offset = src.position();
            entry.readFrom(src);
            usages.add(new Section.Usage(offset, entry.getSize(), "ResourceDirectoryEntry:" + ref));
            entries.add(new ResourceEntry(entry));
        }
        for (int i = 0; i < header.numberOfIdEntries; i++) {
            ResourceDirectoryEntry entry = new ResourceDirectoryEntry();
            entry.isName = false;
            offset = src.position();
            entry.readFrom(src);
            usages.add(new Section.Usage(offset, entry.getSize(), "ResourceDirectoryEntry:" + ref));
            entries.add(new ResourceEntry(entry));
        }
        for (int i = 0; i < entries.size(); i++) {
            usages.addAll(entries.get(i).resolve(src, ref + "/" + String.valueOf(i)));
        }
        return usages;
    }
    
    public ResourceEntry findByName(String name) {
        for (ResourceEntry entry : entries) {
            if (entry.resolvedName != null && entry.resolvedName.equals(name)) {
                return entry;
            }
        }
        return null;
    }

    public ResourceEntry findById(int id) {
        for (ResourceEntry entry : entries) {
            if (!entry.directoryEntry.isName && entry.directoryEntry.integerID == id) {
                return entry;
            }
        }
        return null;
    }

}

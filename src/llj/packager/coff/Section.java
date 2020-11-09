package llj.packager.coff;

import java.util.ArrayList;
import java.util.List;


public class Section {

    public final SectionHeader sectionHeader;
    public final List<RelocationEntry> relocations = new ArrayList<>();

    public Section(SectionHeader sectionHeader) {
        this.sectionHeader = sectionHeader;
    }

    public Section() {
        sectionHeader = new SectionHeader();
    }

}

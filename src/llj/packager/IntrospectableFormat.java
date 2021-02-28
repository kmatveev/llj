package llj.packager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface IntrospectableFormat extends Format {
    List<String> getNames();

    Optional<String> getStringValue(String fieldName, DisplayFormat format);

    int getSize(String fieldName);

    int getOffset(String fieldName);
    
    public boolean isDisplayFormatSupported(String fieldName, DisplayFormat format);

    public void setStringValue(String fieldName, DisplayFormat format);
    
    public static DisplayFormat[] filterSupported(IntrospectableFormat format, String fieldName, DisplayFormat[] displayFormats) {
        List<DisplayFormat> supprotedFormats = new ArrayList<>(displayFormats.length);
        for (DisplayFormat df : displayFormats) {
            if (format.isDisplayFormatSupported(fieldName, df)) {
                supprotedFormats.add(df);
            }
        }
        return supprotedFormats.toArray(new DisplayFormat[supprotedFormats.size()]);
    }
}

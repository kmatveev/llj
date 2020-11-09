package llj.packager;

import java.util.List;
import java.util.Optional;

public interface IntrospectableFormat extends Format {
    List<String> getNames();

    Optional<String> getStringValue(String fieldName, DisplayFormat format);

}

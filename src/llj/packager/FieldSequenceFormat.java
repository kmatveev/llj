package llj.packager;


import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class FieldSequenceFormat implements IntrospectableFormat {
    
    public abstract Collection<? extends Field> fields();

    public Object readFrom(ByteBuffer readBuffer) {
        return readFieldsFrom(readBuffer);
    }

    public Object readFieldsFrom(ByteBuffer readBuffer) {
        for (Field field : fields()) {
            try {
                field.read(readBuffer, this);
            } catch (BufferUnderflowException e) {
                return field;
            }
        }
        return null;
    }

    public void writeTo(ByteBuffer writeBuffer) {
        for (Field field : fields()) {
            field.write(this, writeBuffer);
        }
    }
    
    public static interface Field<Q extends FieldSequenceFormat> {

        public abstract void read(ByteBuffer source, Q dest);

        public abstract void write(Q source, ByteBuffer dest);

        public abstract int size();
        
        public String name();

        public abstract Optional<String> getStringValue(Q format, DisplayFormat displayFormat);
    }

    public List<String> getNames() {
        List<String> result = new ArrayList<String>();
        for (Field field: fields()) {
            result.add(field.name());
        }
        return result;
    }
    
    public Field findField(String fieldName) {
        for (Field field: fields()) {
            if (field.name().equals(fieldName)) {
                return field;
            }
        }
        throw new IllegalArgumentException(fieldName);
    }

    public int getSize(String fieldName) {
        return findField(fieldName).size();
    }

    public int getOffset(String fieldName) {
        int offset = 0;
        for (Field field: fields()) {
            if (field.name().equals(fieldName)) {
                return offset;
            } else {
                offset += field.size();
            }
        }
        throw new IllegalArgumentException(fieldName);
    }

    public Optional<String> getStringValue(String fieldName, DisplayFormat displayFormat) {
        return findField(fieldName).getStringValue(this, displayFormat);
    }
    


}

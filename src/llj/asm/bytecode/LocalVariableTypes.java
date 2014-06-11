package llj.asm.bytecode;

import java.util.Arrays;

public class LocalVariableTypes {

    private final Type[] types;
    private int usedVars = 0;

    public LocalVariableTypes(int size) {
        types = new Type[size];
    }

    private LocalVariableTypes(Type[] types, int usedVars) {
        this.types = types;
        this.usedVars = usedVars;
    }

    public boolean assign(int index, Type type) throws ClassesNotLoadedException {
        if (types[index] == null) {
            types[index] = type;
            return true;
        } else {
            return types[index].isAssignableFrom(type);
        }
    }

    public void add(Type type) {
        usedVars++;
        types[usedVars] = type;
    }

    public void remove() {
        types[usedVars] = null;
        usedVars--;
    }

    public Type get(int index) {
        return types[index];
    }

    public void set(Type[] newTypes) {
        for (int i = 0; i < types.length; i++) {
            types[i] = i < newTypes.length ? newTypes[i] : null;
        }
        usedVars = newTypes.length;
    }

    public int getSize() {
        return types.length;
    }

    public int getUsed() {
        return usedVars;
    }

    protected LocalVariableTypes clone() {
        Type[] types = this.types.clone();
        return new LocalVariableTypes(types, this.usedVars);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalVariableTypes) {
            return Arrays.equals(types, ((LocalVariableTypes)obj).types);
        } else {
            return false;
        }
    }
}

package llj.packager.jclass.attributes;

import llj.packager.jclass.constants.*;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import static llj.util.BinIOTools.*;

public class BootstrapMethodsAttribute extends Attribute {

    public static final AttributeType TYPE = AttributeType.BOOTSTRAP_METHODS;
    public final List<BootstrapMethod> bootstrapMethods;

    public BootstrapMethodsAttribute(ConstantRef<StringConstant> name, List<BootstrapMethod> bootstrapMethods) {
        super(name);
        this.bootstrapMethods = bootstrapMethods;
    }

    @Override
    public AttributeType getType() {
        return TYPE;
    }

    public static BootstrapMethodsAttribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {

        int minLength = SIZE_SHORT;
        if (length < minLength) throw new ReadException("Incorrect specified attribute length; must be at least: " + minLength + "; specified: " + length);

        String part = "";
        try {
            part = "numBootstrapMethods";
            int numBootstrapMethods = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            List<BootstrapMethod> bootstrapMethods = new ArrayList<>();
            for (int i =0; i < numBootstrapMethods; i++) {
                bootstrapMethods.add(BootstrapMethod.readFrom(pool, in));
            }
            return new BootstrapMethodsAttribute(name, bootstrapMethods);

        } catch (ReadException e) {
            throw new ReadException("Was unable to read " + part + " of Code", e);
        }
    }

    @Override
    public int getValueSize() {
        int size = SIZE_SHORT;
        for (BootstrapMethod method : bootstrapMethods) {
            size += method.getSize();
        }
        return size;
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        putUnsignedShort(out, bootstrapMethods.size(), ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        for (BootstrapMethod method : bootstrapMethods) {
            numBytes += method.writeTo(out);
        }
        return numBytes;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        return result;
    }

    public static class BootstrapMethod {

        public final ConstantRef<MethodHandleConstant> methodRef;
        public final List<ConstantRef> arguments;

        public BootstrapMethod(ConstantRef<MethodHandleConstant> methodRef, List<ConstantRef> arguments) {
            this.methodRef = methodRef;
            this.arguments = arguments;
        }

        public static BootstrapMethod readFrom(ConstantPool pool, ReadableByteChannel in) throws ReadException {
            String part = "";
            try {
                part = "methodRef";
                ConstantRef<MethodHandleConstant> methodRef = ConstantRef.readFrom(pool, in);
                part = "numArguments";
                int numArgs = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                List<ConstantRef> arguments = new ArrayList<>(numArgs);
                for (int i = 0; i < numArgs; i++) {
                    part = "arg#" + i;
                    arguments.add(ConstantRef.readFrom(pool, in));
                }
                return new BootstrapMethod(methodRef, arguments);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of BootstrapMethod", e);
            }
        }

        public int getSize() {
            return ConstantRef.getSize() + SIZE_SHORT + arguments.size() * ConstantRef.getSize();
        }

        public int writeTo(WritableByteChannel out) throws IOException {
            int numBytes = 0;
            numBytes += methodRef.writeTo(out);
            putUnsignedShort(out, arguments.size(), ByteOrder.BIG_ENDIAN);
            numBytes += SIZE_SHORT;
            for (ConstantRef argument : arguments) {
                numBytes += argument.writeTo(out);
            }
            return numBytes;
        }


    }

}

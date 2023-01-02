package llj.packager.jclass.constants;

import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static llj.util.BinIOTools.getUnsignedByte;

public abstract class Constant<ST> {

    public static enum ConstType {
        STRING(1) {
            public StringConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                return StringConstant.readFrom(bb);
            }
        },
        INTEGER(3) {
            public IntegerConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                return IntegerConstant.readFrom(bb);
            }
        },
        FLOAT(4) {
            public FloatConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException{
                return FloatConstant.readFrom(bb);
            }
        },
        LONG(5) {
            public LongConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException{
                return LongConstant.readFrom(bb);
            }
        },
        DOUBLE(6) {
            public DoubleConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException{
                return DoubleConstant.readFrom(bb);
            }
        },
        CLASS_REF(7) {
            public ClassRefConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException{
                return ClassRefConstant.readFrom(pool, bb);
            }
        },
        STRING_REF(8) {
            public StringRefConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException{
                return StringRefConstant.readFrom(pool, bb);
            }
        },
        FIELD_REF(9) {
            public FieldRefConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException{
                return FieldRefConstant.readFrom(pool, bb);
            }
        },
        METHOD_REF(10) {
            public MethodRefConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                return MethodRefConstant.readFrom(pool, bb);
            }
        },
        INTERFACE_METHOD_REF(11) {
            public InterfaceMethodRefConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                return InterfaceMethodRefConstant.readFrom(pool, bb);
            }
        },
        NAME_TYPE_PAIR(12)  {
            public NameTypePairConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                return NameTypePairConstant.readFrom(pool, bb);
            }
        },
        METHOD_HANDLE(15) {
            @Override
            public MethodHandleConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                return MethodHandleConstant.readFrom(pool, bb);
            }

        },
        METHOD_TYPE(16) {
            @Override
            public MethodTypeConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                return MethodTypeConstant.readFrom(pool, bb);
            }
        },
        DYNAMIC(17) {
            @Override
            public DynamicConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                return DynamicConstant.readFrom(pool, bb);
            }
        },
        INVOKE_DYNAMIC(18) {
            @Override
            public InvokeDynamicConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                return InvokeDynamicConstant.readFrom(pool, bb);
            }
        },
        MODULE(19) {
            @Override
            public ModuleInfoConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                return ModuleInfoConstant.readFrom(pool, bb);
            }
        },
        PACKAGE(20) {
            @Override
            public PackageInfoConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                return PackageInfoConstant.readFrom(pool, bb);
            }
        },
        PLACEHOLDER(-1)     {
            public ConstantPlaceholder readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
                throw new UnsupportedOperationException();
            }
        };

        private ConstType(int code) {
            this.code = code;
        }

        public final int code;

        public static ConstType getByCode(int code) {
            for (ConstType type : values()) {
                if (type.code == code) return type;
            }
            throw new IllegalArgumentException("Unknown code");
        }

        public abstract <T extends Constant> T readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException;
    }

    protected ST shortcut;

    public ST getShortcut() {
        return shortcut;
    }

    public void setShortcut(ST shortcut) {
        this.shortcut = shortcut;
    }

    public abstract ConstType getType();

    public abstract int writeTo(WritableByteChannel bb) throws IOException;

    public abstract boolean isValid();

}

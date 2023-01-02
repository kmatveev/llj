package llj.packager.jclass.constants;

import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static llj.util.BinIOTools.*;

public class MethodHandleConstant<E>  extends Constant<E> {

    /*
1	REF_getField	getfield C.f:T
2	REF_getStatic	getstatic C.f:T
3	REF_putField	putfield C.f:T
4	REF_putStatic	putstatic C.f:T
5	REF_invokeVirtual	invokevirtual C.m:(A*)T
6	REF_invokeStatic	invokestatic C.m:(A*)T
7	REF_invokeSpecial	invokespecial C.m:(A*)T
8	REF_newInvokeSpecial	new C; dup; invokespecial C.<init>:(A*)V
9	REF_invokeInterface	invokeinterface C.m:(A*)T
     */
    public enum ReferenceKind {
        REF_getField(1), REF_getStatic(2), REF_putField(3), REF_putStatic(4), REF_invokeVirtual(5), REF_invokeStatic(6), REF_invokeSpecial(7), REF_newInvokeSpecial(8), REF_invokeInterface(9);
        public final int code;

        ReferenceKind(int code) {
            this.code = code;
        }

        public static ReferenceKind getByCode(int code) {
            for (ReferenceKind type : values()) {
                if (type.code == code) return type;
            }
            throw new IllegalArgumentException("Unknown code");
        }
        
    }
    
    
    public final ReferenceKind referenceKind;
    public final ConstantRef reference;

    public MethodHandleConstant(ReferenceKind referenceKind, ConstantRef reference) {
        this.referenceKind = referenceKind;
        this.reference = reference;
    }

    @Override
    public ConstType getType() {
        return ConstType.METHOD_HANDLE;
    }

    public static MethodHandleConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
        String part = "";
        try {
            part = "ReferenceKind";
            ReferenceKind refKind = ReferenceKind.getByCode(getUnsignedByte(bb));
            part = "Reference";
            ConstantRef typeRef = ConstantRef.readFrom(pool, bb);
            return new MethodHandleConstant(refKind, typeRef);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read a " + part + " part of MethodHandleConstant", e);
        }
    }
    

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        int i = 0;
        putUnsignedByte(bb, (short)referenceKind.code);
        i += 1;
        i += reference.writeTo(bb);
        return i;

    }

    @Override
    public boolean isValid() {
        // TODO
        return true;
    }
}

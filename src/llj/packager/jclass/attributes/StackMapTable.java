package llj.packager.jclass.attributes;

import llj.packager.jclass.constants.ClassRefConstant;
import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

import static llj.util.BinIOTools.SIZE_BYTE;
import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getUnsignedByte;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedByte;
import static llj.util.BinIOTools.putUnsignedShort;

public class StackMapTable extends Attribute {

    public static final AttributeType TYPE = AttributeType.STACK_MAP_TABLE;

    public final ArrayList<StackMapFrame> frames;

    public StackMapTable(ConstantRef<StringConstant> name, ArrayList<StackMapFrame> frames) {
        super(name);
        this.frames = frames;
    }

    @Override
    public AttributeType getType() {
        return TYPE;
    }

    public static StackMapTable readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        if (length < SIZE_SHORT) throw new ReadException("Incorrect specified attribute length; must at least: " + SIZE_SHORT + "; specified: " + length);
        int numOf = 0;
        try {
            numOf = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read number of entries in StackMapTable");
        }
        length -= SIZE_SHORT;

        ArrayList<StackMapFrame> frames = new ArrayList<StackMapFrame>(numOf);
        for (int j = 0; j < numOf; j++) {
            try {
                StackMapFrame frame = StackMapFrame.readFrom(pool, in, length);
                frames.add(frame);
                length -= frame.getSize();
            } catch (ReadException e) {
                throw new ReadException("Was unable to read StackMapFrame_" + j + " of StackMapTable", e);
            }
        }
        return new StackMapTable(name, frames);
    }

    @Override
    public int getValueSize() {
        int result = SIZE_SHORT;
        for (StackMapFrame frame : frames) {
            result += frame.getSize();
        }
        return result;
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        putUnsignedShort(out, frames.size(), ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        for (int i = 0; i < frames.size(); i++) {
            numBytes += frames.get(i).writeTo(out);
        }
        return numBytes;
    }

    public static abstract class StackMapFrame {

        public static enum StackMapFrameType {
            SAME_FRAME(0, 63), SAME_LOCALS_1_STACK_ITEM(64, 127), SAME_LOCALS_1_STACK_ITEM_EXTENDED(247, 247), CHOP(248, 250), SAME_FRAME_EXTENDED(251), APPEND(252,254), FULL_FRAME(255);

            private final int lower, upper;

            private StackMapFrameType(int lower, int upper) {
                this.lower = lower;
                this.upper = upper;
            }

            private StackMapFrameType(int single) {
                this(single, single);
            }

            public static StackMapFrameType forValue(int val) {
                for (StackMapFrameType type : StackMapFrameType.values()) {
                    if (val <= type.upper && val >= type.lower) {
                        return type;
                    }
                }
                throw new IllegalArgumentException("Value not supported:" + val);
            }
        }

        public abstract boolean isValid();

        public abstract StackMapFrameType getType();

        public static StackMapFrame readFrom(ConstantPool pool, ReadableByteChannel in, int length) throws ReadException {
            if (length < SIZE_BYTE) throw new ReadException("Incorrect specified length; must at least: " + SIZE_BYTE + "; specified: " + length);
            String part = "";
            try {
                part = "stackMapFrameType";
                short tag = getUnsignedByte(in);
                length -= SIZE_BYTE;
                StackMapFrameType type = StackMapFrameType.forValue(tag);
                part = "stackMapFrameContent";
                switch (type) {
                    case SAME_FRAME:
                        return new SameFrame(tag);
                    case SAME_FRAME_EXTENDED:
                        return SameFrameExtended.readValueFrom(tag, in, length);
                    case SAME_LOCALS_1_STACK_ITEM:
                        return SameLocals1StackItemFrame.readValueFrom(tag, pool, in, length);
                    case SAME_LOCALS_1_STACK_ITEM_EXTENDED:
                        return SameLocals1StackItemFrameExtended.readValueFrom(tag, pool, in, length);
                    case CHOP:
                        return ChopFrame.readValueFrom(tag, in, length);
                    case APPEND:
                        return AppendFrame.readValueFrom(tag, pool, in, length);
                    case FULL_FRAME:
                        return FullFrame.readValueFrom(tag, pool, in, length);
                    default:
                        throw new RuntimeException();
                }
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of StackMapFrame", e);
            }
        }

        public abstract int getSize();

        public abstract int writeTo(WritableByteChannel out) throws IOException;

        public abstract int getOffsetDelta();
    }

    public static class SameFrame extends StackMapFrame {

        protected final short tag;

        public SameFrame(short tag) {
            // if (tag < StackMapFrameType.SAME_FRAME.lower || tag > StackMapFrameType.SAME_FRAME.upper) throw new IllegalArgumentException("Illegal tag:" + tag);
            this.tag = tag;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public StackMapFrameType getType() {
            return StackMapFrameType.SAME_FRAME;
        }

        @Override
        public int getSize() {
            return SIZE_BYTE;
        }

        @Override
        public int writeTo(WritableByteChannel out) throws IOException {
            putUnsignedByte(out, tag);
            return SIZE_BYTE;
        }

        @Override
        public int getOffsetDelta() {
            return tag;
        }
    }

    public static class SameFrameExtended extends SameFrame {

        private final int offsetDelta;

        public SameFrameExtended(short tag, int offsetDelta) {
            super(tag);
            this.offsetDelta = offsetDelta;
        }

        @Override
        public StackMapFrameType getType() {
            return StackMapFrameType.SAME_FRAME_EXTENDED;
        }

        @Override
        public int getSize() {
            return super.getSize() + SIZE_SHORT;
        }

        public static SameFrameExtended readValueFrom(short tag, ReadableByteChannel in, int length) throws ReadException {
            if (length < SIZE_SHORT) throw new ReadException("Incorrect specified length; must at least: " + SIZE_SHORT + "; specified: " + length);
            String part = "";
            try {
                part = "offsetDelta";
                int offsetDelta = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                return new SameFrameExtended(tag, offsetDelta);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of SameLocals1StackItemFrameExtended");
            }
        }

        @Override
        public int writeTo(WritableByteChannel out) throws IOException {
            int result = 0;
            putUnsignedByte(out, tag);
            result += SIZE_BYTE;
            putUnsignedShort(out, offsetDelta);
            result += SIZE_SHORT;
            return result;
        }

        @Override
        public int getOffsetDelta() {
            return offsetDelta;
        }
    }

    public static class SameLocals1StackItemFrame extends StackMapFrame {

        public final short tag;
        public final VerificationTypeInfo stackItemInfo;

        public SameLocals1StackItemFrame(short tag, VerificationTypeInfo stackItemInfo) {
            // disable this check because this constructor may be called from sub-classes which have different tags
            // if (tag < StackMapFrameType.SAME_LOCALS_1_STACK_ITEM.lower || tag > StackMapFrameType.SAME_LOCALS_1_STACK_ITEM.upper) throw new IllegalArgumentException("Illegal tag:" + tag);
            this.tag = tag;
            this.stackItemInfo = stackItemInfo;
        }

        @Override
        public boolean isValid() {
            return stackItemInfo.isValid();
        }

        @Override
        public StackMapFrameType getType() {
            return StackMapFrameType.SAME_LOCALS_1_STACK_ITEM;
        }

        @Override
        public int getSize() {
            return SIZE_BYTE + stackItemInfo.getSize();
        }

        public static SameLocals1StackItemFrame readValueFrom(short tag, ConstantPool pool, ReadableByteChannel in, int length) throws ReadException {
            String part = "";
            try {
                part = "stackItemInfo";
                VerificationTypeInfo stackItemInfo = VerificationTypeInfo.readFrom(pool, in, length);
                return new SameLocals1StackItemFrame(tag, stackItemInfo);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of SameLocals1StackItemFrame");
            }
        }

        @Override
        public int writeTo(WritableByteChannel out) throws IOException {
            int result = 0;
            putUnsignedByte(out, tag);
            result += SIZE_BYTE;
            result += stackItemInfo.writeTo(out);
            return result;
        }

        @Override
        public int getOffsetDelta() {
            return tag - 64;
        }
    }

    public static class SameLocals1StackItemFrameExtended extends SameLocals1StackItemFrame {

        private int offsetDelta;

        public SameLocals1StackItemFrameExtended(short tag, VerificationTypeInfo stackItemInfo, int offsetDelta) {
            super(tag, stackItemInfo);
            this.offsetDelta = offsetDelta;
        }

        @Override
        public StackMapFrameType getType() {
            return StackMapFrameType.SAME_LOCALS_1_STACK_ITEM_EXTENDED;
        }

        @Override
        public int getSize() {
            return super.getSize() + SIZE_SHORT;
        }

        public static SameLocals1StackItemFrameExtended readValueFrom(short tag, ConstantPool pool, ReadableByteChannel in, int length) throws ReadException {
            if (length < SIZE_SHORT) throw new ReadException("Incorrect specified length; must at least: " + SIZE_SHORT + "; specified: " + length);
            String part = "";
            try {
                part = "offsetDelta";
                int offsetDelta = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                length -= SIZE_SHORT;
                part = "stackItemInfo";
                VerificationTypeInfo stackItemInfo = VerificationTypeInfo.readFrom(pool, in, length);
                return new SameLocals1StackItemFrameExtended(tag, stackItemInfo, offsetDelta);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of SameLocals1StackItemFrameExtended");
            }
        }

        @Override
        public int writeTo(WritableByteChannel out) throws IOException {
            int result = 0;
            putUnsignedByte(out, tag);
            result += SIZE_BYTE;
            putUnsignedShort(out, offsetDelta);
            result += SIZE_SHORT;
            result += stackItemInfo.writeTo(out);
            return result;
        }

        @Override
        public int getOffsetDelta() {
            return offsetDelta;
        }
    }

    public static class ChopFrame extends StackMapFrame {

        private final short tag;
        public final int offsetDelta;

        public ChopFrame(short tag, int offsetDelta) {
            if (tag < StackMapFrameType.CHOP.lower || tag > StackMapFrameType.CHOP.upper) throw new IllegalArgumentException("Illegal tag:" + tag);
            this.tag = tag;
            this.offsetDelta = offsetDelta;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public StackMapFrameType getType() {
            return StackMapFrameType.CHOP;
        }

        @Override
        public int getSize() {
            return SIZE_BYTE + SIZE_SHORT;
        }

        @Override
        public int writeTo(WritableByteChannel out) throws IOException {
            int result = 0;
            putUnsignedByte(out, tag);
            result += SIZE_BYTE;
            putUnsignedShort(out, offsetDelta);
            result += SIZE_SHORT;
            return result;
        }

        @Override
        public int getOffsetDelta() {
            return offsetDelta;
        }

        public int chopped() {
            return 251 - tag;
        }

        public static ChopFrame readValueFrom(short tag, ReadableByteChannel in, int length) throws ReadException {
            if (length < SIZE_SHORT) throw new ReadException("Incorrect specified length; must at least: " + SIZE_SHORT + "; specified: " + length);
            String part = "";
            try {
                part = "offsetDelta";
                int offsetDelta = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                length -= SIZE_SHORT;
                return new ChopFrame(tag, offsetDelta);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of ChopFrame");
            }
        }

    }

    public static class AppendFrame extends StackMapFrame {

        private final short tag;

        private final int offsetDelta;

        public final VerificationTypeInfo[] appends;

        public AppendFrame(short tag, int offsetDelta, VerificationTypeInfo[] appends) {
            this.tag = tag;
            this.offsetDelta = offsetDelta;
            this.appends = appends;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public StackMapFrameType getType() {
            return StackMapFrameType.APPEND;
        }

        @Override
        public int getSize() {
            int result = SIZE_BYTE + SIZE_SHORT;
            for (VerificationTypeInfo typeInfo : appends) {
                result += typeInfo.getSize();
            }
            return result;
        }

        @Override
        public int writeTo(WritableByteChannel out) throws IOException {
            int result = 0;
            putUnsignedByte(out, tag);
            result += SIZE_BYTE;
            putUnsignedShort(out, offsetDelta);
            result += SIZE_SHORT;
            for (VerificationTypeInfo typeInfo : appends) {
                result += typeInfo.writeTo(out);
            }
            return result;
        }

        public static AppendFrame readValueFrom(short tag, ConstantPool pool, ReadableByteChannel in, int length) throws ReadException {
            if (length < SIZE_SHORT) throw new ReadException("Incorrect specified length; must at least: " + SIZE_SHORT + "; specified: " + length);
            String part = "";
            try {
                part = "offsetDelta";
                int offsetDelta = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                length -= SIZE_SHORT;
                int numAppends = tag - 251;
                VerificationTypeInfo[] appends = new VerificationTypeInfo[numAppends];
                for (int i = 0; i < numAppends; i++) {
                    part = "appendInfo_" + i;
                    VerificationTypeInfo appendInfo = VerificationTypeInfo.readFrom(pool, in, length);
                    appends[i] = appendInfo;
                    length -= appendInfo.getSize();
                }
                return new AppendFrame(tag, offsetDelta, appends);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of AppendFrame");
            }
        }


        @Override
        public int getOffsetDelta() {
            return offsetDelta;
        }
    }

    public static class FullFrame extends StackMapFrame {

        private final int offsetDelta;

        public final VerificationTypeInfo[] locals;
        public final VerificationTypeInfo[] stackItems;

        public FullFrame(int offsetDelta, VerificationTypeInfo[] locals, VerificationTypeInfo[] stackItems) {
            this.offsetDelta = offsetDelta;
            this.locals = locals;
            this.stackItems = stackItems;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public StackMapFrameType getType() {
            return StackMapFrameType.FULL_FRAME;
        }

        @Override
        public int getSize() {
            int result = SIZE_BYTE + SIZE_SHORT + SIZE_SHORT + SIZE_SHORT;
            for (VerificationTypeInfo typeInfo : locals) {
                result += typeInfo.getSize();
            }
            for (VerificationTypeInfo typeInfo : stackItems) {
                result += typeInfo.getSize();
            }
            return result;
        }

        @Override
        public int writeTo(WritableByteChannel out) throws IOException {
            int result = 0;
            putUnsignedByte(out, (short)StackMapFrameType.FULL_FRAME.lower);
            result += SIZE_BYTE;
            putUnsignedShort(out, offsetDelta);
            result += SIZE_SHORT;
            putUnsignedShort(out, locals.length);
            result += SIZE_SHORT;
            for (VerificationTypeInfo typeInfo : locals) {
                result += typeInfo.writeTo(out);
            }
            putUnsignedShort(out, stackItems.length);
            result += SIZE_SHORT;
            for (VerificationTypeInfo typeInfo : stackItems) {
                result += typeInfo.writeTo(out);
            }
            return result;
        }

        public static FullFrame readValueFrom(short tag, ConstantPool pool, ReadableByteChannel in, int length) throws ReadException {
            if (length < (3 * SIZE_SHORT)) throw new ReadException("Incorrect specified length; must at least: " + (3 * SIZE_SHORT) + "; specified: " + length);
            String part = "";
            try {
                part = "offsetDelta";
                int offsetDelta = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                length -= SIZE_SHORT;
                VerificationTypeInfo[] locals;
                {
                    part = "numLocals";
                    int numLocals = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                    length -= SIZE_SHORT;
                    locals = new VerificationTypeInfo[numLocals];
                    for (int i = 0; i < numLocals; i++) {
                        part = "local_" + i;
                        VerificationTypeInfo localInfo = VerificationTypeInfo.readFrom(pool, in, length);
                        locals[i] = localInfo;
                        length -= localInfo.getSize();
                    }
                }
                VerificationTypeInfo[] stackItems;
                {
                    part = "numStackItems";
                    int numStackItems = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                    length -= SIZE_SHORT;
                    stackItems = new VerificationTypeInfo[numStackItems];
                    for (int i = 0; i < numStackItems; i++) {
                        part = "stackItem_" + i;
                        VerificationTypeInfo stackItemInfo = VerificationTypeInfo.readFrom(pool, in, length);
                        stackItems[i] = stackItemInfo;
                        length -= stackItemInfo.getSize();
                    }
                }
                return new FullFrame(offsetDelta, locals, stackItems);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of FullFrame");
            }
        }


        @Override
        public int getOffsetDelta() {
            return offsetDelta;
        }
    }


    public abstract static class VerificationTypeInfo {

        public static enum VerificationTypeInfoKind {
            TOP_VARIABLE_INFO((short)0), INTEGER_VARIABLE_INFO((short)1), FLOAT_VARIABLE_INFO((short)2),
            LONG_VARIABLE_INFO((short)4), DOUBLE_VARIABLE_INFO((short)3), NULL_VARIABLE_INFO((short)5),
            UNINITIALIZED_THIS_VARIABLE_INFO((short)6), OBJECT_VARIABLE_INFO((short)7), UNINITIALIZED_VARIABLE_INFO((short)8);

            public final short tag;

            private VerificationTypeInfoKind(short tag) {
                this.tag = tag;
            }

            public static VerificationTypeInfoKind forTag(int tag) {
                for (VerificationTypeInfoKind kind : VerificationTypeInfoKind.values()) {
                    if (kind.tag == tag) return kind;
                }
                throw new IllegalArgumentException("Unknown tag:" + tag);
            }
        }

        public abstract VerificationTypeInfoKind getKind();

        public static VerificationTypeInfo readFrom(ConstantPool pool, ReadableByteChannel in, int length) throws ReadException {
            if (length < SIZE_BYTE) throw new ReadException("Incorrect specified length; must at least: " + SIZE_BYTE + "; specified: " + length);
            String part = "";
            try {
                part = "verificationTypeInfoTag";
                short tag = getUnsignedByte(in);
                length -= SIZE_BYTE;
                VerificationTypeInfoKind type = VerificationTypeInfoKind.forTag(tag);
                part = "verificationTypeInfoContent";
                switch (type) {
                    case TOP_VARIABLE_INFO:         return new TopVariableInfo();
                    case INTEGER_VARIABLE_INFO:     return new IntegerVariableInfo();
                    case FLOAT_VARIABLE_INFO:       return new FloatVariableInfo();
                    case DOUBLE_VARIABLE_INFO:      return new DoubleVariableInfo();
                    case LONG_VARIABLE_INFO:        return new LongVariableInfo();
                    case NULL_VARIABLE_INFO:        return new NullVariableInfo();
                    case UNINITIALIZED_THIS_VARIABLE_INFO:  return new UninitializedThisVariableInfo();
                    case OBJECT_VARIABLE_INFO:      return ObjectVariableInfo.readFrom(pool, in, length);
                    case UNINITIALIZED_VARIABLE_INFO:       return UninitializedVariableInfo.readFrom(in, length);
                    default: throw new RuntimeException();
                }
            } catch (IllegalArgumentException e) {
                throw new ReadException("Was unable to read " + part + " part of LocalVariableDesc");
            }

        }

        public int writeTo(WritableByteChannel out) throws IOException {
            putUnsignedByte(out, getKind().tag);
            return 1;
        }

        public boolean isValid() {
            return true;
        }

        public int getSize() {
            return SIZE_BYTE;
        }
    }

    public static class TopVariableInfo extends VerificationTypeInfo {

        @Override
        public VerificationTypeInfoKind getKind() {
            return VerificationTypeInfoKind.TOP_VARIABLE_INFO;
        }

    }

    public static class IntegerVariableInfo extends VerificationTypeInfo {

        @Override
        public VerificationTypeInfoKind getKind() {
            return VerificationTypeInfoKind.INTEGER_VARIABLE_INFO;
        }
    }

    public static class FloatVariableInfo extends VerificationTypeInfo {
        @Override
        public VerificationTypeInfoKind getKind() {
            return VerificationTypeInfoKind.FLOAT_VARIABLE_INFO;
        }
    }

    public static class LongVariableInfo extends VerificationTypeInfo {
        @Override
        public VerificationTypeInfoKind getKind() {
            return VerificationTypeInfoKind.LONG_VARIABLE_INFO;
        }
    }

    public static class DoubleVariableInfo extends VerificationTypeInfo {
        @Override
        public VerificationTypeInfoKind getKind() {
            return VerificationTypeInfoKind.DOUBLE_VARIABLE_INFO;
        }
    }

    public static class NullVariableInfo extends VerificationTypeInfo {
        @Override
        public VerificationTypeInfoKind getKind() {
            return VerificationTypeInfoKind.NULL_VARIABLE_INFO;
        }
    }

    public static class UninitializedThisVariableInfo extends VerificationTypeInfo {
        @Override
        public VerificationTypeInfoKind getKind() {
            return VerificationTypeInfoKind.UNINITIALIZED_THIS_VARIABLE_INFO;
        }
    }

    public static class ObjectVariableInfo extends VerificationTypeInfo {

        public final ConstantRef<ClassRefConstant> classRef;

        public ObjectVariableInfo(ConstantRef<ClassRefConstant> classRef) {
            this.classRef = classRef;
        }

        public static ObjectVariableInfo readFrom(ConstantPool pool, ReadableByteChannel in, int length) throws ReadException {
            if (length < ConstantRef.getSize()) throw new ReadException("Incorrect specified length; must at least: " + ConstantRef.getSize() + "; specified: " + length);
            ConstantRef classRef = ConstantRef.readFrom(pool, in);
            return new ObjectVariableInfo(classRef);
        }

        @Override
        public VerificationTypeInfoKind getKind() {
            return VerificationTypeInfoKind.OBJECT_VARIABLE_INFO;
        }

        @Override
        public boolean isValid() {
            return super.isValid() && classRef.isValid(Constant.ConstType.CLASS_REF);
        }

        @Override
        public int writeTo(WritableByteChannel out) throws IOException {
            int result = super.writeTo(out);
            result += classRef.writeTo(out);
            return result;
        }

        @Override
        public int getSize() {
            return super.getSize() + ConstantRef.getSize();
        }
    }

    public static class UninitializedVariableInfo extends VerificationTypeInfo {

        public final int offset;

        public UninitializedVariableInfo(int offset) {
            this.offset = offset;
        }

        public static UninitializedVariableInfo readFrom(ReadableByteChannel in, int length) throws ReadException {
            if (length < ConstantRef.getSize()) throw new ReadException("Incorrect specified length; must at least: " + SIZE_SHORT + "; specified: " + length);
            int offset = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            return new UninitializedVariableInfo(offset);
        }

        @Override
        public VerificationTypeInfoKind getKind() {
            return VerificationTypeInfoKind.UNINITIALIZED_VARIABLE_INFO;
        }

        @Override
        public int writeTo(WritableByteChannel out) throws IOException {
            int result = super.writeTo(out);
            putUnsignedShort(out, offset);
            result += SIZE_SHORT;
            return result;
        }

        @Override
        public int getSize() {
            return super.getSize() + SIZE_SHORT;
        }
    }

}

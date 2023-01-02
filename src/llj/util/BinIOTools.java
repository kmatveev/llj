package llj.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public class BinIOTools {

    public static final int SIZE_BYTE = 1, SIZE_CHAR = 1, SIZE_SHORT = 2, SIZE_INT = 4, SIZE_LONG = 8, SIZE_DOUBLE = 8;

    static ThreadLocal<ByteBuffer> tempBuffer = new ThreadLocal<ByteBuffer>() {
        @Override
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocate(16);  // enough for small values
        }

        @Override
        public ByteBuffer get() {
            ByteBuffer byteBuffer = super.get();
            byteBuffer.clear();
            return byteBuffer;
        }
    };

    public static byte[] getBytes(ReadableByteChannel readChannel, int length) throws ReadException {
        byte[] result = new byte[length];
        ByteBuffer readBuffer = ByteBuffer.wrap(result);
        readFully(readChannel, readBuffer);
        return result;
    }

    public static void readFully(ReadableByteChannel readChannel, ByteBuffer readBuffer) throws ReadException {
        try {
            while (readBuffer.remaining() > 0) {
                boolean eof = readChannel.read(readBuffer) < 0;
                if (eof) throw new ReadException("Attempt to read beyond the end of file, expected " + readBuffer.limit() + " bytes, but available " + readBuffer.position() + " bytes");
            }
        } catch (IOException e) {
            throw new ReadException("Was unable to read", e);
        }
    }

    public static void putBytes(WritableByteChannel writeChannel, byte[] data) throws IOException {
        ByteBuffer readBuffer = ByteBuffer.wrap(data);
        writeChannel.write(readBuffer);
    }

    public static short getUnsignedByte(ReadableByteChannel readChannel) throws ReadException {
        ByteBuffer readBuffer = tempBuffer.get();
        readBuffer.limit(1);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return (short) (readBuffer.get() & 0xFF);
    }

    public static short getUnsignedByte(ByteBuffer readBuffer) {
        return (short) (readBuffer.get() & 0xFF);
    }

    public static char getUnsignedChar(ReadableByteChannel readChannel) throws ReadException {
        ByteBuffer readBuffer = tempBuffer.get();
        readBuffer.limit(1);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return (char) (readBuffer.get() & 0xFF);
    }

    public static char getUnsignedChar(ByteBuffer readBuffer) {
        return (char) (readBuffer.get() & 0xFF);
    }

    public static int getUnsignedShort(ReadableByteChannel readChannel) throws ReadException {
        return getUnsignedShort(readChannel, ByteOrder.nativeOrder());
    }

    public static int getUnsignedShort(ReadableByteChannel readChannel, ByteOrder order) throws ReadException {
        ByteBuffer readBuffer = tempBuffer.get();
        readBuffer.limit(2);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return getUnsignedShort(readBuffer, order);
    }


    public static int getUnsignedShort(ByteBuffer readBuffer) {
        return readBuffer.getShort() & 0xFFFF;
    }

    public static int getUnsignedShort(ByteBuffer readBuffer, ByteOrder order) {
        readBuffer.order(order);
        return readBuffer.getShort() & 0xFFFF;
    }
    

    public static long getUnsignedInt(ReadableByteChannel readChannel) throws ReadException {
        return getUnsignedInt(readChannel, ByteOrder.nativeOrder());
    }

    public static long getUnsignedInt(ReadableByteChannel readChannel, ByteOrder order) throws ReadException {
        ByteBuffer readBuffer = tempBuffer.get();
        readBuffer.limit(4);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return getUnsignedInt(readBuffer, order);
    }

    public static long getUnsignedInt(ByteBuffer readBuffer) {
        int l = readBuffer.getInt();
        return l & 0xFFFFFFFFL;
    }

    public static long getUnsignedInt(ByteBuffer readBuffer, ByteOrder order) {
        readBuffer.order(order);
        int l = readBuffer.getInt();
        return l & 0xFFFFFFFFL;
    }
    

    public static int getInt(ReadableByteChannel readChannel, ByteOrder order) throws ReadException {
        ByteBuffer readBuffer = tempBuffer.get();
        readBuffer.limit(4);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return getInt(readBuffer, order);
    }

    public static int getInt(ByteBuffer readBuffer) {
        return readBuffer.getInt();
    }

    public static int getInt(ByteBuffer readBuffer, ByteOrder order) {
        readBuffer.order(order);
        return readBuffer.getInt();
    }

    public static long getLong(ReadableByteChannel readChannel, ByteOrder order) throws ReadException {
        ByteBuffer readBuffer = tempBuffer.get();
        readBuffer.limit(8);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return getLong(readBuffer, order);
    }

    public static long getLong(ByteBuffer readBuffer) {
        return readBuffer.getLong();
    }

    public static long getLong(ByteBuffer readBuffer, ByteOrder order) {
        readBuffer.order(order);
        return readBuffer.getLong();
    }

    public static float getFloat(ReadableByteChannel readChannel, ByteOrder order) throws ReadException {
        ByteBuffer readBuffer = tempBuffer.get();
        readBuffer.limit(4);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return getFloat(readBuffer, order);
    }

    public static float getFloat(ByteBuffer readBuffer) {
        return readBuffer.getFloat();
    }

    public static float getFloat(ByteBuffer readBuffer, ByteOrder order) {
        readBuffer.order(order);
        return readBuffer.getFloat();
    }

    public static double getDouble(ReadableByteChannel readChannel, ByteOrder order) throws ReadException {
        ByteBuffer readBuffer = tempBuffer.get();
        readBuffer.limit(8);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return getDouble(readBuffer, order);
    }

    public static double getDouble(ByteBuffer readBuffer) {
        return readBuffer.getDouble();
    }

    public static double getDouble(ByteBuffer readBuffer, ByteOrder order) {
        readBuffer.order(order);
        return readBuffer.getDouble();
    }

    public static void putUnsignedByte(ByteBuffer readBuffer, short value) {
        readBuffer.put((byte)value);
    }

    public static void putUnsignedByte(WritableByteChannel writeChannel, short value) throws IOException {
        ByteBuffer bb = tempBuffer.get();
        bb.clear();
        putUnsignedByte(bb, value);
        bb.flip();
        writeChannel.write(bb);
    }

    public static void putUnsignedChar(ByteBuffer readBuffer, char value) {
        readBuffer.put((byte)value);
    }

    public static void putUnsignedChar(WritableByteChannel writeChannel, char value) throws IOException {
        ByteBuffer bb = tempBuffer.get();
        bb.clear();
        putUnsignedChar(bb, value);
        bb.flip();
        writeChannel.write(bb);
    }

    public static void putUnsignedShort(ByteBuffer readBuffer, int value) {
        readBuffer.putShort((short)value);
    }

    public static void putUnsignedShort(ByteBuffer writeBuffer, int value, ByteOrder bo) {
        writeBuffer.order(bo);
        writeBuffer.putShort((short)value);
    }

    public static void putUnsignedShort(WritableByteChannel writeChannel, int value) throws IOException {
        putUnsignedShort(writeChannel, value, ByteOrder.nativeOrder());
    }

    public static void putUnsignedShort(WritableByteChannel writeChannel, int value, ByteOrder order) throws IOException {
        ByteBuffer bb = tempBuffer.get();
        bb.clear();
        putUnsignedShort(bb, value, order);
        bb.flip();
        writeChannel.write(bb);
    }

    public static void putInt(ByteBuffer writeBuffer, int value, ByteOrder order) {
        writeBuffer.order(order);
        writeBuffer.putInt(value);
    }

    public static void putInt(WritableByteChannel writeChannel, int value) throws IOException {
        putInt(writeChannel, value, ByteOrder.nativeOrder());
    }

    public static void putInt(WritableByteChannel writeChannel, int value, ByteOrder order) throws IOException {
        ByteBuffer bb = tempBuffer.get();
        bb.clear();
        putInt(bb, value, order);
        bb.flip();
        writeChannel.write(bb);
    }

    public static void putUnsignedInt(ByteBuffer writeBuffer, long value, ByteOrder bo) {
        writeBuffer.order(bo);
        writeBuffer.putInt((int) value);
    }

    public static void putUnsignedInt(ByteBuffer writeBuffer, long value) {
        writeBuffer.putInt((int) value);
    }

    public static void putUnsignedInt(WritableByteChannel writeChannel, long value) throws IOException {
        putUnsignedInt(writeChannel, value, ByteOrder.nativeOrder());
    }

    public static void putUnsignedInt(WritableByteChannel writeChannel, long value, ByteOrder order) throws IOException {
        ByteBuffer bb = tempBuffer.get();
        bb.clear();
        bb.order(order);
        putUnsignedInt(bb, value);
        bb.flip();
        writeChannel.write(bb);
    }

    public static void putFloat(ByteBuffer writeBuffer, float value) {
        writeBuffer.putFloat(value);
    }

    public static void putFloat(ByteBuffer writeBuffer, float value, ByteOrder order) {
        writeBuffer.order(order);
        writeBuffer.putFloat(value);
    }

    public static void putFloat(WritableByteChannel writeChannel, float value, ByteOrder order) throws IOException {
        ByteBuffer bb = tempBuffer.get();
        bb.clear();
        putFloat(bb, value, order);
        bb.flip();
        writeChannel.write(bb);
    }

    public static void putLong(ByteBuffer writeBuffer, long value) {
        writeBuffer.putLong(value);
    }

    public static void putLong(ByteBuffer writeBuffer, long value, ByteOrder order) {
        writeBuffer.order(order);
        writeBuffer.putLong(value);
    }

    public static void putLong(WritableByteChannel writeChannel, long value) throws IOException {
        putLong(writeChannel, value, ByteOrder.nativeOrder());
    }

    public static void putLong(WritableByteChannel writeChannel, long value, ByteOrder order) throws IOException {
        ByteBuffer bb = tempBuffer.get();
        bb.clear();
        putLong(bb, value, order);
        bb.flip();
        writeChannel.write(bb);
    }

    public static void putDouble(ByteBuffer writeBuffer, double value) {
        writeBuffer.putDouble(value);
    }

    public static void putDouble(WritableByteChannel writeChannel, double value) throws IOException {
        putDouble(writeChannel, value, ByteOrder.nativeOrder());
    }

    public static void putDouble(WritableByteChannel writeChannel, double value, ByteOrder order) throws IOException {
        ByteBuffer bb = tempBuffer.get();
        bb.clear();
        bb.order(order);
        putDouble(bb, value);
        bb.flip();
        writeChannel.write(bb);
    }


    public static boolean readIntoBuffer(ReadableByteChannel in, ByteBuffer readBuffer, int size) throws IOException {
        readBuffer.clear();
        if (size >= 0 && size < readBuffer.capacity()) {
            readBuffer.limit(size);
        }
        while (readBuffer.hasRemaining() && (in.read(readBuffer) >= 0));  // TODO need a better way to avoid spinning on non-blocking input channel
        boolean hasRemaining = readBuffer.hasRemaining();
        readBuffer.flip();
        return hasRemaining;
    }

    public static void combineUnsignedShortWithExisting(ByteBuffer dest, IntUnaryOperator op) {
        int pos = dest.position();
        dest.position(pos - 2);
        int val = getUnsignedShort(dest);
        val = op.applyAsInt(val);
        putUnsignedShort(dest, val);
    }
    
}

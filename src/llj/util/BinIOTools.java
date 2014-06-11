package llj.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class BinIOTools {

    public static final int SIZE_BYTE = 1, SIZE_CHAR = 1, SIZE_SHORT = 2, SIZE_INT = 4, SIZE_LONG = 8;

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
        readBuffer.order(order);
        readBuffer.limit(2);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return getUnsignedShort(readBuffer);
    }


    public static int getUnsignedShort(ByteBuffer readBuffer) {
        return readBuffer.getShort() & 0xFFFF;
    }

    public static long getUnsignedInt(ReadableByteChannel readChannel) throws ReadException {
        return getUnsignedInt(readChannel, ByteOrder.nativeOrder());
    }

    public static long getUnsignedInt(ReadableByteChannel readChannel, ByteOrder order) throws ReadException {
        ByteBuffer readBuffer = tempBuffer.get();
        readBuffer.order(order);
        readBuffer.limit(4);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return getUnsignedInt(readBuffer);
    }

    public static long getUnsignedInt(ByteBuffer readBuffer) {
        long l = readBuffer.getInt();
        return l & 0xFFFFFFFF;
    }

    public static int getInt(ReadableByteChannel readChannel, ByteOrder order) throws ReadException {
        ByteBuffer readBuffer = tempBuffer.get();
        readBuffer.order(order);
        readBuffer.limit(4);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return getInt(readBuffer);
    }

    public static int getInt(ByteBuffer readBuffer) {
        return readBuffer.getInt();
    }

    public static long getLong(ReadableByteChannel readChannel, ByteOrder order) throws ReadException {
        ByteBuffer readBuffer = tempBuffer.get();
        readBuffer.order(order);
        readBuffer.limit(8);
        readFully(readChannel, readBuffer);
        readBuffer.flip();
        return getLong(readBuffer);
    }

    public static long getLong(ByteBuffer readBuffer) {
        return readBuffer.getLong();
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

    public static void putUnsignedShort(WritableByteChannel writeChannel, int value) throws IOException {
        putUnsignedShort(writeChannel, value, ByteOrder.nativeOrder());
    }

    public static void putUnsignedShort(WritableByteChannel writeChannel, int value, ByteOrder order) throws IOException {
        ByteBuffer bb = tempBuffer.get();
        bb.clear();
        bb.order(order);
        putUnsignedShort(bb, value);
        bb.flip();
        writeChannel.write(bb);
    }

    public static void putInt(ByteBuffer writeBuffer, int value) {
        writeBuffer.putInt(value);
    }

    public static void putInt(WritableByteChannel writeChannel, int value) throws IOException {
        putInt(writeChannel, value, ByteOrder.nativeOrder());
    }

    public static void putInt(WritableByteChannel writeChannel, int value, ByteOrder order) throws IOException {
        ByteBuffer bb = tempBuffer.get();
        bb.clear();
        bb.order(order);
        putInt(bb, value);
        bb.flip();
        writeChannel.write(bb);
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

    public static void putLong(ByteBuffer writeBuffer, long value) {
        writeBuffer.putLong(value);
    }

    public static void putLong(WritableByteChannel writeChannel, long value) throws IOException {
        putLong(writeChannel, value, ByteOrder.nativeOrder());
    }

    public static void putLong(WritableByteChannel writeChannel, long value, ByteOrder order) throws IOException {
        ByteBuffer bb = tempBuffer.get();
        bb.clear();
        bb.order(order);
        putLong(bb, value);
        bb.flip();
        writeChannel.write(bb);
    }


    public static boolean readIntoBuffer(ReadableByteChannel in, ByteBuffer readBuffer, int size) throws Exception {
        readBuffer.clear();
        readBuffer.limit(size);
        while (in.read(readBuffer) > 0);  // TODO need a better way
        if (readBuffer.hasRemaining()) return false;
        readBuffer.flip();
        return true;
    }
}

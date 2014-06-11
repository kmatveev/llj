package llj.util;

import java.util.Arrays;

public class BinTools {

    public static final int bufSize = 64;

    private static ThreadLocal<byte[]> buffers = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[bufSize];
        }
    };

    public static byte[] getTemporary() {
        return buffers.get();
    }

    public static int getInt(byte[] source, int i1) {
        return ((source[i1] & 0xFF) << 24) + ((source[i1 + 1] & 0xFF) << 16) + ((source[i1 + 2] & 0xFF) << 8) + (source[(i1 + 3)] & 0xFF);
    }

    public static void setInt(byte[] source, int offset, int val) {
        source[offset]     =  (byte) ((val >> 24) & 0xFF);
        source[offset + 1] =  (byte) ((val >> 16) & 0xFF);
        source[offset + 2] =  (byte) ((val >> 8 ) & 0xFF);
        source[offset + 3] =  (byte) ((val >> 0) & 0xFF);
    }

    public static int getSignedInt(byte[] source, int i1) {
        return (source[i1] << 24) | ((source[i1 + 1] & 0xFF) << 16) | ((source[i1 + 2] & 0xFF) << 8) | (source[(i1 + 3)] & 0xFF);
    }

    public static short getByte(byte[] source, int i1) {
        return (short)(source[i1] & 0xFF);
    }

    public static byte[] getBytes(byte[] source, int i1, int size) {
        return Arrays.copyOfRange(source, i1, i1 + size);
    }

    public static int getShort(byte[] source, int i1) {
        return ((source[i1] & 0xFF) << 8) + (source[i1 + 1] & 0xFF);
    }

    public static int getSignedShort(byte[] source, int i1) {
        return (source[i1] << 8) | (source[i1 + 1] & 0xFF);
    }

    public static interface BinReader<T> {
        public T get(byte[] source, int offset);
    }

    public static interface BinWriter<S> {
        public void set(byte[] source, int offset, S param);
    }

    public static interface BinAccess<T, S> {
        public T doIt(byte[] source, int offset, S param);
    }


    public static final BinReader<Integer> intReader = new GetInt();

    public static class GetInt implements BinReader<Integer> {
        @Override
        public Integer get(byte[] source, int offset) {
            return getInt(source, offset);
        }
    }

    public static final BinWriter<Integer> intWriter = new SetInt();

    public static class SetInt implements BinWriter<Integer> {
        @Override
        public void set(byte[] source, int offset, Integer a) {
            setInt(source, offset, a);
        }
    }


    public static class GetSignedInt implements BinReader<Integer> {
        @Override
        public Integer get(byte[] source, int offset) {
            return getSignedInt(source, offset);
        }
    }

    public static class GetShort implements BinReader<Integer> {
        @Override
        public Integer get(byte[] source, int offset) {
            return getShort(source, offset);
        }
    }

    public static class GetSignedShort implements BinReader<Integer> {
        @Override
        public Integer get(byte[] source, int offset) {
            return getSignedShort(source, offset);
        }
    }



}

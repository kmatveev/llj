package llj.asm.bytecode.exec;

import llj.asm.bytecode.TypeType;
import llj.util.BinTools;

import java.util.ArrayList;
import java.util.List;

public class Heap {

    private final List<Chunk> chunks = new ArrayList<Chunk>();
    private static final int CHUNK_SIZE = 1024;
    private static final int MAX_CHUNKS = 48;

    public final Heap.Pointer nullPointer;

    public Heap() {
        Chunk newChunk = new Chunk();
        chunks.add(newChunk);
        try {
            nullPointer = allocate(4);
        } catch (OutOfMemory e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isNull(Pointer pointer) {
        return pointer.equals(nullPointer);
    }

    public Pointer allocate(int size) throws OutOfMemory {
        if (size > CHUNK_SIZE) throw new OutOfMemory("Can't allocate more than chunk size:" + size);
        short chunkIndex = (short) (chunks.size() - 1);
        Chunk current = chunks.get(chunkIndex);
        short offset = current.allocate(size);
        if (offset >= 0) {
            return new Pointer(chunkIndex, offset);
        } else if (chunks.size() < MAX_CHUNKS) {
            Chunk newChunk = new Chunk();
            chunks.add(newChunk);
            offset = newChunk.allocate(size);
            if (offset < 0) throw new RuntimeException();
            chunkIndex = (short) (chunks.size() - 1);
            return new Pointer(chunkIndex, offset);
        } else {
            runGC();
            // TODO
            return allocate(size);
        }
    }

    public Pointer moveUpFrom(Pointer base, int offset) {
        int chunkIndex = base.chunk;
        int resultOffset = offset + base.internalOffset;
        if (resultOffset >= CHUNK_SIZE) {
            chunkIndex += (resultOffset / CHUNK_SIZE);
            resultOffset = resultOffset % CHUNK_SIZE;
        }
        return new Pointer((short)chunkIndex, (short)resultOffset);
    }

    private void runGC() throws OutOfMemory {
        throw new OutOfMemory("No more chunks");
    }

    private static class Chunk {
        private final byte[] storage = new byte[CHUNK_SIZE];
        private short allocated = 0;

        private short allocate(int size) {
            if (allocated + size < storage.length) {
                short pointer = allocated;
                allocated += size;
                return pointer;
            } else {
                return -1;
            }

        }
    }

    public static class Pointer extends Value {

        private short chunk;
        private short internalOffset;

        public Pointer(short chunk, short offset) {
            if (chunk < 0 || offset < 0) throw new IllegalArgumentException("Negative argument");
            this.chunk = chunk;
            this.internalOffset = offset;
        }

        public static Pointer load(OpaqueSingleSizeValue opaque) {
            short internalOffset = (short) (opaque.getFirstWord() & 0x7FFF);
            short chunk = (short) ((opaque.getFirstWord() >> 16) & 0x7FFF);
            return new Pointer(chunk, internalOffset);
        }

        @Override
        public TypeType getType() {
            return TypeType.REF;
        }

        @Override
        public int getSize() {
            return SIZE_SINGLE;
        }

        public byte[] array(Heap heap) throws MemoryAccessError {
            return heap.chunks.get(chunk).storage;
        }

        public int offset(Heap heap) throws MemoryAccessError {
            return internalOffset;
        }

        public int getFirstWord() {
            return (chunk << 16) | internalOffset;
        }

        public int getSecondWord() {
            throw new UnsupportedOperationException();
        }

        public byte readByte(Heap heap) throws MemoryAccessError {
            if (internalOffset >= CHUNK_SIZE) throw new MemoryAccessError("Access outside of chunk");
            return array(heap)[internalOffset];
        }

        public int readWord(Heap heap) throws MemoryAccessError {
            if (internalOffset + 4 < heap.CHUNK_SIZE) {
                return BinTools.getInt(array(heap), internalOffset);
            } else {
                throw new MemoryAccessError("Access outside of chunk");
            }
        }

        public void writeWord(Heap heap, int word) throws MemoryAccessError {
            if (internalOffset + 4 < heap.CHUNK_SIZE) {
                BinTools.setInt(array(heap), internalOffset, word);
            } else {
                throw new MemoryAccessError("Access outside of chunk");
            }
        }

        public Heap.Pointer moveUp(Heap heap, int offset) {
            return heap.moveUpFrom(this, offset);
        }

        public void store(byte[] source, int offset) {
            BinTools.setInt(source, offset, getFirstWord());
            BinTools.setInt(source, offset + SIZE_SINGLE, getSecondWord());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Pointer)) return false;
            Pointer another = (Pointer)obj;
            return (another.chunk == this.chunk) && (another.internalOffset == this.internalOffset);
        }
    }
}

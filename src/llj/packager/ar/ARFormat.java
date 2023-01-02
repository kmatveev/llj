package llj.packager.ar;

import llj.packager.Format;
import llj.util.BinIOTools;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ARFormat implements Format {

    private byte[] signature;

    private static final byte[] SIGNATURE = "!<arch>\n".getBytes(StandardCharsets.US_ASCII);

    public List<FileHeader> itemHeaders = new ArrayList<>();
    public List<Long> itemDataOffsets = new ArrayList<>();

    public void readFrom(SeekableByteChannel in) throws Exception  {
        signature = BinIOTools.getBytes(in, SIGNATURE.length);

        boolean hasNext = true;
        while (hasNext) {
            FileHeader item = new FileHeader();
            item.readFrom(in);
            itemHeaders.add(item);
            itemDataOffsets.add(in.position());
            hasNext = movePosition(in, item.fileSize);
        }

    }

    private boolean movePosition(SeekableByteChannel in, long itemSize) throws IOException {
        long newPosition = in.position() + itemSize;
        if ((newPosition & 0x01) == 1 ) {
            newPosition += 1;
        }
        if (newPosition < in.size()) {
            in.position(newPosition);
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void writeTo(ByteBuffer out) {

    }

    @Override
    public int getSize() {
        return 0;
    }

    public int findLookupTable() {
        for (int i = 0; i < itemHeaders.size(); i++) {
            if(itemHeaders.get(i).fileIdentifier.trim().equals("/")) {
                return i;
            }
        }
        return -1;
    }

    public int findFilenameTable() {
        for (int i = 0; i < itemHeaders.size(); i++) {
            if(itemHeaders.get(i).fileIdentifier.trim().equals("//")) {
                return i;
            }
        }
        return -1;
    }

    public int getNormalFilename(String filename, List<String> filenameTable) {
        if (filename.startsWith("/")) {
            int offset = Integer.parseInt(filename.substring(1).trim());
        }
        for (int i = 0; i < itemHeaders.size(); i++) {
            if(itemHeaders.get(i).fileIdentifier.trim().equals("//")) {
                return i;
            }
        }
        return -1;
    }

    public List<String> parseFilenameTable(byte[] raw) {
        List<String> result = new ArrayList<>();
        int begin = 0;
        while (true) {
            int i = begin;
            for (; i < raw.length; i++) {
                if (raw[i] == '\n') {
                    break;
                }
            }
            if (i > begin) {
                result.add(new String(raw, begin, i - begin, StandardCharsets.US_ASCII));
                while (i < raw.length && raw[i] == '\n') {
                    i++;
                }
                begin = i;
            } else {
                return result;
            }
        }
    }


    public static class FileHeader {
        public String fileIdentifier;
        public Instant fileModificationTimestamp;
        public Integer ownerId;
        public Integer groupId;
        public Integer fileMode;
        public Integer fileSize;
        public byte[] ending;

        public void readFrom(SeekableByteChannel in) throws ReadException, ARFormatException, IOException {
            try {
                fileIdentifier = new String(BinIOTools.getBytes(in, 16), StandardCharsets.US_ASCII);
            } catch (Exception e) {
                throw new ARFormatException("File identifier error", e);
            }
            try {
                Integer epochMilli = readInt(in, 12);
                if (epochMilli != null) {
                    fileModificationTimestamp = Instant.ofEpochMilli(epochMilli);
                }
            } catch (Exception e) {
                throw new ARFormatException("File timestamp error", e);
            }
            try {
                ownerId = readInt(in, 6);
            } catch (Exception e) {
                throw new ARFormatException("Owner id error", e);
            }
            try {
                groupId = readInt(in, 6);
            } catch (Exception e) {
                throw new ARFormatException("Group id error", e);
            }
            try {
                fileMode = readInt(in, 8);
            } catch (Exception e) {
                throw new ARFormatException("File mode error", e);
            }
            try {
                fileSize = readInt(in, 10);
            } catch (Exception e) {
                throw new ARFormatException("File size error", e);
            }
            try {
                ending = BinIOTools.getBytes(in, 2);
            } catch (Exception e) {
                throw new ARFormatException("Ending error", e);
            }


        }

        private static Integer readInt(SeekableByteChannel in, int length) throws ReadException {
            String strVal = new String(BinIOTools.getBytes(in, length), StandardCharsets.US_ASCII).trim();
            if (strVal.length() == 0) {
                return null;
            } else {
                return Integer.parseInt(strVal);
            }
        }


    }

    @Override
    public String getStringValue() {
        return null;
    }
}

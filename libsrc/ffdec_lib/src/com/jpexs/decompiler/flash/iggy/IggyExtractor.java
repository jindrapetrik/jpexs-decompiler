package com.jpexs.decompiler.flash.iggy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 *
 * Based of works of somebody called eternity.
 *
 */
public class IggyExtractor extends AbstractDataStream implements AutoCloseable {

    final static Logger LOGGER = Logger.getLogger(IggyExtractor.class.getName());

    private RandomAccessFile raf;
    private IggyHeader header;
    private List<IggySubFileEntry> subFileEntries = new ArrayList<>();

    public IggyExtractor(File file) throws IOException {
        raf = new RandomAccessFile(file, "r");
        header = new IggyHeader(this);
        for (int i = 0; i < header.num_subfiles; i++) {
            subFileEntries.add(new IggySubFileEntry(this));
        }

        List<byte[]> indexDatas = new ArrayList<>();
        List<ByteArrayDataStream> flashStreams = new ArrayList<>();

        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            if (entry.type == IggySubFileEntry.TYPE_INDEX) {
                indexDatas.add(getEntryData(i));
            } else if (entry.type == IggySubFileEntry.TYPE_FLASH) {
                flashStreams.add(getEntryDataStream(i));
            }
        }

        IggyFlashHeader64 fh64 = null;
        IggyFlashHeader32 fh32 = null;

        for (ByteArrayDataStream fs : flashStreams) {
            if (is64()) {
                fh64 = new IggyFlashHeader64(fs);
            } else {
                fh32 = new IggyFlashHeader32(fs);
            }
        }
    }

    private boolean is64() {
        return header.is64();
    }

    public IggyHeader getHeader() {
        return header;
    }

    public IggySubFileEntry getSubFileEntry(int entryIndex) {
        if (entryIndex < 0 || entryIndex >= subFileEntries.size()) {
            throw new ArrayIndexOutOfBoundsException("No entry with index " + entryIndex + " exists");
        }
        return subFileEntries.get(entryIndex);
    }

    public int getNumEntries() {
        return subFileEntries.size();
    }

    public byte[] getEntryData(int entryIndex) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = getEntryInputStream(entryIndex);
        byte buf[] = new byte[1024];
        int cnt;
        while ((cnt = is.read(buf)) > 0) {
            baos.write(buf, 0, cnt);
        }
        return baos.toByteArray();
    }

    public ByteArrayDataStream getEntryDataStream(int entryIndex) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = getEntryInputStream(entryIndex);
        byte buf[] = new byte[1024];
        int cnt;
        while ((cnt = is.read(buf)) > 0) {
            baos.write(buf, 0, cnt);
        }
        byte data[] = baos.toByteArray();
        return new ByteArrayDataStream(data);
    }

    public InputStream getEntryInputStream(int entryIndex) {
        IggySubFileEntry entry = getSubFileEntry(entryIndex);

        return new InputStream() {
            long offset = entry.offset;
            long maxOffset = entry.offset + entry.size;

            @Override
            public synchronized int read() throws IOException {
                if (offset < maxOffset) {
                    raf.seek(offset);
                    offset++;
                    return raf.read();
                }
                return -1;
            }
        };
    }

    protected int read() throws IOException {
        int val = raf.read();
        if (val == -1) {
            throw new EOFException();
        }
        return val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[IggyFile:").append("\r\n");
        sb.append(header).append("\r\n");
        sb.append("Entries:").append("\r\n");
        for (IggySubFileEntry entry : subFileEntries) {
            sb.append(entry).append("\r\n");
        }
        sb.append("]");
        return sb.toString();
    }

    public static void extractIggyFile(File iggyFile, File extractDir) throws IOException {
        final String FILENAME_FORMAT = "index%d_type%d.bin";
        try (IggyExtractor ir = new IggyExtractor(iggyFile)) {
            for (int i = 0; i < ir.getNumEntries(); i++) {
                IggySubFileEntry entry = ir.getSubFileEntry(i);
                try (FileOutputStream fos = new FileOutputStream(new File(extractDir, String.format(FILENAME_FORMAT, i, entry.type)))) {
                    fos.write(ir.getEntryData(i));
                }
            }
        }
    }

    private static void processFile(File f) {
        if (f.isDirectory()) {
            System.out.println("Processing directory " + f + ":");
            File iggyFiles[] = f.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".iggy");
                }
            });
            for (File sf : iggyFiles) {
                processFile(sf);
            }
            return;
        }
        System.out.print("Processing file " + f + "...");
        try {
            File dir = f.getParentFile();
            File extractDir = new File(dir, "extracted_" + (f.getName()).replace(".iggy", ""));
            extractDir.mkdir();
            extractIggyFile(f, extractDir);
            System.out.println("OK");
        } catch (Exception ex) {
            System.err.println("FAIL");
            System.exit(1);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("No file specified");
            System.exit(1);
        }

        for (String s : args) {
            File f = new File(s);
            if (!f.exists()) {
                System.err.println("File " + f + " does not exists");
                System.exit(1);
            }
            processFile(f);

        }
        System.exit(0);
    }

    private static void copyStream(InputStream is, OutputStream os) {
        try {
            final int bufSize = 4096;
            byte[] buf = new byte[bufSize];
            int cnt;
            while ((cnt = is.read(buf)) > 0) {
                os.write(buf, 0, cnt);
            }
        } catch (IOException ex) {
            // ignore
        }
    }

    @Override
    public void close() {
        try {
            raf.close();
        } catch (IOException ex) {
            //ignore
        }
    }

    @Override
    protected void seek(long pos, SeekMode mode) throws IOException {
        long newpos = pos;
        if (mode == SeekMode.CUR) {
            newpos = raf.getFilePointer() + pos;
        } else if (mode == SeekMode.END) {
            newpos = raf.length() - pos;
        }
        if (newpos > raf.length()) {
            throw new ArrayIndexOutOfBoundsException("Position outside bounds accessed: " + pos + ". Size: " + raf.length());
        } else if (newpos < 0) {
            throw new ArrayIndexOutOfBoundsException("Negative position accessed: " + pos);
        } else {
            raf.seek(newpos);
        }
    }

    private static boolean updateIndex(long item_offset /*uint32_t*/, boolean is_64, byte index_bytes[], long item_size_change /*int32_t*/) throws IOException {
        ByteArrayDataStream stream = new ByteArrayDataStream(index_bytes);

        /*
        index_table:
            n = UI8
            for i=0..n-1
                table[i] = UI8
                cnt = UI8
                cnt * UI16
        
         */
        int index_table_size = stream.readUI8();
        int index_table[] = new int[index_table_size];

        for (int i = 0; i < index_table_size; i++) {
            index_table[i] = stream.readUI8();
            int num = stream.readUI8();
            stream.seek(num * 2, SeekMode.CUR);
            //num  * UI16
        }

        int state = 0;
        long offset = 0;//uint32_t
        int code; //uint8_t

        while ((code = stream.readUI8()) >= 0) {
            if (state == 1) {
                if (code != 0xFD) {
                    LOGGER.log(Level.WARNING, "We were expecting code 0xFD in state 1.");
                    return false;
                }
            } else if (state == 2) {
                if (code != 0xFF) {
                    LOGGER.log(Level.WARNING, "We were expecting code 0xFF in state 2.");
                    return false;
                }
            }

            if (code < 0x80) // 0-0x7F
            {
                // code is directly an index to the index_table
                if (code >= index_table_size) {
                    LOGGER.log(Level.WARNING, String.format("< 0x80: index is greater than index_table_size. %x > %x", code, index_table_size));
                    return false;
                }

                offset += index_table[code];
            } else if (code < 0xC0) // 0x80-BF
            {
                int index; //uint8_t

                if ((index = stream.readUI8()) < 0) {
                    LOGGER.log(Level.WARNING, "< 0xC0: Cannot read index.");
                    return false;
                }

                if (index >= index_table_size) {
                    LOGGER.log(Level.WARNING, String.format("< 0xC0: index is greater than index_table_size. %x > %x", index, index_table_size));
                    return false;
                }

                int n = code - 0x7F;
                offset += index_table[index] * n;
            } else if (code < 0xD0) // 0xC0-0xCF
            {
                offset += ((code * 2) - 0x17E);
            } else if (code < 0xE0) // 0xD0-0xDF
            {
                // Code here depends on plattform[0], we are assuming it is 1, as we checked in load function
                int i = code & 0xF; //uint8_t
                int n8; //uint8_t
                int n;

                if ((n8 = stream.readUI8()) < 0) {
                    LOGGER.log(Level.WARNING, "< 0xE0: Cannot read n.");
                    return false;
                }

                n = n8 + 1;

                if (is_64) {
                    if (i <= 2) {
                        offset += 8 * n; // Ptr type
                    } else if (i <= 4) {
                        offset += 2 * n;
                    } else if (i == 5) {
                        offset += 4 * n;
                    } else if (i == 6) {
                        offset += 8 * n; // 64 bits type
                    } else {
                        LOGGER.log(Level.WARNING, String.format("< 0xE0: Invalid value for i (%x %x)", i, code));
                    }
                } else {
                    switch (i) {
                        case 2:
                            offset += 4 * n;  // Ptr type
                            break;
                        case 4:
                            offset += 2 * n;
                            break;
                        case 5:
                            offset += 4 * n; // 32 bits type
                            break;
                        case 6:
                            offset += 8 * n;
                            break;
                        default:
                            LOGGER.log(Level.WARNING, String.format("< 0xE0: invalid value for i (%x %x)", i, code));
                    }
                }
            } else if (code == 0xFC) {
                stream.seek(1, SeekMode.CUR);
            } else if (code == 0xFD) {
                int n, m; //uint8_t

                if ((n = stream.readUI8()) < 0) {
                    LOGGER.log(Level.WARNING, "0xFD: Cannot read n.");
                    return false;
                }

                if (state == 1) {
                    if (is_64) {
                        if (n != 0xF) {
                            LOGGER.log(Level.WARNING, String.format("We were expecting an offset of 0xF in state 1."));
                            return false;
                        }
                    } else if (n != 0xB) {
                        LOGGER.log(Level.WARNING, "We were expecting an offset of 0xB in state 1.");
                        return false;
                    }

                    state = 2;
                }

                if ((m = stream.readUI8()) < 0) {
                    LOGGER.log(Level.WARNING, "0xFD: Cannot read m.");
                    return false;
                }

                offset += n;
                stream.seek(m * 2, SeekMode.CUR);
            } else if (code == 0xFE) {
                int n8; //uint8_t
                int n;

                if ((n8 = stream.readUI8()) < 0) {
                    LOGGER.log(Level.WARNING, "0xFE: Cannot read n.");
                    return false;
                }

                n = n8 + 1;
                offset += n;
            } else if (code == 0xFF) {
                long n; //uint32_t

                if ((n = stream.readUI32()) < 0) {
                    LOGGER.log(Level.WARNING, "0xFF: Cannot read n.");
                    return false;
                }

                if (state == 2) {
                    n += item_size_change;
                    stream.seek(-4, SeekMode.CUR);
                    return stream.writeUI32(n);
                }

                offset += n;
            } else {
                LOGGER.log(Level.WARNING, String.format("Unrecognized code: %x", code));
            }

            if (state == 0 && offset == item_offset) {
                state = 1;
            }
        }
        return false;
    }

    /**
     * Gets length of an item.
     *
     * @param item_offset
     * @param is_64
     * @param index_bytes
     * @return null when item not exists, item length otherwise
     * @throws IOException
     */
    private static Long getItemLength(long item_offset /*uint32_t*/, boolean is_64, byte index_bytes[]) throws IOException {
        return itemLength(item_offset, is_64, index_bytes, null);
    }

    /**
     * Sets new length of an item.
     *
     * @param item_offset
     * @param is_64
     * @param index_bytes
     * @param newLength
     * @return null when item not exists, old item length otherwise
     * @throws IOException
     */
    private static Long setItemLength(long item_offset /*uint32_t*/, boolean is_64, byte index_bytes[], long newLength) throws IOException {
        return itemLength(item_offset, is_64, index_bytes, newLength);
    }

    /**
     * Sets/Gets length of an item
     *
     * @param item_offset
     * @param is_64
     * @param index_bytes
     * @param newValue New value to set. If null then no change.
     * @return null when item does not exists, old item length otherwise
     * @throws IOException
     */
    private static Long itemLength(long item_offset /*uint32_t*/, boolean is_64, byte index_bytes[], Long newValue) throws IOException {
        ByteArrayDataStream stream = new ByteArrayDataStream(index_bytes);

        /*
        index_table:
            n = UI8
            for i=0..n-1
                table[i] = UI8
                cnt = UI8
                cnt * UI16
        
         */
        int index_table_size = stream.readUI8();
        int index_table[] = new int[index_table_size];

        for (int i = 0; i < index_table_size; i++) {
            index_table[i] = stream.readUI8();
            int num = stream.readUI8();
            stream.seek(num * 2, SeekMode.CUR);
            //num  * UI16
        }

        int state = 0;
        long offset = 0;//uint32_t
        int code; //uint8_t

        while ((code = stream.readUI8()) >= 0) {
            if (state == 1) {
                if (code != 0xFD) {
                    LOGGER.log(Level.WARNING, "We were expecting code 0xFD in state 1.");
                    return null;
                }
            } else if (state == 2) {
                if (code != 0xFF) {
                    LOGGER.log(Level.WARNING, "We were expecting code 0xFF in state 2.");
                    return null;
                }
            }

            if (code < 0x80) // 0-0x7F
            {
                // code is directly an index to the index_table
                if (code >= index_table_size) {
                    LOGGER.log(Level.WARNING, String.format("< 0x80: index is greater than index_table_size. %x > %x", code, index_table_size));
                    return null;
                }

                offset += index_table[code];
            } else if (code < 0xC0) // 0x80-BF
            {
                int index; //uint8_t

                if ((index = stream.readUI8()) < 0) {
                    LOGGER.log(Level.WARNING, "< 0xC0: Cannot read index.");
                    return null;
                }

                if (index >= index_table_size) {
                    LOGGER.log(Level.WARNING, String.format("< 0xC0: index is greater than index_table_size. %x > %x", index, index_table_size));
                    return null;
                }

                int n = code - 0x7F;
                offset += index_table[index] * n;
            } else if (code < 0xD0) // 0xC0-0xCF
            {
                offset += ((code * 2) - 0x17E);
            } else if (code < 0xE0) // 0xD0-0xDF
            {
                // Code here depends on plattform[0], we are assuming it is 1, as we checked in load function
                int i = code & 0xF; //uint8_t
                int n8; //uint8_t
                int n;

                if ((n8 = stream.readUI8()) < 0) {
                    LOGGER.log(Level.WARNING, "< 0xE0: Cannot read n.");
                    return null;
                }

                n = n8 + 1;

                if (is_64) {
                    if (i <= 2) {
                        offset += 8 * n; // Ptr type
                    } else if (i <= 4) {
                        offset += 2 * n;
                    } else if (i == 5) {
                        offset += 4 * n;
                    } else if (i == 6) {
                        offset += 8 * n; // 64 bits type
                    } else {
                        LOGGER.log(Level.WARNING, String.format("< 0xE0: Invalid value for i (%x %x)", i, code));
                    }
                } else {
                    switch (i) {
                        case 2:
                            offset += 4 * n;  // Ptr type
                            break;
                        case 4:
                            offset += 2 * n;
                            break;
                        case 5:
                            offset += 4 * n; // 32 bits type
                            break;
                        case 6:
                            offset += 8 * n;
                            break;
                        default:
                            LOGGER.log(Level.WARNING, String.format("< 0xE0: invalid value for i (%x %x)", i, code));
                    }
                }
            } else if (code == 0xFC) {
                stream.seek(1, SeekMode.CUR);
            } else if (code == 0xFD) {
                int n, m; //uint8_t

                if ((n = stream.readUI8()) < 0) {
                    LOGGER.log(Level.WARNING, "0xFD: Cannot read n.");
                    return null;
                }

                if (state == 1) {
                    if (is_64) {
                        if (n != 0xF) {
                            LOGGER.log(Level.WARNING, String.format("We were expecting an offset of 0xF in state 1."));
                            return null;
                        }
                    } else if (n != 0xB) {
                        LOGGER.log(Level.WARNING, "We were expecting an offset of 0xB in state 1.");
                        return null;
                    }

                    state = 2;
                }

                if ((m = stream.readUI8()) < 0) {
                    LOGGER.log(Level.WARNING, "0xFD: Cannot read m.");
                    return null;
                }

                offset += n;
                stream.seek(m * 2, SeekMode.CUR);
            } else if (code == 0xFE) {
                int n8; //uint8_t
                int n;

                if ((n8 = stream.readUI8()) < 0) {
                    LOGGER.log(Level.WARNING, "0xFE: Cannot read n.");
                    return null;
                }

                n = n8 + 1;
                offset += n;
            } else if (code == 0xFF) {
                long n; //uint32_t

                if ((n = stream.readUI32()) < 0) {
                    LOGGER.log(Level.WARNING, "0xFF: Cannot read n.");
                    return null;
                }

                if (state == 2) {
                    if (newValue != null) {
                        stream.seek(-4, SeekMode.CUR);
                        stream.writeUI32(newValue);
                    }
                    return n;
                }

                offset += n;
            } else {
                LOGGER.log(Level.WARNING, String.format("Unrecognized code: %x", code));
            }

            if (state == 0 && offset == item_offset) {
                state = 1;
            }
        }
        return null;
    }

    @Override
    public Long available() {
        try {
            return raf.length() - raf.getFilePointer();
        } catch (IOException ex) {
            return null;
        }
    }

}

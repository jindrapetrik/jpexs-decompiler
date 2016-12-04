package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.IggyIndexParser;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.RandomAccessFileDataStream;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.DataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.TemporaryDataStream;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 *
 * Based of works of somebody called eternity.
 *
 */
public class IggyFile implements StructureInterface {

    final static Logger LOGGER = Logger.getLogger(IggyFile.class.getName());

    private IggyHeader header;
    private List<IggySubFileEntry> subFileEntries = new ArrayList<>();
    private List<byte[]> subFileEntriesData = new ArrayList<>();

    private List<IggySwf> iggySwfs = new ArrayList<>();

    public static final int FIRST_TAG_POSITION = 3;

    public void replaceFontTag(int targetSwfIndex, int fontIndex, IggyFont newFont) throws IOException {
        IggySwf iggySwf = iggySwfs.get(targetSwfIndex);
        iggySwf.replaceFontTag(fontIndex, newFont);

        IggyIndexBuilder indexStream = new IggyIndexBuilder();
        DataStreamInterface flashStream = new TemporaryDataStream();
        flashStream.setIndexing(indexStream);
        iggySwf.writeToDataStream(flashStream);
        byte flashData[] = flashStream.getAllBytes();
        int swfIndex = 0;
        int offsetChange = 0;
        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            entry.offset += offsetChange;
            if (entry.type == IggySubFileEntry.TYPE_INDEX) {
                if (swfIndex == targetSwfIndex) {
                    byte indexData[] = indexStream.getIndexBytes();
                    long newLen = indexData.length;
                    long oldLen = entry.size;
                    entry.size = newLen;
                    entry.size2 = newLen;
                    offsetChange += (newLen - oldLen);
                    subFileEntriesData.set(i, indexData);
                }
                swfIndex++;
            }
            if (entry.type == IggySubFileEntry.TYPE_FLASH) {
                if (swfIndex == targetSwfIndex) {
                    long newLen = flashData.length;
                    long oldLen = entry.size;
                    entry.size = newLen;
                    entry.size2 = newLen;
                    offsetChange += (newLen - oldLen);
                    subFileEntriesData.set(i, flashData);
                }
            }
        }
    }

    public IggySwf getSwf(int swfIndex) {
        return iggySwfs.get(swfIndex);
    }

    public int getFontCount(int swfIndex) {
        return iggySwfs.get(swfIndex).fonts.size();
    }

    public IggyFont getFont(int swfIndex, int fontId) {
        return iggySwfs.get(swfIndex).fonts.get(fontId);
    }

    public IggyFile(String filePath) throws IOException {
        this(new File(filePath));
    }

    public IggyFile(File file) throws IOException {
        try (ReadDataStreamInterface stream = new RandomAccessFileDataStream(file)) {
            readFromDataStream(stream);
        }
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

    public byte[] getEntryData(int entryIndex) {
        if (entryIndex < 0 || entryIndex >= subFileEntries.size()) {
            throw new ArrayIndexOutOfBoundsException("No entry with index " + entryIndex + " exists");
        }
        return subFileEntriesData.get(entryIndex);
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
        IggyFile ir = new IggyFile(iggyFile);
        for (int i = 0; i < ir.getNumEntries(); i++) {
            IggySubFileEntry entry = ir.getSubFileEntry(i);
            try (FileOutputStream fos = new FileOutputStream(new File(extractDir, String.format(FILENAME_FORMAT, i, entry.type)))) {
                fos.write(ir.getEntryData(i));
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
        /*
        String indexFileName = "d:\\Dropbox\\jpexs-laptop\\iggi\\extraxtdir_orig\\index4_type0.bin";
        IggyIndexParser.parseIndex(true, new TemporaryDataStream(Helper.readFile(indexFileName)), new ArrayList<>(), new ArrayList<>());

        System.exit(0);*/
        String inFileName = "d:\\Dropbox\\jpexs-laptop\\iggi\\lib_loc_english_font.iggy";
        String outFileName = "d:\\Dropbox\\jpexs-laptop\\iggi\\lib_loc_english_font2.iggy";

        File extractDirOrig = new File("d:\\Dropbox\\jpexs-laptop\\iggi\\extraxtdir_orig");
        File extractDirNew = new File("d:\\Dropbox\\jpexs-laptop\\iggi\\extraxtdir_new");

        if (!extractDirOrig.exists()) {
            extractDirOrig.mkdir();
        }
        if (!extractDirNew.exists()) {
            extractDirNew.mkdir();
        }

        File inFile = new File(inFileName);
        File outFile = new File(outFileName);
        IggyFile iggyFile = new IggyFile(inFile);
        extractIggyFile(inFile, extractDirOrig);
        IggySwf iswf = iggyFile.getSwf(0);
        iggyFile.replaceSwf(0, iswf);
        outFile.delete();
        try (RandomAccessFileDataStream outputStream = new RandomAccessFileDataStream(outFile)) {
            iggyFile.writeToDataStream(outputStream);
        }
        extractIggyFile(outFile, extractDirNew);
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

    private static boolean updateIndex(long item_offset /*uint32_t*/, boolean is_64, byte index_bytes[], long item_size_change /*int32_t*/) throws IOException {
        TemporaryDataStream stream = new TemporaryDataStream(index_bytes);

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
        TemporaryDataStream stream = new TemporaryDataStream(index_bytes);

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

    public int getSwfCount() {
        return iggySwfs.size();
    }

    public String getSwfName(int swfIndex) {
        return iggySwfs.get(swfIndex).getName();
    }

    public long getSwfXMin(int swfIndex) {
        return iggySwfs.get(swfIndex).getHdr().getXMin();
    }

    public long getSwfYMin(int swfIndex) {
        return iggySwfs.get(swfIndex).getHdr().getYMin();
    }

    public long getSwfXMax(int swfIndex) {
        return iggySwfs.get(swfIndex).getHdr().getXMax();
    }

    public long getSwfYMax(int swfIndex) {
        return iggySwfs.get(swfIndex).getHdr().getYMax();
    }

    public float getSwfFrameRate(int swfIndex) {
        return iggySwfs.get(swfIndex).getHdr().getFrameRate();
    }

    /**
     * Removes entries of type INDEX.There can be more than one INDEX,
     * continuous. This removes all ot them.
     */
    public void removeIndexEntries() {
        long offsetsChange = 0;
        final int ENTRY_SIZE = 16;
        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            entry.offset += offsetsChange;
            if (entry.type == IggySubFileEntry.TYPE_INDEX) {
                offsetsChange = offsetsChange - entry.size - ENTRY_SIZE;
                subFileEntriesData.remove(i);
                subFileEntries.remove(i);
                i--;
            }
        }
    }

    public boolean replaceSwf(int targetSwfIndex, IggySwf iggySwf) throws IOException {

        if (targetSwfIndex < 0 || targetSwfIndex >= getSwfCount()) {
            throw new ArrayIndexOutOfBoundsException("No such SWF file index");
        }
        byte replacementData[];
        byte replacementIndexData[];
        IggyIndexBuilder ib = new IggyIndexBuilder();
        try (DataStreamInterface stream = new TemporaryDataStream()) {
            stream.setIndexing(ib);
            iggySwf.writeToDataStream(stream);
            replacementData = stream.getAllBytes();
            replacementIndexData = ib.getIndexBytes();
        } catch (IOException ex) {
            Logger.getLogger(IggyFile.class.getName()).log(Level.SEVERE, "Error during updating SWF", ex);
            return false;
        }

        //IggyIndexParser.parseIndex(true, new TemporaryDataStream(replacementIndexData), new ArrayList<>(), new ArrayList<>());
        int swfIndex = 0;
        long offsetsChange = 0;
        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            entry.offset += offsetsChange;
            if (entry.type == IggySubFileEntry.TYPE_FLASH) {
                if (swfIndex == targetSwfIndex) {
                    long oldSize = entry.size;
                    long newSize = replacementData.length;
                    entry.size = newSize;
                    entry.size2 = newSize;
                    offsetsChange = offsetsChange + (newSize - oldSize); //entries after this one will have modified offsets
                    subFileEntriesData.set(i, replacementData);
                }
            }
        }

        removeIndexEntries();
        IggySubFileEntry indexEntry = new IggySubFileEntry(IggySubFileEntry.TYPE_INDEX, replacementIndexData.length, replacementIndexData.length, 0 /*offset will be set automatically*/);
        subFileEntries.add(indexEntry);
        subFileEntriesData.add(replacementIndexData);
        return true;
    }

    private void parseEntries() throws IOException {
        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            if (entry.type == IggySubFileEntry.TYPE_FLASH) {
                iggySwfs.add(new IggySwf(new TemporaryDataStream(getEntryData(i))));
            }
            /*if (entry.type == IggySubFileEntry.TYPE_INDEX) {
                IggyIndexParser.parseIndex(true, new TemporaryDataStream(getEntryData(i)), new ArrayList<>(), new ArrayList<>());
            }*/
        }
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        header = new IggyHeader(stream);
        for (int i = 0; i < header.getNumSubfiles(); i++) {
            subFileEntries.add(new IggySubFileEntry(stream));
        }
        for (IggySubFileEntry entry : subFileEntries) {
            stream.seek(entry.offset, SeekMode.SET);
            byte[] entryData = stream.readBytes((int) entry.size);
            subFileEntriesData.add(entryData);
        }
        parseEntries();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        header.writeToDataStream(stream);

        long startOffset = IggyHeader.STRUCT_SIZE + IggySubFileEntry.STRUCTURE_SIZE * subFileEntries.size();
        long currentOffset = startOffset;

        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            entry.offset = currentOffset;
            currentOffset += entry.size;
            entry.writeToDataStream(stream);
        }

        for (int i = 0; i < subFileEntries.size(); i++) {
            byte[] entryData = subFileEntriesData.get(i);
            stream.writeBytes(entryData);
        }

    }

}

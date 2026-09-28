package com.jpexs.decompiler.flash.tags.profiles.bioshock;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import net.npe.dds.DDSReader;

/**
 *
 * @author JPEXS
 */
public class BioshockImage extends ImageTag {
    
    public static final int ID = 512;

    public static final String NAME = "BioshockImage";
    
    public static final long FORMAT_DXT1 = 0;    
    public static final long FORMAT_DXT5 = 4;
    
    
    @SWFType(BasicType.UI16)
    public int width;
    
    @SWFType(BasicType.UI16)
    public int height;

    @SWFType(BasicType.UI32)
    public long formatID;   
    
    public ByteArrayRange imageData;

    
    public BioshockImage(SWF swf) {
        super(swf, ID, NAME, null);
    }
    
    public BioshockImage(SWF swf, ByteArrayRange data, byte[] imageData) {
        super(swf, ID, NAME, data);
    }
    
    public BioshockImage(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        width = sis.readUI16("width");
        height = sis.readUI16("height");
        formatID = sis.readUI32("formatID");
        imageData = sis.readByteRangeEx(sis.available(), "imageData");
    }
    
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterID);
        sos.writeUI16(width);
        sos.writeUI16(height);
        sos.writeUI32(formatID);
        sos.write(imageData);
    }

    @Override
    public InputStream getOriginalImageData() {
        return null;
    }

    @Override
    protected SerializableImage getImage() {
        if (width <= 0 || height <= 0) {
            return createFailedImage();
        }
        int[] pixels = null;
        
        if (formatID == FORMAT_DXT1) {
            pixels = DDSReader.decodeDXT1(width, height, imageData.getPos(), imageData.getArray(), DDSReader.ARGB);
        } else if (formatID == FORMAT_DXT5) {
            pixels = DDSReader.decodeDXT5(width, height, imageData.getPos(), imageData.getArray(), DDSReader.ARGB);          
        } else {
            return createFailedImage();
        }
        
        BufferedImage bufImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufImage.getRaster().setDataElements(0, 0, bufImage.getWidth(), bufImage.getHeight(), pixels);
        return new SerializableImage(bufImage);                        
    }
    
    private SerializableImage createFailedImage() {
        if (width <= 0 || height <= 0) {
            SerializableImage serImage = new SerializableImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            serImage.fillTransparent();
            return serImage;
        }

        SerializableImage serImage = new SerializableImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics g = serImage.getGraphics();
        g.setColor(SWF.ERROR_COLOR);
        g.fillRect(0, 0, width, height);
        return serImage;
    }

    @Override
    public Dimension getImageDimension() {
        return new Dimension(width, height);
    }

    @Override
    public boolean importSupported() {
        return false;
    }        
    
    @Override
    public void setImage(byte[] data) throws IOException {
        //empty
    }

    @Override
    public ImageFormat getImageFormat() {
        return ImageFormat.PNG;
    }

    @Override
    public ImageFormat getOriginalImageFormat() {
        return ImageFormat.PNG;
    }
    
}

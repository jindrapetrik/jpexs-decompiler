/*
 *  Copyright (C) 2025 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.types.filters.CONVOLUTIONFILTER;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Convolution preset
 *
 * @author JPEXS
 */
public class ConvolutionPreset {

    public static final ConvolutionPreset Identity = new ConvolutionPreset("identity", 3, 3, new float[]{
        0, 0, 0,
        0, 1, 0,
        0, 0, 0
    });
    public static final ConvolutionPreset BoxBlur = new ConvolutionPreset("boxBlur", 3, 3, new float[]{
        1, 1, 1,
        1, 1, 1,
        1, 1, 1
    }, 9);
    public static final ConvolutionPreset GaussianBlur = new ConvolutionPreset("gaussianBlur", 3, 3, new float[]{
        1, 2, 1,
        2, 4, 2,
        1, 2, 1
    }, 16);
    public static final ConvolutionPreset Sharpen = new ConvolutionPreset("sharpen", 3, 3, new float[]{
        0, -1, 0,
        -1, 5, -1,
        0, -1, 0
    });
    public static final ConvolutionPreset EdgeDetectionXSobel = new ConvolutionPreset("edgeDetectionXSobel", 3, 3, new float[]{
        -1, 0, 1,
        -2, 0, 2,
        -1, 0, 1
    });
    public static final ConvolutionPreset EdgeDetectionYSobel = new ConvolutionPreset("edgeDetectionYSobel", 3, 3, new float[]{
        -1, -2, -1,
        0, 0, 0,
        1, 2, 1
    });
    public static final ConvolutionPreset EdgeDetectionXPrewitt = new ConvolutionPreset("edgeDetectionXPrewitt", 3, 3, new float[]{
        -1, 0, 1,
        -1, 0, 1,
        -1, 0, 1
    });
    public static final ConvolutionPreset EdgeDetectionYPrewitt = new ConvolutionPreset("edgeDetectionYPrewitt", 3, 3, new float[]{
        -1, -1, -1,
        0, 0, 0,
        1, 1, 1
    });
    public static final ConvolutionPreset EdgeDetectionXScharr = new ConvolutionPreset("edgeDetectionXScharr", 3, 3, new float[]{
        3, 0, -3,
        10, 0, -10,
        3, 0, -3
    });
    public static final ConvolutionPreset EdgeDetectionYScharr = new ConvolutionPreset("edgeDetectionYScharr", 3, 3, new float[]{
        3, 10, 3,
        0, 0, 0,
        -3, -10, -3
    });
    public static final ConvolutionPreset Laplacian = new ConvolutionPreset("laplacian", 3, 3, new float[]{
        0, -1, 0,
        -1, 4, -1,
        0, -1, 0
    });
    public static final ConvolutionPreset Emboss = new ConvolutionPreset("emboss", 3, 3, new float[]{
        -2, -1, 0,
        -1, 1, 1,
        0, 1, 2
    });
    public static final ConvolutionPreset Outline = new ConvolutionPreset("outline", 3, 3, new float[]{
        -1, -1, -1,
        -1, 8, -1,
        -1, -1, -1
    });
    public static final ConvolutionPreset MotionBlurX = new ConvolutionPreset("motionBlurX", 5, 1, new float[]{
        1, 1, 1, 1, 1
    }, 5);
    public static final ConvolutionPreset MotionBlurY = new ConvolutionPreset("motionBlurY", 1, 5, new float[]{
        1,
        1,
        1,
        1,
        1
    }, 5);
    public static final ConvolutionPreset HighPass = new ConvolutionPreset("highPass", 3, 3, new float[]{
        -1, -1, -1,
        -1, 9, -1,
        -1, -1, -1
    });
    private final String identifier;

    private final int matrixX;
    private final int matrixY;
    private final float[] matrix;
    private final float divisor;

    /**
     * Constructor
     *
     * @param identifier Convolution type identifier
     * @param matrixX Matrix width
     * @param matrixY Matrix height
     * @param matrix Matrix
     */
    public ConvolutionPreset(String identifier, int matrixX, int matrixY, float[] matrix) {
        this(identifier, matrixX, matrixY, matrix, 1);

    }

    /**
     * Constructor with divisor
     *
     * @param identifier Convolution type identifier
     * @param matrixX Matrix width
     * @param matrixY Matrix height
     * @param matrix Matrix
     * @param divisor Divisor of values
     */
    public ConvolutionPreset(String identifier, int matrixX, int matrixY, float[] matrix, float divisor) {
        this.identifier = identifier;
        this.matrixX = matrixX;
        this.matrixY = matrixY;
        this.matrix = matrix;
        this.divisor = divisor;
    }

    /**
     * Get matrix
     *
     * @return
     */
    public float[] getMatrix() {
        return matrix;
    }

    /**
     * Get matrix width
     *
     * @return
     */
    public int getMatrixX() {
        return matrixX;
    }

    /**
     * Get matrix height
     *
     * @return
     */
    public int getMatrixY() {
        return matrixY;
    }

    @Override
    public String toString() {
        return EasyStrings.translate("convolution." + identifier);
    }

    /**
     * Creates new CONVOLUTIONFILTER with the preset values
     *
     * @return The filter
     */
    public CONVOLUTIONFILTER createFilter() {
        CONVOLUTIONFILTER filter = new CONVOLUTIONFILTER();
        filter.matrixX = matrixX;
        filter.matrixY = matrixY;
        filter.matrix = matrix.clone();
        filter.divisor = divisor;
        return filter;
    }

    /**
     * Returns all built-in presets
     *
     * @return List of presets
     */
    public static List<ConvolutionPreset> getAllPresets() {
        List<ConvolutionPreset> ret = new ArrayList<>();
        for (Field field : ConvolutionPreset.class.getFields()) {
            if (field.getType() == ConvolutionPreset.class) {
                try {
                    ret.add((ConvolutionPreset) field.get(ConvolutionPreset.class));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    //ignore
                }
            }
        }
        return ret;
    }

    public boolean matchesFilter(CONVOLUTIONFILTER other) {
        int maxX = Math.max(other.matrixX, matrixX);
        int maxY = Math.max(other.matrixY, matrixY);

        int halfMaxX = (maxX - 1) / 2;
        int halfMaxY = (maxY - 1) / 2;

        int halfX = (matrixX - 1) / 2;
        int halfY = (matrixY - 1) / 2;

        int halfOtherX = (other.matrixX - 1) / 2;
        int halfOtherY = (other.matrixY - 1) / 2;

        int halfMax = Math.max(halfMaxX, halfMaxY);

        for (int i = 0; i <= halfMax; i++) {
            for (int x = -i; x <= i; x++) {
                if (!compare(other, halfX + x, halfY - i, halfOtherX + x, halfOtherY - i)) {
                    return false;
                }
                if (!compare(other, halfX + x, halfY + i, halfOtherX + x, halfOtherY + i)) {
                    return false;
                }
            }
            for (int y = -i; y <= i; y++) {
                if (!compare(other, halfX - i, halfY + y, halfOtherX - i, halfOtherY + y)) {
                    return false;
                }
                if (!compare(other, halfX + i, halfY + y, halfOtherX + i, halfOtherY + y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean compare(CONVOLUTIONFILTER other, int x1, int y1, int x2, int y2) {
        int thisIndex = y1 * matrixX + x1;
        int otherIndex = y2 * other.matrixX + x2;

        float val = x1 < 0 || x1 >= matrixX || y1 < 0 || y1 >= matrixY ? 0f : matrix[thisIndex] / divisor;
        float otherVal = x2 < 0 || x2 >= other.matrixX || y2 < 0 || y2 >= other.matrixY ? 0f : other.matrix[otherIndex] / other.divisor;

        return val == otherVal;
    }

    public static void main(String[] args) {
        ConvolutionPreset p1 = new ConvolutionPreset("x1", 3, 3, new float[]{
            1, 2, 3,
            4, 5, 6,
            7, 8, 9
        });

        CONVOLUTIONFILTER f1 = new CONVOLUTIONFILTER();
        f1.matrixX = 5;
        f1.matrixY = 5;
        f1.matrix = new float[]{
            0, 0, 0, 0, 0,
            0, 1, 2, 3, 0,
            0, 4, 5, 6, 0,
            0, 7, 8, 9, 0,
            0, 0, 0, 0, 0
        };

        assertMatch(p1, f1);

        CONVOLUTIONFILTER f2 = new CONVOLUTIONFILTER();
        f2.matrixX = 5;
        f2.matrixY = 3;
        f2.matrix = new float[]{
            0, 1, 2, 3, 0,
            0, 4, 5, 6, 0,
            0, 7, 8, 9, 0
        };

        assertMatch(p1, f2);

        ConvolutionPreset p2 = new ConvolutionPreset("x1", 3, 3, new float[]{
            1, 2, 3,
            4, 5, 6,
            7, 8, 9
        }, 5);

        CONVOLUTIONFILTER f3 = new CONVOLUTIONFILTER();
        f3.matrixX = 3;
        f3.matrixY = 3;
        f3.matrix = new float[]{
            1 / 5f, 2 / 5f, 3 / 5f,
            4 / 5f, 5 / 5f, 6 / 5f,
            7 / 5f, 8 / 5f, 9 / 5f
        };

        assertMatch(p2, f3);
    }

    private static void assertMatch(ConvolutionPreset preset, CONVOLUTIONFILTER filter) {
        if (!preset.matchesFilter(filter)) {
            throw new RuntimeException("Preset and filter do not match!");
        }
    }

    public static ConvolutionPreset getPresetOfFilter(CONVOLUTIONFILTER filter) {
        for (ConvolutionPreset preset : getAllPresets()) {
            if (preset.matchesFilter(filter)) {
                return preset;
            }
        }
        return null;
    }
}

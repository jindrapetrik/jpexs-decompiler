package com.jpexs.decompiler.flash.types.filters;

/**
 * Converts brightness, saturation, contrast and hue to color matrix and
 * vice-versa
 *
 * @author JPEXS
 */
public class ColorMatrixConvertor {

    private static double[] contrastMap = {
        //      0     1     2     3     4     5     6     7     8     9
        /*0*/0, 0.01, 0.02, 0.04, 0.05, 0.06, 0.07, 0.08, 0.1, 0.11,
        /*1*/ 0.12, 0.14, 0.15, 0.16, 0.17, 0.18, 0.20, 0.21, 0.22, 0.24,
        /*2*/ 0.25, 0.27, 0.28, 0.30, 0.32, 0.34, 0.36, 0.38, 0.40, 0.42,
        /*3*/ 0.44, 0.46, 0.48, 0.5, 0.53, 0.56, 0.59, 0.62, 0.65, 0.68,
        /*4*/ 0.71, 0.74, 0.77, 0.80, 0.83, 0.86, 0.89, 0.92, 0.95, 0.98,
        /*5*/ 1.0, 1.06, 1.12, 1.18, 1.24, 1.30, 1.36, 1.42, 1.48, 1.54,
        /*6*/ 1.60, 1.66, 1.72, 1.78, 1.84, 1.90, 1.96, 2.0, 2.12, 2.25,
        /*7*/ 2.37, 2.50, 2.62, 2.75, 2.87, 3.0, 3.2, 3.4, 3.6, 3.8,
        /*8*/ 4.0, 4.3, 4.7, 4.9, 5.0, 5.5, 6.0, 6.5, 6.8, 7.0,
        /*9*/ 7.3, 7.5, 7.8, 8.0, 8.4, 8.7, 9.0, 9.4, 9.6, 9.8,
        /*10*/ 10.0};
    private float[] matrix = new float[]{
        1, 0, 0, 0, 0,
        0, 1, 0, 0, 0,
        0, 0, 1, 0, 0,
        0, 0, 0, 1, 0
    };

    private int brightness = 0;
    private int saturation = 0;
    private int contrast = 0;
    private int hue = 0;

    public ColorMatrixConvertor(float[] matrix) {
        this.matrix = matrix;
        convertFromMatrix();
    }

    public ColorMatrixConvertor() {
    }

    public int getBrightness() {
        return brightness;
    }

    public int getSaturation() {
        return saturation;
    }

    public int getContrast() {
        return contrast;
    }

    public int getHue() {
        return hue;
    }

    public void setBrightness(int brightness) {
        if (brightness < -100) {
            brightness = -100;
        }
        if (brightness > 100) {
            brightness = 100;
        }
        this.brightness = brightness;
        convertToMatrix();
    }

    public void setContrast(int contrast) {
        if (contrast < -100) {
            contrast = -100;
        }
        if (contrast > 100) {
            contrast = 100;
        }

        this.contrast = contrast;
        convertToMatrix();
    }

    public void setHue(int hue) {
        if (hue < -180) {
            hue = -180;
        }
        if (hue > 180) {
            hue = 180;
        }
        this.hue = hue;
        convertToMatrix();
    }

    public void setSaturation(int saturation) {
        if (saturation < -100) {
            saturation = -100;
        }
        if (saturation > 100) {
            saturation = 100;
        }

        this.saturation = saturation;
        convertToMatrix();
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;

        convertFromMatrix();
    }

    public float[] getMatrix() {
        return matrix;
    }

    private void convertToMatrix() {

        float b = brightness;

        float c;
        if (contrast == 0) {
            c = 1;
        } else if (contrast > 0) {
            c = (float) contrastMap[contrast] + 1;
        } else {
            c = contrast / 100f + 1;
        }

        float s;
        if (saturation == 0) {
            s = 1;
        } else if (saturation > 0) {
            s = 1.0f + (3 * saturation / 100f); // max value is 4
        } else {
            s = saturation / 100f + 1;
        }
        float h = (float) Math.toRadians(hue);

        float[][] mBrightness = new float[][]{
            {1, 0, 0, 0, b},
            {0, 1, 0, 0, b},
            {0, 0, 1, 0, b},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1}
        };

        float t = 0.5f * (1f - c) * 127f;
        float[][] mContrast = new float[][]{
            {c, 0, 0, 0, t},
            {0, c, 0, 0, t},
            {0, 0, c, 0, t},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1}
        };

        float lumSatR = 0.3086f;
        float lumSatG = 0.6094f;
        float lumSatB = 0.0820f;

        float[][] mSaturation = new float[][]{
            {s + (1 - s) * lumSatR, (1 - s) * lumSatG, (1 - s) * lumSatB, 0, 0},
            {(1 - s) * lumSatR, s + (1 - s) * lumSatG, (1 - s) * lumSatB, 0, 0},
            {(1 - s) * lumSatR, (1 - s) * lumSatG, s + (1 - s) * lumSatB, 0, 0},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1}
        };

        float lumHueR = 0.213f;
        float lumHueG = 0.715f;
        float lumHueB = 0.072f;

        float cosH = (float) Math.cos(h);
        float sinH = (float) Math.sin(h);

        float[][] mHue = new float[][]{
            {
                lumHueR + cosH * (1 - lumHueR) + sinH * (-lumHueR),
                lumHueG + cosH * (-lumHueG) + sinH * (-lumHueG),
                lumHueB + cosH * (-lumHueB) + sinH * (1 - lumHueB),
                0f,
                0f
            },
            {
                lumHueR + cosH * (-lumHueR) + sinH * 0.143f,
                lumHueG + cosH * (1 - lumHueG) + sinH * 0.140f,
                lumHueB + cosH * (-lumHueB) + sinH * (-0.283f),
                0f,
                0f
            },
            {
                lumHueR + cosH * (-lumHueR) + sinH * (-(1 - lumHueR)),
                lumHueG + cosH * (-lumHueG) + sinH * lumHueG,
                lumHueB + cosH * (1 - lumHueB) + sinH * lumHueB,
                0f,
                0f
            },
            {0f, 0f, 0f, 1f, 0f},
            {0f, 0f, 0f, 0f, 1f}
        };

        float[][] result2d = multiplyMatrices(mContrast, mBrightness);
        result2d = multiplyMatrices(mSaturation, result2d);
        result2d = multiplyMatrices(mHue, result2d);

        float[] result = new float[5 * 4];
        int i = 0;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 5; x++) {
                result[i] = result2d[y][x];
                i++;
            }
        }

        this.matrix = result;
    }

    public static float[][] multiplyMatrices(float[][] a, float[][] b) {
        float[][] result = new float[5][5];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 5; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    private static int normHue(double h) {
        if (Double.isNaN(h)) {
            h = -Math.PI;
        }
        int ret = (int) Math.round(h * 180 / Math.PI);
        while (ret > 180) {
            ret -= 360;
        }
        while (ret < -180) {
            ret += 360;
        }
        return ret;
    }

    private static int normBrightness(double b) {
        if (Double.isNaN(b)) {
            b = -100;
        }
        return (int) Math.round(b);
    }

    private static int normSaturation(double s) {
        if (Double.isNaN(s)) {
            return -100;
        } else if (s == 1) {
            return 0;
        } else if (s - 1 < 0) {
            return (int) Math.round((s - 1) * 100);
        } else {
            return (int) Math.round(((s - 1) * 100) / 3);
        }
    }

    private static int normContrast(double c) {
        if (c == 127) {
            return 0;
        } else if (c - 127 < 0) {
            return (int) Math.round((c - 127) * 100.0 / 127.0);
        } else {
            c = (c - 127) / 127;
            c = Math.round(c * 100.0) / 100.0;
            for (int i = 0; i < contrastMap.length; i++) {
                if (contrastMap[i] >= c) {
                    return i;
                }
            }
        }
        return contrastMap.length - 1;
    }

    private static boolean sameDouble(double a, double b) {
        final double EPSILON = 0.00001;
        return a == b ? true : Math.abs(a - b) < EPSILON;
    }

    private void convertFromMatrix() {
        float[][] matrix2d = new float[5][5];
        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                matrix2d[j][i] = this.matrix[index];
                index++;
            }
        }
        double a11 = matrix2d[0][0];
        double a12 = matrix2d[0][1];
        double a13 = matrix2d[0][2];
        double a21 = matrix2d[1][0];
        double a22 = matrix2d[1][1];
        double a23 = matrix2d[1][2];
        double a31 = matrix2d[2][0];
        double a32 = matrix2d[2][1];
        double a33 = matrix2d[2][2];
        double a41 = matrix2d[4][0];

        double b;
        double c;
        double h;
        double s;

        //Magic formulas calculated long time ago with Wolphram Alpha.
        b = (24872168661075.0 * a11 * a11 - 151430415740925.0 * a12 + 341095051289483.0 * a12 * a12 - 15302094789450.0 * a13 + 82428663495404.0 * a12 * a13
                - 4592294873812.0 * a13 * a13 + 43556251470.0 * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13
                        - 930 * a11 * (287 * a12 + 178 * a13)) + 2384730956550.0 * a12 * a41 + 240977870700.0 * a13 * a41
                - 685925220 * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13 - 930 * a11 * (287 * a12 + 178 * a13))
                * a41 + 465 * a11 * (466201717582.0 * a12 + 55756962908.0 * a13 + 764132175 * (-127 + 2 * a41)))
                / (391687695450.0 * a11 * a11 + 5371575610858.0 * a12 * a12 + 1298089188904.0 * a12 * a13 - 72319604312.0 * a13 * a13
                + 1860 * a11 * (1835439833 * a12 + 219515602 * a13));
        c = (127 * (495225 * a11 + 1661845 * a12 + 167930 * a13
                + 478 * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13 - 930 * a11 * (287 * a12 + 178 * a13))))
                / 717495;
        h = 2 * (Math.atan((-465 * a11 + 287 * a12 + 178 * a13 + Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13
                - 930 * a11 * (287 * a12 + 178 * a13))) / (500. * (a12 - a13))) + Math.PI/*+ Pi*C(1)*/);
        s = (1543 * (-103355550 * a11 * a11 - 158872382 * a12 * a12 + 190161784 * a12 * a13 - 134644952 * a13 * a13
                + 1661845 * a12 * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13
                        - 930 * a11 * (287 * a12 + 178 * a13)) + 167930 * a13
                * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13 - 930 * a11 * (287 * a12 + 178 * a13))
                + 465 * a11 * (274372 * a12 + 170168 * a13 + 1065 * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13
                        + 281684 * a13 * a13 - 930 * a11 * (287 * a12 + 178 * a13)))))
                / (195843847725.0 * a11 * a11 + 2685787805429.0 * a12 * a12 + 649044594452.0 * a12 * a13 - 36159802156.0 * a13 * a13
                + 930 * a11 * (1835439833 * a12 + 219515602 * a13));

        if (sameDouble(410 * a12, 1543 * a31) && sameDouble(410 * a12, 1543 * a32) && sameDouble(3047 * a12, 1543 * a21) && sameDouble(3047 * a12, 1543 * a23)
                && sameDouble(a22, a11 + (1504 * a12) / 1543.) && sameDouble((1133 * a12) / 1543. + a33, a11)
                && !sameDouble(a11, a12) && !sameDouble(1543 * a11 + 3457 * a12, 0)) {
            h = 0;
        }

        brightness = normBrightness(b);
        contrast = normContrast(c);
        saturation = normSaturation(s);
        hue = normHue(h);
    }

    @Override
    public String toString() {
        return "[brightness: " + brightness + ", contrast:" + contrast + ", saturation:" + saturation + ", hue: " + hue + "]";
    }

}

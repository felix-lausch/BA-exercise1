/**
 * @author Nico Hezel & Felix Lausch
 */
package de.htw.ba.ue01;

import java.awt.*;
import java.util.stream.IntStream;

public class EdgeDetectionController extends EdgeDetectionBase {
    //region Fields
    private final int[] yGradientKernel = {
            0, 1, 0,
            0, 0, 0,
            0, -1, 0,
    };

    private final int[] xGradientKernel = {
            0, 0, 0,
            1, 0, -1,
            0, 0, 0,
    };

    private final int[] xGradientSobelKernel = {
            1, 0, -1,
            2, 0, -2,
            1, 0, -1,
    };

    private final int[] yGradientSobelKernel = {
            1, 2, 1,
            0, 0, 0,
            -1, -2, -1,
    };
    // endregion

    protected enum Methods {
        Kopie, Graustufen, xGradient, yGradient, yGradientSobel,
        xGradientSobel, BetragSobel, WinkelSobel, WinkelColor, Kombiniert
    }

    @Override
    public void runMethod(Methods currentMethod, int[] srcPixels, int[] dstPixels, int width, int height) throws Exception {
        int[] greyscale = convertToGray(srcPixels, width, height, getParameter());

        switch (currentMethod) {
            case xGradient:
                applyKernel(greyscale, dstPixels, width, height, xGradientKernel);
                break;

            case yGradient:
                applyKernel(greyscale, dstPixels, width, height, yGradientKernel);
                break;

            case yGradientSobel:
                applyKernel(greyscale, dstPixels, width, height, yGradientSobelKernel);
                break;

            case xGradientSobel:
                applyKernel(greyscale, dstPixels, width, height, xGradientSobelKernel);
                break;

            case Graustufen:
                applyGreyscale(greyscale, dstPixels);
                break;

            case BetragSobel:
                applyBetragSobel(greyscale, dstPixels, width, height);
                break;

            case WinkelSobel:
                applyWinkelSobel(greyscale, dstPixels, width, height);
                break;

            case WinkelColor:
                applyAngleColor(greyscale, dstPixels, width, height);
                break;

            case Kombiniert:
                applyKombiniert(greyscale, dstPixels, width, height);
                break;

            case Kopie:
            default:
                System.arraycopy(srcPixels, 0, dstPixels, 0, greyscale.length);
                break;
        }
    }

    /**
     * Apply the given kernel directly to the dstPixels array
     *
     * @param kernel needs to be 1 dimensional of size 3x3, 5x5, 7x7 etc.
     */
    private void applyKernel(int[] srcPixels, int[] dstPixels, int width, int height, int[] kernel) {
        int kernelSize = (int) Math.sqrt(kernel.length);
        int kernelSum = IntStream.of(kernel)
                .map(Math::abs)
                .sum();

        int delta = -kernelSize / 2;

        // Double for loop for each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int pos = y * width + x;
                int value = 0;
                int kernelPos = 0;

                // Double for loop for each kernel Position
                for (int kernelY = y + delta; kernelY <= y - delta; kernelY++) {
                    for (int kernelX = x + delta; kernelX <= x - delta; kernelX++) {

                        int currentKernelVal = kernel[kernelPos];
                        int pixel;

                        // wenn Randbehandlungsfälle -> Pixelwiederholung
                        if (kernelY < 0 || kernelX < 0 || kernelY >= height || kernelX >= width) {
                            pixel = srcPixels[pos];
                        } else {
                            pixel = srcPixels[kernelY * width + kernelX];
                        }
                        value += (pixel * currentKernelVal);
                        kernelPos++; // Count up kernelPos to access correct value in kernel array
                    }
                }
                value /= kernelSum;
                value += 128;
                value = capValue(value);

                dstPixels[pos] = 0xFF000000 | (value << 16) | (value << 8) | value;
            }
        }
    }

    /**
     * Convert the srcPixels array with the given kernel.
     *
     * @param kernel needs to be 1 dimensional of size 3x3, 5x5, 7x7 etc.
     * @return The result of srcPixels with kernel applied to it.
     */
    private int[] convertWithKernel(int[] srcPixels, int width, int height, int[] kernel) {
        int[] result = new int[srcPixels.length];
        int kernelSize = (int) Math.sqrt(kernel.length);
        int kernelSum = IntStream.of(kernel)
                .map(Math::abs)
                .sum();

        int delta = -kernelSize / 2;

        // Double for loop for each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int pos = y * width + x;
                int value = 0;
                int kernelPos = 0;

                // Double for loop for each kernel Position
                for (int kernelY = y + delta; kernelY <= y - delta; kernelY++) {
                    for (int kernelX = x + delta; kernelX <= x - delta; kernelX++) {

                        int currentKernelVal = kernel[kernelPos];
                        int pixel;

                        // Randbehandlung -> Pixelwiederholung
                        if (kernelY < 0 || kernelX < 0 || kernelY >= height || kernelX >= width) {
                            pixel = srcPixels[pos];
                        } else {
                            pixel = srcPixels[kernelY * width + kernelX];
                        }
                        value += (pixel * currentKernelVal);
                        kernelPos++; // Count up kernelPos to access correct value in kernel array
                    }
                }

                value /= kernelSum;
                result[pos] = value;
            }
        }
        return result;
    }

    /**
     * Calculate angle of the x&y derivation using Math.atan2(a,b), normalize the result and use the angle
     * as the hue value for HSBtoRGB(). Use normalized result of convertToBetragSobel() as brightness factor.
     * Applied directly to dstPixels
     */
    private void applyKombiniert(int[] greyscale, int[] dstPixels, int width, int height) {
        int[] xAbleitung = convertWithKernel(greyscale, width, height, xGradientSobelKernel);
        int[] yAbleitung = convertWithKernel(greyscale, width, height, yGradientSobelKernel);

        int[] betragArray = convertToBetragSobel(xAbleitung, yAbleitung);
        double maxBetrag = IntStream.of(betragArray).max().getAsInt();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;

                double atan = Math.atan2(xAbleitung[pos], yAbleitung[pos]);
                double betragFaktor = betragArray[pos] / maxBetrag;

                double normalized = (atan + Math.PI) / (2 * Math.PI);
                double color = Color.HSBtoRGB((float) normalized, 1, (float) betragFaktor);

                int value = (int) color;
                dstPixels[pos] = value;
            }
        }
    }

    /**
     * Calculate angle of the x&y derivation using Math.atan2(a,b), normalize the result and use the angle
     * as the hue value for HSBtoRGB().
     * Applied directly to dstPixels.
     */
    private void applyAngleColor(int[] greyscale, int[] dstPixels, int width, int height) {
        int[] xAbleitung = convertWithKernel(greyscale, width, height, xGradientSobelKernel);
        int[] yAbleitung = convertWithKernel(greyscale, width, height, yGradientSobelKernel);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;

                double atan = Math.atan2(xAbleitung[pos], yAbleitung[pos]);
                double normalized = (atan + Math.PI) / (2 * Math.PI);
                double color = Color.HSBtoRGB((float) normalized, 1, 1);

                int value = (int) color;

                dstPixels[pos] = value;
            }
        }
    }

    /**
     * Apply GradientBetrag to dstPixels array
     */
    private void applyBetragSobel(int[] greyscale, int[] dstPixels, int width, int height) {
        int[] xAbleitung = convertWithKernel(greyscale, width, height, xGradientSobelKernel);
        int[] yAbleitung = convertWithKernel(greyscale, width, height, yGradientSobelKernel);

        int[] betragArray = convertToBetragSobel(xAbleitung, yAbleitung);
        double maxBetrag = IntStream.of(betragArray).max().getAsInt();

        int[] normalized = IntStream.of(betragArray)
                .map(value -> (int) ((double) value / maxBetrag * 255))
                .map(value -> 0xFF000000 | (value << 16) | (value << 8) | value)
                .toArray();

        System.arraycopy(normalized, 0, dstPixels, 0, greyscale.length);
    }

    /**
     * Take x&y derivation arrays and calculate value using Math.hypot(a,b)
     *
     * @return the resulting array
     */
    private int[] convertToBetragSobel(int[] xAbleitung, int[] yAbleitung) {
        return IntStream.range(0, xAbleitung.length)
                .map(i -> (int) Math.hypot(xAbleitung[i], yAbleitung[i]))
                .map(this::capValue)
                .toArray();
    }

    private void applyWinkelSobel(int[] greyscale, int[] dstPixels, int width, int height) {
        int[] xAbleitung = convertWithKernel(greyscale, width, height, xGradientSobelKernel);
        int[] yAbleitung = convertWithKernel(greyscale, width, height, yGradientSobelKernel);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;

                double xCol = xAbleitung[pos];
                double yCol = yAbleitung[pos];

                double atan = Math.atan2(xCol, yCol);
                double normalized = (atan + Math.PI) / (2 * Math.PI) * 255;
                int value = (int) normalized;

                dstPixels[pos] = 0xFF000000 | (value << 16) | (value << 8) | value;
            }
        }
    }

    private int[] convertToGray(int[] srcPixels, int width, int height, double parameter) {
        // loop over all pixels of the destination image
        int[] result = new int[srcPixels.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;

                int lum = calculateLum(srcPixels, parameter, pos);
                lum = capValue(lum);
                result[pos] = lum;
            }
        }
        return result;
    }

    private void applyGreyscale(int[] greyscale, int[] dstPixels) {
        int[] result = IntStream.of(greyscale)
                .map(value -> 0xFF000000 | (value << 16) | (value << 8) | value)
                .toArray();
        System.arraycopy(result, 0, dstPixels, 0, greyscale.length);
    }

    private int capValue(int value) {
        value = Math.max(value, 0);
        value = Math.min(value, 255);
        return value;
    }

    private int calculateLum(int[] srcPixels, double parameter, int pos) {
        int c = srcPixels[pos];
        int r = (c >> 16) & 0xFF;
        int g = (c >> 8) & 0xFF;
        int b = (c) & 0xFF;

        int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b + parameter);
        return lum;
    }
}

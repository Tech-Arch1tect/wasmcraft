package uk.co.techarchitect.wasmcraft.drawing;

public class MonitorDrawing {

    public static void fillRect(byte[] pixels, int bufferWidth, int bufferHeight,
                                int x, int y, int width, int height,
                                int r, int g, int b) {
        int x1 = Math.max(0, x);
        int y1 = Math.max(0, y);
        int x2 = Math.min(bufferWidth, x + width);
        int y2 = Math.min(bufferHeight, y + height);

        if (x1 >= x2 || y1 >= y2) {
            return;
        }

        byte rByte = (byte) r;
        byte gByte = (byte) g;
        byte bByte = (byte) b;

        for (int py = y1; py < y2; py++) {
            int rowStart = (py * bufferWidth + x1) * 3;
            int rowEnd = (py * bufferWidth + x2) * 3;

            for (int offset = rowStart; offset < rowEnd; offset += 3) {
                pixels[offset] = rByte;
                pixels[offset + 1] = gByte;
                pixels[offset + 2] = bByte;
            }
        }
    }

    public static void drawHLine(byte[] pixels, int bufferWidth, int bufferHeight,
                                 int x, int y, int length,
                                 int r, int g, int b) {
        fillRect(pixels, bufferWidth, bufferHeight, x, y, length, 1, r, g, b);
    }

    public static void drawVLine(byte[] pixels, int bufferWidth, int bufferHeight,
                                 int x, int y, int length,
                                 int r, int g, int b) {
        fillRect(pixels, bufferWidth, bufferHeight, x, y, 1, length, r, g, b);
    }

    public static void drawRect(byte[] pixels, int bufferWidth, int bufferHeight,
                                int x, int y, int width, int height,
                                int r, int g, int b) {
        drawHLine(pixels, bufferWidth, bufferHeight, x, y, width, r, g, b);
        drawHLine(pixels, bufferWidth, bufferHeight, x, y + height - 1, width, r, g, b);
        drawVLine(pixels, bufferWidth, bufferHeight, x, y, height, r, g, b);
        drawVLine(pixels, bufferWidth, bufferHeight, x + width - 1, y, height, r, g, b);
    }

    public static void drawChar(byte[] pixels, int bufferWidth, int bufferHeight,
                                int x, int y, char c,
                                int fgR, int fgG, int fgB,
                                int bgR, int bgG, int bgB,
                                int scale) {
        BitmapFont.renderChar(pixels, bufferWidth, bufferHeight, x, y, c,
                            fgR, fgG, fgB, bgR, bgG, bgB, scale);
    }

    public static int drawText(byte[] pixels, int bufferWidth, int bufferHeight,
                               int x, int y, String text,
                               int fgR, int fgG, int fgB,
                               int bgR, int bgG, int bgB,
                               int scale) {
        int currentX = x;
        int charWidth = BitmapFont.getCharWidth(scale);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            BitmapFont.renderChar(pixels, bufferWidth, bufferHeight,
                                currentX, y, c,
                                fgR, fgG, fgB, bgR, bgG, bgB, scale);
            currentX += charWidth;
        }

        return currentX - x;
    }

    public static int measureTextWidth(String text, int scale) {
        return text.length() * BitmapFont.getCharWidth(scale);
    }

    public static int measureTextHeight(int scale) {
        return BitmapFont.getCharHeight(scale);
    }

    public static void copyRegion(byte[] pixels, int bufferWidth, int bufferHeight,
                                  int srcX, int srcY, int width, int height,
                                  int dstX, int dstY) {
        int srcX1 = Math.max(0, srcX);
        int srcY1 = Math.max(0, srcY);
        int srcX2 = Math.min(bufferWidth, srcX + width);
        int srcY2 = Math.min(bufferHeight, srcY + height);

        if (srcX1 >= srcX2 || srcY1 >= srcY2) {
            return;
        }

        int clipLeft = srcX1 - srcX;
        int clipTop = srcY1 - srcY;
        int actualWidth = srcX2 - srcX1;
        int actualHeight = srcY2 - srcY1;
        int actualDstX = dstX + clipLeft;
        int actualDstY = dstY + clipTop;

        int dstX1 = Math.max(0, actualDstX);
        int dstY1 = Math.max(0, actualDstY);
        int dstX2 = Math.min(bufferWidth, actualDstX + actualWidth);
        int dstY2 = Math.min(bufferHeight, actualDstY + actualHeight);

        if (dstX1 >= dstX2 || dstY1 >= dstY2) {
            return;
        }

        int dstClipLeft = dstX1 - actualDstX;
        int dstClipTop = dstY1 - actualDstY;
        int finalSrcX = srcX1 + dstClipLeft;
        int finalSrcY = srcY1 + dstClipTop;
        int finalWidth = dstX2 - dstX1;
        int finalHeight = dstY2 - dstY1;

        boolean copyBackward = (finalSrcY < dstY1) || (finalSrcY == dstY1 && finalSrcX < dstX1);

        if (copyBackward) {
            for (int row = finalHeight - 1; row >= 0; row--) {
                int srcRow = finalSrcY + row;
                int dstRow = dstY1 + row;
                int srcOffset = (srcRow * bufferWidth + finalSrcX) * 3;
                int dstOffset = (dstRow * bufferWidth + dstX1) * 3;
                int rowBytes = finalWidth * 3;

                for (int i = rowBytes - 3; i >= 0; i -= 3) {
                    pixels[dstOffset + i] = pixels[srcOffset + i];
                    pixels[dstOffset + i + 1] = pixels[srcOffset + i + 1];
                    pixels[dstOffset + i + 2] = pixels[srcOffset + i + 2];
                }
            }
        } else {
            for (int row = 0; row < finalHeight; row++) {
                int srcRow = finalSrcY + row;
                int dstRow = dstY1 + row;
                int srcOffset = (srcRow * bufferWidth + finalSrcX) * 3;
                int dstOffset = (dstRow * bufferWidth + dstX1) * 3;
                int rowBytes = finalWidth * 3;

                System.arraycopy(pixels, srcOffset, pixels, dstOffset, rowBytes);
            }
        }
    }
}

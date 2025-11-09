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
}

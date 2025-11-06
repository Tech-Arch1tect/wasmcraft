import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TextureGenerator {
    private static final int SIZE = 16;
    private static final Color BLACK = new Color(0, 0, 0);
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color DARK_GRAY = new Color(40, 40, 40);
    private static final Color GRAY = new Color(80, 80, 80);
    private static final Color LIGHT_GRAY = new Color(160, 160, 160);

    public static void main(String[] args) throws IOException {
        String outputDir = "common/src/main/resources/assets/wasmcraft/textures/block";
        new File(outputDir).mkdirs();

        generateFrontTexture(outputDir + "/computer_front.png");
        generateSideTexture(outputDir + "/computer_side.png");
        generateTopTexture(outputDir + "/computer_top.png");

        System.out.println("Textures generated successfully in " + outputDir);
        System.out.println("- computer_front.png (screen with text)");
        System.out.println("- computer_side.png (circuit pattern)");
        System.out.println("- computer_top.png (vent pattern)");
    }

    private static void generateFrontTexture(String path) throws IOException {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setColor(BLACK);
        g.fillRect(0, 0, SIZE, SIZE);

        g.setColor(DARK_GRAY);
        g.drawRect(0, 0, SIZE - 1, SIZE - 1);
        g.drawRect(1, 1, SIZE - 3, SIZE - 3);

        g.setColor(WHITE);
        for (int line = 0; line < 3; line++) {
            int y = 4 + (line * 3);
            drawTextLine(g, y);
        }

        g.fillRect(3, 13, 2, 1);

        g.dispose();
        ImageIO.write(img, "PNG", new File(path));
    }

    private static void drawTextLine(Graphics2D g, int y) {
        int[] pattern = {3, 4, 6, 7, 8, 10, 11, 13};
        for (int x : pattern) {
            g.fillRect(x, y, 1, 1);
        }
    }

    private static void generateSideTexture(String path) throws IOException {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setColor(DARK_GRAY);
        g.fillRect(0, 0, SIZE, SIZE);

        g.setColor(GRAY);
        g.drawLine(2, 3, 13, 3);
        g.drawLine(4, 7, 11, 7);
        g.drawLine(3, 11, 12, 11);
        g.drawLine(5, 3, 5, 7);
        g.drawLine(9, 7, 9, 11);

        g.setColor(LIGHT_GRAY);
        g.fillRect(6, 4, 2, 2);
        g.fillRect(10, 8, 2, 2);
        g.fillRect(4, 12, 2, 2);

        g.setColor(WHITE);
        g.fillRect(5, 3, 1, 1);
        g.fillRect(9, 7, 1, 1);
        g.fillRect(5, 7, 1, 1);
        g.fillRect(9, 11, 1, 1);

        g.dispose();
        ImageIO.write(img, "PNG", new File(path));
    }

    private static void generateTopTexture(String path) throws IOException {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setColor(DARK_GRAY);
        g.fillRect(0, 0, SIZE, SIZE);

        g.setColor(BLACK);
        for (int y = 2; y < SIZE - 2; y += 3) {
            for (int x = 2; x < SIZE - 2; x += 2) {
                g.drawLine(x, y, x + 1, y);
            }
        }

        g.setColor(GRAY);
        for (int y = 3; y < SIZE - 2; y += 3) {
            for (int x = 2; x < SIZE - 2; x += 2) {
                g.fillRect(x, y, 1, 1);
            }
        }

        g.dispose();
        ImageIO.write(img, "PNG", new File(path));
    }
}

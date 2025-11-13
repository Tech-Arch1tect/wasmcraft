import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class EntityTextureGenerator {
    private static final int SIZE = 64;
    private static final Color BLACK = new Color(0, 0, 0);
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color DARK_GRAY = new Color(40, 40, 40);
    private static final Color GRAY = new Color(80, 80, 80);
    private static final Color LIGHT_GRAY = new Color(160, 160, 160);
    private static final Color RED = new Color(200, 0, 0);
    private static final Color GREEN = new Color(0, 200, 0);
    private static final Color BLUE = new Color(0, 100, 200);
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static void main(String[] args) throws IOException {
        String outputDir = "common/src/main/resources/assets/wasmcraft/textures/entity";
        new File(outputDir).mkdirs();

        generateDroneTexture(outputDir + "/drone.png");

        System.out.println("Entity textures generated successfully in " + outputDir);
        System.out.println("- drone.png (quadcopter style drone with rotors)");
    }

    private static void generateDroneTexture(String path) throws IOException {

        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setColor(TRANSPARENT);
        g.fillRect(0, 0, SIZE, SIZE);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawDroneFront(g, 16, 16, 16);

        drawDroneBack(g, 48, 16, 16);

        drawDroneTop(g, 16, 0, 16);

        drawDroneBottom(g, 32, 0, 16);

        drawDroneSide(g, 0, 16, 16, false);

        drawDroneSide(g, 32, 16, 16, true);

        drawRotor(g, 0, 32, 8);

        drawRotor(g, 8, 32, 8);

        drawRotor(g, 16, 32, 8);

        drawRotor(g, 24, 32, 8);

        g.dispose();
        ImageIO.write(img, "PNG", new File(path));
    }

    private static void drawDroneFront(Graphics2D g, int x, int y, int size) {
        g.setColor(DARK_GRAY);
        g.fillRect(x, y, size, size);

        g.setColor(GRAY);
        g.drawRect(x, y, size - 1, size - 1);

        g.setColor(BLACK);
        g.fillRect(x + 4, y + 4, size - 8, size - 8);

        g.setColor(GREEN);
        g.fillRect(x + 5, y + 5, 2, 2);

        g.setColor(BLUE);
        g.drawLine(x + 6, y + 9, x + size - 6, y + 9);
        g.drawLine(x + 8, y + 11, x + size - 8, y + 11);
        g.fillRect(x + 9, y + 12, 1, 1);
        g.fillRect(x + size - 9, y + 12, 1, 1);

        g.setColor(RED);
        g.fillRect(x + 4, y + size - 3, 1, 1);
        g.setColor(GREEN);
        g.fillRect(x + 6, y + size - 3, 1, 1);
    }

    private static void drawDroneBack(Graphics2D g, int x, int y, int size) {
        g.setColor(DARK_GRAY);
        g.fillRect(x, y, size, size);

        g.setColor(GRAY);
        g.drawRect(x, y, size - 1, size - 1);

        g.setColor(BLACK);
        for (int i = 0; i < 3; i++) {
            int ventY = y + 4 + (i * 4);
            g.drawLine(x + 4, ventY, x + size - 4, ventY);
            g.drawLine(x + 4, ventY + 1, x + size - 4, ventY + 1);
        }

        g.setColor(LIGHT_GRAY);
        g.drawLine(x + 1, y + 1, x + size - 2, y + 1);
        g.drawLine(x + 1, y + 1, x + 1, y + size - 2);
    }

    private static void drawDroneTop(Graphics2D g, int x, int y, int size) {
        g.setColor(GRAY);
        g.fillRect(x, y, size, size);

        g.setColor(DARK_GRAY);
        g.drawRect(x, y, size - 1, size - 1);

        g.setColor(LIGHT_GRAY);
        g.fillRect(x + 6, y, 4, size);

        g.setColor(DARK_GRAY);
        g.fillOval(x + 2, y + 2, 3, 3);
        g.fillOval(x + size - 5, y + 2, 3, 3);
        g.fillOval(x + 2, y + size - 5, 3, 3);
        g.fillOval(x + size - 5, y + size - 5, 3, 3);

        g.setColor(BLACK);
        g.drawOval(x + size/2 - 2, y + size/2 - 2, 4, 4);
    }

    private static void drawDroneBottom(Graphics2D g, int x, int y, int size) {
        g.setColor(DARK_GRAY);
        g.fillRect(x, y, size, size);

        g.setColor(GRAY);
        g.drawRect(x, y, size - 1, size - 1);

        g.setColor(BLACK);
        g.fillRect(x + 2, y + 2, 2, 4);
        g.fillRect(x + size - 4, y + 2, 2, 4);
        g.fillRect(x + 2, y + size - 6, 2, 4);
        g.fillRect(x + size - 4, y + size - 6, 2, 4);

        g.setColor(GRAY);
        g.fillRect(x + 5, y + 5, size - 10, size - 10);
    }

    private static void drawDroneSide(Graphics2D g, int x, int y, int size, boolean left) {
        g.setColor(DARK_GRAY);
        g.fillRect(x, y, size, size);

        g.setColor(GRAY);
        g.drawRect(x, y, size - 1, size - 1);

        g.setColor(LIGHT_GRAY);
        int arm1X = left ? x + 3 : x + size - 4;
        int arm2X = left ? x + 3 : x + size - 4;
        g.fillRect(arm1X, y + 2, 2, 3);
        g.fillRect(arm2X, y + size - 5, 2, 3);

        g.setColor(BLACK);
        g.drawLine(x + 4, y + size/2, x + size - 4, y + size/2);

        g.setColor(BLUE);
        g.fillRect(x + size/2 - 1, y + size/2 + 2, 2, 2);
    }

    private static void drawRotor(Graphics2D g, int x, int y, int size) {
        g.setColor(DARK_GRAY);
        g.fillOval(x + size/2 - 1, y + size/2 - 1, 3, 3);

        g.setColor(LIGHT_GRAY);

        g.fillRect(x + 1, y + 1, size - 2, 1);
        g.fillRect(x + 1, y + 2, size - 2, 1);

        g.fillRect(x + 1, y + size - 3, size - 2, 1);
        g.fillRect(x + 1, y + size - 2, size - 2, 1);

        g.setColor(BLACK);
        g.fillRect(x + size/2, y + size/2, 1, 1);
    }
}

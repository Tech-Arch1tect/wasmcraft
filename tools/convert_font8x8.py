#!/usr/bin/env python3
"""
Convert font8x8_basic.h (Public Domain) to Java BitmapFont class.

This script downloads the font8x8_basic.h file from:
https://github.com/dhepper/font8x8

Font License: Public Domain
Author: Daniel Hepper
Based on work by Marcel Sondaar and IBM public domain VGA fonts

This conversion script is provided to generate Java code from the
Public Domain font data without directly copying it into the codebase.
"""

import re
import sys

def parse_font_data(header_file):
    """Parse the C header file and extract font data."""
    with open(header_file, 'r') as f:
        content = f.read()

    # Find the font array
    pattern = r'char font8x8_basic\[128\]\[8\] = \{(.*?)\};'
    match = re.search(pattern, content, re.DOTALL)

    if not match:
        print("Error: Could not find font data in header file", file=sys.stderr)
        sys.exit(1)

    font_data_str = match.group(1)

    # Parse each character's data
    char_pattern = r'\{([^}]+)\}'
    char_matches = re.findall(char_pattern, font_data_str)

    font_array = []
    for char_data in char_matches:
        # Extract hex values
        bytes_str = re.findall(r'0x[0-9A-Fa-f]+', char_data)
        font_array.append([int(b, 16) for b in bytes_str])

    return font_array

def generate_java_class(font_data):
    """Generate Java class with font data."""

    # Only include printable ASCII (32-126)
    printable_start = 32
    printable_end = 126
    printable_data = font_data[printable_start:printable_end+1]

    java_code = '''package uk.co.techarchitect.wasmcraft.drawing;

/**
 * 8x8 bitmap font for text rendering.
 *
 * Font Data: Public Domain
 * Source: https://github.com/dhepper/font8x8
 * Author: Daniel Hepper
 * Based on: Marcel Sondaar and IBM public domain VGA fonts
 *
 * This class was generated from the Public Domain font8x8_basic.h
 * using tools/convert_font8x8.py
 */
public class BitmapFont {
    public static final int CHAR_WIDTH = 8;
    public static final int CHAR_HEIGHT = 8;
    public static final int FIRST_CHAR = 32;  // Space
    public static final int LAST_CHAR = 126;  // ~

    // Font data: [char_index][row] = byte with 8 pixels
    // Each byte represents one row of 8 pixels (MSB = leftmost pixel)
    private static final byte[][] FONT_8X8 = {
'''

    # Generate font data
    for i, char_bytes in enumerate(printable_data):
        char_code = printable_start + i
        char_display = chr(char_code) if 32 <= char_code <= 126 else f'0x{char_code:02X}'

        # Format bytes as Java byte array
        byte_strs = []
        for b in char_bytes:
            if b > 127:
                byte_strs.append(f'(byte)0x{b:02X}')
            else:
                byte_strs.append(f'0x{b:02X}')

        java_code += f'        // {char_code}: {char_display}\n'
        java_code += f'        {{{", ".join(byte_strs)}}}'

        if i < len(printable_data) - 1:
            java_code += ','
        java_code += '\n'

    java_code += '''    };

    public static void renderChar(byte[] pixels, int bufferWidth, int bufferHeight,
                                   int x, int y, char c,
                                   int fgR, int fgG, int fgB,
                                   int bgR, int bgG, int bgB,
                                   int scale) {
        if (c < FIRST_CHAR || c > LAST_CHAR) {
            c = '?';
        }

        int charIndex = c - FIRST_CHAR;
        byte[] charData = FONT_8X8[charIndex];

        for (int row = 0; row < CHAR_HEIGHT; row++) {
            byte rowData = charData[row];
            for (int col = 0; col < CHAR_WIDTH; col++) {
                boolean isForeground = (rowData & (1 << col)) != 0;

                int r = isForeground ? fgR : bgR;
                int g = isForeground ? fgG : bgG;
                int b = isForeground ? fgB : bgB;

                // Draw scaled pixel
                for (int sy = 0; sy < scale; sy++) {
                    for (int sx = 0; sx < scale; sx++) {
                        int px = x + col * scale + sx;
                        int py = y + row * scale + sy;

                        // Bounds check
                        if (px >= 0 && px < bufferWidth && py >= 0 && py < bufferHeight) {
                            int offset = (py * bufferWidth + px) * 3;
                            pixels[offset] = (byte) r;
                            pixels[offset + 1] = (byte) g;
                            pixels[offset + 2] = (byte) b;
                        }
                    }
                }
            }
        }
    }

    public static int getCharWidth(int scale) {
        return CHAR_WIDTH * scale;
    }

    public static int getCharHeight(int scale) {
        return CHAR_HEIGHT * scale;
    }
}
'''

    return java_code

def main():
    if len(sys.argv) != 3:
        print("Usage: python3 convert_font8x8.py <input.h> <output.java>")
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]

    print(f"Parsing {input_file}...")
    font_data = parse_font_data(input_file)
    print(f"Found {len(font_data)} characters")

    print(f"Generating Java class...")
    java_code = generate_java_class(font_data)

    print(f"Writing to {output_file}...")
    with open(output_file, 'w') as f:
        f.write(java_code)

    print("Done!")

if __name__ == '__main__':
    main()

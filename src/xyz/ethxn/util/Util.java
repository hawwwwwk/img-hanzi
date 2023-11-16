package xyz.ethxn.util;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Util {

    /**
     * Finds the greatest value in an ArrayList
     * @param arrayList the ArrayList to search
     * @param returnIndex whether to return the index of the greatest value or the value itself
     * @return the greatest value in an ArrayList
     */
    public static int findGreatestValue(ArrayList<Integer> arrayList, boolean returnIndex){
        if (arrayList.isEmpty()) {
            throw new IllegalArgumentException("the ArrayList is empty!!");
        }

        // if all digits are equal to each other, return the first digit:
        for (int i = 1; i < arrayList.size(); i++) {
            if (!Objects.equals(arrayList.get(i), arrayList.get(0))){
                break;
            }
            if (i == arrayList.size() - 1){
                if (returnIndex){
                    return 0;
                }
                return arrayList.get(0);
            }
        }


        int maxValue = arrayList.get(0); // Initialize with the first element

        for (int i = 1; i < arrayList.size(); i++) {
            int current = arrayList.get(i);
            if (current > maxValue) {
                maxValue = current;
            }
        }
        if (returnIndex){
            return arrayList.indexOf(maxValue);
        }
        return maxValue;
    }

    /**
     * Returns the complexity of a number, based of character
     * visual density. Used mostly in the HanziArt classes.
     * @param number the number to check
     * @return the complexity of the number
     */
    // todo: find a use for this class. unsure why it's unused...
    public static int getComplexity(int number) {
        return switch (number) {
            case 1 -> 10;
            case 2 -> 9;
            case 3 -> 8;
            case 0 -> 7;
            case 7 -> 6;
            case 8 -> 5;
            case 9 -> 4;
            case 4 -> 3;
            case 6 -> 2;
            case 5 -> 1;
            default -> -1;
        };
    }

    /**
     * Turn the digits of an int into an ArrayList (1 int per index)
     * @param number the int to convert
     * @return the ArrayList of digits
     */
    public static ArrayList<Integer> getDigits(int number) {
        int temp = number;
        int digitCount = 0;
        while (temp != 0) {
            temp /= 10;
            digitCount++;
        }

        ArrayList<Integer> digits = new ArrayList<>();

        for (int i = digitCount - 1; i >= 0; i--) {
            digits.add(number % 10);
            number /= 10;
        }

        return digits;
    }

    /**
     * Converts a unicode key to a String
     * @param code the unicode key
     * @return the String representation of the unicode key
     */
    public static String unicodeKeyToString(String code){
        int codePoint = Integer.parseInt(code.substring(2), 16); // Remove the leading "\\"
        return new String(Character.toChars(codePoint));
    }

    /**
     * Returns a HashMap formatted from a txt file.
     * @param requestedValue the value to search for in the txt file
     * @param txtPath the path to the txt file
     * @return a hashmap from a txt file.
     * @throws IOException if the txt file is not found
     */
    public static Map<String, String> hashMapFromTXT(String requestedValue, String txtPath) throws IOException{
        Map<String, String> outputMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(txtPath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\t");
                if (parts.length == 3 && parts[1].equals(requestedValue)) {
                    outputMap.put(parts[0], parts[2]);
                }
            }
            return outputMap;
        }
    }

    /**
     * Resizes an image to a specified width, maintaining aspect ratio.
     * @param image the image to resize
     * @param outputWidth the width to resize to in {@code pixels}
     * @return the resized image
     */
    public static BufferedImage resizeImage(BufferedImage image, int outputWidth){
        int outputHeight = (int) (outputWidth * (double) image.getHeight() / image.getWidth());
        BufferedImage resizedImage = new BufferedImage(outputWidth, outputHeight, image.getType());

        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, outputWidth, outputHeight, null);
        g.dispose();

        // Convert the resized image to grayscale
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        BufferedImage monochromeImage = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_BYTE_GRAY);
        op.filter(resizedImage, monochromeImage);

        return monochromeImage;
    }

    /**
     * Returns the brightness of a pixel in the range 0-255
     * @param image the image to get the pixel brightness from
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the brightness of a pixel in the range 0-255
     */
    public static int getPixelBrightness(BufferedImage image, int x, int y) {
        // Get the RGB value of the pixel at (x, y)
        int pixel = image.getRGB(x, y);

        return (pixel >> 16) & 0xFF;
    }
}

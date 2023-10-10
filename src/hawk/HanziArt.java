package hawk;

import hawk.util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class HanziArt {

    public static final Random random = new Random();
    public static HashSet<String> strokeKeys = new HashSet<>();
    public static StringBuilder outputArt = new StringBuilder();

    public static void main(String[] args) throws IOException {
        // just for testing, todo: remove later
        long startTime = System.nanoTime();

        // user inputs
        System.out.println("...loading inputs");
        String imgPath = "C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\test.jpg";
        BufferedImage image = ImageIO.read(new File(imgPath));
        String unihanDictionaryPath = "C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\Unihan_DictionaryLikeData.txt";
        String unihanIRGSourcesPath = "C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\Unihan_IRGSources.txt";
        int outputWidth = 75;
        int maxStrokeCount = 25; // 1-25, more usually means more detail but longer processing time

        Map<String, String> strokeCountMap = hashMapFromTXT("kTotalStrokes", unihanIRGSourcesPath);
        // for complex image
        Map<String, String> fourCornerCodeMap = hashMapFromTXT("kFourCornerCode", unihanDictionaryPath);


        System.out.println("...resizing image 1");
        BufferedImage resizedImage = resizeImage(image, outputWidth);
        ImageIO.write(resizedImage, "png", new File("C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\resized1.png"));
        // for complex image
        System.out.println("...resizing image 2");
        BufferedImage resizedImage2x = resizeImage(image, outputWidth * 2);
        ImageIO.write(resizedImage2x, "png", new File("C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\resized2.png"));

        fastOutput(image, maxStrokeCount, strokeCountMap, outputWidth);
        //complexOutput(resizedImage, resizedImage2x, maxStrokeCount, strokeCountMap, fourCornerCodeMap);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Finished in " + duration + " ms");
    }

    /**
     * Outputs a hanzi character for each pixel in the image.
     * @param image the image to process
     * @param maxStrokeCount the max stroke count for the characters
     * @param strokeCountMap the stroke count map
     */
    public static void fastOutput(BufferedImage image, int maxStrokeCount, Map<String, String> strokeCountMap, int outputWidth) throws IOException{
        BufferedImage resizedImage = resizeImage(image, outputWidth);

        for (int y = 0; y < resizedImage.getHeight(); y++) {
            System.out.println((y+1) + "/" + resizedImage.getHeight());

            for (int x = 0; x < resizedImage.getWidth(); x++) {
                int pixelBrightness = getPixelBrightness(resizedImage, x, y);
                int pixelStrokeCount = maxStrokeCount - (pixelBrightness) * (maxStrokeCount - 1) / 255;
                String pixelHanzi = getRandomHanziFromStrokeCount(pixelStrokeCount, strokeCountMap, strokeKeys);
                outputArt.append(pixelHanzi);
            }
            outputArt.append('\n');
        }
        System.out.println(outputArt);
    }

    /**
     * Returns a random hanzi character from the stroke count map.
     * @param pixelStrokeCount
     * @param strokeCountMap
     * @param keys
     * @return a random hanzi character from the stroke count map.
     */
    public static String getRandomHanziFromStrokeCount(int pixelStrokeCount, Map<String, String> strokeCountMap, HashSet<String> keys){
        // caching algorithm, matches stroke count
        regenerateStrokeKeySet(strokeCountMap, pixelStrokeCount);

        while (!keys.isEmpty()) {
            int randomIndex = random.nextInt(keys.size());
            String unicodeKey = (String) keys.toArray()[randomIndex];
            if (unicodeKey.length() <= 6 && !unicodeKey.contains("F")) {
                return Util.unicodeKeyToString(unicodeKey);
            } else {
                keys.remove(unicodeKey);
            }
        }
        return Util.unicodeKeyToString("U+3000"); // if the function reaches this something weird went wrong
    }

    /**
     * Regenerates the stroke key set if the stroke count of the image does not match the stroke count of the cached keys.
     * @param strokeCountMap hashmap of unicode keys and stroke counts
     * @param strokeCount max stroke count of the image
     */
    public static void regenerateStrokeKeySet(Map<String, String> strokeCountMap, int strokeCount){
        // caching alg
        if (!strokeKeys.isEmpty()){
            if (!strokeCountMap.get(strokeKeys.iterator().next()).equals(String.valueOf(strokeCount))){
                strokeKeys.clear();
            } else {
                return;
            }
        }
        forceRegenerateStrokeKeySet(strokeCountMap, strokeCount); // this method shouldn't be called anywhere else really.
    }

    /**
     * This method is called by regenerateStrokeKeySet() if the stroke count of the image does not match the stroke count of the cached keys.
     * @param strokeCountMap
     * @param strokeCount
     */
    public static void forceRegenerateStrokeKeySet(Map<String, String> strokeCountMap, int strokeCount){
        for (Map.Entry<String, String> entry : strokeCountMap.entrySet()) {
            if (entry.getValue().equals(Integer.toString(strokeCount))) {
                strokeKeys.add(entry.getKey());
            }
        }
    }

    /**
     * Resizes an image to a specified width, maintaining aspect ratio.
     * @param image
     * @param outputWidth
     * @return
     * @throws IOException
     */
    public static BufferedImage resizeImage(BufferedImage image, int outputWidth) throws IOException {
        int outputHeight = (int) (outputWidth * (double) image.getHeight() / image.getWidth());
        BufferedImage resizedImage = new BufferedImage(outputWidth, outputHeight, image.getType());

        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, outputWidth, outputHeight, null);
        g.dispose();

        return resizedImage;
    }

    /**
     * Returns the brightness of a pixel in the range 0-255
     * @param image the image to get the pixel brightness from
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the brightness of a pixel in the range 0-255
     */
    public static int getPixelBrightness(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) & 0xFF;
    }

    /**
     * Returns the brightness of a pixel in the range 0-255
     * @param image the image to get the pixel brightness from
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param redBias how much to weight the red component
     * @param greenBias how much to weight the green component
     * @param blueBias how much to weight the blue component
     * @param inverted whether to invert the weight on the components or not
     * @return the brightness of a pixel in the range 0-255
     */
    public static int getPixelBrightness(BufferedImage image, int x, int y, double redBias, double greenBias, double blueBias, boolean inverted) {

        int pixel = image.getRGB(x, y);
        int red = (pixel >> 16) & 0xFF;   // Extract the red component
        int green = (pixel >> 8) & 0xFF;  // Extract the green component
        int blue = pixel & 0xFF;          // Extract the blue component

        // Calculate the weighted average based on biases
        return (int) (red * redBias + green * greenBias + blue * blueBias) / 3;
    }

    /**
     * Returns a hashmap from a txt file.
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
            System.out.println("...loaded " + requestedValue + " map");
            return outputMap;
        }
    }

    /**
     * Outputs a hanzi character for each 2x2 pixel block in the image.
     * @param resizedImage the image to process
     * @param resizedImage2x the image to process (2x size of resizedImage)
     * @param maxStrokeCount the max stroke count for the characters
     * @param strokeCountMap the stroke count map
     * @param fourCornerCodeMap the four corner code map
     * @throws NoSuchElementException if the stroke count map does not contain the stroke count of a character
     */
    public static void complexOutput(BufferedImage resizedImage, BufferedImage resizedImage2x, int maxStrokeCount, Map<String, String> strokeCountMap, Map<String, String> fourCornerCodeMap) throws NoSuchElementException {

        // iterate through image in 2x2 blocks
        for (int y = 0; y < resizedImage2x.getHeight(); y += 2) {
            System.out.println("...building row " + ((y / 2) + 1) + " of " + resizedImage.getHeight());
            for (int x = 0; x < resizedImage2x.getWidth(); x += 2) {

                ArrayList<Integer> blockBrightness = new ArrayList<>();

                // iterate through 2x2 pixel block
                for (int blockY = y; blockY < y + 2; blockY++) {
                    for (int blockX = x; blockX < x + 2; blockX++) {
                        // add brightness for each pixel in block
                        int pixelBrightnessBlock = getPixelBrightness(resizedImage2x, blockX, blockY, 1.1, 1.1, 1.1, false);
                        blockBrightness.add(pixelBrightnessBlock);
                    }
                }
                int sum = 0;
                for (int brightness : blockBrightness) {
                    sum += brightness;
                }
                int pixelBrightness = sum / blockBrightness.size();
                int pixelStrokeCount = maxStrokeCount - (pixelBrightness * (maxStrokeCount - 1) / 255);

                int brightestPixel = Util.findGreatestValue(blockBrightness, false);
                int brightestPixelIndex = Util.findGreatestValue(blockBrightness, true);
                int brightestPixelComplexity = Util.getComplexity((brightestPixel) * (8) / 255);

                //System.out.println("brightest pixel: " + brightestPixel + " at index " + brightestPixelIndex + " with complexity " + brightestPixelComplexity);

                String hanzi = getRandomHanziFromCornerComplexity(brightestPixel, brightestPixelIndex, brightestPixelComplexity, strokeCountMap, fourCornerCodeMap, pixelStrokeCount, maxStrokeCount);
                //System.out.println("x: "+x+" y: "+y+" hanzi: "+hanzi);
                outputArt.append(hanzi);
            }

            // next line
            outputArt.append('\n');
        }

        // final check for spaces
        String outputArtString = String.valueOf(outputArt);
        outputArtString = outputArtString.replaceAll(" ", Util.unicodeKeyToString("U+2003"));

        // Print the entire outputArt after processing all rows
        System.out.print(outputArtString);
    }

    /**
     * Returns a random hanzi character from the stroke count map.
     * @param brightestPixel the brightest pixel in the 2x2 block
     * @param brightestPixelIndex the index of the brightest pixel in the 2x2 block
     * @param brightestPixelComplexity the complexity of the brightest pixel in the 2x2 block
     * @param strokeCountMap the stroke count map
     * @param fourCornerCodeMap the four corner code map
     * @param strokeCount the stroke count of the character
     * @param maxStrokeCount the max stroke count of the character
     * @return a random hanzi character from the stroke count map.
     * @throws NumberFormatException if the four corner code is not a number
     */
    public static String getRandomHanziFromCornerComplexity(int brightestPixel, int brightestPixelIndex, int brightestPixelComplexity, Map<String, String> strokeCountMap, Map<String, String> fourCornerCodeMap, int strokeCount, int maxStrokeCount) throws NumberFormatException{
        regenerateStrokeKeySet(strokeCountMap, strokeCount);

        while (!strokeKeys.isEmpty()) {
            int randomIndex = random.nextInt(strokeKeys.size());
            String unicodeKey = (String) strokeKeys.toArray()[randomIndex];

            // makes sure unicode key has a matching four corners value
            if (fourCornerCodeMap.get(unicodeKey) == null){
                strokeKeys.remove(unicodeKey);
                continue;
            }

            // todo: clean this up !! idk what im looking at !!
            // if the four corner code is 0000.0, then the character is not in the dictionary
            if (fourCornerCodeMap.get(unicodeKey).equals("0000.0") || strokeCount == 1) {
                if (unicodeKey.length() <= 6 && !unicodeKey.contains("F")) {
                    return Util.unicodeKeyToString(unicodeKey);
                } else {
                    strokeKeys.remove(unicodeKey);
                }
            }

            String cornerCodesString = fourCornerCodeMap.get(unicodeKey);

            // Split the string into individual values using space as a delimiter
            String[] cornerCodesArray = cornerCodesString.split(" ");

            // Parse the first value as an integer
            int cornerCode = (int) Double.parseDouble(cornerCodesArray[0]);

            ArrayList<Integer> cornerDigits = Util.getDigits(cornerCode);

            // check for corner complexity match
            if (brightestPixelIndex == Util.findGreatestValue(cornerDigits, true)) {
                if (unicodeKey.length() <= 6 && !unicodeKey.contains("F")) {
                    return Util.unicodeKeyToString(unicodeKey);
                } else {
                    strokeKeys.remove(unicodeKey);
                }
            }
        }
        return Util.unicodeKeyToString("U+3000"); // if the function reaches this something weird went wrong
    }
}
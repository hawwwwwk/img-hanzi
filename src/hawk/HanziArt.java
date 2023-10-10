package hawk;

import hawk.util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class HanziArt {

    public static final Random random = new Random();
    public static HashSet<String> strokeKeys = new HashSet<>();
    public StringBuilder outputArt = new StringBuilder();
    private BufferedImage image;        // the image to process
    private int outputWidth = 30;       // the width of the output image
    private int maxStrokeCount = 25;    // 1-25, more usually means more detail but longer processing time
    private Map<String, String> strokeCountMap; // hashmap of unicode keys and stroke counts
    private Map<String, String> fourCornerCodeMap; // hashmap of unicode keys and four corner codes
    private String buildType = "fast"; // fast or complex
    private int redBias = 1;
    private int greenBias = 1;
    private int blueBias = 1;

    public HanziArt(BufferedImage image){
        this.image = image;
    }
    public HanziArt(BufferedImage image, int outputWidth){
        this.image = image;
        this.outputWidth = outputWidth;
    }

    public HanziArt(BufferedImage image, int outputWidth, String unihanDictionaryPath, String unihanIRGSourcesPath) throws IOException {
        this.image = image;
        this.outputWidth = outputWidth;
        this.strokeCountMap = Util.hashMapFromTXT("kTotalStrokes", unihanIRGSourcesPath);
        this.fourCornerCodeMap = Util.hashMapFromTXT("kFourCornerCode", unihanDictionaryPath);
    }

    public HanziArt(BufferedImage image, int outputWidth, String unihanDictionaryPath, String unihanIRGSourcesPath, String buildType) throws IOException {
        this.image = image;
        this.outputWidth = outputWidth;
        this.strokeCountMap = Util.hashMapFromTXT("kTotalStrokes", unihanIRGSourcesPath);
        this.fourCornerCodeMap = Util.hashMapFromTXT("kFourCornerCode", unihanDictionaryPath);
        this.buildType = buildType;
    }

    public HanziArt(BufferedImage image, int outputWidth, String unihanDictionaryPath, String unihanIRGSourcesPath, int buildType) throws IOException {
        this.image = image;
        this.outputWidth = outputWidth;
        this.strokeCountMap = Util.hashMapFromTXT("kTotalStrokes", unihanIRGSourcesPath);
        this.fourCornerCodeMap = Util.hashMapFromTXT("kFourCornerCode", unihanDictionaryPath);
        this.setBuildType(buildType);
    }

    public static void main(String[] args){
//        // just for testing, todo: remove later
//        long startTime = System.nanoTime();
//
//        // user inputs
//        System.out.println("...loading inputs");
//        String imgPath = "C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\test.jpg";
//        BufferedImage image = ImageIO.read(new File(imgPath));
//        String unihanDictionaryPath = "C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\Unihan_DictionaryLikeData.txt";
//        String unihanIRGSourcesPath = "C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\Unihan_IRGSources.txt";
//        int outputWidth = 75;
//
//
//        System.out.println("...resizing image 1");
//        BufferedImage resizedImage = resizeImage(image, outputWidth);
//        ImageIO.write(resizedImage, "png", new File("C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\resized1.png"));
//        // for complex image
//        System.out.println("...resizing image 2");
//        BufferedImage resizedImage2x = resizeImage(image, outputWidth * 2);
//        ImageIO.write(resizedImage2x, "png", new File("C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\resized2.png"));
//
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime) / 1000000;
//        System.out.println(duration + " ms");
    }

    /**
     * Builds the output art.
     * @throws IOException if the image is not found
     */
    public void build() throws IOException {
        switch (this.getBuildType()) {
            case "fast":
                this.fastOutput();
                break;
            case "complex":
                this.complexOutput();
                break;
            default:
                throw new RuntimeException("Invalid build type");
        }
    }

    private void fastOutput() throws IOException{
        BufferedImage resizedImage = Util.resizeImage(this.getImage(), outputWidth);
        StringBuilder outputArt = this.getOutputArt();
        int maxStrokeCount = this.getMaxStrokeCount();
        Map<String, String> strokeCountMap = this.getStrokeCountMap();

        for (int y = 0; y < resizedImage.getHeight(); y++) {
            System.out.println((y+1) + "/" + resizedImage.getHeight());

            for (int x = 0; x < resizedImage.getWidth(); x++) {
                int pixelBrightness = Util.getPixelBrightness(resizedImage, x, y);
                int pixelStrokeCount = maxStrokeCount - (pixelBrightness) * (maxStrokeCount - 1) / 255;
                String pixelHanzi = getRandomHanziFromStrokeCount(pixelStrokeCount, strokeCountMap, strokeKeys);
                this.getOutputArt().append(pixelHanzi);
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
     * Called by regenerateStrokeKeySet() if the stroke count
     * of the image does not match the stroke count of the cached keys.
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
     * Outputs a hanzi character for each 2x2 pixel block in the image.
     * @throws NoSuchElementException if the stroke count map does not contain the stroke count of a character
     */
    private void complexOutput() throws NoSuchElementException, IOException {
        StringBuilder outputArt = this.getOutputArt();
        BufferedImage resizedImage = Util.resizeImage(image, this.getOutputWidth());
        BufferedImage resizedImage2x = Util.resizeImage(image, this.getOutputWidth() * 2);
        int maxStrokeCount = this.getMaxStrokeCount();
        Map<String, String> strokeCountMap = this.getStrokeCountMap();
        Map<String, String> fourCornerCodeMap = this.getFourCornerCodeMap();

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

                String hanzi = getRandomHanziFromCornerComplexity(brightestPixel, brightestPixelIndex, brightestPixelComplexity, strokeCountMap, fourCornerCodeMap, pixelStrokeCount, maxStrokeCount);
                outputArt.append(hanzi);
            }

            // next line
            outputArt.append('\n');
        }

        // final check for spaces
        String outputArtString = String.valueOf(outputArt);
        outputArtString = outputArtString.replaceAll(" ", Util.unicodeKeyToString("U+2003"));

        // Print the entire outputArt after processing all rows
        this.setOutputArt(outputArt);
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

    private void setOutputArt(StringBuilder outputArt){
        this.outputArt = outputArt;
    }

    public StringBuilder getOutputArt() {
        return outputArt;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setOutputWidth(int outputWidth) {
        this.outputWidth = outputWidth;
    }
    public int getOutputWidth() {
        return outputWidth;
    }

    public void setMaxStrokeCount(int maxStrokeCount) {
        this.maxStrokeCount = maxStrokeCount;
    }

    public int getMaxStrokeCount() {
        return maxStrokeCount;
    }

    public void setStrokeCountMap(String unihanIRGSourcesPath) throws IOException {
        this.strokeCountMap = Util.hashMapFromTXT("kTotalStrokes", unihanIRGSourcesPath);
    }

    public Map<String, String> getStrokeCountMap() {
        return strokeCountMap;
    }

    public void setFourCornerCodeMap(String unihanDictionaryPath) throws IOException {
        this.fourCornerCodeMap = Util.hashMapFromTXT("kFourCornerCode", unihanDictionaryPath);
    }

    public Map<String, String> getFourCornerCodeMap() {
        return fourCornerCodeMap;
    }

    public void setBuildType(int buildType){
        switch (buildType) {
            case 0:
                this.buildType = "fast";
                break;
            case 1:
                this.buildType = "complex";
                break;
            default:
                throw new RuntimeException("Invalid build type");
        }
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setRedBias(int redBias) {
        this.redBias = redBias;
    }

    public int getRedBias() {
        return redBias;
    }

    public void setGreenBias(int greenBias) {
        this.greenBias = greenBias;
    }

    public int getGreenBias() {
        return greenBias;
    }

    public void setBlueBias(int blueBias) {
        this.blueBias = blueBias;
    }

    public int getBlueBias() {
        return blueBias;
    }

    public void setBias(int redBias, int greenBias, int blueBias) {
        this.redBias = redBias;
        this.greenBias = greenBias;
        this.blueBias = blueBias;
    }
}
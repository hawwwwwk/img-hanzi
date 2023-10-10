package hawk;

import hawk.util.Util;

import java.awt.image.BufferedImage;
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
    private double redBias = 1.0;
    private double greenBias = 1.0;
    private double blueBias = 1.0;
    private boolean outputProgress = false;

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

    /**
     * Builds the output art.
     * @throws IOException if the image is not found
     */
    public void build(boolean outputProgress) throws IOException {
        this.outputProgress = outputProgress;
        this.build();
    }

    private void fastOutput() throws IOException{
        BufferedImage resizedImage = Util.resizeImage(this.getImage(), outputWidth);
        StringBuilder outputArt = this.getOutputArt();
        int maxStrokeCount = this.getMaxStrokeCount();

        for (int y = 0; y < resizedImage.getHeight(); y++) {
            if (outputProgress){
                System.out.println("...building row " + (y+1) + " of " + resizedImage.getHeight());
            }

            for (int x = 0; x < resizedImage.getWidth(); x++) {
                int pixelBrightness = Util.getPixelBrightness(resizedImage, x, y, redBias, greenBias, blueBias, false);
                int pixelStrokeCount = maxStrokeCount - (pixelBrightness) * (maxStrokeCount - 1) / 255;
                String pixelHanzi = getRandomHanziFromStrokeCount(pixelStrokeCount, strokeKeys);
                outputArt.append(pixelHanzi);
            }
            outputArt.append('\n');
        }
        this.setOutputArt(outputArt);
    }

    /**
     * Returns a random hanzi character from the stroke count map.
     * @param strokeCount the stroke count of the character (within the pixel)
     * @param keys the keys to choose from
     * @return a random hanzi character from the stroke count map.
     */
    private String getRandomHanziFromStrokeCount(int strokeCount, HashSet<String> keys){
        Map<String, String> strokeCountMap = this.getStrokeCountMap();

        // caching algorithm, matches stroke count
        regenerateStrokeKeySet(strokeCountMap, strokeCount);

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
    private static void regenerateStrokeKeySet(Map<String, String> strokeCountMap, int strokeCount){
        // caching alg
        if (!strokeKeys.isEmpty()){
            if (!strokeCountMap.get(strokeKeys.iterator().next()).equals(String.valueOf(strokeCount))){
                strokeKeys.clear();
            } else {
                return;
            }
        }
        for (Map.Entry<String, String> entry : strokeCountMap.entrySet()) {
            if (entry.getValue().equals(Integer.toString(strokeCount))) {
                strokeKeys.add(entry.getKey());
            }
        }
    }

    /**
     * Outputs a hanzi character for each 2x2 pixel block in the image.
     * @throws NoSuchElementException if the stroke count map does not contain the stroke count of a character
     */
    private void complexOutput() throws NoSuchElementException, IOException {
        StringBuilder outputArt = this.getOutputArt();
        BufferedImage resizedImage = Util.resizeImage(this.getImage(), outputWidth);
        BufferedImage resizedImage2x = Util.resizeImage(this.getImage(), (outputWidth * 2));
        int maxStrokeCount = this.getMaxStrokeCount();
        Map<String, String> strokeCountMap = this.getStrokeCountMap();
        Map<String, String> fourCornerCodeMap = this.getFourCornerCodeMap();

        // iterate through image in 2x2 blocks
        for (int y = 0; y < resizedImage2x.getHeight() - 1; y += 2) {
            System.out.println("...building row " + ((y / 2) + 1) + " of " + resizedImage.getHeight());
            for (int x = 0; x < resizedImage2x.getWidth(); x += 2) {

                ArrayList<Integer> blockBrightness = new ArrayList<>();

                // iterate through 2x2 pixel block
                for (int blockY = y; blockY < y + 2; blockY++) {
                    for (int blockX = x; blockX < x + 2; blockX++) {
                        // add brightness for each pixel in block
                        int pixelBrightnessBlock = Util.getPixelBrightness(resizedImage2x, blockX, blockY, this.getRedBias(), this.getGreenBias(), this.getBlueBias(), false);
                        blockBrightness.add(pixelBrightnessBlock);
                    }
                }
                int sum = 0;
                for (int brightness : blockBrightness) {
                    sum += brightness;
                }
                int pixelBrightness = sum / blockBrightness.size();
                int pixelStrokeCount = maxStrokeCount - (pixelBrightness * (maxStrokeCount - 1) / 255);

                int brightestPixelIndex = Util.findGreatestValue(blockBrightness, true);

                String hanzi = getRandomHanziFromCornerComplexity(brightestPixelIndex, strokeCountMap, fourCornerCodeMap, pixelStrokeCount);
                outputArt.append(hanzi);
            }

            // next line
            outputArt.append('\n');
        }

        this.setOutputArt(outputArt);
    }

    /**
     * Returns a random hanzi character from the stroke count map.
     * @param brightestPixelIndex the index of the brightest pixel in the 2x2 block
     * @param strokeCountMap the stroke count map
     * @param fourCornerCodeMap the four corner code map
     * @param strokeCount the stroke count of the character
     * @return a random hanzi character from the stroke count map.
     * @throws NumberFormatException if the four corner code is not a number
     */
    private String getRandomHanziFromCornerComplexity(int brightestPixelIndex, Map<String, String> strokeCountMap, Map<String, String> fourCornerCodeMap, int strokeCount) throws NumberFormatException{
        // todo: this whole method makes no sense to me, please make it readable oh my GOSH!!!
        regenerateStrokeKeySet(strokeCountMap, strokeCount);

        while (!strokeKeys.isEmpty()) {
            int randomIndex = random.nextInt(strokeKeys.size());
            String unicodeKey = (String) strokeKeys.toArray()[randomIndex];

            // makes sure unicode key has a matching four corners value
            if (fourCornerCodeMap.get(unicodeKey) == null){
                strokeKeys.remove(unicodeKey);
                continue;
            }

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

    public void setRedBias(double redBias) {
        this.redBias = redBias;
    }

    public double getRedBias() {
        return redBias;
    }

    public void setGreenBias(double greenBias) {
        this.greenBias = greenBias;
    }

    public double getGreenBias() {
        return greenBias;
    }

    public void setBlueBias(double blueBias) {
        this.blueBias = blueBias;
    }

    public double getBlueBias() {
        return blueBias;
    }

    public void setBias(double redBias, double greenBias, double blueBias) {
        this.redBias = redBias;
        this.greenBias = greenBias;
        this.blueBias = blueBias;
    }

    public void setOutputProgress(boolean outputProgress) {
        this.outputProgress = outputProgress;
    }

    public boolean getOutputProgress() {
        return outputProgress;
    }
}
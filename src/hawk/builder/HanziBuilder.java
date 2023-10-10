package hawk.builder;

import hawk.HanziArt;
import hawk.util.Util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Builds the output art. Mostly used by internal methods,
 * but you can create a HanziBuilder and use it yourself if
 * you really want.
 */
public class HanziBuilder {

    /**
     * Build the output art using the fast method.
     * @param hanziArt the hanzi art object
     * @throws IOException if the image is not found
     */
    public void buildFastOutput(HanziArt hanziArt) throws IOException {
        BufferedImage resizedImage = Util.resizeImage(hanziArt.getImage(), hanziArt.getOutputWidth());
        StringBuilder outputArt = hanziArt.getOutputArt();
        int maxStrokeCount = hanziArt.getMaxStrokeCount();

        for (int y = 0; y < resizedImage.getHeight(); y++) {
            if (hanziArt.isOutputProgress()){
                System.out.println("...building row " + (y+1) + " of " + resizedImage.getHeight());
            }

            for (int x = 0; x < resizedImage.getWidth(); x++) {
                int pixelBrightness = Util.getPixelBrightness(
                        resizedImage, x, y, hanziArt.getRedBias(), hanziArt.getGreenBias(), hanziArt.getBlueBias(), hanziArt.isInverted()
                );
                int pixelStrokeCount = maxStrokeCount - (pixelBrightness) * (maxStrokeCount - 1) / 255;
                String pixelHanzi = getRandomHanziFromStrokeCount(pixelStrokeCount, hanziArt);
                outputArt.append(pixelHanzi);
            }
            outputArt.append('\n');
        }
        hanziArt.setOutputArt(outputArt);
    }

    /**
     * Build the output art using the complex method.
     * @param hanziArt the hanzi art object
     * @throws NoSuchElementException if the stroke count map or four corner code map is not found
     * @throws IOException if the image is not found
     */
    public void buildComplexOutput(HanziArt hanziArt) throws NoSuchElementException, IOException {
        StringBuilder outputArt = hanziArt.getOutputArt();
        BufferedImage resizedImage = Util.resizeImage(hanziArt.getImage(), hanziArt.getOutputWidth());
        BufferedImage resizedImage2x = Util.resizeImage(hanziArt.getImage(), (hanziArt.getOutputWidth() * 2));
        int maxStrokeCount = hanziArt.getMaxStrokeCount();
        Map<String, String> strokeCountMap = hanziArt.getStrokeCountMap();
        Map<String, String> fourCornerCodeMap = hanziArt.getFourCornerCodeMap();

        // iterate through image in 2x2 blocks
        for (int y = 0; y < resizedImage2x.getHeight() - 1; y += 2) {
            System.out.println("...building row " + ((y / 2) + 1) + " of " + resizedImage.getHeight());
            for (int x = 0; x < resizedImage2x.getWidth(); x += 2) {

                ArrayList<Integer> blockBrightness = new ArrayList<>();

                // iterate through 2x2 pixel block
                for (int blockY = y; blockY < y + 2; blockY++) {
                    for (int blockX = x; blockX < x + 2; blockX++) {
                        // add brightness for each pixel in block
                        int pixelBrightnessBlock = Util.getPixelBrightness(
                                resizedImage2x, blockX, blockY, hanziArt.getRedBias(), hanziArt.getGreenBias(), hanziArt.getBlueBias(), false
                        );
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

                String hanzi = getRandomHanziFromCornerComplexity(brightestPixelIndex, strokeCountMap, fourCornerCodeMap, pixelStrokeCount, hanziArt);
                outputArt.append(hanzi);
            }

            // next line
            outputArt.append('\n');
        }

        hanziArt.setOutputArt(outputArt);
    }

    /**
     * Returns a random hanzi character from the stroke count map.
     * @param strokeCount the stroke count of the character (within the pixel)
     * @param hanziArt the hanzi art object
     * @return a random hanzi character from the stroke count map.
     */
    private String getRandomHanziFromStrokeCount(int strokeCount, HanziArt hanziArt){
        HashSet<String> keys = hanziArt.getStrokeKeys();
        Map<String, String> strokeCountMap = hanziArt.getStrokeCountMap();

        // caching algorithm, matches stroke count
        hanziArt.regenerateStrokeKeySet(strokeCountMap, strokeCount);

        while (!keys.isEmpty()) {
            int randomIndex = HanziArt.random.nextInt(keys.size());
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
     * Returns a random hanzi character from the stroke count map.
     * @param brightestPixelIndex the index of the brightest pixel in the 2x2 block
     * @param strokeCountMap the stroke count map
     * @param fourCornerCodeMap the four corner code map
     * @param strokeCount the stroke count of the character
     * @return a random hanzi character from the stroke count map.
     * @throws NumberFormatException if the four corner code is not a number
     */
    // todo: reduce number of input variables, just grab from the object
    private String getRandomHanziFromCornerComplexity(int brightestPixelIndex, Map<String, String> strokeCountMap, Map<String, String> fourCornerCodeMap, int strokeCount, HanziArt hanziArt) throws NumberFormatException{
        // todo: this whole method makes no sense to me, please make it readable oh my GOSH!!!
        hanziArt.regenerateStrokeKeySet(strokeCountMap, strokeCount);

        while (!hanziArt.getStrokeKeys().isEmpty()) {
            int randomIndex = HanziArt.random.nextInt(hanziArt.getStrokeKeys().size());
            String unicodeKey = (String) hanziArt.getStrokeKeys().toArray()[randomIndex];

            // makes sure unicode key has a matching four corners value
            if (fourCornerCodeMap.get(unicodeKey) == null){
                hanziArt.getStrokeKeys().remove(unicodeKey);
                continue;
            }

            // if the four corner code is 0000.0, then the character is not in the dictionary
            if (fourCornerCodeMap.get(unicodeKey).equals("0000.0") || strokeCount == 1) {
                if (unicodeKey.length() <= 6 && !unicodeKey.contains("F")) {
                    return Util.unicodeKeyToString(unicodeKey);
                } else {
                    hanziArt.getStrokeKeys().remove(unicodeKey);
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
                    hanziArt.getStrokeKeys().remove(unicodeKey);
                }
            }
        }
        return Util.unicodeKeyToString("U+3000"); // if the function reaches this something weird went wrong
    }
}

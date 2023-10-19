package xyz.ethxn.builder;

import xyz.ethxn.HanziArt;
import xyz.ethxn.util.Util;

import java.awt.image.BufferedImage;
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
     */
    public void buildFastOutput(HanziArt hanziArt) {
        StringBuilder outputArt = hanziArt.getOutputArt();
        BufferedImage resizedImage = Util.resizeImage(hanziArt.getImage(), hanziArt.getOutputWidth());
        int maxStrokeCount = hanziArt.getMaxStrokeCount();

        // todo: instead of generating a random character from the whole HashSet, break them into smaller HashSets

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
     */
    public void buildComplexOutput(HanziArt hanziArt) throws NoSuchElementException{
        // local declarations
        StringBuilder outputArt = hanziArt.getOutputArt();
        BufferedImage resizedImage = Util.resizeImage(hanziArt.getImage(), hanziArt.getOutputWidth());
        BufferedImage resizedImage2x = Util.resizeImage(hanziArt.getImage(), (hanziArt.getOutputWidth() * 2));
        Map<String, String> strokeCountMap = hanziArt.getStrokeCountMap();
        Map<String, String> fourCornerCodeMap = hanziArt.getFourCornerCodeMap();
        int maxStrokeCount = hanziArt.getMaxStrokeCount();

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

                // get average brightness of block
                for (int brightness : blockBrightness) {
                    sum += brightness;
                }
                int pixelBrightness = sum / blockBrightness.size();

                int pixelStrokeCount = maxStrokeCount - (pixelBrightness * (maxStrokeCount - 1) / 255);
                int brightestPixelIndex = Util.findGreatestValue(blockBrightness, true);

                String hanzi = getRandomHanziFromCornerComplexity(hanziArt, strokeCountMap, fourCornerCodeMap, brightestPixelIndex, pixelStrokeCount);
                outputArt.append(hanzi);
            }

            // next line
            outputArt.append('\n');
        }

        hanziArt.setOutputArt(outputArt);
    }

    // todo: make specialized option for monochrome images, lets make bad apple lmfao
    // todo: add option to find edges of image and increase the density along them, maybe using canny detection

    /**
     * Returns a random hanzi character from the stroke count map.
     * @param strokeCount the stroke count of the character (within the pixel)
     * @param hanziArt the hanzi art object
     * @return a random hanzi character from the stroke count map.
     */
    private String getRandomHanziFromStrokeCount(int strokeCount, HanziArt hanziArt){
        HashSet<String> keys = hanziArt.getStrokeKeys();
        Map<String, String> strokeCountMap = hanziArt.getStrokeCountMap();

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
        return Util.unicodeKeyToString("U+3000"); // no match found, default to white space
    }

    /**
     * Returns a random hanzi character from the stroke count map.
     * @param brightestPixelIndex the index of the brightest pixel in the 2x2 block
     * @param strokeCount the stroke count of the character
     * @return a random hanzi character from the stroke count map.
     * @throws NumberFormatException if the four corner code is not a number
     */
    private String getRandomHanziFromCornerComplexity(HanziArt hanziArt, Map<String, String> strokeCountMap, Map<String, String> fourCornerCodeMap, int brightestPixelIndex, int strokeCount) throws NumberFormatException{
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
        return Util.unicodeKeyToString("U+3000"); // no match found, default to white space
    }
}

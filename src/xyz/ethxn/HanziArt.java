package xyz.ethxn;

import xyz.ethxn.builder.HanziBuilder;
import xyz.ethxn.util.Util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public class HanziArt {

    public static final Random random = new Random();
    private List<Set<String>> strokeKeySets = new ArrayList<>();
    private Set<String> strokeKeys = new HashSet<>();
    private StringBuilder outputArt = new StringBuilder();
    private String buildType = "fast";  // fast or complex
    private BufferedImage image;        // the image to process
    private int outputWidth = 30;       // the width of the output image
    private int maxStrokeCount = 25;    // 1-25, higher usually means more detail but longer processing time
    private Map<String, String> strokeCountMap;     // hashmap of unicode keys and stroke counts
    private Map<String, String> fourCornerCodeMap;  // hashmap of unicode keys and four corner codes
    private boolean outputProgress = false;


    /**
     * Constructs a new HanziArt object.
     */
    public HanziArt(){}

    public HanziArt(BufferedImage image, int outputWidth, String unihanDictionaryPath, String unihanIRGSourcesPath) throws IOException {
        this.image = image;
        this.outputWidth = outputWidth;
        this.strokeCountMap = Util.hashMapFromTXT("kTotalStrokes", unihanIRGSourcesPath);
        this.fourCornerCodeMap = Util.hashMapFromTXT("kFourCornerCode", unihanDictionaryPath);
    }

    /**
     * Builds the output art.
     */
    public void build() {
        // sets up key sets
        for (int i = 1; i <= this.getMaxStrokeCount(); i++) {
            Set<String> keysForStrokeCount = new HashSet<>();
            for (Map.Entry<String, String> entry : strokeCountMap.entrySet()) {
                if (entry.getValue().equals(Integer.toString(i))) {
                    keysForStrokeCount.add(entry.getKey());
                }
            }
            this.strokeKeySets.add(keysForStrokeCount);
        }

        HanziBuilder builder = new HanziBuilder();
        switch (this.getBuildType()) {
            case "fast":
                builder.buildFastOutput(this);
                break;
            case "complex":
                builder.buildComplexOutput(this);
                break;
            default:
                throw new IndexOutOfBoundsException("Invalid build type");
        }
    }

    /**
     * Builds the output art.
     * @param outputProgress whether to output build progress to the console
     */
    public void build(boolean outputProgress) {
        this.outputProgress = outputProgress;
        this.build();
    }

    /** todo: update javadoc
     * Regenerates the stroke key set if the stroke count of the image does not match the stroke count of the cached keys.
     * @param strokeCount max stroke count of the image
     */
    public void regenerateStrokeKeySet(int strokeCount){
        if (strokeCount <= 1){
            this.strokeKeys.clear();
            this.strokeKeys.add("U+3000");
            return;
        } else if (strokeCount >= this.getMaxStrokeCount()){
            strokeCount = this.maxStrokeCount;
        }

        strokeKeys = strokeKeySets.get(strokeCount - 1);
    }

    public void setOutputArt(StringBuilder outputArt){
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

    public void setStrokeKeys(Set<String> strokeKeys) {
        this.strokeKeys = strokeKeys;
    }

    public Set<String> getStrokeKeys() {
        return strokeKeys;
    }

    /**
     * Set's the build type for the HanziArt object.
     * @param buildType The build type, {@code 0 = fast, 1 = complex}
     * @throws RuntimeException if the build type is invalid
     */
    public void setBuildType(int buildType){
        switch (buildType) {
            case 0:
                this.buildType = "fast";
                break;
            case 1:
                this.buildType = "complex";
                break;
            default:
                throw new IndexOutOfBoundsException("'"+buildType+"'"+" is an invalid build type.");
        }
    }

    /**
     * Set's the build type for the HanziArt object.
     * @param buildType The build type, {@code "fast" or "complex"}
     * @throws RuntimeException if the build type is invalid
     */
    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public String getBuildType() {
        return buildType;
    }

    public boolean isOutputProgress() {
        return outputProgress;
    }

    public void setOutputProgress(boolean outputProgress) {
        this.outputProgress = outputProgress;
    }

    public List<Set<String>> getStrokeKeySets() {
        return strokeKeySets;
    }

    public void setStrokeKeySets(List<Set<String>> strokeKeySets) {
        this.strokeKeySets = strokeKeySets;
    }
}
package hawk.examples.banner;

import hawk.HanziArt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HanziBannerTester {
    public static void main(String[] args) throws IOException {

        String imgPath = ".\\src\\hawk\\examples\\banner\\banner.png";
        BufferedImage image = ImageIO.read(new File(imgPath));
        String unihanDictionaryPath = ".\\src\\Unihan_DictionaryLikeData.txt";
        String unihanIRGSourcesPath = ".\\src\\Unihan_IRGSources.txt";
        int outputWidth = 100;


        HanziArt hanziArt = new HanziArt(image, outputWidth, unihanDictionaryPath, unihanIRGSourcesPath);
        hanziArt.setBuildType(0); // 0 = fast, 1 = complex
        hanziArt.setBias(2, 2, 2);
        hanziArt.setMaxStrokeCount(25);
        long startTime = System.currentTimeMillis();
        hanziArt.build(true);
        System.out.println(hanziArt.getOutputArt());

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Program execution time: " + elapsedTime + " milliseconds");
    }
}

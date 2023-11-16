package xyz.ethxn.examples.banner;

import xyz.ethxn.HanziArt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HanziBannerTester {
    public static void main(String[] args) throws IOException {

        String imgPath = ".\\src\\xyz\\ethxn\\examples\\banner\\banner.png";
        BufferedImage image = ImageIO.read(new File(imgPath));
        String unihanDictionaryPath = ".\\src\\Unihan_DictionaryLikeData.txt";
        String unihanIRGSourcesPath = ".\\src\\Unihan_IRGSources.txt";
        int outputWidth = 100;

        HanziArt hanziArt = new HanziArt(image, outputWidth, unihanDictionaryPath, unihanIRGSourcesPath);
        hanziArt.setBuildType(1); // 0 = fast, 1 = complex
        hanziArt.setMaxStrokeCount(25);

        hanziArt.build(true);
        System.out.println(hanziArt.getOutputArt());
    }
}

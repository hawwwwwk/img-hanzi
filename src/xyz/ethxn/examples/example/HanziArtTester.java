package xyz.ethxn.examples.example;

import xyz.ethxn.HanziArt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HanziArtTester {
    public static void main(String[] args) throws IOException {
        String imgPath = ".\\src\\xyz.hawk\\examples\\example\\test.jpg";
        BufferedImage image = ImageIO.read(new File(imgPath));
        String unihanDictionaryPath = ".\\src\\Unihan_DictionaryLikeData.txt";
        String unihanIRGSourcesPath = ".\\src\\Unihan_IRGSources.txt";
        int outputWidth = 150;

        HanziArt ha = new HanziArt(image, outputWidth, unihanDictionaryPath, unihanIRGSourcesPath);
        ha.setBuildType(1); // 0 = fast, 1 = complex
        ha.setBias(1.0, 1.0, 1.4);
        ha.setMaxStrokeCount(20);
        ha.build(true);
        System.out.println(ha.getOutputArt());

        HanziArt ha2 = new HanziArt();
        ha2.setImage(image);
        ha2.setOutputWidth(outputWidth);
        ha2.setFourCornerCodeMap(unihanDictionaryPath);
        ha2.setStrokeCountMap(unihanIRGSourcesPath);

        ha2.setMaxStrokeCount(20); // lower *usually* equals more clarity
        ha2.setBuildType(0); // 0 = fast, 1 = complex
        ha2.setBias(1.02, 1.2, 1.05); // values farther from 1 can increase processing time
        ha2.build(true);
        System.out.println(ha2.getOutputArt());
    }
}

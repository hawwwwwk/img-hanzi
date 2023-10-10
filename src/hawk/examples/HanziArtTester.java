package hawk.examples;

import hawk.HanziArt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HanziArtTester {
    public static void main(String[] args) throws IOException {
        System.out.println("...loading inputs");
        String imgPath = "C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\test.jpg";
        BufferedImage image = ImageIO.read(new File(imgPath));
        String unihanDictionaryPath = "C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\Unihan_DictionaryLikeData.txt";
        String unihanIRGSourcesPath = "C:\\Users\\ethan\\Project Storage\\ImgToHanzi\\src\\Unihan_IRGSources.txt";
        int outputWidth = 75;

        HanziArt ha = new HanziArt(image, outputWidth, unihanDictionaryPath, unihanIRGSourcesPath);
        ha.setBuildType(0);
        ha.build();
        System.out.println(ha.getOutputArt());
    }
}

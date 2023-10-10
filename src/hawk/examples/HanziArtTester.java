package hawk.examples;

import hawk.HanziArt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HanziArtTester {
    public static void main(String[] args) throws IOException {
        String imgPath = ".\\src\\hawk\\examples\\test.jpg";
        BufferedImage image = ImageIO.read(new File(imgPath));
        String unihanDictionaryPath = ".\\src\\Unihan_DictionaryLikeData.txt";
        String unihanIRGSourcesPath = ".\\src\\Unihan_IRGSources.txt";
        int outputWidth = 15;

        HanziArt ha = new HanziArt(image, outputWidth, unihanDictionaryPath, unihanIRGSourcesPath);
        ha.setBuildType(0);
        ha.build(true);
        System.out.println(ha.getOutputArt());
    }
}

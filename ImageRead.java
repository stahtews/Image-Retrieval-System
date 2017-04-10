/*Content based image retrieval
CBIR.java
Author: Swetha Tayalur
Purpose: Display 100 images already provided to the system in different pages in the original order
         Provide Jbuttons to navigate to right and left pages, Intensity , Colorcode and reset. 
         Select an image and show in a panel
	 Arrange images according to the intensity and colorcode by following repective algorithms with selected image as input 
	 Reset to the original view

 */
package imageread;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.abs;
import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.*;
import java.util.*;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import static java.lang.Math.abs;
import java.net.URL;

public class ImageRead extends JPanel {

    static int[][] intensityMatrix = new int[101][26];
    static int[][] colorCodeMatrix = new int[101][65];

    
    // this methos takes intensity and colorcode arrays and selcted pic number as input and calculates the first normalized matrix
    public static double[][] relevanceFeedback(int intensity[][], int colorCode[][], int picNum) {

        double intensityFeature[][] = new double[101][26];
        double colorCodeFeature[][] = new double[101][65];
        double featureArray[][] = new double[101][90];
        double nornmalizedFeature[][] = new double[101][90];
        double average[][] = new double[4][90]; // iteration to store average
        double stdDev[][] = new double[4][91]; // iteration to store stddeviation 
        double stdDev2[][] = new double[4][91];
        StandardDeviation sd = new StandardDeviation(false);

        for (int img = 1; img < 101; img++) {
            for (int bin = 1; bin < 26; bin++) {
                if (intensity[img][bin] > 0) {
                    intensityFeature[img][bin] = (double) intensity[img][bin] / (double) intensity[img][0]; // array to store intensity  features which are bins divided by respective size or pixels
                } else {
                    intensityFeature[img][bin] = 0;
                }
                featureArray[img][bin] = intensityFeature[img][bin];
            }
            System.out.println();
        }

        for (int img = 1; img < 101; img++) {
            for (int bin = 1; bin < 65; bin++) {
                colorCodeFeature[img][bin] = (double) colorCode[img][bin] / (double) colorCode[img][0]; // array to store  colorcode features which are bins divided by respective image size or pixels   
                featureArray[img][bin + 25] = colorCodeFeature[img][bin];
            }

        }

        for (int bin = 1; bin < 90; bin++) {
            double sum = 0;
            for (int img = 1; img < 101; img++) {
                sum = sum + featureArray[img][bin];
            }
            average[0][bin] = sum / 100;
            System.out.print("average is " + average[0][bin]);
        }

        System.out.println();
        for (int bin = 1; bin < 90; bin++) {
            double temp[] = new double[101];
            for (int img = 1; img < 101; img++) {
                temp[img] = featureArray[img][bin];
            }
            stdDev2[0][bin] = sd.evaluate(temp);

            System.out.print(" this is stddev " + stdDev2[0][bin]);
        }

        double min = Double.MAX_VALUE;
        for (int bin = 1; bin < 90; bin++) {
            min = (stdDev2[0][bin] == 0) ? min : Math.min(min, stdDev2[0][bin]);
        }

        System.out.println();
        for (int img = 1; img < 101; img++) {
            for (int bin = 1; bin < 90; bin++) {
                if (stdDev2[0][bin] != 0.0) {
                    nornmalizedFeature[img][bin] = (double) (featureArray[img][bin] - average[0][bin]) / (double) stdDev2[0][bin];
                } else {
                    nornmalizedFeature[img][bin] = 0;//(double)(featureArray[img][num]-average[0][num])/(double)stdDev2[0][num];
                }

            }

        }

        return nornmalizedFeature;

    }

    
    // calculates the deviation of values of selected picture array 
    public double[][] deviation(double featureArray[][], double average[][], int iteration) {

        double deviations[][] = new double[4][90];
        double tempDeviations[] = new double[101];
        double min;
        min = 100000.000;
        if (iteration == 0) {

            // Get deviation of mean from each number 
            for (int bin = 1; bin < 90; bin++) {
                double sum = 0;

                for (int img = 1; img < featureArray.length; img++) {
                    tempDeviations[img] = featureArray[img][bin] - average[0][bin];
                    tempDeviations[img] = tempDeviations[img] * tempDeviations[img];
                    sum = sum + tempDeviations[img];
                }
                deviations[iteration][bin] = Math.sqrt(sum / 100);
            }

            for (int bin = 1; bin < 90; bin++) {
                if (deviations[iteration][bin] == 0.0) {
                    deviations[iteration][bin] = 0.5 * min;
                }
            }
        }

        return deviations;
    }

    /*This method reads the image file and calculates the number of RGB componants and number of pixels in each histogram num.
     * The contents or pixels processed and stored in a two dimensional array called imageArray.
     */
    public static int[][] readIntensity() {

        BufferedImage bi = null;
        int[][] imageArray = new int[101][26];
        Double d = 0.0;
        int intensityBin = 0;

        for (int i = 1; i < 101; i++) {
            try {
                URL urlToImage;
                urlToImage = ImageRead.class.getResource("images/" + i + ".jpg");
                bi = ImageIO.read(urlToImage);
            } catch (Exception e) {
            }

            for (int x = 0; x < bi.getWidth(); x++) {
                for (int y = 0; y < bi.getHeight(); y++) {
                    imageArray[i][0]++; // size of the array or sum of number of pixels in each image 
                    Color mycolor = new Color(bi.getRGB(x, y));

                    int pixelr = mycolor.getRed();
                    int pixelg = mycolor.getGreen();
                    int pixelb = mycolor.getBlue();
                    d = (0.299 * pixelr) + (0.587 * pixelg) + (0.114 * pixelb);

                    intensityBin = (int) (d / 10);
                    if (d < 240) {
                        imageArray[i][intensityBin + 1]++;
                    } else {
                        imageArray[i][25]++;
                    }

                }
            }

        }
        return imageArray;
    }

    /*This method reads the image file and calculates the number of RGB componants and number of pixels in each histogram num.
     * The contents or pixels processed and stored in a two dimensional array called imageArray.
     */
    public static int[][] readColorCode() {

        BufferedImage bi = null;
        int[][] imageArray = new int[101][65];// changed from 66 to 65
        int[] pixel = new int[4];

        int k = 0;
        Color c = new Color(k);

        Double d = 0.0;
        int intensityBin = 0;

        for (int i = 1; i < 101; i++) {

            try {

                URL urlToImage;
                urlToImage = ImageRead.class.getResource("images/" + i + ".jpg");
                bi = ImageIO.read(urlToImage);

            } catch (Exception e) {
            }
            for (int x = 0; x < bi.getWidth(); x++) {
                for (int y = 0; y < bi.getHeight(); y++) {
                    imageArray[i][0]++;
                    Color mycolor = new Color(bi.getRGB(x, y));

                    int pixelr = mycolor.getRed();
                    int pixelg = mycolor.getGreen();
                    int pixelb = mycolor.getBlue();
                    int pixelR = pixelr & 0xC0;
                    int pixelG = pixelg & 0xC0;
                    int pixelB = pixelb & 0xC0;

                    pixelR = pixelR >> 2;
                    pixelG = pixelG >> 4;
                    pixelB = pixelB >> 6;

                    int a = pixelR + pixelG + pixelB;
                    imageArray[i][a + 1]++;

                }
            }

        }

        return imageArray;
    }

    public static float[] manhattanDistance(int imageArray[][], int imageNum) {

        float sum[] = new float[101];

        for (int i = 1; i < 101; i++) {
            for (int j = 1; j < 26; j++) {
                sum[i] += abs(((float) imageArray[imageNum][j] / (float) imageArray[imageNum][0]) - (float) (imageArray[i][j] / (float) imageArray[i][0]));
            }
        }

        return sum;
    }

    
    
    // calculates the distance between the selected pic and all other pictures 
    public static double[] manhattanDistanceIntensityandColorCode(double imageArray[][], int imageNum, double weight[]) {

        double sum[] = new double[101];

        for (int img = 1; img < 101; img++) {
            for (int bin = 1; bin < 90; bin++) {

                sum[img] = sum[img] + abs((((double) imageArray[imageNum][bin]) - (double) (imageArray[img][bin])) * (double) weight[bin]);
            }
        }

        return sum;
    }

    public static double[] manhattanDistanceColorCode(int imageArray[][], int imageNum) {

        double sum[] = new double[101];

        for (int i = 1; i < 101; i++) {
            for (int j = 1; j < 65; j++) {  //changes from 66 to 65

                sum[i] = sum[i] + abs(((double) imageArray[imageNum][j] / (double) imageArray[imageNum][0]) - (double) (imageArray[i][j] / (double) imageArray[i][0]));
                //System.out.println(value of imageArray[i][j] is  "+ i + "  " + j + "  " + (double)imageArray[i][j]/(double)imageArray[i][0]); 
            }
        }

        return sum;
    }
// calculates the weight of the iteration depending on the selected picture numbers
    public static double[] weightCalculator(double imageArray[][], int imageNums[]) {
        double tempFeatureArray[][] = new double[imageNums.length][90];
        double tempBinVal[] = new double[101];
        StandardDeviation sd = new StandardDeviation(false);
        double stdDev1[] = new double[90];
        double weight[] = new double[90];
        double sum = 0;
        double normWeight[] = new double[90];
        double average1[] = new double[90];

        System.out.println("printing selected image values");
        for (int i = 1; i < 101; i++) {
            if (imageNums[i] != 0) {
                tempFeatureArray[i] = imageArray[i];
                for (int bin = 1; bin < 90; bin++) {
                    System.out.print(tempFeatureArray[i][bin] + " ");
                }
                System.out.println();
            }

        }

        for (int bin = 1; bin < 90; bin++) {
            int i = 0;
            for (int img = 1; img < 101; img++) {
                if (imageNums[img] == 1) {
                    tempBinVal[i++] = tempFeatureArray[img][bin];
                }
            }

           // System.out.print(Arrays.toString(tempBinVal) + "  this is feature value " + bin + "  ");
            stdDev1[bin] = sd.evaluate(tempBinVal, 0, i);
            //System.out.println(stdDev1[bin] + " this is std dev ");

        }

        double min = Double.MAX_VALUE;

        for (int num = 1; num < 90; num++) {
            if (stdDev1[num] != 0.0 && stdDev1[num] < min) {
                min = stdDev1[num];
               // System.out.println("this is  to find min " + stdDev1[num]);
            }
        }

        //  System.out.print("this is min" + min);
        for (int bin = 1; bin < 90; bin++) {
            double sumTemp = 0;
            for (int img = 1; img < 101; img++) {
                sumTemp = sum + imageArray[img][bin];
            }
            average1[bin] = sumTemp / 100;
        }

        for (int num = 1; num < 90; num++) {
            if ((Double.compare(stdDev1[num], 0.0) == 0) && (Double.compare(average1[num], 0.0) != 0)) {
                stdDev1[num] = 0.5 * min;
            } else if ((Double.compare(stdDev1[num], 0.0) == 0) && (Double.compare(average1[num], 0.0) == 0)) {
                stdDev1[num] = 0;
            }
        }

        for (int num = 1; num < 90; num++) {
            if (stdDev1[num] != 0) {
                weight[num] = 1 / stdDev1[num];
            } else {
                weight[num] = 0;
            }
            sum = sum + weight[num];

        }

        for (int i = 1; i < 90; i++) {
            normWeight[i] = weight[i] / sum; // calculates normalized weights of each feature
        }

        return normWeight;

    }

}

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

import static imageread.ImageRead.intensityMatrix;
import static imageread.ImageRead.manhattanDistance;
import static imageread.ImageRead.manhattanDistanceColorCode;
import static imageread.ImageRead.manhattanDistanceIntensityandColorCode;
import static imageread.ImageRead.weightCalculator;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractAction;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.*;

public class CBIR extends JFrame {

    private JLabel photographLabel = new JLabel();  //container to hold a large 
    private JButton[] button; //creates an array of JButtons
    private JCheckBox[] checkBox;
    private JCheckBox relevanceCheckBox = new JCheckBox("relevance");
    static int[] buttonOrder = new int[101]; //creates an array to keep up with the image order
    static int[] buttonOrderIntensity = new int[101]; //creates an array to keep up with the image order when intensity is selected
    static int[] buttonOrderColorCode = new int[101]; //creates an array to keep up with the image order when colorcode is selected
    static int[] buttonOrderIntensityAndColorCode = new int[101];
    static int[] checkedBoxes = new int[101];
    private double[] imageSize = new double[101]; //keeps up with the image sizes
    private GridLayout gridLayout1;
    private GridLayout gridLayout2;
    private GridLayout gridLayout3;
    private GridLayout gridLayout4;
    private JPanel panelBottom1;
    private JPanel panelBottom2;
    private JPanel panelTop;
    private JPanel buttonPanel;
    private Map<Double, LinkedList<Integer>> map;
    static HashMap<Float, Integer> hashMIntensity;
    static HashMap<Double, Integer> hashMColorCode;
    static HashMap<Double, Integer> hashMIntensityAndColorCode;
    static int iteration;

    static int picNo = 0;
    int imageCount = 1; //keeps up with the number of images displayed since the first page.
    int pageNo = 1;
    static int[][] intensityMatrix = new int[101][26];
    static int[][] colorCodeMatrix = new int[101][65];
    static double[][] normalizedMatrix = new double[101][65];
    static float sum[] = new float[101];
    static double sumC[] = new double[101];
    static String flagIntensityOrColorCode = "null"; //keeps up with the option selected either colorcode or intensity or none
    static String flagIntensityAndColorCode = "null";

    public static void main(String args[]) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                CBIR app = new CBIR();
                app.setVisible(true);
            }
        });

        intensityMatrix = ImageRead.readIntensity();
        colorCodeMatrix = ImageRead.readColorCode();
        hashMIntensity = new HashMap<>();
        hashMColorCode = new HashMap<>();

    }

    public CBIR() {
        //The following lines set up the interface including the layout of the buttons and JPanels.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Icon Demo: Please Select an Image");
        panelBottom1 = new JPanel();
        panelBottom2 = new JPanel();
        panelTop = new JPanel();
        buttonPanel = new JPanel();
        gridLayout1 = new GridLayout(4, 5, 5, 5);  // for images
        gridLayout2 = new GridLayout(2, 1, 5, 5);
        gridLayout3 = new GridLayout(1, 2, 5, 5);
        gridLayout4 = new GridLayout(7, 1, 5, 5); // buttons 
        setLayout(gridLayout2);
        panelBottom1.setLayout(gridLayout1);
        panelBottom2.setLayout(gridLayout1);
        panelTop.setLayout(gridLayout3);

        add(panelBottom1);
        add(panelTop);
        photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setLayout(gridLayout4);
        panelTop.add(photographLabel);

        panelTop.add(buttonPanel);

        JButton nextPage = new JButton("Next Page");
        JButton previousPage = new JButton("Previous Page");
        JButton intensity = new JButton("Intensity");
        JButton colorCode = new JButton("Color Code");
        JButton reset = new JButton("reset");
        JButton relevance = new JButton("Intensity and ColorCode with Feedback");

        buttonPanel.add(nextPage);
        buttonPanel.add(previousPage);
        buttonPanel.add(intensity);
        buttonPanel.add(colorCode);
        buttonPanel.add(reset);
        buttonPanel.add(relevance);
        buttonPanel.add(relevanceCheckBox);

        nextPage.addActionListener(new nextPageHandler());
        previousPage.addActionListener(new previousPageHandler());
        intensity.addActionListener(new intensityHandler());
        colorCode.addActionListener(new colorCodeHandler());
        reset.addActionListener(new resetHandler());
        relevance.addActionListener(new relevanceHandler());
        setSize(1100, 750);
        // this centers the frame on the screen
        setLocationRelativeTo(null);

        button = new JButton[101];
        checkBox = new JCheckBox[101];
        /*This for loop goes through the images in the database and stores them as icons and adds
         * the images to JButtons and then to the JButton array
         */
        for (int i = 1; i < 101; i++) {
            ImageIcon icon, original = null;
            icon = new ImageIcon(new ImageIcon(getClass().getResource("images/" + i + ".jpg")).getImage().getScaledInstance(-1, 80, Image.SCALE_SMOOTH));
            original = new ImageIcon(getClass().getResource("images/" + i + ".jpg"));
            // Image m= iconToImage(icon);
            //  ImageIcon icon1 = new ImageIcon(scaleImage(m)); 
            // icon=icon1;
            if (icon != null) {
                button[i] = new JButton(icon);
                checkBox[i] = new JCheckBox(Integer.toString(i));
                //panelBottom1.add(button[i]);
                button[i].addActionListener(new IconButtonHandler(i, original));
                //  checkBox[i].addActionListener(new CheckBoxHandler(i));
                buttonOrder[i] = i;
            }
        }

        displayFirstPage();
    }

    public static Image iconToImage(Icon icon) {
        return ((ImageIcon) icon).getImage();
    }

    /*This method displays the first twenty images in the panelBottom.  The for loop starts at number one and gets the image
     * number stored in the buttonOrder array and assigns the value to imageButNo.  The button associated with the image is 
     * then added to panelBottom1.  The for loop continues this process until twenty images are displayed in the panelBottom1
     */
    private void displayFirstPage() {
        int imageButNo = 0;
        panelBottom1.removeAll();
        for (int i = 1; i < 21; i++) {

            imageButNo = buttonOrder[i];
            JPanel imagePanel = new JPanel();

            imagePanel.add(button[imageButNo], BorderLayout.CENTER);

            panelBottom1.add(imagePanel);

            imageCount++;
        }
        panelBottom1.revalidate();
        panelBottom1.repaint();

    }

    /*This class implements an ActionListener for each iconButton.  When an icon button is clicked, the image on the 
     * the button is added to the photographLabel and the picNo is set to the image number selected and being displayed.
     */
    private class IconButtonHandler implements ActionListener {

        int pNo = 0;
        ImageIcon iconUsed;

        IconButtonHandler(int i, ImageIcon j) {
            pNo = i;
            iconUsed = j;  //sets the icon to the one used in the button
        }

        public void actionPerformed(ActionEvent e) {
            photographLabel.setIcon(iconUsed);

            picNo = pNo;
            checkBox[picNo].setSelected(true);
        }

    }

    /*This class implements an ActionListener for the nextPageButton.  The last image number to be displayed is set to the 
     * current image count plus 20.  If the endImage number equals 101, then the next page button does not display any new 
     * images because there are only 100 images to be displayed.  The first picture on the next page is the image located in 
     * the buttonOrder array at the imageCount
     */
    private class nextPageHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int imageButNo = 0;
            int endImage = imageCount + 20;
            if (endImage <= 101) {
                panelBottom1.removeAll();
                for (int i = imageCount; i < endImage; i++) {

                    if (flagIntensityOrColorCode.equalsIgnoreCase("intensity")) {
                        imageButNo = buttonOrderIntensity[i];
                    } else if (flagIntensityOrColorCode.equalsIgnoreCase("colorcode")) {
                        imageButNo = buttonOrderColorCode[i];
                    } else if (flagIntensityAndColorCode.equalsIgnoreCase("yes")) {

                        imageButNo = buttonOrderIntensityAndColorCode[i];
                    } else {
                        imageButNo = buttonOrder[i];
                    }
                    JPanel imagePanel = new JPanel();

                    imagePanel.setLayout(new BorderLayout());
                    imagePanel.add(button[imageButNo], BorderLayout.CENTER);

                    panelBottom1.add(imagePanel);
                    imageCount++;

                }

                panelBottom1.revalidate();
                panelBottom1.repaint();
            }
        }

    }

    /*This class implements an ActionListener for the previousPageButton.  The last image number to be displayed is set to the 
     * current image count minus 40.  If the endImage number is less than 1, then the previous page button does not display any new 
     * images because the starting image is 1.  The first picture on the next page is the image located in 
     * the buttonOrder array at the imageCount
     */
    private class previousPageHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int imageButNo = 0;
            int startImage = imageCount - 40;
            int endImage = imageCount - 20;
            if (startImage >= 1) {
                panelBottom1.removeAll();
                /*The for loop goes through the buttonOrder array starting with the startImage value
             * and retrieves the image at that place and then adds the button to the panelBottom1.
                 */
                for (int i = startImage; i < endImage; i++) {

                    if (flagIntensityOrColorCode.equalsIgnoreCase("intensity")) {
                        imageButNo = buttonOrderIntensity[i];
                    } else if (flagIntensityOrColorCode.equalsIgnoreCase("colorcode")) {
                        imageButNo = buttonOrderColorCode[i];
                    } else if (flagIntensityAndColorCode.equalsIgnoreCase("yes")) {

                        imageButNo = buttonOrderIntensityAndColorCode[i];
                    } else {
                        imageButNo = buttonOrder[i];
                    }
                    JPanel imagePanel = new JPanel();
                    imagePanel.add(button[imageButNo], BorderLayout.CENTER);
                    panelBottom1.add(imagePanel);
                    imageCount--;

                }

                panelBottom1.revalidate();
                panelBottom1.repaint();
            }
        }

    }

    /*This class implements an ActionListener when the user selects the intensityHandler button.  The image number that the
     * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
     * the image selected and subtracts one to account for the fact that the intensityMatrix starts with zero and not one.
     * The size of the image is retrieved from the imageSize array.  The selected image's intensity num values are 
     * compared to all the other image's intensity num values and a score is determined for how well the images compare.
     * The images are then arranged from most similar to the least.
     */
    private class intensityHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            flagIntensityAndColorCode = "null";
            flagIntensityOrColorCode = "intensity";
            sum = manhattanDistance(intensityMatrix, picNo);

            for (int i = 1; i < 101; i++) {
                hashMIntensity.put(sum[i], i);
            }
            Arrays.sort(sum);

            for (int k = 1; k < 101; k++) {
                buttonOrderIntensity[k] = hashMIntensity.get(sum[k]);
            }

            int imageButNo = 0;
            imageCount = 1;
            int endImage = imageCount + 20;
            panelBottom1.removeAll();
            for (int i = imageCount; i < endImage; i++) {
                imageButNo = buttonOrderIntensity[i];//hashMIntensity.get(sum[i]);

                JPanel imagePanel = new JPanel();
                imagePanel.add(button[imageButNo], BorderLayout.CENTER);
                panelBottom1.add(imagePanel);
                imageCount++;

            }
            panelBottom1.revalidate();
            panelBottom1.repaint();
        }

    }

    /*This class implements an ActionListener when the user selects the colorCode button.  The image number that the
     * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
     * the image selected and subtracts one to account for the fact that the intensityMatrix starts with zero and not one. 
     * The size of the image is retrieved from the imageSize array.  The selected image's intensity num values are 
     * compared to all the other image's intensity num values and a score is determined for how well the images compare.
     * The images are then arranged from most similar to the least.
     */
    private class colorCodeHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            flagIntensityAndColorCode = "null";
            flagIntensityOrColorCode = "colorcode";
            sumC = manhattanDistanceColorCode(colorCodeMatrix, picNo);
            for (int i = 1; i < 101; i++) {
                hashMColorCode.put(sumC[i], i);
            }

            Arrays.sort(sumC);

            for (int k = 1; k < 101; k++) {
                buttonOrderColorCode[k] = hashMColorCode.get(sumC[k]);
            }

            int imageButNo = 0;
            imageCount = 1;
            int endImage = imageCount + 20;
            panelBottom1.removeAll();
            for (int i = imageCount; i < endImage; i++) {
                imageButNo = buttonOrderColorCode[i]; //hashMColorCode.get(sumC[i]);;
                JPanel imagePanel = new JPanel();
                imagePanel.add(button[imageButNo], BorderLayout.CENTER);
                panelBottom1.add(imagePanel);
                imageCount++;

            }
            panelBottom1.revalidate();
            panelBottom1.repaint();

        }
    }

    private class resetHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            flagIntensityAndColorCode = "null";
            flagIntensityOrColorCode = "null";

            for (int i = 1; i < 101; i++) {
                checkBox[i].setSelected(false);
            }

            if (relevanceCheckBox.isSelected() == true) {
                relevanceCheckBox.setSelected(false);
            }

            imageCount = 1;
            displayFirstPage();
            iteration = -1;
            photographLabel.setIcon(null);
            panelBottom1.revalidate();
            panelBottom1.repaint();

        }
    }

    private class relevanceHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            hashMIntensityAndColorCode = new HashMap<>();
            normalizedMatrix = ImageRead.relevanceFeedback(intensityMatrix, colorCodeMatrix, 1);

            if (photographLabel.getIcon() != null && relevanceCheckBox.isSelected() == true) {
                for (int img = 1; img < 101; img++) {
                    normalizedMatrix[img][0] = colorCodeMatrix[img][0];
                    if (checkBox[img].isSelected()) {
                        checkedBoxes[img] = 1;
                        System.out.println("these are selected" + img);
                    }
                }

                iteration++;
                flagIntensityAndColorCode = "yes";
                double weight[] = new double[90];
                int picNumArray[] = new int[101];

                if (iteration == 1) {
                    for (int bin = 0; bin < 90; bin++) {
                        weight[bin] = (double) 1 / (double) 89;
                    }
                } else {
                    weight = weightCalculator(normalizedMatrix, checkedBoxes);
                }

                sumC = manhattanDistanceIntensityandColorCode(normalizedMatrix, picNo, weight);
                for (int i = 1; i < 101; i++) {
                    hashMIntensityAndColorCode.put(sumC[i], i);
                }

                Arrays.sort(sumC);

                System.out.println(" Distance of above selected ");
                for (int k = 1; k < 101; k++) {
                    buttonOrderIntensityAndColorCode[k] = hashMIntensityAndColorCode.get(sumC[k]);
                    System.out.println(buttonOrderIntensityAndColorCode[k] + " --> " + sumC[k]);
                }

                int imageButNo = 0;
                imageCount = 1;
                int endImage = imageCount + 20;
                panelBottom1.removeAll();
                for (int i = imageCount; i < endImage; i++) {
                    imageButNo = buttonOrderIntensityAndColorCode[i]; //hashMColorCode.get(sumC[i]);;
                    JPanel imagePanel = new JPanel();
                    imagePanel.setLayout(new BorderLayout());
                    imagePanel.add(button[imageButNo], BorderLayout.CENTER);
                    
                    if (relevanceCheckBox.isSelected() == true) {
                        imagePanel.add(checkBox[imageButNo], BorderLayout.SOUTH);
                    }
                    panelBottom1.add(imagePanel);
                    imageCount++;

                }

            }
            panelBottom1.revalidate();
            panelBottom1.repaint();

        }
    }

}

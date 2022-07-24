/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package directvolumerendering;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author Tomek
 */
public class VolumeRenderingGamePanel  extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    private static final Dimension PANEL_SIZE = new Dimension(800, 800);
    private static final int REFRESH_RATE = 1;
    private static final int CHARACTER_WIDTH = 32;
    private static final int CHARACTER_HEIGHT = 64;

    private Timer timer = null;
    private int currentRow = 0;
    private int currentCol = 0;
    private int randomRow = 0;
    private int randomCol = 0;

    private BufferedImage image = null;
    //public static int[][][] volume = null;
    private Random rand = new Random();
    
    
    
    BufferedImage img = null;
    BufferedImage img2 = null;
    //BufferedImage transfer_rgb = null;
    
    //ArrayList volumes = new ArrayList();
    int volumes[][] = null;
    
    double sampleDistance = 0;
    double rayLength = 0;
    int zSize = 0;
    int width = 0;
    
    double[] squared = null;
    double[] cubed = null;
    
    double cutCanvas_height = 0;
    double cutCanvas_width = 0;
    double imageSize = 0;
    RenderingWorker[] renderingWorkers = null;
    int[]transfer_rgb = null;
    int[]transfer_a = null;
    
    public void initializeThreads(){
        renderingWorkers = new RenderingWorker[4 * 4];
        int id = 0;
        int startA = 0;
        int startB = 0;
        int splitsX = 4;
        int splitsY = 4;
        for (int a = 0; a < splitsX; a++){
            startB = 0;
            for (int b = 0; b < splitsY; b++){
                
                //int aa = a img2.getWidth() / a
                renderingWorkers[id] = new RenderingWorker(this, startA, startB, 
                        startA + img2.getWidth() / splitsX, startB + img2.getHeight() / splitsY);
                id++;
                startB += img2.getHeight() / splitsY;
                //renderingWorker rw = new renderingWorker(this, 0, 0, img2.getWidth() / 2, img2.getHeight() / 2);
            }
            startA += img2.getWidth() / splitsX;
        }
    }
    
    float lastY = 0;
    public VolumeRenderingGamePanel() {
        try {
            final String path = System.getProperty("user.dir");
            img = ImageIO.read(new File(path + "\\img\\volume.png"));
            BufferedImage t_rgb = ImageIO.read(new File(path + "\\img\\transfer_rgb.png"));
            BufferedImage t_a = ImageIO.read(new File(path + "\\img\\transfer_a.png"));
            transfer_rgb = t_rgb.getRGB(0, 0, t_rgb.getWidth(), t_rgb.getHeight(), null, 0, t_rgb.getWidth());
            transfer_a = t_a.getRGB(0, 0, t_a.getWidth(), t_a.getHeight(), null, 0, t_a.getWidth());
            
        } catch (IOException e) {
        }
        int height = img.getHeight();
	width = img.getWidth();
	int zSizeIter = height / width;
        zSize = zSizeIter;
        //zSize = zSize / 2;
        //zSize = 88;
        //double imageSize = img.getWidth();
        int[] imageInPixels = img.getRGB(0, 0, width, height, null, 0, width);
        
        imageSize = img.getWidth();
        cutCanvas_height = imageSize;
        cutCanvas_width = imageSize * Math.sqrt(2);
        
        volumes = new int[zSize][];
        /////////////////
        int id = 0;
        //for (int a = 0; a < zSizeIter; a+=2)
        for (int a = 0; a < zSize; a++)
        {
            int id_i = width * width * a;
            //int[] imageInPixels2 = img2.getRGB(0, 0, img2.getWidth(), img2.getHeight(), null, 0, width);
            //int[] imageOutPixels = new int[imageInPixels.length];
            int[] xx = new int[width * width];
            
            for (int i = 0; i < xx.length; i++) {
                /*int alpha = (imageInPixels[i] & 0xFF000000) >> 24;
                int red = (imageInPixels[i] & 0x00FF0000) >> 16;
                int green = (imageInPixels[i] & 0x0000FF00) >> 8;
                int blue = (imageInPixels[i] & 0x000000FF) >> 0;*/

                xx[i] = 0x00ffffff & imageInPixels[i + id_i];
                /*
                int alpha = 255;
                //int id1 = i % 10;
                //int id2 = i / 10;
                int red = imageInPixels[i + id_i];
                int green = imageInPixels[i + id_i];
                int blue = imageInPixels[i + id_i];

                xx[i] = (red & 0xFF) << 16
                                | (green & 0xFF) << 8
                                | (blue & 0xFF);*/
            }
            volumes[id] = xx;
            id++;
        }
        
        /////////////////
        
        img2 = new BufferedImage((int)(width * Math.sqrt(2.0)), (int)(width * Math.sqrt(2.0)), BufferedImage.TYPE_3BYTE_BGR);
        //img2.setRGB(0, 0, img2.getWidth(), img2.getHeight(), (int[])volumes.get(60), 0, width);
        
        
        double startAngle = 0;
	//var startTime = Date.now();
	//var turnsPerSecond = 0.1;
	
	this.rayLength = width * Math.sqrt(2);
	double samplingRate = 1;
	double sampleCount = samplingRate*rayLength;
	this.sampleDistance = rayLength/sampleCount;

	squared = new double[256];
	cubed = new double[256];

	for(int i = 0; i < 256; i++){
            squared[i] = Math.pow(i/256.0, 2.0) / 3;
	}

	for(int i = 0; i < 256; i++){
		cubed[i] = Math.pow(i/256.0, 3.0);
	}

        
        timer = new Timer(REFRESH_RATE, this);
        
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer.start();
    }

    public Dimension getPreferredSize() {
        return PANEL_SIZE;
    }

    //https://www.javatips.net/api/robotutils-master/src/main/java/robotutils/Quaternion.java
        public static double[] fromEulerAngles(double roll, double pitch, double yaw) {
        double q[] = new double[4];
        
        // Apply Euler angle transformations
        // Derivation from www.euclideanspace.com
        double c1 = Math.cos(yaw/2.0);
        double s1 = Math.sin(yaw/2.0);
        double c2 = Math.cos(pitch/2.0);
        double s2 = Math.sin(pitch/2.0);
        double c3 = Math.cos(roll/2.0);
        double s3 = Math.sin(roll/2.0);
        double c1c2 = c1*c2;
        double s1s2 = s1*s2;
        
        // Compute quaternion from components
        q[0] = c1c2*c3 - s1s2*s3;
        q[1] = c1c2*s3 + s1s2*c3;
        q[2] = s1*c2*c3 + c1*s2*s3;
        q[3] = c1*s2*c3 - s1*c2*s3;
        return q;
    }
    
        //https://automaticaddison.com/how-to-convert-a-quaternion-to-a-rotation-matrix/
    public static double[][]matrixFromQuaternion(double[]q){
        double[][]retMatrix = generateMatrix(3, 3);
        
        double q0 = q[0];
        double q1 = q[1];
        double q2 = q[2];
        double q3 = q[3];
        // First row of the rotation matrix
        retMatrix[0][0] = 2 * (q0 * q0 + q1 * q1) - 1;
        retMatrix[0][1] = 2 * (q1 * q2 - q0 * q3);
        retMatrix[0][2] = 2 * (q1 * q3 + q0 * q2);
     
        //Second row of the rotation matrix
        retMatrix[1][0] = 2 * (q1 * q2 + q0 * q3);
        retMatrix[1][1] = 2 * (q0 * q0 + q2 * q2) - 1;
        retMatrix[1][2] = 2 * (q2 * q3 - q0 * q1);

        //Third row of the rotation matrix
        retMatrix[2][0] = 2 * (q1 * q3 - q0 * q2);
        retMatrix[2][1] = 2 * (q2 * q3 + q0 * q1);
        retMatrix[2][2] = 2 * (q0 * q0 + q3 * q3) - 1;
        
        return retMatrix;
    }
    
    double angle = 0;
    double angle2 = 0;
    double[][]rotMatrix = null;
    double []direction = null;
    int[]vvv = null;
    public void draw(){
        double []increment = new double[2];
        //angle += 0.1;
        direction = new double[3];
        double []start = new double[3];
        direction[0] = 0;
        direction[1] = 1;
        direction[2] = 0;
    
        //int[] zVolume;
        //int[]vvv = new int[img2.getWidth() * img2.getHeight()];
        vvv = new int[img2.getWidth() * img2.getHeight()];
        //vvv = img2.getRGB(0, 0, img2.getWidth(), img2.getHeight(), null, 0, img2.getWidth());
            
            //for(int a = 0; a < 176; a++){
                //for(int b = 0; b < 176; b++){
        
        try {
            double[] q = null;
            if (angle2 >= 0)
                q = fromEulerAngles(angle2,0,angle);
            else
                q = fromEulerAngles(angle2,0,-angle);
            //double[] q = fromEulerAngles(angle2,0,-angle);
            rotMatrix = matrixFromQuaternion(q);
            int a = 0;
            a++;
            //rotMatrix = rotationMatrix(angle,0,angle2);
            //direction =  matMul(rotMatrix, direction);
            //start =  matMul(rotMatrix, start);
        } catch (Exception ex) {
            Logger.getLogger(VolumeRenderingGamePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*
        renderingWorker rw = new renderingWorker(this, 0, 0, img2.getWidth(), img2.getHeight());
        rw.start();
        while (!rw.end) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(VolumeRenderingGamePanel3.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
        
        //renderingWorker rw = new renderingWorker(this, 0, 0, img2.getWidth() / 2, img2.getHeight() / 2);
        //renderingWorker rw3 = new renderingWorker(this, 0, img2.getHeight() / 2, img2.getWidth() / 2 , img2.getHeight());
        //renderingWorker rw2 = new renderingWorker(this, img2.getWidth() / 2, 0, img2.getWidth(), img2.getHeight() / 2);
        //renderingWorker rw4 = new renderingWorker(this, img2.getWidth() / 2, img2.getHeight() / 2, img2.getWidth(), img2.getHeight());

        /*
        renderingWorker rw = new renderingWorker(this, 0, 0, img2.getWidth(), img2.getHeight());
        rw.start();
        while (!rw.end) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(VolumeRenderingGamePanel3.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
        
        //renderingWorker rw = new renderingWorker(this, 0, 0, img2.getWidth() / 2, img2.getHeight() / 2);
        //renderingWorker rw3 = new renderingWorker(this, 0, img2.getHeight() / 2, img2.getWidth() / 2 , img2.getHeight());
        //renderingWorker rw2 = new renderingWorker(this, img2.getWidth() / 2, 0, img2.getWidth(), img2.getHeight() / 2);
        //renderingWorker rw4 = new renderingWorker(this, img2.getWidth() / 2, img2.getHeight() / 2, img2.getWidth(), img2.getHeight());
        
        
        
        
        /*
        rw.start();
        rw2.start();
        rw3.start();
        rw4.start();

        try {
            rw.join();
                    rw2.join();
        rw3.join();
        rw4.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(VolumeRenderingGamePanel3.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        initializeThreads();
        for (int a = 0; a < renderingWorkers.length; a++){
            renderingWorkers[a].start();
        }
        for (int a = 0; a < renderingWorkers.length; a++){
            try {
                renderingWorkers[a].join();
            } catch (InterruptedException ex) {
                
            }
        }
        img2.setRGB(0, 0, img2.getWidth(), img2.getHeight(), vvv, 0, img2.getWidth());
        fps++;
            

	}

    
    private static double[][] generateMatrix(int x, int y){
        double[][]retMatrix = new double[x][];
        for (int a = 0; a < retMatrix.length; a++) {
            retMatrix[a] = new double[y];
        }
        return retMatrix;
    }
    
    private double[][] yawMatrix(double alpha){
        double[][]retMatrix = generateMatrix(3,3);
        retMatrix[0][0] = Math.cos(alpha);
        retMatrix[0][1] = -Math.sin(alpha);
        retMatrix[0][2] = 0;
        
        retMatrix[1][0] = Math.sin(alpha);
        retMatrix[1][1] = Math.cos(alpha);
        retMatrix[1][2] = 0;
        
        retMatrix[2][0] = 0;
        retMatrix[2][1] = 0;
        retMatrix[2][2] = 1;
        return retMatrix;
    }
    
    private double[][] pitchMatrix(double alpha){
        double[][]retMatrix = generateMatrix(3,3);
        retMatrix[0][0] = Math.cos(alpha);
        retMatrix[0][1] = 0;
        retMatrix[0][2] = Math.sin(alpha);
        
        retMatrix[1][0] = 0;
        retMatrix[1][1] = 1;
        retMatrix[1][2] = 0;
        
        retMatrix[2][0] = -Math.sin(alpha);
        retMatrix[2][1] = 0;
        retMatrix[2][2] = Math.cos(alpha);
        return retMatrix;
    }
    
    private double[][] rollMatrix(double alpha){
        double[][]retMatrix = generateMatrix(3,3);
        retMatrix[0][0] = 1;
        retMatrix[0][1] = 0;
        retMatrix[0][2] = 0;
        
        retMatrix[1][0] = 0;
        retMatrix[1][1] = Math.cos(alpha);
        retMatrix[1][2] = -Math.sin(alpha);
        
        retMatrix[2][0] = 0;
        retMatrix[2][1] = Math.sin(alpha);
        retMatrix[2][2] = Math.cos(alpha);
        return retMatrix;
    }
    
    public double[][] matMul(double[][]m1, double[][]m2) throws Exception{
        if (m1[0].length != m2.length)
            throw new Exception("Wrong matrix dim.");
        double[][]retMatrix = generateMatrix(m1.length,m2[0].length);
        for (int a = 0; a < m1.length; a++)
            for (int b = 0; b < m2[0].length; b++)
                for (int c = 0; c < m1[a].length; c++){
                    retMatrix[a][b] += m1[a][c] * m2[c][b];
                }
        return retMatrix;
    }
    
    public double[]matMul(double[][]m1, double[]v2) throws Exception{
        if (m1[0].length != v2.length)
            throw new Exception("Wrong matrix dim.");
        double[]retVector = new double[v2.length];
        for (int a = 0; a < m1.length; a++)
            for (int b = 0; b < v2.length; b++){
                    retVector[a] += m1[a][b] * v2[b];
        }
        return retVector;
    }
    
    private double[][] rotationMatrix(double yaw, double pitch, double roll) throws Exception{
        double[][]retMatrix = yawMatrix(yaw);
        //double[][]retMatrix = pitchMatrix(pitch);
        retMatrix = matMul(retMatrix, pitchMatrix(pitch));
        retMatrix = matMul(retMatrix, rollMatrix(roll));
        return retMatrix;
    }
    
    private double[]transferFunction(int val) {
        double[]retVal = new double[4];
        retVal[0] = val;
        retVal[1] = 0;
        retVal[2] = 0;
        retVal[3] = 0.1;
        return retVal;
    }

    
    int fps = 0;
    long start = 0;
    protected void paintComponent(Graphics g) {
        if (start == 0)
            start = System.currentTimeMillis();
        
        super.paintComponent(g);
        
        
        draw();
        //g.drawImage(image, 0, 0, 512, 512, this);
        //g.drawImage(img2, 0, 0, 512, 512, this);
        g.drawImage(img2, 0, 0, this.getWidth(), this.getHeight(), this);
        if (System.currentTimeMillis() - start > 1000) {
            System.out.println(fps);
            fps = 0;
            start = 0;
        }
        
        //g.draw
    }

    public void actionPerformed(ActionEvent e) {
        int min = 0;
        int maxRow = (int)PANEL_SIZE.getHeight() - CHARACTER_HEIGHT;
        int maxCol =  (int)PANEL_SIZE.getWidth() - CHARACTER_WIDTH;

        Random rand = new Random();
        randomRow = rand.nextInt((maxRow - min) + 1) + min;
        randomCol = rand.nextInt((maxCol - min) + 1) + min;

        repaint();
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        int rowIncrement = 0;
        int colIncrement = 0;

        if(code == KeyEvent.VK_LEFT) {
            colIncrement--;
        }
        else if(code == KeyEvent.VK_RIGHT) {
            colIncrement++;
        }
        else if(code == KeyEvent.VK_UP) {
            rowIncrement--;
        }
        else {
            if(code == KeyEvent.VK_DOWN) {
                rowIncrement++;
            }
        }

        if(isInBounds(rowIncrement, colIncrement)) {
            currentRow += rowIncrement;
            currentCol += colIncrement;
            repaint();
        }
    }

    private boolean isInBounds(int rowIncrement, int colIncrement) {
        int top = currentRow + rowIncrement;
        int left = currentCol + colIncrement;
        int right = left + CHARACTER_WIDTH;
        int bottom = top + CHARACTER_HEIGHT;

        return (top >= 0 && left >= 0 && right <= PANEL_SIZE.getWidth() && bottom <= PANEL_SIZE.getHeight());       
    }

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    @Override
    public void mouseClicked(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean mouseIsDown = false;
    int mouseStartX = 0;
    int mouseStartY = 0;
    @Override
    public void mousePressed(MouseEvent me) {
         System.out.println("CLICK");
        mouseIsDown = true;
        mouseStartX = me.getX();
        mouseStartY = me.getY();
        //System.out.println("Mouse clicked at " + me.getY());
        //                        lastY = me.getY();
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        mouseIsDown = false;
        System.out.println("NOTCLICK");
        /*
        System.out.println("Mouse released at " + me.getY());
                        if (me.getY() < lastY) {
                            System.out.println("Upward swipe");
                        } else if (me.getY() > lastY) {
                            System.out.println("Downward swipe");
                        } else {
                            System.out.println("No movement");
                        }
                        ;*/
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        //System.err.println(mouseIsDown);
        if (mouseIsDown) {
            int deltaX = me.getX() - mouseStartX;
            int deltaY = me.getY() - mouseStartY;
            angle += (double)deltaX / 1000;
            angle2 += (double)deltaY / 1000;
            //System.err.println(deltaX);
            //System.err.println(deltaY);
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        //System.err.println(mouseIsDown);

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

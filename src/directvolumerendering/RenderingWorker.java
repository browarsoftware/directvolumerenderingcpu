/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package directvolumerendering;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tomek
 */
public class RenderingWorker extends Thread {
        VolumeRenderingGamePanel parent = null;
        int startX = 0;
        int startY = 0; 
        int stopX = 0;
        int stopY = 0;
        boolean end = false;
        
    public RenderingWorker(VolumeRenderingGamePanel parent, 
            int startX, int startY, int stopX, int stopY){
        this.parent = parent;
        this.startX = startX;
        this.startY = startY; 
        this.stopX = stopX;
        this.stopY = stopY;
    }
    public void run() {
        int[]start = new int[3];
        
        double[]light = new double[3];
        light[0] = -1;
        light[1] = -1;
        light[2] = 0;
        double light_l = Math.sqrt((light[0] * light[0]) + (light[1] * light[1]) + (light[2] * light[2]));
        light[0] /= light_l;
        light[1] /= light_l;
        light[2] /= light_l;
        
            try {
                light =  parent.matMul(parent.rotMatrix, light);
                
                light_l = Math.sqrt((light[0] * light[0]) + (light[1] * light[1]) + (light[2] * light[2]));
        light[0] /= light_l;
        light[1] /= light_l;
        light[2] /= light_l;
                
            } catch (Exception ex) {
                Logger.getLogger(RenderingWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        double []direction = null;
        /*direction[0] = 0;
        direction[1] = 0;
        direction[2] = 1;*/
        direction = parent.direction;
        int[] zVolume;
        //System.out.println("This code is running in a thread");
        for(int a = startX; a < stopX; a++){
        for(int b = startY; b < stopY; b++){

            int value = 0;
            double[]rgb = new double[3];
            double out_a = 0;
            double aa = 0;
            double vv = 0;

            //double startX = ;
            //double starty = - 0.5;


            start[0] = a;
            start[1] = 0;
            start[2] = b;


            double[] directionMul = null;



            for (double c = 0; c < parent.zSize; c+=0.75){

                start[0] += direction[0];
                start[1] += direction[1];
                start[2] += direction[2];
                int id_a = (int)(start[0] - (parent.img2.getHeight()/2.0));
                int id_b = (int)(start[1] - (parent.img2.getWidth()/2.0));
                int id_c = (int)(start[2] - (parent.img2.getHeight()/2.0));


                try {
                    //rotMatrix = rotationMatrix(0,angle,0);
                    double[] rot = {id_a, id_b, id_c};
                    rot =  parent.matMul(parent.rotMatrix, rot);
                    id_a = (int)(rot[0] + (parent.width/2.0));
                    id_b = (int)(rot[1] + (parent.width/2.0));
                    id_c = (int)(rot[2] + (parent.zSize/2.0));
                } catch (Exception ex) {
                    Logger.getLogger(VolumeRenderingGamePanel.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (id_a >= 0 && id_b >= 0 && id_c >= 0 &&
                        id_a < parent.width && id_b < parent.width && id_c < parent.zSize)
                {
                    zVolume = parent.volumes[id_c];
                    value = zVolume[id_a + id_b*parent.width] % 256;

                    
                    if(value < 50){
                        continue;
                    }
                    //value = 255;
                    
                    double[]grad = new double[3];
                    
                    int id_a_1 = id_a - 1;
                    int id_b_1 = id_b - 1;
                    int id_c_1 = id_c - 1;
                    
                    int id_a_2 = id_a + 1;
                    int id_b_2 = id_b + 1;
                    int id_c_2 = id_c + 1;
                    
                    if (id_a_1 < 0) id_a_1 = 0;
                    if (id_b_1 < 0) id_b_1 = 0;
                    if (id_c_1 < 0) id_c_1 = 0;
                    
                    if (id_a_2 >= parent.width) id_a_2 = parent.width - 1;
                    if (id_b_2 >= parent.width) id_b_2 = parent.width - 1;
                    if (id_c_2 >= parent.zSize) id_c_2 = parent.zSize - 1;
                    /*
                    if (id_a_1 >= parent.width) id_a_1 = 0;
                    if (id_b_1 >= parent.width) id_b_1 = 0;
                    if (id_c_1 >= parent.zSize) id_c_1 = 0;
                    
                    if (id_a_2 >= parent.width) id_a_2 = 0;
                    if (id_b_2 >= parent.width) id_b_2 = 0;
                    if (id_c_2 >= parent.zSize) id_c_2 = 0;
                    */
                    
                    grad[0] = ((double)(parent.volumes[id_c][id_a + id_b_2*parent.width] & 0x000000FF) / 255.0) 
                            - ((double)(parent.volumes[id_c][id_a + id_b_1*parent.width] & 0x000000FF) / 255.0);
                    grad[1] = ((double)(parent.volumes[id_c][id_a_2 + id_b*parent.width] & 0x000000FF) / 255.0) 
                            - ((double)(parent.volumes[id_c][id_a_1 + id_b*parent.width] & 0x000000FF) / 255.0);
                    grad[2] = ((double)(parent.volumes[id_c_2][id_a + id_b*parent.width] & 0x000000FF) / 255.0) 
                            - ((double)(parent.volumes[id_c_1][id_a + id_b*parent.width] & 0x000000FF) / 255.0);
                    
                    
                    double grad_l = Math.sqrt((grad[0] * grad[0]) + (grad[1] * grad[1]) + (grad[2] * grad[2]));
                    if (grad_l > 0) {
                        grad[0] /= grad_l;
                        grad[1] /= grad_l;
                        grad[2] /= grad_l;
                    }
                    else {
                        grad[0] = 0;
                        grad[1] = 0;
                        grad[2] = 0;
                    }
                    
                    double diffuse = Math.max((grad[0] * light[0]) + (grad[1] * light[1]) + (grad[2] * light[2]), 0);
                    
                    
                    //aa = parent.squared[value];
                    aa = (double)(parent.transfer_a[value] & 0x000000FF) / 255;
                    //vv = parent.cubed[value];
                    
                    int rgb_val = parent.transfer_rgb[value];
                    
                    
                    double vv_alpha = ((rgb_val & 0xFF000000) >> 24) / 255.0;
                    double  vv_red = ((rgb_val & 0x00FF0000) >> 16) / 255.0;
                    double  vv_green = ((rgb_val & 0x0000FF00) >> 8) / 255.0;
                    double  vv_blue = ((rgb_val & 0x000000FF) >> 0) / 255.0;
                    
                    //vv = diffuse * parent.cubed[value] + 0.25 * parent.cubed[value];
                    vv_red = Math.min(diffuse * vv_red + (0.25 * vv_red),1);
                    vv_green = Math.min(diffuse * vv_green + (0.25 * vv_green),1);
                    vv_blue = Math.min(diffuse * vv_blue + (0.25 * vv_blue),1);
                    /*vv_red = (diffuse * vv_red + (0.25 * vv_red));
                    vv_green = (diffuse * vv_green + (0.25 * vv_green));
                    vv_blue = (diffuse * vv_blue + (0.25 * vv_blue));*/
                    
                    
                    //aa = parent.squared[value];
                    //vv = parent.cubed[value];
                    
                    //https://martinopilia.com/posts/2018/09/17/volume-raycasting.html
                    //rgb = aa * vv + (1.0 - aa) * out_a * rgb;
                    //out_a = aa + (1.0 - aa) * out_a;
                    
                    /*
                    rgb[0] = (1.0 - out_a) * vv * aa + rgb[0];
                    rgb[1] = (1.0 - out_a) * vv * aa + rgb[1];
                    rgb[2] = (1.0 - out_a) * vv * aa + rgb[2];*/
                    rgb[0] = (1.0 - out_a) * vv_red * aa + rgb[0];
                    rgb[1] = (1.0 - out_a) * vv_green * aa + rgb[1];
                    rgb[2] = (1.0 - out_a) * vv_blue * aa + rgb[2];
                    
                    
                    out_a = (1.0 - out_a) * aa + out_a;

                    /*if(out_a >= 0.95){
                            break;
                    }*/
                    
                }
            }
            int index = (parent.img2.getWidth()* b) + a;
            int alpha = 255;
            //rgb = 1;
            int red = (int)(rgb[0]*255);
            int green = (int)(rgb[1]*255);
            int blue = (int)(rgb[2]*255);
            parent.vvv[index] = (alpha & 0xFF) << 24
                | (red & 0xFF) << 16
                | (green & 0xFF) << 8
                | (blue & 0xFF);
        }
    }
        end = true;
    }
    }   
    

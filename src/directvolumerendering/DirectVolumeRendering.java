/**
 *
 * @author Tomek
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package directvolumerendering;

//https://draemm.li/various/volumeRendering/cpu/
//https://www.reddit.com/r/GraphicsProgramming/comments/7dlbzo/i_wrote_a_singlethreaded_cpu_volume_ray_casting/
//https://martinopilia.com/posts/2018/09/17/volume-raycasting.html

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 *
 * @author Tomek
 */

public class DirectVolumeRendering extends JFrame{

    private static final long serialVersionUID = 1L;
    //BufferedImage img = null;
    public DirectVolumeRendering() {
        super("Game Frame");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().add(new VolumeRenderingGamePanel(), BorderLayout.CENTER);
        //getContentPane().add(new VolumeRenderingGamePanel5(), BorderLayout.CENTER);
        pack();
        //setResizable(false);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {    
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new DirectVolumeRendering();
                frame.setVisible(true);
            }
        });     
    }

    
}

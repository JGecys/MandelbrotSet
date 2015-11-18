package Proj_JurgisGecys;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jurgis Gečys on 2015-11-09.
 *
 * Assingment description http://introcs.cs.princeton.edu/java/assignments/mandel.html
 *
 */
public class Mandelbrot extends JFrame {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Mandelbrot set - Jurgis Gečys");
        ImagePanel panel = new ImagePanel();
        frame.setLayout(new FlowLayout());
        frame.setContentPane(panel);
        frame.setMinimumSize(new Dimension(400, 400));
        frame.setSize(600, 600);
        frame.setVisible(true);
        panel.validate();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static int mandel(double x, double y) {
        double r = x;
        double s = y;
        for (int i = 0; i < 255; i++) {
            if (r * r + s * s > 4) {
                return i;
            }
            double r0 = r;
            r = (r0 * r0) - (s * s) + x;
            s = (2 * r0 * s) + y;
        }
        return 255;
    }

    public static class ImagePanel extends JPanel implements KeyListener, ActionListener {

        public double x = 0;
        public double y = 0;
        public double size = 5;
        private static double aspect;

        private BufferedImage image;
        private HashMap<Character, Boolean> keyMap = new HashMap<>();

        private Timer timer = new Timer(10, this);

        private long prevTime = 0;

        public ImagePanel() {
            //region Input initialization
            this.setFocusable(true);
            this.addKeyListener(this);
            keyMap.put('a', false);
            keyMap.put('w', false);
            keyMap.put('s', false);
            keyMap.put('d', false);
            keyMap.put('-', false);
            keyMap.put('+', false);
            //endregion
            timer.start();
            prevTime = System.nanoTime();
        }

        @Override
        public void paint(Graphics g) {
            if (image == null || image.getHeight() < getHeight() || image.getWidth() < getWidth()) {
                image = new BufferedImage(getWidth(),
                        getHeight(),
                        BufferedImage.TYPE_INT_RGB); //If image cannot be reused, create new one
            }
            aspect = ((double) getWidth()) / getHeight();       //Calculates aspect ratio of window
            Graphics2D graphics2D = ((Graphics2D) g);
            graphics2D.setRenderingHint(                        //Renders image with antialiasing
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            ArrayList<Thread> threads = new ArrayList<>();  //Stores threads used for rendering image
            Thread temp;
            for (int i = 0; i < getWidth(); i++) {
                threads.add((temp = new Thread(new RowRunnable(i))));
                temp.start();
            }
            for (Thread t : threads) {          //Join all threads to complete rendering image
                if (t.isAlive())
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
            graphics2D.drawImage(image, 0, 0, null);
            graphics2D.setColor(Color.WHITE);           //Draws coordinates
            graphics2D.fillRect(5, 5, 180, 80);
            graphics2D.setColor(Color.BLACK);
            graphics2D.drawRect(5, 5, 180, 80);
            graphics2D.drawString("X: " + x, 10, 30);
            graphics2D.drawString("Y: " + y, 10, 50);
            graphics2D.drawString("Z: " + size, 10, 70);
            int halfWidth = getWidth() / 2;
            int halfHeight = getHeight() / 2;
            Color midColor = new Color(image.getRGB(halfWidth, halfHeight));    //Gets color in the middle
            graphics2D.setColor(new Color(255 - midColor.getRed(),                //Inverts it for best visibility
                    255 - midColor.getGreen(),
                    255 - midColor.getBlue()));
            graphics2D.drawLine(halfWidth, halfHeight - 5, halfWidth, halfHeight + 5);  //Draws crosshair in the middle (10x10)
            graphics2D.drawLine(halfWidth - 5, halfHeight, halfWidth + 5, halfHeight);
        }


        public class RowRunnable implements Runnable {
            final int ii;

            public RowRunnable(final int i) {
                ii = i;
            }

            @Override
            public void run() {
                for (int j = 0; j < getHeight(); j++) {

                    double x0 = x - size / 2 * aspect + size * ii / getWidth() * aspect;
                    double y0 = y - size / 2 + size * j / getHeight();
                    int ss = mandel(x0, y0);
                    Color color = new Color(Math.min(ss * ss / 20, 255),
                            Math.min(ss * ss / 10, 255),
                            Math.min(ss * ss * 2 + 10, 255));
                    image.setRGB(ii, j, color.getRGB());
                }
            }
        }

        //region Input Region.
        @Override
        public void actionPerformed(ActionEvent e) {
            double time = (System.nanoTime() - prevTime) / 100000000f;
            boolean isUpdated = false;
            if (keyMap.get('d')) {
                x += size * 0.02 * time;
                isUpdated = true;
            }
            if (keyMap.get('a')) {
                x -= size * 0.02 * time;
                isUpdated = true;
            }
            if (keyMap.get('w')) {
                y -= size * 0.02 * time;
                isUpdated = true;
            }
            if (keyMap.get('s')) {
                y += size * 0.02 * time;
                isUpdated = true;
            }
            if (keyMap.get('+')) {
                size *= 1 + (-0.1 * time);
                isUpdated = true;
            }
            if (keyMap.get('-')) {
                size *= 1 + (0.1 * time);
                isUpdated = true;
            }
            if (isUpdated)
                this.repaint();
            prevTime = System.nanoTime();
        }


        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (keyMap.containsKey(e.getKeyChar())) {
                keyMap.replace(e.getKeyChar(), true);
            }
            if (e.getKeyChar() == 'r') {
                x = 0;
                y = 0;
                size = 5;
                this.repaint();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (keyMap.containsKey(e.getKeyChar())) {
                keyMap.replace(e.getKeyChar(), false);
            }
        }
    }
//endregion


}

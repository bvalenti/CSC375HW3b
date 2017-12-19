import javax.swing.*;
import java.awt.*;

public class MyPainter extends JPanel {
    int pixSize = 1;
    Solution current;
    double maxTemp;

    public MyPainter(Solution c) {
        current = c;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        performPaint(g);
    }

    public void paintSolution(Solution toPaint) {
        current = toPaint;
        painterRepaint();
    }

    public void painterRepaint() {
        validate();
        paintImmediately(0,0,2*GUI.size*pixSize,GUI.size*pixSize);
    }

    private void performPaint(Graphics g) {
        super.paintComponent(g);
        Color c;
        int widthCount = 0, heightCount = 0;

        for (int i = 0; i < current.width; i++) {
            if ((i % 2) == 0) {
                heightCount = 0;
                for (int j = 0; j < current.height; j++) {
                    if ((j % 2) == 0) {
                        if (current.grid[i][j] == 0) {
                            c = new Color(255, 255, 255);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 1 / 10000000) {
                            c = new Color(54, 153, 255);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 1 / 1000000) {
                            c = new Color(75, 228, 255);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 1 / 100000) {
                            c = new Color(123, 251, 255);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 1 / 10000) {
                            c = new Color(184, 255, 239);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 1 / 1000) {
                            c = new Color(255, 250, 230);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 2 / 1000) {
                            c = new Color(255, 250, 200);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 3 / 1000) {
                            c = new Color(255, 240, 176);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 4 / 1000) {
                            c = new Color(255, 230, 155);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 7 / 1000) {
                            c = new Color(255, 200, 105);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 13 / 1000) {
                            c = new Color(255, 150, 80);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 19 / 1000) {
                            c = new Color(255, 110, 80);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 24 / 1000) {
                            c = new Color(255, 80, 40);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp * 33 / 1000) {
                            c = new Color(255, 53, 225);
                            g.setColor(c);
                        } else if (current.grid[i][j] <= maxTemp) {
                            c = new Color(210, 0, 255);
                            g.setColor(c);
                        } else {
                            c = new Color(210, 0, 255);
                            g.setColor(c);
                        }
                        g.fillRect(pixSize * widthCount, pixSize * heightCount, pixSize, pixSize);
                        g.setColor(Color.BLACK);
                        heightCount++;
                    }
                }
                widthCount++;
            }
        }
    }
}

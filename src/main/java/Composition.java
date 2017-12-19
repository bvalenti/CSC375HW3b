import java.util.concurrent.ThreadLocalRandom;

public class Composition {
    int width, height;
    double C[] = new double[3];
    Cell grid[][];

    public Composition(int w, int h) {
        width = w;
        height = h;
        grid = new Cell[w][h];
        initComposition();
    }

    private void initComposition() {
        double c1, c2, c3, total;
        int variation1, variation2 = 0, c;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                c1 = 33.3; c2 = 33.3; c3 = 33.3;
                grid[i][j] = new Cell();

                c1 += ThreadLocalRandom.current().nextInt(-12,13);
                c2 += ThreadLocalRandom.current().nextInt(-12,13);
                c3 += ThreadLocalRandom.current().nextInt(-12,13);
                total = c1 + c2 + c3;
                grid[i][j].percents[0] = c1/total;
                grid[i][j].percents[1] = c2/total;
                grid[i][j].percents[2] = c3/total;
            }
        }
    }
}

import java.util.concurrent.RecursiveTask;

public class TemperatureSolver extends RecursiveTask<Double> {
    static int THRESHOLD = GUI.size/10;
    int y, height, lo, hi;
    public Solution cs;
    Composition composition;
    TemperatureSolver top, bot;
    Main main;
    Double convergence = 0.0, tmp;

    TemperatureSolver(int y, int height, int lo, int hi, Composition comp, Main m) {
        this.y = y;
        this.height = height;
        this.lo = lo;
        this.hi = hi;
        if (m.state == 0) {
            cs = m.A;
        } else {
            cs = m.B;
        }
        composition = comp;
        main = m;
    }

    @Override
    protected Double compute() {

        if (height <= THRESHOLD) {
            double newTemp, temp;

            for (int i = 0; i < 2*GUI.size; i++) {
                for (int j = y; j < y + height; j++) {
                    temp = 0;
                    for (int k = 0; k < 3; k++) {
                        newTemp = 0;
                        //perform calculation for each cell
                        if (i == 0 && j == 1 && lo == 0) { //Top left corner
                            newTemp = newTemp
                                    + cs.grid[i+1][j] * composition.grid[i+1][j].percents[k]
                                    + cs.grid[i][j+1] * composition.grid[i][j+1].percents[k];
                            newTemp = newTemp/2;
                        } else if (i == 0 && j == y + height - 1 && hi == GUI.size - 1) { //Bottom left corner
                            newTemp = newTemp
                                    + cs.grid[i+1][j] * composition.grid[i+1][j].percents[k]
                                    + cs.grid[i][j-1] * composition.grid[i][j-1].percents[k];
                            newTemp = newTemp/2;
                        } else if (i == GUI.size*2-1 && j == 0 && lo == 0) { //Top right corner
                            newTemp = newTemp
                                    + cs.grid[i-1][j] * composition.grid[i-1][j].percents[k]
                                    + cs.grid[i][j+1] * composition.grid[i][j+1].percents[k];
                            newTemp = newTemp/2;
                        } else if (i == GUI.size*2-1 && j == y + height - 1 && hi == GUI.size - 1) { //Bottom right corner
                            newTemp = newTemp
                                    + cs.grid[i-1][j] * composition.grid[i-1][j].percents[k]
                                    + cs.grid[i][j-1] * composition.grid[i][j-1].percents[k];
                            newTemp = newTemp/2;
                        } else if (i == 0) { //Left side
                            newTemp = newTemp
                                    + cs.grid[i+1][j] * composition.grid[i+1][j].percents[k]
                                    + cs.grid[i][j-1] * composition.grid[i][j-1].percents[k]
                                    + cs.grid[i][j+1] * composition.grid[i][j+1].percents[k];
                            newTemp = newTemp/3;
                        } else if (i == GUI.size*2-1) { //Right side
                            newTemp = newTemp
                                    + cs.grid[i-1][j] * composition.grid[i-1][j].percents[k]
                                    + cs.grid[i][j-1] * composition.grid[i][j-1].percents[k]
                                    + cs.grid[i][j+1] * composition.grid[i][j+1].percents[k];
                            newTemp = newTemp/3;
                        } else if (j == 1 && lo == 0) { //Top side
                            newTemp = newTemp
                                    + cs.grid[i-1][j] * composition.grid[i-1][j].percents[k]
                                    + cs.grid[i+1][j] * composition.grid[i+1][j].percents[k]
                                    + cs.grid[i][j+1] * composition.grid[i][j+1].percents[k];
                            newTemp = newTemp/3;
                        } else if (j == y + height - 1 && hi == GUI.size - 1) { //Bottom side
                            newTemp = newTemp
                                    + cs.grid[i-1][j] * composition.grid[i-1][j].percents[k]
                                    + cs.grid[i+1][j] * composition.grid[i+1][j].percents[k]
                                    + cs.grid[i][j-1] * composition.grid[i][j-1].percents[k];
                            newTemp = newTemp/3;
                        } else {
                            newTemp = newTemp
                                    + cs.grid[i-1][j] * composition.grid[i-1][j].percents[k]
                                    + cs.grid[i+1][j] * composition.grid[i+1][j].percents[k]
                                    + cs.grid[i][j-1] * composition.grid[i][j-1].percents[k]
                                    + cs.grid[i][j+1] * composition.grid[i][j+1].percents[k];
                            newTemp = newTemp/4;
                        }
                        temp = temp + newTemp * composition.C[k];
                    }
                    if (main.state == 0) {
                        main.B.grid[i][j] = temp;
                        tmp = main.A.grid[i][j] - temp;
                        if (tmp > convergence) { convergence = tmp; }
                    } else {
                        main.A.grid[i][j] = temp;
                        tmp = main.B.grid[i][j] - temp;
                        if (tmp > convergence) { convergence = tmp; }
                    }
                }
            }
        } else {
            //fork problem into smaller subtasks
            top = new TemperatureSolver(      y     , height/2 + (height % 2), lo, hi, composition, main);
            bot = new TemperatureSolver(y + height/2, height/2 + (height % 2), lo, hi, composition, main);
            top.fork();
            bot.fork();

            Double conv1 = top.join();
            Double conv2 = bot.join();
            convergence = (conv1 > conv2)? conv1:conv2;
        }
        return convergence;
    }
}

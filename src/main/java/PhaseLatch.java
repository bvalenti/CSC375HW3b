public class PhaseLatch {
    int modulus;
    int count = 0;
    int phase = 1;
    int convergences[], out[];

    PhaseLatch(int m) {
        modulus = m;
        convergences = new int[m];
    }

    int[] increment(int conv) throws InterruptedException {
        synchronized (this) {
            count++;
            convergences[count % modulus] = conv;
            if (count % modulus == 0 || count > phase*modulus) {
                this.notifyAll();
                out = convergences;
                convergences = new int[modulus];
                return out;
            } else {
                await();
                out = convergences;
                convergences = new int[modulus];
                return out;
            }
        }
    }

    void await() throws InterruptedException {
        synchronized (this) {
            while (count % modulus != 0) {
                if (count > phase*modulus) {
                    break;
                }
                this.wait();
            }
        }
    }

    void incrementAndAdvance(int conv) throws InterruptedException {
        synchronized (this) {
            count++;
            convergences[count % modulus] = conv;
            if (count % modulus == 0 || count > phase*modulus) {
                this.notifyAll();
            }
        }
    }

    void incrementPhase() {
        phase++;
    }
}

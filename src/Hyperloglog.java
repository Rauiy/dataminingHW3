import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steven on 2016-11-24.
 */


public class Hyperloglog{
    static final int b = 3, bits = 32-b;
    static int mask = 3, rmask = 1073741823;; // The amount of bits we use

    public class retHll{


        public retHll(int[][] c, Map<?, Node> nodes) {
            this.c = c;
            this.nodes = nodes;
        }

        public int[][] c;
        public Map<?,Node> nodes;
    }

    public static void main(String[] args){



        mask = 0;
        for(int i = 0; i < b;i++)
            mask = (mask << 1) + 1;
        rmask = 0;
        for(int i = 0; i < bits;i++)
            rmask = (rmask << 1) + 1;

        Hyperloglog hll = new Hyperloglog();
        HyperBall hb = new HyperBall();
        retHll rHll = hll.streamFile("out.actor-movie");

        /*int m = (int)Math.pow(2,b);
        for(int i=1; i < max+1; i++){
            for(int j = 0; j < m; j++){
                System.out.print(c[i][j] + " ");
            }
            System.out.println();
        }*/

        HyperBall.retHb rHb = hb.hyperBall(rHll.c, rHll.nodes);
        int[][] sol = rHb.c;
        int m = (int) Math.pow(2,b);
        for(int i=1; i < max+1; i++){
            for(int j = 0; j < m; j++){
                if(sol[i][j] > 0)
                    System.out.print(sol[i][j] + " ");
                else
                    System.out.print("-" + " ");
            }
            System.out.println();
        }

        System.out.println("Max radius: " + rHb.t);

    }

    static int max = 0;
    public retHll streamFile(String filename){
        int m = (int)Math.pow(2,b);

        HashMap<String, Node> nodes = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;

            while((line = br.readLine()) != null){
                if(line.indexOf("%") != -1)
                    continue;
                //M = add(M, set, line.split(" "));
                //list.add(line);
                String[] n = line.split(" ");

                Node n1 = nodes.get(n[0]);
                Node n2 = nodes.get(n[1]);

                if( n1 == null){
                    n1 = new Node(n[0], m);
                    nodes.put(n[0],n1);
                }

                if( n2 == null){
                    n2 = new Node(n[1], m);
                    nodes.put(n[1],n2);
                }

                n1.add(n2);
                n1.M = add(n1.M, n[1]);

                //n2.add(n1);
                //n2.M = hll.add(n2.M, n[0]);

                // Keep track on how many nodes we have
                if(n1.id > max) max = n1.id;
                if(n2.id > max) max = n2.id;

                //if(i++%100000 == 0)
                  //  System.out.println("Estimate: " + hll.harmonic(M) + " Real: " + set.size());//fm(line.split(" ")));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int[][] c = new int[max+1][];
        //System.out.println(max+1);
        for(Node v: nodes.values()) {
            c[v.id] = v.M.clone(); // clone counter into our new format, used by hyperball
            v.M = null; // remove temporary counter
        }

        // return all hllcounters and nodes
        return new retHll(c, nodes);
    }

    /* Used for adding multiple items to counters at the same time */
    public int[] add(int[] M, String[] items){
        for(String s: items) {
            M = add(M,s); // Calling the add for single item for each input item
        }

        return M;
    }

    /* Adding one single item to counters */
    public int[] add(int[] M, String item){
        int x = item.hashCode();
        int j = addressR(x); // addressR takes the rightmost b bits from the input x, remove the capital R for leftmost
        int b = rR(x,bits); // rR counts the trailing rightmost zeros from the input x exclusive the rightmost b bits, remove the capital R for leftmost

        M[j] = Math.max(b,M[j]); // Compare old value with the new value for the bucket j

        return M;
    }

    /* Function for adding a single already hashed item to counters */
    public int[] add(int[] M, int item){
        int j = address(item);
        M[j] = Math.max(r(item,bits),M[j]);

        return M;
    }

    /* Get the address according to the b leftmost bits*/
    public int address(int hash){
        // right shift until we have the b leftmost bits as the b rightmost bits
        // then mask the new value to remove leading 1s if it was a negative value
        return (hash >> (bits))&mask;
    }

    /* Count the number of leading 0s without the leftmost b bits */
    public int r(int hash, int bits){
        // Mask the hash, i.e. remove the first b bits
        hash = hash & rmask;

        // Transform hash to binary and get number of first 1, which is the length of the binary string
        // the amount of bits - number of first 1 is the number of leading 0s, + 1
        return bits - Integer.toBinaryString(hash).length() + 1;
    }

    /* Get the address according to the b rightmost bits */
    public int addressR(int hash){
        return (hash&mask); // 3 = 0b11
    }

    /* Count the number of trailing 0s without the rightmost b bits*/
    public int rR(int hash, int bits){
        hash = hash >> b; // right shift to remove the first b bits

        int i = 1; // start counting from 1, for trailing 0s +1
        while(hash%2 == 0){ // as long as rightmost bit is a 0
            hash = hash >> 1; // right shift once, remove the 0
            i++; // count the 0
        }

        return i;
    }

    /* Used to calculate the normalized harmonic mean */
    public double size(int[] M){
        double alfa = alfa(M); // Get the alfa, i.e. a constant depending on the amount of buckets used
        double p = M.length; // the amount of buckets used, i.e. m in some papers

        // The formula used in the papers, alfa * p^2 * Z
        return alfa*p*p*sum(M);
    }

    /* The harmonic sum of the counters, Z := 2^-M[i] */
    public double sum(int[] M){
        double sum = 0;
        for(int i: M)
            sum+=(Math.pow(2,-i));

        // 1/Z so we can use it to multiple for our formula
        return 1/sum;
    }

    public double alfa(int[] M){
        /* Estimation or precalculated alfas*/
        switch (M.length){
            case(2): return 1/(2*1.42371);
            case(4): return 0.532434;
            case(8): return 0.635576;
            case(16): return 0.673;
            default:
                return 0.7213/(1+1.079/M.length); //Estimations
        }
    }
}




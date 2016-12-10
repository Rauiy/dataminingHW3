import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Steven on 2016-11-24.
 */


public class Hyperloglog{
    static int b = 4, bits = 32-b;
    static int mask = 3, rmask = 1073741823; // The amount of bits we use
    static String filename = "out.maayan-Stelzl";
    static boolean leading = false;
    public class retHll{


        public retHll(int[][] c, Map<?, Node> nodes) {
            this.c = c;
            this.nodes = nodes;
        }

        public int[][] c;
        public Map<?,Node> nodes;
    }



    public static void main(String[] args){



        for(String arg: args){
            if(arg.contains("=")) {

                String[] s = arg.split("=");
                switch(s[0]){
                    case "b":
                        System.out.println("Argument b found");
                        b = Integer.parseInt(s[1]);
                        bits = 32 - b;
                        break;
                    case "f":
                    case "file":
                    case "filename":
                        System.out.println("Argument filename found");
                        filename = s[1];
                        break;
                    default:
                        System.out.println("Wrong input type: " + s[0] + " doesn't exist");
                }

            }
            else if(arg.equals("leading"))
                leading = true;
        }

        if(leading)
            System.out.println("Using leading bits for hyperloglog");
        else
            System.out.println("Using trailing bits for hyperloglog");

        mask = 0;
        for(int i = 0; i < b;i++)
            mask = (mask << 1) + 1;
        rmask = 0;
        for(int i = 0; i < bits;i++)
            rmask = (rmask << 1) + 1;

        Hyperloglog hll = new Hyperloglog();
        HyperBall hb = new HyperBall();


        System.out.println("Generating hyperloglog counters");

        retHll rHll = hll.streamFile(filename);

        /*int m = (int)Math.pow(2,b);
        for(int i=1; i < max+1; i++){
            for(int j = 0; j < m; j++){
                System.out.print(c[i][j] + " ");
            }
            System.out.println();
        }*/

        System.out.println("Generating hyperball counters");
        HyperBall.retHb rHb = hb.hyperBall(rHll.c, rHll.nodes);
        int[] res = hll.analyze(rHb.c);
        int[][] sol = rHb.c;
        int m = (int) Math.pow(2,b);
        /*for(int i=1; i < max+1; i++){
            for(int j = 0; j < m; j++){
                if(sol[i][j] > 0)
                    System.out.print(sol[i][j] + " ");
                else
                    System.out.print("-" + " ");
            }
            System.out.println();
        }*/

        System.out.println("Max radius: " + rHb.t);
        System.out.print("Estimation: " + res[0]);
        System.out.println(" Node id: " + res[1]);
        //System.out.println(" Node degree: " + rHll.nodes.get(res[1]).nodes.size());
        int i = res[1];
        for(int j = 0; j < m; j++){
            if(sol[i][j] > 0)
                System.out.print(sol[i][j] + " ");
            else
                System.out.print("-" + " ");
        }
        System.out.println();
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

    static BASE64Encoder enc = new BASE64Encoder();

    public static String base64encode(String text) {
        return enc.encode(text.getBytes());
    }//base64encode

    /* Adding one single item to counters */
    public int[] add(int[] M, String item){
        String e = base64encode(item); // Encrypt it first to make it uniformed when we hash it
        int x = e.hashCode()*4711;//15487403;
        //hashes[Integer.parseInt(item)];
        //System.out.println("Encrypted: " + e + " Hashcode: " + x);
        int[] tmp;

        if(leading)
            tmp = leading(x, bits); // use the leftmost bits
        else
            tmp = trailing(x,bits); // use the rightmost x bits

        int j = tmp[0];
        int b = tmp[1];

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

    public int[] leading(int x, int bits){
        return new int[]{address(x), r(x,bits)};
    }

    public int[] trailing(int x, int bits){
        return new int[]{addressR(x), rR(x,bits)};
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

    int[] analyze(int[][] c){
        double max = 0;
        int index = -1;
        for(int i = 1; i < c.length; i++){
            double tmp = size(c[i]);

            if(tmp > max) {
                //System.out.println(tmp + " compares to " + max);
                max = tmp;
                index = i;
            }
        }

        return new int[]{(int)max, index};
    }

    /* Used to calculate the normalized harmonic mean */
    public double size(int[] M){
        int n = 0;//M.length;
        /* To prevent empty buckets from disturbing the real value too much*/
        for(int i: M)
            if(i > 0)
                n++;

        double alfa = alfa(n); // Get the alfa, i.e. a constant depending on the amount of buckets used
        double p = n; // the amount of buckets used, i.e. m in some papers

        // The formula used in the papers, alfa * p^2 * Z
        return alfa*p*p*sum(M);
    }

    /* The harmonic sum of the counters, Z := 2^-M[i] */
    public double sum(int[] M){
        double sum = 0;
        for(int i: M)
            sum+=(Math.pow(2,-i));

        // 1/Z so we can use it to multiple for our formula
        if(sum == 0)
            return 1;
        //System.out.println(1/sum);
        return 1/sum;
    }

    public double alfa(int n){
        /* Estimation or precalculated alfas*/
        switch (n){
            case(2): return 1/(2*1.42371);
            case(4): return 0.532434;
            case(8): return 0.635576;
            case(16): return 0.673;
            default:
                return 0.7213/(1+1.079/n); //Estimations
        }
    }
}




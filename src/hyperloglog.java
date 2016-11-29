import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Steven on 2016-11-24.
 */


public class hyperloglog{
    static final int b = 5, bits = 32-b;
    static int mask = 3, rmask = 1073741823;; // The amount of bits we use


    public static void main(String[] args){
        mask = 0;
        for(int i = 0; i < b;i++)
            mask = (mask << 1) + 1;
        rmask = 0;
        for(int i = 0; i < bits;i++)
            rmask = (rmask << 1) + 1;
/*
        Random r = new Random();
        int[] M = new int[(int)Math.pow(2,b)];
        for(int i = 0; i < M.length; i++)
            M[i] = Integer.MIN_VALUE;

        int[] random = new int[200000];
        for(int i = 0; i < random.length; i++){
            random[i] = r.nextInt();
        }

        hyperloglog hll = new hyperloglog();
        HashSet<Integer> set = new HashSet<>();
        for(int i = 0; i < 10000000; i++){
            int n = random[r.nextInt(200000)];
            //System.out.println("Bits: " + hll.r(n,bits) + " nr: " + n + " address: " + hll.address(n));
            //System.out.println("Binary form: " + Integer.toBinaryString(n) + " size: " + Integer.toBinaryString(n).length());
            M = hll.add(M, n+"");
            set.add(n);
        }
        System.out.println("Harmonic mean: " + hll.harmonic(M));
        System.out.println("Real distinct: " + set.size());
        */
        hyperloglog hll = new hyperloglog();
        hll.streamFile("out.actor-collaboration");



    }

    public void streamFile(String filename){
        int[] M = new int[(int)Math.pow(2,b)];
        for(int i = 0; i < M.length; i++)
            M[i] = Integer.MIN_VALUE;
        hyperloglog hll = new hyperloglog();
        HashSet<String> set = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            int i = 0;
            while((line = br.readLine()) != null){
                M = add(M, set, line.split(" "));
                //list.add(line);
                if(i++%100000 == 0)
                    System.out.println("Estimate: " + hll.harmonic(M) + " Real: " + set.size());//fm(line.split(" ")));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i: M)
            System.out.println("M size: " + i);
    }

    public int[] add(int[] M, HashSet set, String[] items){
        for(String s: items) {
            M = add(M, set,s);
        }

        return M;
    }
    public int[] add(int[] M, HashSet set, String item){
        int x = item.hashCode();
        int j = addressR(x);
        int b = rR(x,bits);
        /*if(b > M[j])
            System.out.println("New max for: " + j);*/
        set.add(x);
        M[j] = Math.max(b,M[j]);

        return M;
    }

    public int[] add(int[] M, int item){
        int j = address(item);
        M[j] = Math.max(r(item,bits),M[j]);

        return M;
    }

    public int address(int hash){
        return (hash >> (bits))&mask; // 3 = 0b11
    }

    public int r(int hash, int bits){
        hash = hash & rmask;
        //System.out.println("Hash " + hash + " bits " + bits);
        return bits - Integer.toBinaryString(hash).length() + 1;
    }

    public int addressR(int hash){
        return (hash&mask); // 3 = 0b11
    }

    public int rR(int hash, int bits){
        //hash = hash & rmask;
        hash = hash >> b;

        int i = 1;
        while(hash%2 == 0){
            hash = hash >> 1;
            i++;
        }

        return i;
    }

    public double harmonic(int[] M){
        double alfa = alpha(M);
        double p = M.length;

        return alfa*p*p*sum(M);
    }

    public double sum(int[] M){
        double sum = 0;
        for(int i: M)
            sum+=(Math.pow(2,-i));
        return 1/sum;
    }

    public double alpha(int[] M){
        /* Estimation or precalculated alfas*/
        switch (M.length){
            case(2): return 1/(2*1.42371);
            case(4): return 0.532434;
            case(8): return 0.635576;
            case(16): return 0.673;
            default:
                return 0.7213/(1+1.079/M.length); //Estimation
        }
    }

    public int[] union(int[] M, int[] N){
        for(int i = 0; i < N.length; i++){
            M[i] = Math.max(M[i], N[i]);
        }
        return M;
    }

    public void hyperBall(){

    }
}

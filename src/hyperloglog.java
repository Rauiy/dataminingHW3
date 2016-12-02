import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Steven on 2016-11-24.
 */


public class hyperloglog{
    static final int b = 2, bits = 32-b;
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
        hll.streamFile("out.com-amazon");
        /*int m = (int)Math.pow(2,b);
        for(int i=1; i < max+1; i++){
            for(int j = 0; j < m; j++){
                System.out.print(c[i][j] + " ");
            }
            System.out.println();
        }*/
        int[][] sol = hll.hyperBall(c);
        /*for(int i=1; i < max+1; i++){
            for(int j = 0; j < m; j++){
                System.out.print(sol[i][j] + " ");
            }
            System.out.println();
        }*/


    }
    static HashMap<String, node> nodes;
    static int[][] c;
    static int max = 0;
    public void streamFile(String filename){
        int m = (int)Math.pow(2,b);
       // int[] M = new int[(int)Math.pow(2,b)];
       // for(int i = 0; i < M.length; i++)
        //   M[i] = Integer.MIN_VALUE;
        hyperloglog hll = new hyperloglog();
        //HashSet<String> set = new HashSet<>();
        nodes = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;

            while((line = br.readLine()) != null){
                if(line.indexOf("%") != -1)
                    continue;
                //M = add(M, set, line.split(" "));
                //list.add(line);
                String[] n = line.split(" ");

                node n1 = nodes.get(n[0]);
                node n2 = nodes.get(n[1]);

                if(n1 == null && n2 == null){
                    n1 = new node(n[0], m);
                    n2 = new node(n[1], m);

                    nodes.put(n[0],n1);
                    nodes.put(n[1],n2);
                }
                else if( n1 == null){
                    n1 = new node(n[0], m);
                    nodes.put(n[0],n1);
                }
                else if( n2 == null){
                    n2 = new node(n[1], m);
                    nodes.put(n[1],n2);
                }

                n1.add(n2);
                //n2.add(n1);
                n1.M = hll.add(n1.M, n[1]);
                //n2.M = hll.add(n2.M, n[0]);

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

        c = new int[max+1][];
        System.out.println(max+1);
        for(node v: nodes.values()) {
            c[v.id] = v.M;
        }

        //for(int i: M)
        //    System.out.println("M size: " + i);
    }

    public int[] add(int[] M, String[] items){
        for(String s: items) {
            M = add(M,s);
        }

        return M;
    }
    public int[] add(int[] M, String item){
        int x = item.hashCode();
        int j = addressR(x);
        int b = rR(x,bits);
        /*if(b > M[j])
            System.out.println("New max for: " + j);*/
        M[j] = Math.max(b,M[j]);

        return M;
    }

    public int[] add(int[] M, int item){
        int j = address(item);
        M[j] = Math.max(r(item,bits),M[j]);

        return M;
    }

    public int findCentrality(){

        return 0;
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
        int[] a = new int[M.length];
        for(int i = 0; i < N.length; i++){
            a[i] = Math.max(M[i], N[i]);
        }
        return a;
    }

    public int[][] hyperBall(int[][] c){
        int[][] recent = c.clone();
        int  t= 0;
        boolean changed = true;

        while(changed) {
            changed = false;
            for (node v : nodes.values()) {

                int[] a = c[v.id].clone();

                for (node w : v.nodes) {

                    //System.out.println("edge"+ v.id + " " + w.id);
                    a = union(a, c[w.id]);

                    changed = !compare(a, c[v.id]);
                }
                recent[v.id] = a;
            }
            System.out.println(t + " changed: " + changed);
            /*System.out.println("~~~~~~~~~~");
            int m = (int)Math.pow(2,b);
            for(int i=1; i < max+1; i++){
                for(int j = 0; j < m; j++){
                    System.out.print(recent[i][j] + " ");
                }
                System.out.println();
            }
            System.out.println("~~~~~~~~~~");*/
            c = recent.clone();
            t++;
        }



        return recent;
    }

    boolean compare(int[] a, int[] b){
        //System.out.println("Comparing");
        for(int i = 0; i < a.length; i++) {
            //System.out.println("Comparing " + a[i] + " to " + b[i] + " " + (a[i] == b[i]));
            if (a[i] != b[i]) {
                return false;
            }
        }

        return true;
    }
}


class node{
    public node(String id, int m){
        this.id = Integer.parseInt(id);
        nodes = new ArrayList<>();
        M = new int[m];
        for(int i = 0; i < M.length; i++)
            M[i] = 0;
    }

    public void add(node n){
        nodes.add(n);
    }

    public int id;
    public int[] M;
    List<node> nodes;

}

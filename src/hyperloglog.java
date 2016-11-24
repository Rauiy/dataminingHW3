import java.util.Random;

/**
 * Created by Steven on 2016-11-24.
 */


public class hyperloglog{
    static final int b = 2, bits = 32-b, mask = 3, rmask = 1073741823;; // The amount of bits we use


    public static void main(String[] args){
       // for(int i = 0; i < b;i++)
       //     mask = (mask << 1) + 1;

        Random r = new Random();
        int[] M = new int[(int)Math.pow(2,b)];
        hyperloglog hll = new hyperloglog();
        for(int i = 0; i < 100; i++){
            int n = r.nextInt();
            System.out.println("Bits: " + hll.r(n,bits) + " nr: " + n + " address: " + hll.address(n));
            System.out.println("Binary form: " + Integer.toBinaryString(n) + " size: " + Integer.toBinaryString(n).length());
        }

    }

    public int[] add(int[] M, String item){
        int x = item.hashCode();
        int j = address(x);
        M[j] = Math.max(r(x,bits),M[j]);

        return M;
    }

    public int address(int hash){
        return (hash >> (bits))&mask; // 3 = 0b11
    }

    public int r(int hash, int bits){
        hash = hash & rmask;
        System.out.println("Hash " + hash + " bits " + bits);
        return bits - Integer.toBinaryString(hash).length() + 1;
    }

}

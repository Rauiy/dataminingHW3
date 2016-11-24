import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class flajoletmartin {

    public static void main(String[] args){
        flajoletmartin fm = new flajoletmartin();
        fm.readfile("out.actor-collaboration");
    }

    public void readfile(String filename){
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while((line = br.readLine()) != null){
                System.out.println("Estimate: " + fm(line));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int fm(String input){
        int zeros = r(input.hashCode());
        if(zeros > max)
            max = zeros;

        return estimate(max);
    }

    public int r(int h){
        int i;
        for(i = 0; h%2 == 0; i++)
            h = h>>1;
        return i;
    }

    int max = -1;

    public int estimate(int r){
        return (int) Math.pow(2, r);
    }
}
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class flajoletmartin implements Runnable{

    static int[] maxes;
    static int w = 4;
    public static void main(String[] args){

        flajoletmartin fm = new flajoletmartin();
        System.out.println("reading");
        //fm.readfile("out.actor-collaboration");
        System.out.println("done reading");



        /*
        int l = lines.size()/w;
        Thread[] ts = new Thread[w];
        for(int i = 0; i < ts.length; i++){
            int end = (i+1)*l;
            if( i+1 == ts.length)
                end = ts.length;

            ts[i] = new Thread(new flajoletmartin(lines.subList(i*l,end),i));
            ts[i].start();
        }

        try {
            for(Thread t:ts)
                t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int max = -1;
        for(int i: maxes){
            if(i>max)
                max = i;
        }
        System.out.println("Estimate: " + fm.estimate(max));*/
    }


    public flajoletmartin(){}

    public flajoletmartin(List lines, int id){
        this.lines = lines;
        this.id = id;
    }

    List<String> lines;
    int id;
    @Override
    public void run() {
        System.out.println("Worker: " + id + " starts");
        for(String s:lines){
            fm(s.split(" "));
        }
        maxes[id] = estimate(max);
        System.out.println("Worker: " + id + " done");
    }

    public void readfile(String filename){
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            //List<String> list = new ArrayList<String>();
            int i = 0;
            while((line = br.readLine()) != null){
                int tmp = fm(line.split(" "));
                //list.add(line);
                if(i++%1000 == 0)
                    System.out.println("Line: " + line + "Estimate: " + tmp);//fm(line.split(" ")));
            }
            //return list;
            //System.out.println("Estimated distinct: " + estimate(max));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return null;
    }

    public int fm(String[] input){
        for(String s: input){
            fm(s);
        }
        return estimate(max);
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
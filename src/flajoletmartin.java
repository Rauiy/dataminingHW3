
public class flajoletmartin {


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
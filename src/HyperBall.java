import java.util.Map;

/**
 * Created by steve_000 on 2016-12-02.
 */
public class HyperBall {

    public class retHb{

        public retHb(int[][] c, int t) {
            this.c = c;
            this.t = t;
        }

        public int[][] c;
        public int t;
    }

    /* Union two hyperloglog counters, i.e. return a counter with all max values */
    public int[] union(int[] M, int[] N){
        int[] a = new int[M.length];

        for(int i = 0; i < N.length; i++){
            a[i] = Math.max(M[i], N[i]);
        }
        return a;
    }

    /* The hyperball algorithm using nodes to traverse, could be substitutes with a stream based solution */
    /* With this solution we solve each node at a time, a stream based would jump a lot unless the list of edges is sorted */
    /* Stream based takes less memory, while this is faster */
    public retHb hyperBall(int[][] c, Map<?,Node> nodes){
        int[][] recent = c.clone(); // clone all values to recent
        int  t = 0;
        boolean changed = true; // If a change has occurred

        while(changed) { // while changes still occurs, i.e. until the hyperloglog (hll) counters stabilizes

            changed = false; // reset flag

            for (Node v : nodes.values()) { // for each node of all nodes

                int[] a = c[v.id].clone(); // copy current node hll counter to update

                for (Node w : v.nodes) { // for each neighbour of our node
                    a = union(a, c[w.id]);  // unify current node hll counter with neighbours hll counter

                    // look for change
                    changed = !compare(a, c[v.id]);
                }
                recent[v.id] = a; // temporarily save the new hll counter
            }
            System.out.println(t + " changed: " + changed);

            c = recent.clone(); // update all hll counters

            t++; // increment radius counter
        }
        t--;
        return new retHb(c, t);
    }

    /* Used to compare the value of two arrays, not used anymore, built int equals does the same*/
    boolean compare(int[] a, int[] b){
        //return a.equals(b);

        if(a.length != b.length)
            return false;
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

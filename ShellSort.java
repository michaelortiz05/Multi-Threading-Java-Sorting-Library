import java.util.concurrent.*;
import java.util.*;
import java.io.FileWriter;  
import java.io.IOException;

public class ShellSort <T extends Comparable<? super T>> extends Thread {

    private List<T> arr;

    public ShellSort(List<T> arr) { 
        this.arr = arr; 
    }

    @Override
    public void run() { concurrentShellSortHelper();}

    private void concurrentShellSortHelper() {
        int n = arr.size();
        for (int gap = n/2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                T key = arr.get(i);
                int j = i;  
                while (j >= gap && arr.get(j - gap).compareTo(key) > 0) {
                    arr.set(j, arr.get(j - gap));
                    j -= gap;
                }
                arr.set(j, key);
            }
        }
    }

    // Sequential generic shell sort implementation
    public static <T extends Comparable<? super T>> void shellSort(List<T> list) {
        int n = list.size();
        for (int gap = n/2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                T key = list.get(i);
                int j = i;
                while (j >= gap && list.get(j - gap).compareTo(key) > 0) {
                    list.set(j, list.get(j - gap));
                    j -= gap;
                }
                list.set(j, key);
            }
        }
    }

    public static <T extends Comparable<? super T>> void concurrentShellSort(List<T> list) throws InterruptedException{
        final int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(numThreads); 
        ArrayList<List<T>> partitions = new ArrayList<List<T>>(numThreads);
        int partitionSize = (list.size() + numThreads - 1) / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int start = i * partitionSize;
            int end = Math.min(start + partitionSize, list.size());
            partitions.add(new ArrayList<T>(list.subList(start, end)));
            pool.execute(new ShellSort(partitions.get(i)));
        }

        pool.shutdown();

        try {
            if (!pool.awaitTermination(60000, TimeUnit.SECONDS)) 
                pool.shutdownNow();     
        } catch (InterruptedException e) {
            pool.shutdownNow();
        } finally {
            ShellSort.merge(list, partitions);
        }
    }

    // Repurposed from Interviewdojo.com
    public static <T extends Comparable<? super T>> void merge(List<T> list, ArrayList<List<T>> partitions) {
        PriorityQueue<Node> pq = new PriorityQueue<Node>();

        for (List<T> p : partitions) //List<T> p : partitions
            pq.add(new Node(p, 0));
        
        int i = 0;

        while (!pq.isEmpty()) {
            Node n = pq.poll();
            list.set(i, (T) n.arr.get(n.index));
            i++;

            if (n.hasNext()) {
                pq.add(new Node(n.arr, n.index + 1));
            }
        }
    }

    public static void testShellSort(int arraySize) throws IOException, InterruptedException{
        ArrayList<Integer> ascendingArray = new ArrayList<Integer>();
        ArrayList<Integer> descendingArray = new ArrayList<Integer>();
        ArrayList<Integer> shuffledArray = new ArrayList<Integer>();

        FileWriter sequentialWriter = new FileWriter("sequential-shell-sort-results.txt");
        FileWriter concurrentWriter = new FileWriter("concurrent-shell-sort-results.txt");
        sequentialWriter.write("size\tascending(MS)\tdescending(MS)\tshuffled(MS)\n");
        concurrentWriter.write("size\tascending(MS)\tdescending(MS)\tshuffled(MS)\n");

        // Record start and end times
        long startTime;
        long endTime;
        long ascendingTime;
        long descendingTime;
        long shuffledTime;

        // Record whether the sorts were done correctly
        boolean isSortedAscending;
        boolean isSortedDescending;
        boolean isSortedShuffled;
        
        Random rand = new Random();
        System.out.println("testing shell sort...");

        for (int iter = 0; iter < 10; iter++) {
            for (int i = 0; i < arraySize * (iter + 1); i++) 
                ascendingArray.add(i);
        
            for (int i = 0; i < arraySize * (iter + 1); i ++) 
                shuffledArray.add(rand.nextInt(arraySize * (iter + 1)));
            
            
            for (int i = 0; i < arraySize * (iter + 1); i++) 
                descendingArray.add(arraySize * (iter + 1) - i - 1);

            System.out.println("Iteration " + iter + ": testing on input of size " + arraySize * (iter + 1));

            // Test sequential
            ArrayList arrayCopy = (ArrayList) ascendingArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.shellSort(arrayCopy);
            endTime = System.currentTimeMillis();
            ascendingTime = endTime - startTime;
            isSortedAscending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) descendingArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.shellSort(arrayCopy);
            endTime = System.currentTimeMillis();
            descendingTime = endTime - startTime;
            isSortedDescending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) shuffledArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.shellSort(arrayCopy);
            endTime = System.currentTimeMillis();
            shuffledTime = endTime - startTime;
            isSortedShuffled = Sorter.isSorted(arrayCopy);

            sequentialWriter.write(arraySize * (iter + 1) + "\t" + ascendingTime + "\t" + descendingTime + "\t" + shuffledTime + "\n");
            System.out.println("---sequential sorted correctly: " + (isSortedAscending && isSortedDescending && isSortedShuffled));

            // Test concurrent
            arrayCopy = (ArrayList) ascendingArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.concurrentShellSort(arrayCopy);
            endTime = System.currentTimeMillis();
            ascendingTime = endTime - startTime;
            isSortedAscending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) descendingArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.concurrentShellSort(arrayCopy);
            endTime = System.currentTimeMillis();
            descendingTime = endTime - startTime;
            isSortedDescending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) shuffledArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.concurrentShellSort(arrayCopy);
            endTime = System.currentTimeMillis();
            shuffledTime = endTime - startTime;
            isSortedShuffled = Sorter.isSorted(arrayCopy);

            concurrentWriter.write(arraySize * (iter + 1) + "\t" + ascendingTime + "\t" + descendingTime + "\t" + shuffledTime + "\n");
            System.out.println("---concurrent sorted correctly: " + (isSortedAscending && isSortedDescending && isSortedShuffled) + "\n");

            ascendingArray.clear();
            descendingArray.clear();
            shuffledArray.clear();
            

        }    
        sequentialWriter.close();
        concurrentWriter.close();
        System.out.println("Results written to files.");
    }
}

// Source: Interviewdojo.com
class Node <T extends Comparable<? super T>> implements Comparable<Node> { //T extends Comparable<? super T>
    List<T> arr;
    int index;

    public Node(List<T> arr, int index) {
        this.arr = arr;
        this.index = index;
    }

    public boolean hasNext() {
        return this.index < this.arr.size() - 1;
    }

    @Override
    public int compareTo(Node o) {
        return arr.get(index).compareTo((T) o.arr.get(o.index));//arr[index] - o.arr[o.index];
    }
}

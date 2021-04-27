import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.FileWriter;  
import java.io.IOException;

public class QuickSort<T extends Comparable<? super T>> extends Thread{
    
    private int begin, end;
    private List<T> arr;
    
    public QuickSort(List<T> arr, int l, int r){
        this.arr = arr;
        this.begin = l;
        this.end = r;
    }
    
    @Override
    public void run(){
        quickSort(arr, begin, end);
    }
    
    
    /*
    this is a simple sequential quick sort algorithm made for java created by baeldung
    @https://www.baeldung.com/java-quicksort
    */
    public static <T extends Comparable<? super T>> void quickSort(List<T> arr, int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(arr, begin, end);
    
            quickSort(arr, begin, partitionIndex-1);
            quickSort(arr, partitionIndex+1, end);
        }
    }

    private static <T extends Comparable<? super T>> int partition(List<T> arr, int begin, int end) {
        T pivot = arr.get(end);
        int i = (begin-1);
    
        for (int j = begin; j < end; j++) {
            if (arr.get(j).compareTo(pivot) <= 0) {
                i++;
    
                T swapTemp = arr.get(i);
                arr.set(i, arr.get(j));
                arr.set(j, swapTemp);
            }
        }
    
        T swapTemp = arr.get(i+1);
        arr.set(i+1, arr.get(end));
        arr.set(end, swapTemp);
    
        return i+1;
    }



    static <T extends Comparable<? super T>> void concurrentSort(List<T> arr) throws InterruptedException{
        QuickSort[] threads = new QuickSort[4];
        int threadSize = arr.size() / 4;
        for (int i = 0; i < 4; i++){
            int low = i * threadSize;
            int high = (i +1)* threadSize - 1;
            if (high > arr.size())
                high = arr.size();

            threads[i] = new QuickSort(arr, low, high);
            threads[i].start();
        }

        for (int i = 0; i < 4; i++){
            threads[i].join();
        }

        int low = 0;
        int mid = threadSize-1;
        int high = 2 * threadSize - 1;
        merge(arr, low, mid, high);

        low = 2 *threadSize;
        mid = 3 * threadSize-1;
        high = arr.size() - 1;
        merge(arr, low, mid, high);

        merge(arr, 0, low-1, high);

        
    }

    static <T extends Comparable<? super T>> void merge(List<T> arr, int l, int m, int r)
    {
        // Find sizes of two subarrays to be merged
        int n1 = m - l + 1;
        int n2 = r - m;
 
        /* Create temp arrays */
        ArrayList<T> L =  new ArrayList<T>();
        ArrayList<T> R =  new ArrayList<T>();
 
        /*Copy data to temp arrays*/
        for (int i = 0; i < n1; ++i)
            L.add(i, arr.get(l+i));
        for (int j = 0; j < n2; ++j)
            R.add(j, arr.get(m + 1 + j));
 
        /* Merge the temp arrays */
 
        // Initial indexes of first and second subarrays
        int i = 0, j = 0;
 
        // Initial index of merged subarry array
        int k = l;
        while (i < n1 && j < n2) {
            if (L.get(i).compareTo(R.get(j)) <= 0) {
                arr.set(k, L.get(i));
                i++;
            }
            else {
                arr.set(k, R.get(j));
                j++;
            }
            k++;
        }
 
        /* Copy remaining elements of L[] if any */
        while (i < n1) {
            arr.set(k, L.get(i));
            i++;
            k++;
        }
 
        /* Copy remaining elements of R[] if any */
        while (j < n2) {
            arr.set(k, R.get(j));
            j++;
            k++;
        }
    }
    public static void testQuickSort(int arraySize) throws IOException, InterruptedException{
        ArrayList<Integer> ascendingArray = new ArrayList<Integer>();
        ArrayList<Integer> descendingArray = new ArrayList<Integer>();
        ArrayList<Integer> shuffledArray = new ArrayList<Integer>();

        FileWriter sequentialWriter = new FileWriter("sequential-quick-sort-results.txt");
        FileWriter concurrentWriter = new FileWriter("concurrent-quick-sort-results.txt");
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
        System.out.println("testing quick sort...");

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
            //Sorter.quickSort(arrayCopy);
            endTime = System.currentTimeMillis();
            ascendingTime = endTime - startTime;
            isSortedAscending = true;

            arrayCopy = (ArrayList) descendingArray.clone();
            startTime = System.currentTimeMillis();
            //Sorter.quickSort(arrayCopy);
            endTime = System.currentTimeMillis();
            descendingTime = endTime - startTime;
            isSortedDescending = true;

            arrayCopy = (ArrayList) shuffledArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.quickSort(arrayCopy);
            endTime = System.currentTimeMillis();
            shuffledTime = endTime - startTime;
            isSortedShuffled = Sorter.isSorted(arrayCopy);

            sequentialWriter.write(arraySize * (iter + 1) + "\t" + ascendingTime + "\t" + descendingTime + "\t" + shuffledTime + "\n");
            System.out.println("---sequential sorted correctly: " + (isSortedAscending && isSortedDescending && isSortedShuffled));

            // Test concurrent
            arrayCopy = (ArrayList) ascendingArray.clone();
            startTime = System.currentTimeMillis();
            //Sorter.concurrentQuickSort(arrayCopy);
            endTime = System.currentTimeMillis();
            ascendingTime = endTime - startTime;
            isSortedAscending = true;

            arrayCopy = (ArrayList) descendingArray.clone();
            startTime = System.currentTimeMillis();
            //Sorter.concurrentQuickSort(arrayCopy);
            endTime = System.currentTimeMillis();
            descendingTime = endTime - startTime;
            isSortedDescending = true;

            arrayCopy = (ArrayList) shuffledArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.concurrentQuickSort(arrayCopy);
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

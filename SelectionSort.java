import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Random;
import java.io.FileWriter;  
import java.io.IOException;

class SelectionSort implements Callable<ArrayList<Integer>>
{
    private List<Integer> list;
    private int low;
    private int high;

    public SelectionSort(List<Integer> list, int low, int high) {
        this.list = list;
        this.low = low;
        this.high = high;
    }

    public ArrayList<Integer> call() {
        ArrayList<Integer> values = new ArrayList<>();
        // Find all values in the range
        for (int v: list) {
            if (v >= low && v < high) values.add(v);
        }

        // Sort the values
        selectionSort(values);
        return values;
    }

    public static void selectionSort(List<Integer> list)
    {
        int n = list.size();
        for (int i = 0; i < n - 1; i++)
        {
            int min = i;
            
            for (int j = i+1; j < n; j++)
                if (list.get(j) < list.get(min))
                    min = j;
                    
            int temp = list.get(min);
            list.set(min, list.get(i));
            list.set(i, temp);
        }
    }

    public static void concurrentSelectionSort(List<Integer> list) {
        int n = list.size();
        int k = 4;  // Number of threads

        // Find range of values
        int minValue = list.get(0);
        int maxValue = list.get(0);
        for (int v: list) {
            if (v < minValue) minValue = v;
            if (v > maxValue) maxValue = v;
        }

        // Divide subranges among threads
        ExecutorService executor = Executors.newFixedThreadPool(k);
        List<Future<ArrayList<Integer>>> futures = new ArrayList<>();
        int threadRange = (maxValue - minValue) / k;
        for (int i = 0; i < k; i++) {
            // Remainder values designated to last thread
            SelectionSort task = new SelectionSort(list, minValue + i * threadRange, (i == k - 1) ? maxValue + 1 : minValue + (i + 1) * threadRange);
            futures.add(executor.submit(task));
        }
        executor.shutdown();

        // Get result of each thread
        List<ArrayList<Integer>> sortedSubRanges = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            try {
                sortedSubRanges.add(futures.get(i).get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Stitch sorted subarrays into single array
        List<Integer> sortedList = sortedSubRanges.get(0);
        for (int i = 1; i < k; i++) {
            sortedList.addAll(sortedSubRanges.get(i));
        }
        // Override original array
        for (int i = 0; i < n; i++) {
            list.set(i, sortedList.get(i));
        }
    }

    public static void testSelectionSort(int arraySize) throws IOException, InterruptedException{
        ArrayList<Integer> ascendingArray = new ArrayList<Integer>();
        ArrayList<Integer> descendingArray = new ArrayList<Integer>();
        ArrayList<Integer> shuffledArray = new ArrayList<Integer>();

        FileWriter sequentialWriter = new FileWriter("sequential-selection-sort-results.txt");
        FileWriter concurrentWriter = new FileWriter("concurrent-selection-sort-results.txt");
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
        System.out.println("testing selection sort...");

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
            //Sorter.selectionSort(arrayCopy);
            endTime = System.currentTimeMillis();
            ascendingTime = endTime - startTime;
            isSortedAscending = true;

            arrayCopy = (ArrayList) descendingArray.clone();
            startTime = System.currentTimeMillis();
            //Sorter.selectionSort(arrayCopy);
            endTime = System.currentTimeMillis();
            descendingTime = endTime - startTime;
            isSortedDescending = true;

            arrayCopy = (ArrayList) shuffledArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.selectionSort(arrayCopy);
            endTime = System.currentTimeMillis();
            shuffledTime = endTime - startTime;
            isSortedShuffled = Sorter.isSorted(arrayCopy);

            sequentialWriter.write(arraySize * (iter + 1) + "\t" + ascendingTime + "\t" + descendingTime + "\t" + shuffledTime + "\n");
            System.out.println("---sequential sorted correctly: " + (isSortedAscending && isSortedDescending && isSortedShuffled));

            // Test concurrent
            arrayCopy = (ArrayList) ascendingArray.clone();
            startTime = System.currentTimeMillis();
            //Sorter.concurrentSelectionSort(arrayCopy);
            endTime = System.currentTimeMillis();
            ascendingTime = endTime - startTime;
            isSortedAscending = true;

            arrayCopy = (ArrayList) descendingArray.clone();
            startTime = System.currentTimeMillis();
            //Sorter.concurrentSelectionSort(arrayCopy);
            endTime = System.currentTimeMillis();
            descendingTime = endTime - startTime;
            isSortedDescending = true;

            arrayCopy = (ArrayList) shuffledArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.concurrentSelectionSort(arrayCopy);
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
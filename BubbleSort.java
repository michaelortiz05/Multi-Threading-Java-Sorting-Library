import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Random;
import java.io.FileWriter;  
import java.io.IOException;

class Sort <T extends Comparable<? super T>> implements Runnable
{   List<T> arr;
    int mid;
    boolean direction;
    public Sort(List<T> arr, int mid, boolean direction)
    {
        this.arr = arr;
        this.mid = mid;
        this.direction = direction;
    }

    @Override
    public void run()
    {
        BubbleSort.directionalBubbleSort(arr, mid, direction);
    }
}

public class BubbleSort 
{ 
    public static <T extends Comparable<? super T>> void bubbleSort(List<T> arr) 
    { 
        //System.out.println(Arrays.toString(arr));
        int n = arr.size(); 
        for (int i = 0; i < n-1; i++) 
            for (int j = 0; j < n-i-1; j++) 
                if (arr.get(j).compareTo(arr.get(j+1)) > 0) 
                { 
                    
                    T temp = arr.get(j); 
                    arr.set(j, arr.get(j+1));
                    arr.set(j+1, temp);
                } 
                //System.out.println(Arrays.toString(arr));
    }

    static <T extends Comparable<? super T>> void directionalBubbleSort(List<T> arr, int mid, boolean direction) 
    { 
        int n = arr.size();
        if(direction){
            for (int i = 0; i < mid; i++) 
            for (int j = 0; j < n-i-1; j++) 
                if (arr.get(j).compareTo(arr.get(j+1)) > 0) 
                   { 
                   
                        T temp = arr.get(j); 
                        arr.set(j, arr.get(j+1));
                        arr.set(j+1, temp);
                  } 
            
        }
        else{
            for (int i = 0; i < mid; i++) 
            for (int j = 0; j < n-i-1; j++) 
                if (arr.get(j).compareTo(arr.get(j+1)) < 0) 
                   { 
                   
                    T temp = arr.get(j); 
                    arr.set(j, arr.get(j+1));
                    arr.set(j+1, temp);
                  } 

        }
    }

    
    

    public static <T extends Comparable<? super T>> void concurrentBubbleSort(List<T> arr)
    {   
        //System.out.println(Arrays.toString(arr));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        int length = arr.size();
        int mid = length / 2;
        List<T> arr1 = arr.subList(0, length);//List<T> arr1 = arr.clone();
        List<T> arr2 = arr.subList(0, length);//List<T> arr2 = arr.clone();
        executor.submit(new Sort(arr1, mid, true));
        executor.submit(new Sort(arr2, length - mid, false));
        
        //System.out.println(Arrays.toString(arr1));
        //System.out.println(Arrays.toString(arr2));

        //Collections.reverse(Arrays.asList(arr2));
        //System.out.println(Arrays.toString(arr2));
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);


        try {
            if (!executor.awaitTermination(60000, TimeUnit.SECONDS)) 
                executor.shutdownNow();     
        } catch (InterruptedException e) {
            executor.shutdownNow();
        } finally {
            for(int i = 0; i < length - mid; i++)
                arr.set(i,arr2.get(length - i - 1));

            for(int i = length - mid; i < length; i++)
                arr.set(i,arr1.get(i));
        }
        

        //System.out.println(Arrays.toString(arr));

    }

    public static void testBubbleSort(int arraySize) throws IOException, InterruptedException{
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
        System.out.println("testing bubble sort...");

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
            Sorter.bubbleSort(arrayCopy);
            endTime = System.currentTimeMillis();
            ascendingTime = endTime - startTime;
            isSortedAscending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) descendingArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.bubbleSort(arrayCopy);
            endTime = System.currentTimeMillis();
            descendingTime = endTime - startTime;
            isSortedDescending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) shuffledArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.bubbleSort(arrayCopy);
            endTime = System.currentTimeMillis();
            shuffledTime = endTime - startTime;
            isSortedShuffled = Sorter.isSorted(arrayCopy);

            sequentialWriter.write(arraySize * (iter + 1) + "\t" + ascendingTime + "\t" + descendingTime + "\t" + shuffledTime + "\n");
            System.out.println("---sequential sorted correctly: " + (isSortedAscending && isSortedDescending && isSortedShuffled));

            // Test concurrent
            arrayCopy = (ArrayList) ascendingArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.concurrentBubbleSort(arrayCopy);
            endTime = System.currentTimeMillis();
            ascendingTime = endTime - startTime;
            isSortedAscending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) descendingArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.concurrentBubbleSort(arrayCopy);
            endTime = System.currentTimeMillis();
            descendingTime = endTime - startTime;
            isSortedDescending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) shuffledArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.concurrentBubbleSort(arrayCopy);
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
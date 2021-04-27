import java.io.*;
import java.util.*;
import java.lang.*;
import java.lang.Runtime;
import java.lang.reflect.Array;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class Value
{
    public Integer value;
    public ReentrantLock lock;

    Value ()
    {
        this.value = 0;
        this.lock = new ReentrantLock();
    }

    public String toString()
    {
        return "(" + this.value + ")";
    }
}

public class BucketSort extends Thread
{
    private Integer start, end;
    private static Integer min, max;
    private static List<Integer> array;
    private static Value [] bucket;

    BucketSort ()
    {
        this.min = Integer.MAX_VALUE;
        this.max = Integer.MIN_VALUE;
    }

    BucketSort (Integer start, Integer end)
    {   
        this.start = start;
        this.end = end;
        // System.out.println("start: " + this.start);
        // System.out.println("finish: " + this.end);
    }

    public void run()
    {
        for (int i = this.start; i < this.end; i++)
        {
            int k = this.array.get(i) - this.min;
            
            this.bucket[k].lock.lock();

            try
            {
                this.bucket[k].value++;
            }
            finally
            {
                this.bucket[k].lock.unlock();
            }
        }
    }

    public void maxmin()
    {
        for (int i = 0; i < this.array.size(); i++)
        {
            if (this.array.get(i) > this.max)
            {
                this.max = this.array.get(i);
            }

            if (this.array.get(i) < this.min)
            {
                this.min = this.array.get(i);                
            }
        }
    }

    public void sortSequential(List<Integer> arr)
    {
        this.array = arr;
        maxmin();

        int bucketSize = this.max - this.min + 1;
        
        this.bucket = new Value[bucketSize];

        for (int i = 0; i < bucketSize; i++)
        {
            this.bucket[i] = new Value();
        }

        for (int i = 0; i < this.array.size(); i++)
        {
            int k = this.array.get(i) - this.min;
            this.bucket[k].value++;
        }

        int j = 0;
        
        for (int i = 0; i < this.bucket.length; i++)
        {
            while (this.bucket[i].value > 0)
            {
                this.bucket[i].value--;
                this.array.set(j, i + min);
                j++;
            }
        }
    }

    public void sort(List<Integer> arr) throws InterruptedException
    {
        this.array = arr;
        maxmin();

        int bucketSize = this.max - this.min + 1;

        this.bucket = new Value[bucketSize];

        for (int i = 0; i < bucketSize; i++)
        {
            this.bucket[i] = new Value();
        }
        
        int numThreads = Runtime.getRuntime().availableProcessors();
        BucketSort [] threads = null;
        int begin = 0;
        int finish = 0;
        
        if (this.array.size() <= numThreads)
        {
            threads = new BucketSort[this.array.size()];
            for (int i = 0; i < threads.length; i++)
            {
                threads[i] = new BucketSort(i, i + 1);
                threads[i].start();
            }
        }
        else
        {
            threads = new BucketSort[numThreads];
            int partition = this.array.size() / numThreads;

            for (int i = 0; i < threads.length; i++)
            {
                finish = (i < threads.length - 1) ? finish + partition : this.array.size();
                
                threads[i] = new BucketSort(begin, finish);
                threads[i].start();
                begin = finish;
            }
        }

        for (BucketSort thread: threads)
        {
            thread.join();
        }

        int j = 0;

        for (int i = 0; i < this.bucket.length; i++)
        {
            while (this.bucket[i].value > 0)
            {
                bucket[i].value--;
                this.array.set(j, i + min);
                j++;
            }
        }
    }

    public static void testBucketSort(int arraySize) throws IOException, InterruptedException{
        ArrayList<Integer> ascendingArray = new ArrayList<Integer>();
        ArrayList<Integer> descendingArray = new ArrayList<Integer>();
        ArrayList<Integer> shuffledArray = new ArrayList<Integer>();

        FileWriter sequentialWriter = new FileWriter("sequential-bucket-sort-results.txt");
        FileWriter concurrentWriter = new FileWriter("concurrent-bucket-sort-results.txt");
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
        System.out.println("testing bucket sort...");

        for (int iter = 0; iter < 10; iter++) {
            for (int i = 0; i < arraySize * (iter + 1); i++) 
                ascendingArray.add(i);
        
            for (int i = 0; i < arraySize * (iter + 1); i ++) 
                shuffledArray.add(rand.nextInt(arraySize * (iter + 1)));
            
            
            for (int i = 0; i < arraySize * (iter + 1); i++) 
                descendingArray.add(arraySize * (iter + 1) - i - 1);

            System.out.println("Iteration " + iter + ": testing on input of size " + arraySize * (iter + 1));

            // Test sequential
            ArrayList<Integer> arrayCopy = (ArrayList) ascendingArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.bucketSort(arrayCopy);
            endTime = System.currentTimeMillis();
            ascendingTime = endTime - startTime;
            isSortedAscending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) descendingArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.bucketSort(arrayCopy);
            endTime = System.currentTimeMillis();
            descendingTime = endTime - startTime;
            isSortedDescending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) shuffledArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.bucketSort(arrayCopy);
            endTime = System.currentTimeMillis();
            shuffledTime = endTime - startTime;
            isSortedShuffled = Sorter.isSorted(arrayCopy);

            sequentialWriter.write(arraySize * (iter + 1) + "\t" + ascendingTime + "\t" + descendingTime + "\t" + shuffledTime + "\n");
            System.out.println("---sequential sorted correctly: " + (isSortedAscending && isSortedDescending && isSortedShuffled));

            // Test concurrent
            arrayCopy = (ArrayList) ascendingArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.concurrentBucketSort(arrayCopy);
            endTime = System.currentTimeMillis();
            ascendingTime = endTime - startTime;
            isSortedAscending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) descendingArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.concurrentBucketSort(arrayCopy);
            endTime = System.currentTimeMillis();
            descendingTime = endTime - startTime;
            isSortedDescending = Sorter.isSorted(arrayCopy);

            arrayCopy = (ArrayList) shuffledArray.clone();
            startTime = System.currentTimeMillis();
            Sorter.concurrentBucketSort(arrayCopy);
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

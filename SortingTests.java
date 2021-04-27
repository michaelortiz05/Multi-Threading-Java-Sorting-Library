import java.util.*; 
import java.io.IOException;

public class SortingTests {
    public static void main ( String[] args ) throws IOException, InterruptedException{

        // Create data to be sorted
        int arraySize = (int)500000;  
        int algoChoice = 0;
        Scanner in = new Scanner(System.in);

        do {
            System.out.println("Enter list size (default 50,000)");
            arraySize = in.nextInt();
        } while (arraySize < 0);
        

        System.out.println("Comparison of execution time for sequential and multi-threaded sorting algorithms\n");

        do {
            System.out.println("Select a sorting algorithm to test:\n1. Merge Sort\n2. Quick Sort\n3. Bucket Sort\n4. Shell Sort\n5. Selection Sort");
            algoChoice = in.nextInt();

            switch (algoChoice) {
                case 1:
                    MergeSort.testMergeSort(arraySize);
                    break;
                case 2:
                    QuickSort.testQuickSort(arraySize);
                    break;
                case 3:
                    BucketSort.testBucketSort(arraySize);
                    break;
                case 4:
                    ShellSort.testShellSort(arraySize);
                    break;
                case 5:
                    SelectionSort.testSelectionSort(arraySize);
                    break;
                default:
                    System.out.println("Wrong input");
            }

        } while (algoChoice <= 0 || algoChoice > 5);
        in.close();
    }
}

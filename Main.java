import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class Main {

    // Hızlı Sıralama (Quicksort) Algoritması
    public static class QuickSortTask<T extends Comparable<T>> extends RecursiveAction {
        private T[] array;
        private int left;
        private int right;
        private final int THRESHOLD;

        public QuickSortTask(T[] array, int left, int right, int threshold) {
            this.array = array;
            this.left = left;
            this.right = right;
            this.THRESHOLD = threshold;
        }

        @Override
        protected void compute() {
            if (right - left < THRESHOLD) {
                insertionSort(array, left, right);
            } else {
                if (left < right) {
                    int pivotIndex = partition(array, left, right);
                    QuickSortTask<T> leftTask = new QuickSortTask<>(array, left, pivotIndex - 1, THRESHOLD);
                    QuickSortTask<T> rightTask = new QuickSortTask<>(array, pivotIndex + 1, right, THRESHOLD);
                    invokeAll(leftTask, rightTask);
                }
            }
        }

        private int partition(T[] array, int left, int right) {
            T pivot = array[right];
            int i = left - 1;
            for (int j = left; j < right; j++) {
                if (array[j].compareTo(pivot) < 0) {
                    i++;
                    swap(array, i, j);
                }
            }
            swap(array, i + 1, right);
            return i + 1;
        }

        private void swap(T[] array, int i, int j) {
            T temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }

        private void insertionSort(T[] array, int left, int right) {
            for (int i = left + 1; i <= right; i++) {
                T key = array[i];
                int j = i - 1;
                while (j >= left && array[j].compareTo(key) > 0) {
                    array[j + 1] = array[j];
                    j--;
                }
                array[j + 1] = key;
            }
        }
    }

    // Birleştirme Sıralaması (Merge Sort) Algoritması
    public static class MergeSortTask<T extends Comparable<T>> extends RecursiveAction {
        private T[] array;
        private final int THRESHOLD;

        public MergeSortTask(T[] array, int threshold) {
            this.array = array;
            this.THRESHOLD = threshold;
        }

        @Override
        protected void compute() {
            if (array.length < THRESHOLD) {
                insertionSort(array);
            } else {
                if (array.length > 1) {
                    int mid = array.length / 2;
                    T[] leftArray = Arrays.copyOfRange(array, 0, mid);
                    T[] rightArray = Arrays.copyOfRange(array, mid, array.length);

                    MergeSortTask<T> leftTask = new MergeSortTask<>(leftArray, THRESHOLD);
                    MergeSortTask<T> rightTask = new MergeSortTask<>(rightArray, THRESHOLD);

                    invokeAll(leftTask, rightTask);

                    merge(array, leftArray, rightArray);
                }
            }
        }

        private void insertionSort(T[] array) {
            for (int i = 1; i < array.length; i++) {
                T key = array[i];
                int j = i - 1;
                while (j >= 0 && array[j].compareTo(key) > 0) {
                    array[j + 1] = array[j];
                    j--;
                }
                array[j + 1] = key;
            }
        }

        private void merge(T[] array, T[] leftArray, T[] rightArray) {
            int leftLength = leftArray.length;
            int rightLength = rightArray.length;
            int i = 0, j = 0, k = 0;
            while (i < leftLength && j < rightLength) {
                if (leftArray[i].compareTo(rightArray[j]) <= 0) {
                    array[k++] = leftArray[i++];
                } else {
                    array[k++] = rightArray[j++];
                }
            }
            while (i < leftLength) {
                array[k++] = leftArray[i++];
            }
            while (j < rightLength) {
                array[k++] = rightArray[j++];
            }
        }
    }

    // SumTask sınıfı
    public static class SumTask extends RecursiveTask<Integer> {
        private int[] array;
        private int low;
        private int high;
        private final int THRESHOLD;

        public SumTask(int[] array, int low, int high, int threshold) {
            this.array = array;
            this.low = low;
            this.high = high;
            this.THRESHOLD = threshold;
        }

        @Override
        protected Integer compute() {
            if (high - low < THRESHOLD) {
                int sum = 0;
                for (int i = low; i <= high; i++) {
                    sum += array[i];
                }
                return sum;
            } else {
                int mid = low + (high - low) / 2;
                SumTask left = new SumTask(array, low, mid, THRESHOLD);
                SumTask right = new SumTask(array, mid + 1, high, THRESHOLD);
                left.fork();
                int rightResult = right.compute();
                int leftResult = left.join();
                return leftResult + rightResult;
            }
        }
    }

    public static void main(String[] args) {
        Integer[] testArray = {5, 3, 8, 1, 4, 7, 2, 6};
        int[] primitiveArray = Arrays.stream(testArray).mapToInt(Integer::intValue).toArray();

    
        // Hızlı Sıralama (Quicksort) kullanarak sıralama
        QuickSortTask<Integer> quickSortTask = new QuickSortTask<>(testArray, 0, testArray.length - 1, 100);
        quickSortTask.invoke();
        System.out.println("QuickSort Result: " + Arrays.toString(testArray));
    
        // Birleştirme Sıralaması (Mergesort) kullanarak sıralama
        MergeSortTask<Integer> mergeSortTask = new MergeSortTask<>(testArray, 100);
        mergeSortTask.invoke();
        System.out.println("MergeSort Result: " + Arrays.toString(testArray));

        SumTask sumTask = new SumTask(primitiveArray, 0, primitiveArray.length - 1, 100);
        ForkJoinPool pool = new ForkJoinPool();
        int sum = pool.invoke(sumTask);

    System.out.println("Toplam: " + sum);
    }
    
}

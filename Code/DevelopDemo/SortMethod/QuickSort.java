package DevelopDemo.SortMethod;

/**
 * 快速排序
 */
public class QuickSort {

    /**
     * 快速排序方法，使用到了递归的方式
     * 
     * @param array
     * @param start
     * @param end
     * @return
     */
    public static int[] quickSort(int[] array, int start, int end) {
        if (array.length < 1 || start < 0 || end >= array.length || start > end) {
            return null;
        }
        // 找到基准
        int smallIndex = partition(array, start, end);
        if (smallIndex > start) {
            quickSort(array, start, smallIndex - 1);
        }
        if (smallIndex < end) {
            quickSort(array, smallIndex + 1, end);
        }
        return array;
    }

    /**
     * 快速排序算法-分区操作（partition）
     * 
     * @param array
     * @param start
     * @param end
     * @return
     */
    private static int partition(int[] array, int start, int end) {
        double random = Math.random();
        int pivot = (int) (start + random * (end - start + 1));
        System.out.println("start = " + start + ", end = " + end + ", random = " + random + ", pivot = " + pivot);

        int smallIndex = start - 1;
        swap(array, pivot, end);

        for (int i = start; i <= end; i++) {
            // System.out.print("array[i] = " + array[i]);
            // System.out.println(" array[end] = " + array[end]);
            if (array[i] <= array[end]) {
                smallIndex++;
                if (i > smallIndex) {
                    swap(array, i, smallIndex);
                }
            }
        }
        return smallIndex;
    }

    /**
     * 交换数组内两个元素
     * 
     * @param array
     * @param i
     * @param j
     */
    private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;

        System.out.print("tempArraySort = ");
        for (int var : array) {
            System.out.print(var + ",");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int[] array = { 9, 2, 5, 1, 3, 4, 8, 0, 7, 6 };
        System.out.println("array = 9, 2, 5, 1, 3, 4, 8, 0, 7, 6");

        int[] arraySort = QuickSort.quickSort(array, 0, array.length - 1);
        // System.out.print("arraySort = ");
        // for (int var : arraySort) {
        // System.out.print(var + ",");
        // }

    }
}
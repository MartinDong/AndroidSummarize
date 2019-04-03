package DevelopDemo.SortMethod;

/**
 * 选择排序
 */
public class SelectSort {

    public static void selectSort(int[] array) {
        for (int i = 0; i < array.length; i++) {
            // 需要一个变量记录最小值的下标，
            int minPos = i;

            for (int j = i + 1; j < array.length; j++) {
                // 两两比较，记录最小值的下标
                if (array[j] < array[minPos]) {
                    minPos = j;
                }

            }

            int temp = array[i];
            array[i] = array[minPos];
            array[minPos] = temp;
            System.out.println("minPos===" + minPos);
        }
    }

    public static void main(String[] args) {
        int[] array = { 7, 3, 1, 4, 8, 1, 5, 7, 2 };

        SelectSort.selectSort(array);

        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }

        System.out.println();

    }

}
package DevelopDemo.SortMethod;

import java.util.Arrays;

/**
 * 合并数组
 */
public class MergeArray {

    public static int[] merge(int[] arrayA, int[] arrayB) {
        // TODO Check arrayA and arrayB is ordered

        int a_len = arrayA.length;
        int b_len = arrayB.length;

        int[] merge = new int[a_len + b_len];
        int i = 0, j = 0, k = 0;
        while (i < a_len && j < b_len) {
            if (arrayA[i] < arrayB[j]) {
                merge[k++] = arrayA[i++];
            } else {
                merge[k++] = arrayB[j++];
            }
        }

        // A 数组全部合并完成，将 B 数组剩余值全都加入到数组
        if (i == a_len) {
            for (; j < b_len; j++) {
                merge[k++] = arrayB[j];
            }
        }
        // B 数组全部合并完成，将 A 数组剩余值全都加入到数组
        else {
            for (; i < a_len; i++) {
                merge[k++] = arrayA[i];
            }
        }
        return merge;
    }

    public static void main(String[] args) {
        int arrayA[] = { 1, 2, 3, 4, 5 };
        int arrayB[] = { 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] merge = merge(arrayA, arrayB);
        System.out.println("merge===" + Arrays.toString(merge));
    }
}
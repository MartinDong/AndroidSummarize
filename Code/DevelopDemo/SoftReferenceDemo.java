package DevelopDemo;

import java.lang.ref.SoftReference;

public class SoftReferenceDemo {

    public static void main(String[] args) {
        SoftReference<String> sr = new SoftReference<String>(new String("Hellow soft reference"));
        System.out.println(sr.get());
        // 通知 JVM 的 GC 进行垃圾回收
        System.gc();
        System.out.println(sr.get());
    }
}
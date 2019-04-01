package DevelopDemo;

import java.lang.ref.WeakReference;

public class WeakReferenceDemo {
    public static void main(String[] args) {
        WeakReference<String> sr = new WeakReference<String>(new String("Hello weake reference"));

        System.out.println(sr.get());

        System.gc();

        System.out.println(sr.get());
    }
}
package DevelopDemo;

public class StringTest {

    // 编译期已经创建好的（直接使用双引号声明的）就存储在常量池中
    public static String strStatic = "ABC";
    public String strNormal = "ABC";
    public String strRuntime;

    public void checkValue() {
        // 运行期（new 出来的）存储在堆中
        strRuntime = new String("ABC");
        String strMethodNormal2 = new String("ABC");
        String strMethodNormal = "ABC";

        System.out.println("strStatic 是在编译期间已经创建好的，直接存储在常量池中 = address =" + System.identityHashCode(strStatic));
        System.out.println("strNormal 是在编译期间已经创建好的，直接存储在常量池中 = address =" + System.identityHashCode(strNormal));
        System.out
                .println("strRuntime 是运行时使用 new 创建的对象，引用存储在栈，对象存储在堆 = address =" + System.identityHashCode(strRuntime));
        System.out.println(
                "strMethodNormal 是运行时通过双引号进行的初始化操作，使用的也是常量区的对象 = address =" + System.identityHashCode(strMethodNormal));
        System.out.println("strMethodNormal2 是运行时通过 new 创建的对象，引用地址存储在栈，对象存储在堆 = address ="
                + System.identityHashCode(strMethodNormal2));

        System.out.println("---------------------------------------------------");

        System.out.println("strStatic.equals(strNormal)====" + strStatic.equals(strNormal));
        System.out.println("strStatic.equals(strRuntime)====" + strStatic.equals(strRuntime));
        System.out.println("strRuntime.equals(strMethodNormal)====" + strRuntime.equals(strMethodNormal));
        System.out.println("strStatic.equals(strMethodNormal)====" + strStatic.equals(strMethodNormal));
        System.out.println("strStatic.equals(strMethodNormal2)====" + strStatic.equals(strMethodNormal2));
    }

    public static void main(String[] args) {
        StringTest stringTest = new StringTest();
        stringTest.checkValue();
    }
}
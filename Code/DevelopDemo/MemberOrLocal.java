package DevelopDemo;

class BirthDate {
    private int day;
    private int month;
    private int year;

    public BirthDate(int d, int m, int y) {
        day = d;
        month = m;
        year = y;
    }
}

public class MemberOrLocal {
    public void change(int i) {
        System.out.println("change befor i=" + i);
        i = 1122;
        System.out.println("change after i=" + i);
    }

    public static void main(String args[]) {
        int date = 9;
        MemberOrLocal test = new MemberOrLocal();
        test.change(date);
        BirthDate d1 = new BirthDate(9, 9, 1999);

        System.out.println(date);
    }
}
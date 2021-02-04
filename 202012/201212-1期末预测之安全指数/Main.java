
public class Main {
    public static void main(String[] args) {
        java.util.Scanner in = new java.util.Scanner(System.in);
        int length = in.nextInt();
        int result = 0;
        for (int i = 0; i < length; i++) {
            result += in.nextInt() * in.nextInt();
        }
        System.out.println(result >= 0 ? result : 0);
    }
}

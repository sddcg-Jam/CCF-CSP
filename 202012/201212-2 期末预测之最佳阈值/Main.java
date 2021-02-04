import java.util.*;

//期末预测之最佳阈值  暴力解法70分，剩余超时
//100分  //使用map + 排序 +前缀和
public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int m = in.nextInt();
        Map<Integer, Integer> map = new HashMap<>(10001);

        int[] y = new int[m];
        int[][] sum = new int[m][2]; //进一步优化空间  int[] sum=new int[m];// 过-不过

        for (int i = 0; i < m; i++) {
            int index = in.nextInt();
            int value = in.nextInt();// 同一阈值，不同学生可能过也可能不过
            y[i] = index;
            int key = index * 10 + value;
            map.put(key, map.getOrDefault(key, 0) + 1);
        }
        Arrays.sort(y);//排序 还需要排除重复

        //更新前缀和
        for (int i = 0; i < m; i++) {
            if (i == 0) {
                int key0 = y[i] * 10 + 0;
                int key1 = key0 + 1;
                sum[i][0] = map.getOrDefault(key0, 0);
                sum[i][1] = map.getOrDefault(key1, 0);
            } else if (i >= 1 && y[i] == y[i - 1]) {
                sum[i][0] = sum[i - 1][0];
                sum[i][1] = sum[i - 1][1];
            } else {
                int key0 = y[i] * 10 + 0;
                int key1 = key0 + 1;
                sum[i][0] = sum[i - 1][0] + map.getOrDefault(key0, 0);
                sum[i][1] = sum[i - 1][1] + map.getOrDefault(key1, 0);
            }
        }
        int betterTheta = y[0];
        int maxRight = sum[0][1] - sum[0][0];
        //计算最好值
        for (int i = 1; i < m; i++) {
            //int right = sum[i - 1][0] - sum[i - 1][1];//前面的正例 - 反例
            //后面正例 - 反例
            //right += sum[m - 1][1] - sum[i - 1][1] - sum[m - 1][0] + sum[i - 1][0];
            int right = sum[m - 1][1] - 2 * sum[i - 1][1] - sum[m - 1][0] + 2 * sum[i - 1][0];
            if (right > maxRight) {
                maxRight = right;
                betterTheta = y[i];
            } else if (right == maxRight) {
                betterTheta = y[i];
            }
        }
        System.out.println(betterTheta);
    }
}
/* 可行方案，687ms，105M
 */
/*
    public static int getTheta1(int[] y, int[] result, Set<Integer> set) {
int[] y = new int[m];
        int[] result = new int[m];
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < m; i++) {
            y[i] = in.nextInt();
            set.add(y[i]);
            result[i] = in.nextInt();
        }
        System.out.println(getTheta1(y, result,set));

        int maxRightNumber = -1;
        int bestTheta = 0;
        for (int theta : set) {
            //for (int i = 0; i < m; i++) {//尝试每一个同学的指数作为阈值
            //int theta = y[i];
            int rightNumber = 0;
            for (int j = 0; j < y.length; j++) {
                if ((y[j] < theta && result[j] == 0) || (y[j] >= theta && result[j] == 1)) {
                    rightNumber++;
                }
            }
            if (rightNumber == maxRightNumber && theta > bestTheta) {
                bestTheta = theta;
            } else if (rightNumber > maxRightNumber) {
                maxRightNumber = rightNumber;
                bestTheta = theta;
            }
        }
        return bestTheta;
    }

    public static void getTheta() {
        Scanner in = new Scanner(System.in);
        int m = in.nextInt();
        //计数排序空间消耗太大，不可行
        int[][] array = new int[100000001][3]; //该阈值过的人数、 下标0为不过的人数, 下标3表示是否出现过的theta
        int maxIndex = 0; //记录最大阈值
        int minIndex = 100000001;
        for (int i = 0; i < m; i++) { //计数排序
            int index = in.nextInt();
            int value = in.nextInt();// 同一阈值，不同学生可能过也可能不过
            array[index][value] += 1;
            array[index][2] = 1;
            if (index > maxIndex) {
                maxIndex = index;
            }
            if (index < minIndex) {
                minIndex = index;
            }
        }
        //更新前缀和
        for (int i = minIndex + 1; i <= maxIndex; i++) {
            array[i][0] += array[i - 1][0];
            array[i][1] += array[i - 1][1];
        }
        int betterTheta = minIndex;
        int maxRight = array[minIndex][1] - array[minIndex][0];
        int preRight = maxRight;
        //计算最好值
        for (int i = minIndex + 1; i <= maxIndex; i++) {
            if (array[i][2] == 0) {
                continue;
            }
            int right = array[i - 1][0] - array[i - 1][1];//前面的正例 - 反例
            //后面正例 - 反例
            right += array[maxIndex][1] - array[i - 1][1] - array[maxIndex][0] + array[i - 1][0];
            if (right > maxRight) {
                maxRight = right;
                betterTheta = i;
            } else if (right == maxRight) {
                betterTheta = i;
            }
        }
        System.out.println(betterTheta);
    }

   */

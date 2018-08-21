package indi.ysgj;

import java.util.Arrays;
import java.util.Random;

public class RandomDemo {

	public static void main(String[] args) {
		randomArray(1, 10, 10);
	}

	/**
	 * @param min	随机数所在范围 最小
	 * @param max	随机数所在范围 最大
	 * @param n:随机数的个数
	 * @return
	 */
	public static int[] randomArray(int min, int max, int n) {
		int len = max - min + 1;

		if (max < min || n > len) {
			return null;
		}

		// 初始化给定范围的待选数组
		int[] source = new int[len];
		for (int i = min; i < min + len; i++) {
			source[i - min] = i;
		}

		int[] result = new int[n];
		Random rd = new Random();
		for (int i = 0; i < result.length; i++) {
			int index = rd.nextInt(source.length);
			// 将随机到的数放入结果集
			result[i] = source[index];
			// 将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换
			System.out.println(result[i]);
			source[index] = source[source.length-1];
			source=Arrays.copyOf(source, source.length-1);
		}
		return result;
	}

}

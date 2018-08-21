package indi.ysgj;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

public class SortDemo {

	@Test
	public void sortTest() {

	    int[] str = { 1, 4, 6, 7, 1, 3, 7, 2, 3, 5, 8, 10 };
		Arrays.sort(str);

		int orderNum = 0;
		ArrayList<String> list = new ArrayList<String>();

		for (int i = 0; i < str.length; i++) {
			orderNum = i + 1;
			for (int j = 0; j < i; j++) {
				if (str[i] == str[i - j - 1]) {
					orderNum--;
				}
			}
			list.add("str:" + str[i] + " - orderNum:" + orderNum);
		}

		// for (String string : list) {
		// System.out.println(string);
		// }
		// 用String.join就不用使用for循环
		System.out.print(String.join("\n", list));
	}
}

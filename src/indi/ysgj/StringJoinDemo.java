package indi.ysgj;

import java.util.ArrayList;

public class StringJoinDemo {
	
	public static void main(String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		String[] arr = {"1","2","3","6","4"};
		
		list.add("1");
		list.add("2");
		list.add("3");
		
		System.out.println(String.join(";", arr));
		System.out.println(String.join(";", list));
		
	}

}

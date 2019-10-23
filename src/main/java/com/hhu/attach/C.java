package com.hhu.attach;

public class C {

	private static long timer;

	public static void main(String[] args) throws InterruptedException {
		C c = new C();
		while (true) {
			c.m();
		}
	}

	private void m() throws InterruptedException {
		// timer -= System.currentTimeMillis();
		// int a = 1;
		Thread.sleep(1000);
		// timer += System.currentTimeMillis();
		// System.out.println(timer);
		// System.out.println(a);
	}


}

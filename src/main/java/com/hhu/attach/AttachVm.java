package com.hhu.attach;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;
import java.util.Scanner;

public class AttachVm {

	public static void main(String[] args) throws IOException{

		Scanner scanner = new Scanner(System.in);
		System.out.println("attach pid");
		String pid = scanner.nextLine();

		VirtualMachine vm = null;
		try {
			vm = VirtualMachine.attach(pid);
			vm.loadAgent("F:\\Project\\java\\ASM\\target\\ASM-1.0-SNAPSHOT.jar");
		} catch (AttachNotSupportedException | AgentLoadException | AgentInitializationException e) {
			e.printStackTrace();
		} finally {
			if (vm != null) {
				vm.detach();
			}
		}

	}


}

package com.test;

import java.io.PrintWriter;

public interface TestCase {
	void run();

	void runSingle(PrintWriter outStream);

}

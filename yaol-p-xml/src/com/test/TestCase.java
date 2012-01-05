package com.test;

import java.io.PrintWriter;

public interface TestCase {
	long run();

	void runSingle(PrintWriter outStream);

}

package train;

import java.util.Arrays;

import org.junit.Test;

import common.AbstractTest;
import parse.Parse;

public class IntegrationTest extends AbstractTest {

	@Test
	public void integrationTest() {
		for (int h = -1; h < 3; ++h) {
			for (boolean parentEncoding : Arrays.asList(false, true)) {
				String[] args = { "../data/heb-ctrees.gold", "../data/heb-ctrees.train", "../../exps/test_h" + h + "_pe" + (parentEncoding ? 1 : 0) };
				Parse.h = h;
				Parse.multithreaded = true;
				Parse.numOfThreads = 20;
				Parse.parentEncoding = parentEncoding;
				Parse.main(args);
			}
		}
	}

}

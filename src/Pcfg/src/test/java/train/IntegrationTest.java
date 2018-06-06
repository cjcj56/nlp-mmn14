package train;

import org.junit.Test;

import common.AbstractTest;
import parse.Parse;

public class IntegrationTest extends AbstractTest {

	@Test
	public void integrationTest() {
		for(int h = -1; h < 3; ++h) {
			String[] args = {"../data/heb-ctrees.gold", "../data/heb-ctrees.train", "../../exps/test"};
			Parse.h = h;
			Parse.multithreaded = true;
			Parse.numOfThreads = 20;
			Parse.main(args);
		}
	}

}

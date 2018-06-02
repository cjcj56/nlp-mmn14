package train;

import org.junit.Test;

import common.AbstractTest;
import parse.Parse;

public class IntegrationTest extends AbstractTest {

	@Test
	public void integrationTest() {
		String[] args = {"../data/heb-ctrees.gold", "../data/heb-ctrees.train", "../../exps/test"};
		Parse.h = 1;
		Parse.multithreaded = true;
		Parse.numOfThreads = 20;
		Parse.main(args);
	}

}

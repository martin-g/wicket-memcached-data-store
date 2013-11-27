package com.mycompany;

import java.io.IOException;
import java.net.InetSocketAddress;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationCompletionListener;
import net.spy.memcached.internal.OperationFuture;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestHomePage
{
	private WicketTester tester;

	@Before
	public void setUp()
	{
		tester = new WicketTester(new WicketApplication());
	}

	@Test
	public void homepageRendersSuccessfully()
	{
		//start and render the test page
		tester.startPage(HomePage.class);

		//assert rendered page class
		tester.assertRenderedPage(HomePage.class);
	}

	private static final String KEY = "key";

	public static void main(String[] args) throws IOException
	{
		final MemcachedClient c = new MemcachedClient(new InetSocketAddress("localhost", 11211));
		Object o = c.get(KEY);
		System.err.println("1. get: " + o);
		OperationFuture<Boolean> add = c.add(KEY, 50000, "1");
		add.addListener(new OperationCompletionListener()
		{
			@Override
			public void onComplete(OperationFuture<?> operationFuture) throws Exception
			{
				Object o2 = c.get(KEY);
				System.err.println("2. get: " + o2);

				c.delete(KEY);

				Object o3 = c.get(KEY);
				System.err.println("3. get: " + o3);

				c.shutdown();
			}
		});
	}
}

package com.mycompany;

import org.apache.wicket.DefaultPageManagerProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.datastore.memcached.IMemcachedSettings;
import org.wicketstuff.datastore.memcached.MemcachedDataStore;
import org.wicketstuff.datastore.memcached.MemcachedSettings;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 * 
 * @see com.mycompany.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return HomePage.class;
	}

	@Override
	public void init()
	{
		super.init();

		mountPage("page1", HomePage.class);
		mountPage("page2", Page2.class);

		// for testing #getData().
		// getStoreSettings().setInmemoryCacheSize(1);

		setPageManagerProvider(new DefaultPageManagerProvider(this)
		{
			@Override
			protected IDataStore newDataStore()
			{
				IMemcachedSettings settings = new MemcachedSettings();
				return new MemcachedDataStore(settings);
			}
		});
	}
}

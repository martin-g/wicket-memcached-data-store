package org.wicketstuff.datastore.memcached;

import org.apache.wicket.util.time.Duration;

/**
 *
 */
public interface IMemcachedSettings
{
	String getHost();

	IMemcachedSettings setHost(String host);

	int getPort();

	IMemcachedSettings setPost(int port);

	Duration getExpirationTime();

	IMemcachedSettings setExpirationTime(Duration expirationTime);

	Duration getShutdownTimeout();

	IMemcachedSettings setShutdownTimeout(Duration timeout);
}

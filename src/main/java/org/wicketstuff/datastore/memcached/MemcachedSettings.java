package org.wicketstuff.datastore.memcached;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;

/**
 *
 */
public class MemcachedSettings implements IMemcachedSettings
{
	private String host = "localhost";

	private int port = 11211;

	private Duration expirationTime = Duration.minutes(30);

	private Duration shutdownTimeout = Duration.seconds(10);

	@Override
	public String getHost()
	{
		return host;
	}

	@Override
	public IMemcachedSettings setHost(String host)
	{
		this.host = Args.notNull(host, "host");
		return this;
	}

	@Override
	public int getPort()
	{
		return port;
	}

	@Override
	public IMemcachedSettings setPost(int port)
	{
		this.port = port;
		return this;
	}

	@Override
	public Duration getExpirationTime()
	{
		return expirationTime;
	}

	@Override
	public IMemcachedSettings setExpirationTime(Duration expirationTime)
	{
		this.expirationTime = Args.notNull(expirationTime, "expirationTime");
		return this;
	}

	@Override
	public Duration getShutdownTimeout()
	{
		return shutdownTimeout;
	}

	@Override
	public IMemcachedSettings setShutdownTimeout(Duration timeout)
	{
		this.shutdownTimeout = Args.notNull(timeout, "timeout");
		return this;
	}
}

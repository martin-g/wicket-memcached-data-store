package org.wicketstuff.datastore.memcached;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;

import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.time.Duration;

/**
 * IDataStore that stores the data in Memcached
 */
public class MemcachedDataStore implements IDataStore
{
	/**
	 * A prefix for the keys to avoid duplication of keys
	 * and to make it easier to find out who put the data
	 * at the server
	 */
	private static final String KEY_PREFIX = "Wicket-Memcached";

	/**
	 * A separator used for the key construction
	 */
	private static final String SEPARATOR = "|||";

	/**
	 * The connection to the Memcached server
	 */
	private final MemcachedClient client;

	/**
	 * The configuration for the client
	 */
	private final IMemcachedSettings settings;

	/**
	 * Constructor.
	 *
	 * @param settings The configuration for the client
	 */
	public MemcachedDataStore(IMemcachedSettings settings)
	{
		this.settings = Args.notNull(settings, "settings");

		String host = settings.getHost();
		Checks.notEmptyShort(host, "host");

		int port = settings.getPort();
		Checks.withinRangeShort(1, 65535, port, "port");

		try
		{
			this.client = new MemcachedClient(new InetSocketAddress(host, port));
		}
		catch (IOException iox)
		{
			throw new RuntimeException(iox);
		}
	}

	@Override
	public byte[] getData(String sessionId, int pageId)
	{
		byte[] bytes = null;
		SessionData data = getSessionData(sessionId);
		if (data != null)
		{
			bytes = data.pages.get(pageId);
		}
		return bytes;
	}

	@Override
	public void removeData(String sessionId, int pageId)
	{
		SessionData sessionData = getSessionData(sessionId);
		if (sessionData != null)
		{
			byte[] removed = sessionData.pages.remove(pageId);
			if (removed != null)
			{
				storeSessionData(sessionId, sessionData);
			}
		}
	}

	@Override
	public void removeData(String sessionId)
	{
		String key = getKey(sessionId);
		client.delete(key);
	}

	@Override
	public void storeData(String sessionId, int pageId, byte[] data)
	{
		SessionData sessionData = getSessionData(sessionId);
		if (sessionData == null)
		{
			sessionData = new SessionData();
		}
		sessionData.pages.put(pageId, data);
		storeSessionData(sessionId, sessionData);
	}

	@Override
	public void destroy()
	{
		Duration timeout = settings.getShutdownTimeout();
		client.shutdown(timeout.getMilliseconds(), TimeUnit.MILLISECONDS);
	}

	@Override
	public boolean isReplicated()
	{
		return true;
	}

	@Override
	public boolean canBeAsynchronous()
	{
		// no need to be asynchronous
		return false;
	}


	private String getKey(String sessionId)
	{
		StringBuilder key = new StringBuilder();
		key
			.append(KEY_PREFIX)
			.append(SEPARATOR)
			.append(sessionId);
		return key.toString();
	}

	private SessionData getSessionData(String sessionId)
	{
		String key = getKey(sessionId);
		return (SessionData) client.get(key);
	}

	/**
	 * Stores the SessionData in Memcached.
	 *
	 * @param sessionId  The session id
	 * @param sessionData The pages' for this session
	 */
	private void storeSessionData(String sessionId, SessionData sessionData)
	{
		Duration expirationTime = settings.getExpirationTime();

		// TODO Improve to follow Memcached protocol.
		// See net.spy.memcached.MemcachedClient.set(java.lang.String, int, java.lang.Object)()
//		Time timeToExpire = Time.now().add(expirationTime);

		client.set(getKey(sessionId), (int) expirationTime.getMilliseconds(), sessionData);
	}

	/**
	 * Holds the pages per session
	 */
	// TODO Get rid of this because it will keep not needed data until the session expires
	private static class SessionData implements Serializable
	{
		ConcurrentMap<Integer, byte[]> pages = new ConcurrentHashMap<Integer, byte[]>();
	}
}

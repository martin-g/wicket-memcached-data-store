package org.wicketstuff.datastore.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.MemcachedClient;

import net.spy.memcached.internal.OperationCompletionListener;
import net.spy.memcached.internal.OperationFuture;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IDataStore that stores the data in Memcached
 */
public class MemcachedDataStore implements IDataStore
{
	private static final Logger LOG = LoggerFactory.getLogger(MemcachedDataStore.class);

	/**
	 * A prefix for the keysPerSession to avoid duplication of keysPerSession
	 * and to make it easier to find out who put the data
	 * at the server
	 */
	private static final String KEY_SUFFIX = "Wicket-Memcached";

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
	 * Tracks the keys for all operations per session.
	 * Used to delete all entries for this session.
	 */
	private final ConcurrentMap<String, SortedSet<String>> keysPerSession =
			new ConcurrentHashMap<String, SortedSet<String>>();
	
	/**
	 * Constructor.
	 *
	 * Creates a MemcachedClient from the provided settings
	 *
	 * @param settings The configuration for the client
	 */
	public MemcachedDataStore(IMemcachedSettings settings)
	{
		this(createClient(settings), settings);
	}

	/**
	 * Constructor.
	 *
	 * @param client   The connection to Memcached
	 * @param settings The configuration for the client
	 */
	public MemcachedDataStore(MemcachedClient client, IMemcachedSettings settings)
	{
		this.client = Args.notNull(client, "client");
		this.settings = Args.notNull(settings, "settings");

		client.addObserver(new ConnectionObserver()
		{
			@Override
			public void connectionEstablished(SocketAddress sa, int reconnectCount)
			{
				LOG.info("Established connection to: {}, reconnect count: {}", sa, reconnectCount);
			}

			@Override
			public void connectionLost(SocketAddress sa)
			{
				LOG.warn("Lost connection to: {}", sa);
			}
		});

	}

	/**
	 * Creates MemcachedClient with the provided hostname and port
	 * in the settings
	 *
	 * @param settings  The configuration for the client
	 * @return A MemcachedClient
	 */
	private static MemcachedClient createClient(IMemcachedSettings settings)
	{
		Args.notNull(settings, "settings");

		String host = settings.getHost();
		Checks.notEmptyShort(host, "host");

		int port = settings.getPort();
		Checks.withinRangeShort(1, 65535, port, "port");

		try
		{
			MemcachedClient memcachedClient = new MemcachedClient(new InetSocketAddress(host, port));
			return memcachedClient;
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
		String key = getKey(sessionId, pageId);
		SortedSet<String> keys = keysPerSession.get(sessionId);
		if (keys != null && keys.contains(key))
		{
			bytes = (byte[]) client.get(key);

			if (bytes == null)
			{
				// maybe the entry has expired
				keys.remove(key);
			}
		}
		LOG.debug("Got {} for session '{}' and page id '{}'",
				new Object[] {bytes != null ? "data" : "'null'", sessionId, pageId});
		return bytes;
	}

	@Override
	public void removeData(final String sessionId, final int pageId)
	{
		final Set<String> keys = keysPerSession.get(sessionId);
		final String key = getKey(sessionId, pageId);

		if (keys != null && keys.contains(key))
		{
			OperationFuture<Boolean> delete = client.delete(key);
			delete.addListener(new OperationCompletionListener()
			{
				@Override
				public void onComplete(OperationFuture<?> future) throws Exception
				{
					keys.remove(key);
					LOG.debug("Removed the data for session '{}' and page id '{}'", sessionId, pageId);
				}
			});
		}
	}

	@Override
	public void removeData(String sessionId)
	{
		Set<String> keys = keysPerSession.get(sessionId);
		if (keys != null)
		{
			for (String key : keys)
			{
				client.delete(key);
			}
			keysPerSession.remove(sessionId);
			LOG.debug("Removed the data for session '{}'", sessionId);
		}
	}

	@Override
	public void storeData(final String sessionId, final int pageId, byte[] data)
	{
		final String key = getKey(sessionId, pageId);
		SortedSet<String> keys = keysPerSession.get(sessionId);
		if (keys == null)
		{
			keys = new TreeSet<String>();
			SortedSet<String> old = keysPerSession.putIfAbsent(sessionId, keys);
			if (old != null)
			{
				keys = old;
			}
		}

		Duration expirationTime = settings.getExpirationTime();

		// TODO Improve to follow Memcached protocol.
		// See net.spy.memcached.MemcachedClient.set(java.lang.String, int, java.lang.Object)()
//		Time timeToExpire = Time.now().add(expirationTime);

		final SortedSet<String> _keys = keys;

		OperationFuture<Boolean> set = client.set(key, (int) expirationTime.getMilliseconds(), data);
		set.addListener(new OperationCompletionListener()
		{
			@Override
			public void onComplete(OperationFuture<?> future) throws Exception
			{
				_keys.add(key);
				LOG.debug("Stored data for session '{}' and page id '{}'", sessionId, pageId);
			}
		});
	}

	@Override
	public void destroy()
	{
		Duration timeout = settings.getShutdownTimeout();
		LOG.info("Shutting down gracefully for {}", timeout);
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
		// MemcachedClient is asynchronous itself
		return false;
	}

	/**
	 * Creates a key that is used for the lookup in Memcached.
	 * The key starts with sessionId and the pageId so
	 * {@linkplain #keysPerSession} can be sorted faster.
	 *
	 * @param sessionId The id of the http session.
	 * @param pageId    The id of the stored page
	 * @return A key that is used for the lookup in Memcached
	 */
	private String getKey(String sessionId, int pageId)
	{
		return new StringBuilder()
			.append(sessionId)
			.append(SEPARATOR)
			.append(pageId)
			.append(SEPARATOR)
			.append(KEY_SUFFIX)
			.toString();
	}
}

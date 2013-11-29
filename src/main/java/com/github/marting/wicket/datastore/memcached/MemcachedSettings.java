/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.marting.wicket.datastore.memcached;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;

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

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

import org.apache.wicket.util.time.Duration;

/**
 * Settings for MemcachedDataStore
 */
public interface IMemcachedSettings
{
	/**
	 * @return The name of the host where Memcached runs
	 */
	String getHost();

	/**
	 * Sets the name of the host where Memcached runs
	 *
	 * @param host the name of the host where Memcached runs
	 * @return this instance, for chaining
	 */
	IMemcachedSettings setHost(String host);

	/**
	 * @return the port where Memcached listens to
	 */
	int getPort();

	/**
	 * Sets the port where Memcached listens to
	 *
	 * @param port the port where Memcached listens to
	 * @return this instance, for chaining
	 */
	IMemcachedSettings setPost(int port);

	/**
	 * @return the duration after which the record will be evicted by Memcached
	 */
	Duration getExpirationTime();

	/**
	 * Sets the duration after which the record will be
	 * evicted by Memcached
	 *
	 * @param expirationTime the duration after which the record will be
	 *                       evicted by Memcached
	 * @return this instance, for chaining
	 */
	IMemcachedSettings setExpirationTime(Duration expirationTime);

	/**
	 * @return the time to wait when shutting down the connection to
	 *                the Memcached server
	 */
	Duration getShutdownTimeout();

	/**
	 * Sets the time to wait when shutting down the connection to
	 * the Memcached server
	 *
	 * @param timeout the time to wait when shutting down the connection to
	 *                the Memcached server
	 * @return this instance, for chaining
	 */
	IMemcachedSettings setShutdownTimeout(Duration timeout);
}

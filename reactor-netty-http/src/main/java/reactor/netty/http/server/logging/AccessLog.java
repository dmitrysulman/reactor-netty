/*
 * Copyright (c) 2020-2025 VMware, Inc. or its affiliates, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.netty.http.server.logging;

/**
 * Interface for logging HTTP access information using the {@code log()} method.
 * <p>
 * See {@link AccessLogFactory} for convenience methods to create an access log factory to be passed to
 * {@link reactor.netty.http.server.HttpServer#accessLog(boolean, AccessLogFactory)} during server configuration.
 *
 * @author limaoning
 * @author Dmitry Sulman
 * @since 1.0.1
 */
public interface AccessLog {

	/**
	 * Creates a default access log implementation.
	 *
	 * @param logFormat the log format string
	 * @param args the list of arguments
	 *
	 * @return {@link DefaultAccessLog} instance
	 *
	 * @see DefaultAccessLog
	 */
	static AccessLog create(String logFormat, Object... args) {
		return new DefaultAccessLog(logFormat, args);
	}

	/**
	 *  Logs HTTP access information.
	 */
	void log();

}

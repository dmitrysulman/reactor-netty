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

import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Objects;

/**
 * Default implementation of {@link AccessLog} that logs HTTP access information into a Logger named
 * {@code reactor.netty.http.server.AccessLog} at INFO level.
 *
 * @author limaoning
 * @author Dmitry Sulman
 * @since 1.3.0
 */
public final class DefaultAccessLog implements AccessLog {

	static final Logger LOG = Loggers.getLogger("reactor.netty.http.server.AccessLog");

	final String logFormat;
	final Object[] args;

	DefaultAccessLog(String logFormat, Object... args) {
		Objects.requireNonNull(logFormat, "logFormat");
		this.logFormat = logFormat;
		this.args = args;
	}

	@Override
	public void log() {
		if (LOG.isInfoEnabled()) {
			LOG.info(logFormat, args);
		}
	}

}

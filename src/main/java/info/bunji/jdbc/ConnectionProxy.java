/*
 * Copyright 2016 Fumiharu Kinoshita
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.bunji.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;

/**
 **********************************************************
 * implements Connection Wrapper.
 *
 * @author f.kinoshita
 **********************************************************
 */
public class ConnectionProxy extends LoggerHelper implements InvocationHandler {

	/** real Connection */
	private Connection _conn;

	/**
	 **********************************************
	 * constractor
	 * @param conn real connection
	 * @param url connection url
	 **********************************************
	 */
	ConnectionProxy(Connection conn, String url, String connectionId) {
		super(url, connectionId);
		_conn = conn;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			// TODO:Connection pooling環境で動的にログレベルを変えるためにはStatementで制御すべき
			//if (!JdbcLoggerFactory.getLogger().isJdbcLoggingEnabled()) {
			//	return method.invoke(_conn, args);
			//}

			Object ret;
			String name = method.getName();
			if (name.equals("createStatement")) {
				Statement stmt = (Statement) method.invoke(_conn, args);
				ret = ProxyFactory.wrapStatement(stmt, url, getConnectionId());
			} else if (name.equals("prepareStatement")) {
				PreparedStatement stmt = null;
				try {
					startExecute((String) args[0]);
					stmt = (PreparedStatement) method.invoke(_conn, args);
				} catch (Exception e) {
					reportException(e.getCause());
					throw e;
				}
				ret = ProxyFactory.wrapPreparedStatement(stmt, url, args[0].toString(), getConnectionId());
			} else if (name.equals("prepareCall")) {
				CallableStatement stmt = null;
				try {
					startExecute((String) args[0]);
					stmt = (CallableStatement) method.invoke(_conn, args);
				} catch (Exception e) {
					reportException(e.getCause());
					throw e;
				}
				ret = ProxyFactory.wrapCallableStatement(stmt, url, args[0].toString(), getConnectionId());
			} else if (name.equals("close") && isConnectionLogging()) {
				long start  = System.currentTimeMillis();
				JdbcLogger logger = JdbcLoggerFactory.getLogger(this.url);
				ret = method.invoke(_conn, args);
				logger.debug(String.format(JdbcLogger.RETURN_MSG_FORMAT_WITH_CONN, System.currentTimeMillis() - start, getConnectionId(), "close connection."));
			} else {
				ret = method.invoke(_conn, args);
			}
			return ret;
		} catch (InvocationTargetException e) {
			throw e.getCause();
		} finally {
			endExecute();
		}
	}
}

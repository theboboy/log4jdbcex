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
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * @author f.kinoshita
 */
class ProxyFactory {

	private static final ProxyFactory factory = new ProxyFactory();

	private ProxyFactory() {
		super();
	}

	/**
	 ********************************************
	 *
	 * @param driver real driver
	 * @return wrapperd driver
	 ********************************************
	 */
	static Driver wrapDriver() {
		return factory.newProxyInstance(Driver.class, new DriverProxy());
	}

	static ProxyFactory getInstance() {
		return factory;
	}

	/**
	 ********************************************
	 *
	 * @param conn real connection
	 * @param url jdbc url
	 * @return wrapperd connection
	 ********************************************
	 */
	static Connection wrapConnection(Connection conn, String url, String connectionId) {
		if (conn == null) return null;
		return factory.newProxyInstance(Connection.class, new ConnectionProxy(conn,  url, connectionId));
	}

	static Statement wrapStatement(Statement conn, String url, String connectionId) {
		return factory.newProxyInstance(Statement.class, new StatementProxy(conn,  url, connectionId));
	}

	static PreparedStatement wrapPreparedStatement(PreparedStatement conn, String url, String sql, String connectionId) {
		return factory.newProxyInstance(PreparedStatement.class, new StatementProxy(conn,  url, sql, connectionId));
	}

	static CallableStatement wrapCallableStatement(CallableStatement conn, String url, String sql, String connectionId) {
		return factory.newProxyInstance(CallableStatement.class, new StatementProxy(conn,  url, sql, connectionId));
	}

	/**
	 ********************************************
	 * Convenience method to generate a single-interface proxy using the handler's classloader
	 *
	 * @param <T> The type of object to proxy
	 * @param type The type of object to proxy
	 * @param handler The handler that intercepts/overrides method calls.
	 * @return proxied object
	 ********************************************
	 */
	<T> T newProxyInstance(Class<T> type, InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class<?>[] {type}, handler));
	}
}

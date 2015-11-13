package org.hibernate.tool.test;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.reveng.DefaultDatabaseCollector;
import org.hibernate.cfg.reveng.ReverseEngineeringRuntimeInfo;
import org.hibernate.cfg.reveng.dialect.JDBCMetaDataDialect;
import org.hibernate.cfg.reveng.dialect.MetaDataDialect;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.JDBCMetaDataBinderTestCase;


/**
 * Various tests to validate the "sanity" of the jdbc drivers meta data implementation.
 * 
 * @author Max Rydahl Andersen
 *
 */
public class DriverMetaDataTest extends JDBCMetaDataBinderTestCase {

protected String[] getCreateSQL() {
		
	return new String[] {
				"create table tab_master ( id char not null, name varchar(20), primary key (id) )",
				"create table tab_child  ( childid character not null, masterref character, primary key (childid), foreign key (masterref) references tab_master(id) )",
		};
	}

	protected String[] getDropSQL() {
		
		return new String[]  {				
				"drop table tab_child",
				"drop table tab_master",					
		};
	}

	public void testExportedKeys() {	
		MetaDataDialect dialect = new JDBCMetaDataDialect();
		ServiceRegistry serviceRegistry = getConfiguration().getServiceRegistry();
		JdbcServices jdbcServices = serviceRegistry.getService(JdbcServices.class);
		ConnectionProvider connectionProvider = 
				serviceRegistry.getService(ConnectionProvider.class);			
		dialect.configure(
				ReverseEngineeringRuntimeInfo.createInstance(
						connectionProvider,
						jdbcServices.getSqlExceptionHelper().getSqlExceptionConverter(), 
						new DefaultDatabaseCollector(dialect)));		
		Properties properties = getConfiguration().getProperties();
		String catalog = properties.getProperty(AvailableSettings.DEFAULT_CATALOG);
		String schema = properties.getProperty(AvailableSettings.DEFAULT_SCHEMA);		
		Iterator<Map<String,Object>> tables = 
				dialect.getTables(
						catalog, 
						schema, 
						identifier("tab_master") ); 		
		boolean foundMaster = false;
		while(tables.hasNext()) {
			Map<?,?> map = (Map<?,?>) tables.next();		
			String tableName = (String) map.get("TABLE_NAME");
			String schemaName = (String) map.get("TABLE_SCHEM");
	        String catalogName = (String) map.get("TABLE_CAT");        
	        if(tableName.equals(identifier("tab_master"))) {
				foundMaster = true;
				Iterator<?> exportedKeys = 
						dialect.getExportedKeys(
								catalogName, 
								schemaName, 
								tableName );
				int cnt = 0;
				while ( exportedKeys.hasNext() ) {
					exportedKeys.next();
					cnt++;
				}
				assertEquals(1,cnt);
			}
		}
		
		assertTrue(foundMaster);
	}

	public void testDataType() {		
		MetaDataDialect dialect = new JDBCMetaDataDialect();
		ServiceRegistry serviceRegistry = getConfiguration().getServiceRegistry();
		JdbcServices jdbcServices = serviceRegistry.getService(JdbcServices.class);
		ConnectionProvider connectionProvider = 
				serviceRegistry.getService(ConnectionProvider.class);	
		dialect.configure(
				ReverseEngineeringRuntimeInfo.createInstance(
						connectionProvider,
						jdbcServices.getSqlExceptionHelper().getSqlExceptionConverter(), 
						new DefaultDatabaseCollector(dialect)));		
		Properties properties = getConfiguration().getProperties();
		String catalog = properties.getProperty(AvailableSettings.DEFAULT_CATALOG);
		String schema = properties.getProperty(AvailableSettings.DEFAULT_SCHEMA);		
		Iterator<?> tables = 
				dialect.getColumns( 
						catalog, 
						schema, 
						"test", 
						null ); 
		while(tables.hasNext()) {
			Map<?,?> map = (Map<?,?>) tables.next();			
			System.out.println(map);			
		}
	}
	
	public void testCaseTest() {
		MetaDataDialect dialect = new JDBCMetaDataDialect();
		ServiceRegistry serviceRegistry = getConfiguration().getServiceRegistry();
		JdbcServices jdbcServices = serviceRegistry.getService(JdbcServices.class);
		ConnectionProvider connectionProvider = 
				serviceRegistry.getService(ConnectionProvider.class);
		dialect.configure( 
				ReverseEngineeringRuntimeInfo.createInstance(
						connectionProvider,
						jdbcServices.getSqlExceptionHelper().getSqlExceptionConverter(), 
						new DefaultDatabaseCollector(dialect)));
		Properties properties = getConfiguration().getProperties();
		String catalog = properties.getProperty(AvailableSettings.DEFAULT_CATALOG);
		String schema = properties.getProperty(AvailableSettings.DEFAULT_SCHEMA);		
		Iterator<Map<String, Object>> tables = 
				dialect.getTables(
						catalog, 
						schema, 
						identifier( "TAB_MASTER"));		
		assertHasNext( 1,	tables );
	}

	
}

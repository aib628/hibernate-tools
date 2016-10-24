package org.hibernate;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringSettings;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.tool.hbm2x.DAOExporter;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate反向工程生成工具、使用单例模式实现
 * 
 * @author Sunshine
 *
 */
public class HibernateTools implements Runnable {

	private final Logger LOGGER = LoggerFactory.getLogger(HibernateTools.class);

	private static final class SingletonHolder {
		private static final HibernateTools INSTANCE = new HibernateTools();
	}

	private HibernateTools() {
	}

	/**
	 * 获取单例工具类对象
	 * 
	 * @param hibernateCfgXmlFile Hibernate配置文件
	 * @return 实例对象
	 */
	public static final HibernateTools getInstance(File hibernateCfgXmlFile) {
		HibernateTools hibernateTools = SingletonHolder.INSTANCE;
		hibernateTools.setHibernateCfgXmlFile(hibernateCfgXmlFile);
		return hibernateTools;
	}

	/**
	 * 获取单例工具类对象
	 * 
	 * @param hibernateCfgXmlFile Hibernate配置文件
	 * @return 实例对象
	 */
	public static final HibernateTools getInstance(Properties properties) {
		HibernateTools hibernateTools = SingletonHolder.INSTANCE;
		hibernateTools.properties = properties;
		return hibernateTools;
	}

	/**
	 * 是否使用自定义生成策略配置
	 */
	private boolean useCustomSettings = true;

	/**
	 * Hibernate配置文件
	 */
	private File hibernateCfgXmlFile;

	/**
	 * Hibernate配置文件属性
	 */
	private Properties properties = null;

	public void run() {
		if (properties == null) {
			if (hibernateCfgXmlFile == null || !hibernateCfgXmlFile.exists()) {
				LOGGER.error("hibernate.cfg.xml File Not Found : {}");
				try {
					throw new FileNotFoundException("找不到hibernate.cfg.xml配置文件");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			// 直接给绝对路径会出问题
			this.properties = new Configuration().configure(hibernateCfgXmlFile).getProperties();
		}

		JDBCMetaDataConfiguration metaDataConfig = new JDBCMetaDataConfiguration();
		metaDataConfig.setProperties(properties);
		metaDataConfig.setReverseEngineeringStrategy(new DefaultReverseEngineeringStrategy());
		if (useCustomSettings) {
			ReverseEngineeringStrategy strategy = metaDataConfig.getReverseEngineeringStrategy();
			strategy.setSettings(getCustomSettings(strategy));
		}
		// metaDataConfig.readFromJDBC();不区分schema和catlog的话,容易碰到错误.
		metaDataConfig.readFromJDBC(PropertiesUtils.getString(properties, Settings.CATALOG),
				PropertiesUtils.getString(properties, Settings.SCHEMA));
		metaDataConfig.buildMappings();

		String includeTables = PropertiesUtils.getString(properties, Settings.INCLUDE_TABLES);

		if (PropertiesUtils.getBoolean(properties, Settings.GENERATE_POJO, true)) {
			POJOExporter exporter = new POJOExporter(metaDataConfig, getOutputDir(PojoSettings.OUTPUT_DIRECTORY));
			if (PropertiesUtils.getBoolean(properties, PojoSettings.IS_ANNOTATION, true)) {
				exporter.getProperties().setProperty("ejb3", "true");// ejb3注解
				exporter.getProperties().setProperty("jdk5", "true");// jdk5语法(主要是集合类的泛型处理)
			}

			String templatePath = PropertiesUtils.getString(properties, PojoSettings.TEMPLATE_PATH);
			if (templatePath.length() > 0) {
				exporter.setTemplatePath(new String[] { templatePath });
			}
			
			if(includeTables != null){
				exporter.getProperties().setProperty(Settings.INCLUDE_TABLES, includeTables);
			}
			
			exporter.start();
		}

		if (PropertiesUtils.getBoolean(properties, Settings.GENERATE_DAO)) {
			DAOExporter daoExporter = new DAOExporter(metaDataConfig, getOutputDir(DaoSettings.OUTPUT_DIRECTORY));
			String templatePath = PropertiesUtils.getString(properties, DaoSettings.TEMPLATE_PATH);
			if (templatePath.length() > 0) {
				daoExporter.setTemplatePath(new String[] { templatePath });
			}
			
			if(includeTables != null){
				daoExporter.getProperties().setProperty(Settings.INCLUDE_TABLES, includeTables);
			}
			
			daoExporter.start();
		}

	}

	private File getOutputDir(String name) {
		File file = new File(PropertiesUtils.getString(properties, name, "/"));// 生成项目的物理位置（跟目录，tools会自动根据pacakge建立相应路径）
		return file;
	}

	/**
	 * 自定义生成策略配置、只需要将下列名称的Property配置在配置文件中即可
	 * 
	 * @param rootStrategy 策略
	 * @param properties 配置来源
	 * @return 自定义策略配置
	 */
	private ReverseEngineeringSettings getCustomSettings(ReverseEngineeringStrategy rootStrategy) {
		return new ReverseEngineeringSettings(rootStrategy)
				.setDefaultPackageName(PropertiesUtils.getString(properties, StrategySettings.DEFAULT_PACKAGE))
				.setCreateCollectionForForeignKey(
						PropertiesUtils
								.getBoolean(properties, StrategySettings.CREATE_COLLECTION_FOR_FOREIGN_KEY, true))
				.setCreateManyToOneForForeignKey(
						PropertiesUtils.getBoolean(properties, StrategySettings.CREATE_MANY_TO_ONE_FOR_FOREIGN_KEY,
								true))
				.setDetectOneToOne(PropertiesUtils.getBoolean(properties, StrategySettings.DETECT_ONE_TO_ONE, true))
				.setDetectManyToMany(PropertiesUtils.getBoolean(properties, StrategySettings.DETECT_MANY_TO_MANY, true))
				.setDetectOptimisticLock(
						PropertiesUtils.getBoolean(properties, StrategySettings.DETECT_OPTIMISTIC_LOCK, true));
	}

	public File getHibernateCfgXmlFile() {
		return hibernateCfgXmlFile;
	}

	public void setHibernateCfgXmlFile(File hibernateCfgXmlFile) {
		this.hibernateCfgXmlFile = hibernateCfgXmlFile;
	}

	public boolean isUseCustomSettings() {
		return useCustomSettings;
	}

	public void setUseCustomSettings(boolean useCustomSettings) {
		this.useCustomSettings = useCustomSettings;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Hibernate工具常量配置
	 * 
	 * @author Sunshine
	 *
	 */
	public class Settings {

		/**
		 * 是否生成Pojo：默认为False
		 */
		public static final String GENERATE_POJO = "custom.isPojo";

		/**
		 * 是否生成Dao：默认为False
		 */
		public static final String GENERATE_DAO = "custom.isDao";
		
		/**
		 * 只生成指定的表,以逗号分隔、这里要以表对应的Java名称来写，不可直接写数据库里的表名称
		 */
		public static final String INCLUDE_TABLES = "custom.includeTables";

		/**
		 * 指定要反向生成的Schema名称：默认未指定
		 */
		public static final String SCHEMA = "custom.schema";

		/**
		 * 指定要反向生成的Catalog名称：默认未指定
		 */
		public static final String CATALOG = "custom.catalog";
	}

	/**
	 * Pojo常量配置
	 * 
	 * @author Sunshine
	 *
	 */
	public class PojoSettings {
		/**
		 * 是否使用注解：若使用则默认用EJB3和JDK5语法生成Pojo，否则二者语法均不使用
		 */
		public static final String IS_ANNOTATION = "custom.isAnnotation";

		/**
		 * 使用指定的Ftl模板路径
		 */
		public static final String TEMPLATE_PATH = "custom.pojo.templatePath";

		/**
		 * Pojo输出路径
		 */
		public static final String OUTPUT_DIRECTORY = "custom.pojo.outputDirectory";
	}

	/**
	 * Dao常量配置
	 * 
	 * @author Sunshine
	 *
	 */
	public class DaoSettings {

		/**
		 * 使用指定的Ftl模板路径
		 */
		public static final String TEMPLATE_PATH = "custom.dao.templatePath";

		/**
		 * Dao输出路径
		 */
		public static final String OUTPUT_DIRECTORY = "custom.dao.outputDirectory";
	}

	/**
	 * 生成策略常量配置
	 * 
	 * @author Sunshine
	 *
	 */
	public class StrategySettings {
		/**
		 * 默认包名称
		 */
		public static final String DEFAULT_PACKAGE = "custom.package";

		/**
		 * 是否生成many-to-one的在one端的集合类,就是一对多的关系
		 */
		public static final String CREATE_COLLECTION_FOR_FOREIGN_KEY = "custom.one2many";

		/**
		 * 是否生成many-to-one
		 */
		public static final String CREATE_MANY_TO_ONE_FOR_FOREIGN_KEY = "custom.many2one";

		/**
		 * 是否生成one-to-one
		 */
		public static final String DETECT_ONE_TO_ONE = "custom.one2one";

		/**
		 * 是否生成many-to-many
		 */
		public static final String DETECT_MANY_TO_MANY = "custom.many2many";

		/**
		 * 是否使用乐观锁
		 */
		public static final String DETECT_OPTIMISTIC_LOCK = "custom.detectOptimisticLock";
	}
}
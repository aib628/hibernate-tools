package org.hibernate;

import java.io.File;
import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringSettings;
import org.hibernate.tool.hbm2x.DAOExporter;
import org.hibernate.tool.hbm2x.POJOExporter;

public class HibernateTools implements Runnable {

	private static final class SingletonHolder {
		private static final HibernateTools INSTANCE = new HibernateTools();
	}

	private HibernateTools() {
	}

	public static final HibernateTools getInstance(File hibernateCfgXmlFile) {
		HibernateTools hibernateTools = SingletonHolder.INSTANCE;
		hibernateTools.setHibernateCfgXmlFile(hibernateCfgXmlFile);
		return hibernateTools;
	}

	private File hibernateCfgXmlFile;

	public File getHibernateCfgXmlFile() {
		return hibernateCfgXmlFile;
	}

	public void setHibernateCfgXmlFile(File hibernateCfgXmlFile) {
		this.hibernateCfgXmlFile = hibernateCfgXmlFile;
	}

	@Override
	public void run() {
		if (hibernateCfgXmlFile == null || !hibernateCfgXmlFile.exists()) {
			throw new RuntimeException("找不到hibernate.cfg.xml配置文件");
		}

		System.out.println("----------------代码生成开始-----------------------");

		// 直接给绝对路径会出问题
		Configuration xmlcfg = new Configuration().configure(hibernateCfgXmlFile);

		JDBCMetaDataConfiguration cfg = new JDBCMetaDataConfiguration();
		Properties properties = xmlcfg.getProperties();
		cfg.setProperties(properties);

		DefaultReverseEngineeringStrategy configurableNamingStrategy = new DefaultReverseEngineeringStrategy();
		configurableNamingStrategy.setSettings(new ReverseEngineeringSettings(configurableNamingStrategy)
				.setDefaultPackageName(getString(properties, "custom.package", "com"))// 要生成的包名
				.setCreateCollectionForForeignKey(getBoolean(properties, "custom.one2many", false))// 是否生成many-to-one的在one端的集合类,
				// 就是一对多的关系
				.setCreateManyToOneForForeignKey(getBoolean(properties, "custom.many2one", true))// 是否生成many-to-one
				.setDetectManyToMany(getBoolean(properties, "custom.many2many", true))// 是否生成many-to-many
				.setDetectOptimisticLock(getBoolean(properties, "custom.detectOptimisticLock", true)) // 乐观锁对象？
		);
		cfg.setReverseEngineeringStrategy(configurableNamingStrategy);
		// cfg.readFromJDBC();// 不区分schema和catlog的话,容易碰到错误.
		cfg.readFromJDBC(getString(properties, "custom.catlog", "Test"), getString(properties, "custom.schema", "dbo"));// 只从数据的这些信息生成
		cfg.buildMappings();

		if (getBoolean(properties, "custom.genPojo", true)) {
			POJOExporter exporter = new POJOExporter(cfg, getOutputDir(properties));
			if (getBoolean(properties, "custom.isAnnotation", true)) {
				exporter.getProperties().setProperty("ejb3", "true");// ejb3注解
				exporter.getProperties().setProperty("jdk5", "true");// jdk5语法(主要是集合类的泛型处理)
			}
			exporter.start();
		}

		if (getBoolean(properties, "custom.genDao", false)) {
			DAOExporter daoExporter = new DAOExporter(cfg, getOutputDir(properties));
			daoExporter.start();
		}

		System.out.println("----------------代码生成完成-----------------------");
	}

	private File getOutputDir(Properties properties) {
		File file = new File(getString(properties, "custom.outputDir", "/"));// 生成项目的物理位置（跟目录，tools会自动根据pacakge建立相应路径）
		return file;
	}

	private static boolean getBoolean(Properties properties, String key, boolean defaultValue) {
		String val = properties.getProperty(key);
		if (isBlank(val)) {
			return defaultValue;
		}
		return Boolean.valueOf(val);
	}

	private static String getString(Properties properties, String key, String defaultValue) {
		String val = properties.getProperty(key);
		if (isBlank(val)) {
			return defaultValue;
		}
		return val;
	}

	private static boolean isBlank(String val) {
		if (val == null || val.trim().length() == 0)
			return true;
		return false;
	}
}
package org.hibernate;

import java.io.File;

import org.junit.Test;

public class Testor {

	@Test
	public void testHibernateTools() throws InterruptedException{
		String path = HibernateTools.class.getResource("/").toString();
		if (path.startsWith("file:"))
			path = path.substring("file:".length() + 1);

		String fileName = path + "hibernate.cfg.xml";
		
		System.out.println(fileName);
		
//		String fileName = "H:/IsharedSpace/Ishared/Ishared-common-hibernate-tools/target/test-classes/hibernate.cfg.xml";
		File file = new File(fileName);
		new Thread(HibernateTools.getInstance(file)).start();
		
		Thread.sleep(10*1000);
	}

}

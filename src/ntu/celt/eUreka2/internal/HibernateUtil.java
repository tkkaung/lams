package ntu.celt.eUreka2.internal;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();
    private static SessionFactory buildSessionFactory() {
        try {
            // Resource configResource = new ClasspathResource("hibernate.cfg.xml");
             //return new Configuration().configure(configResource.toURL()).buildSessionFactory();
        	
        	// Create the SessionFactory from hibernate.cfg.xml
        //	return new Configuration().configure().buildSessionFactory();
        	Resource configResource = new ClasspathResource("hibernate.cfg.xml");
            return new Configuration().configure(configResource.toURL()).buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println(ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    
    private static final SessionFactory sessionFactory2 = buildSessionFactory2();
    private static SessionFactory buildSessionFactory2() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            //return new Configuration().configure("hibernate.eureka1.cfg.xml").buildSessionFactory();
            Resource configResource = new ClasspathResource("hibernate.cfg.xml");
            return new Configuration().configure(configResource.toURL()).buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory2 creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory2() {
        return sessionFactory2;
    }

    private static final SessionFactory sessionFactory3 = buildSessionFactory3();
    private static SessionFactory buildSessionFactory3() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            //return new Configuration().configure("hibernate.eureka1.cfg.xml").buildSessionFactory();
            Resource configResource = new ClasspathResource("hibernate.cfg.xml");
            return new Configuration().configure(configResource.toURL()).buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory3 creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory3() {
        return sessionFactory3;
    }
    
}

package ua.edg.logparser.parser;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import transimpex.logParser.TableRowDTO;

public class DataBaseReader{

	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory(){
		if(sessionFactory == null || sessionFactory.isClosed()){
			sessionFactory = buildSessionFactory();
		}
		return sessionFactory;
	}

	private SessionFactory buildSessionFactory(){
		try{
			Configuration configuration = new Configuration();
			configuration.configure();
			configuration.addAnnotatedClass(TableRowDTO.class);

			return configuration.buildSessionFactory();
		}
		catch(Exception e){
			throw new ExceptionInInitializerError("Initial SessionFactory failed: " + e);
		}
	}

	public void close(){
		if(sessionFactory != null && !sessionFactory.isClosed()){
			sessionFactory.close();
		}
	}
}

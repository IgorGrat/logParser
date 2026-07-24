package ua.edg.logparser.Models;

import lombok.ToString;
import org.hibernate.Session;
import org.hibernate.Transaction;
import transimpex.logParser.TableRowDTO;
import ua.edg.logparser.parser.DataBaseReader;

import java.util.List;

@ToString
public class TableRowDAO{

	DataBaseReader reader = new DataBaseReader();


	public void save(TableRowDTO tableRowDTO){
		Session session = reader.getSessionFactory().openSession();
		Transaction tx1 = session.beginTransaction();
		session.persist(tableRowDTO);
		tx1.commit();
		session.close();
	}

	public void update(TableRowDTO tableRowDTO){
		Session session = reader.getSessionFactory().openSession();
		Transaction tx1 = session.beginTransaction();
		session.persist(tableRowDTO);
		tx1.commit();
		session.close();
	}

	public void delete(TableRowDTO tableRowDTO){
		Session session = reader.getSessionFactory().openSession();
		Transaction tx1 = session.beginTransaction();
		session.detach(tableRowDTO);
		tx1.commit();
		session.close();
	}

	public List<TableRowDTO> findAll() {
		try (Session session = reader.getSessionFactory().openSession()) {
			return session.createQuery("from TableRowDTO", TableRowDTO.class).list();
		}
	}

}

package ua.edg.logparser.Models;

import lombok.ToString;
import org.hibernate.Session;
import org.hibernate.Transaction;
import transimpex.logParser.TableRowDTO;
import ua.edg.logparser.parser.DataBaseReader;

import java.time.LocalDateTime;
import java.util.List;

@ToString
public class TableRowDAO{

	DataBaseReader reader = new DataBaseReader();


	public void save(TableRowDTO tableRowDTO){
		Session session = reader.getSessionFactory().openSession();
		Transaction tx1 = session.beginTransaction();
		clearObjectFields(tableRowDTO);
		session.persist(tableRowDTO);
		tx1.commit();
		session.close();
	}

	public void save(List<TableRowDTO> tableRowDTOS){
		Session session = reader.getSessionFactory().openSession();
		Transaction tx1 = session.beginTransaction();
		for(TableRowDTO tableRowDTO : tableRowDTOS){
			clearObjectFields(tableRowDTO);
			session.persist(tableRowDTO);
		}
		tx1.commit();
		session.close();
	}

	public void update(TableRowDTO tableRowDTO){
		Session session = reader.getSessionFactory().openSession();
		Transaction tx1 = session.beginTransaction();
		clearObjectFields(tableRowDTO);
		session.persist(tableRowDTO);
		tx1.commit();
		session.close();
	}

	public void delete(TableRowDTO tableRowDTO){
		Session session = reader.getSessionFactory().openSession();
		Transaction tx1 = session.beginTransaction();
		clearObjectFields(tableRowDTO);
		session.detach(tableRowDTO);
		tx1.commit();
		session.close();
	}

//	public List<TableRowDTO> get

	public List<TableRowDTO> getAll(){
		try(Session session = reader.getSessionFactory().openSession()){
			return session.createQuery("from TableRowDTO", TableRowDTO.class).list();
		}
	}

	public List<TableRowDTO> getAllByMask(String where){
		try(Session session = reader.getSessionFactory().openSession()){
			return session.createQuery("from TableRowDTO where " + where, TableRowDTO.class).list();
		}
	}

	public List<TableRowDTO> getAllByDateRange(LocalDateTime since, LocalDateTime until){
		try(Session session = reader.getSessionFactory().openSession()){
			return session.createQuery(
							"from TableRowDTO where dateTime > :since and dateTime < :until",
							TableRowDTO.class
					)
					.setParameter("since", since)
					.setParameter("until", until)
					.list();
		}
	}

	private static void clearObjectFields(TableRowDTO tableRowDTO){
		String serverResponse = tableRowDTO.getServerResponse().substring
						(tableRowDTO.getServerResponse().indexOf("process"))
				.replace(")>", "");
		String clazz = tableRowDTO.getClazz();
		if(clazz.endsWith(".")) tableRowDTO.setClazz(clazz.substring(0, clazz.length() - 1));
		tableRowDTO.setServerResponse(serverResponse);
	}
}

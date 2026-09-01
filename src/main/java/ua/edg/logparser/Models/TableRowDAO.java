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

	public void save(List<TableRowDTO> tableRowDTOs){
		if(tableRowDTOs == null || tableRowDTOs.isEmpty()){
			return;
		}
		Transaction transaction = null;
		try(Session session = reader.getSessionFactory().openSession()){
			transaction = session.beginTransaction();
			for(int i = 0; i < tableRowDTOs.size(); i++){
				TableRowDTO tableRowDTO = tableRowDTOs.get(i);
				clearObjectFields(tableRowDTO);
				session.persist(tableRowDTO);
				if((i + 1) % 5000 == 0){
					session.flush();
					session.clear();
				}
			}
			session.flush();
			transaction.commit();
		}
		catch(RuntimeException e){
			if(transaction != null && transaction.isActive()){
				transaction.rollback();
			}
			System.err.println("Failed to save " + tableRowDTOs.size() + " rows");
			throw e;
		}
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

	public List<TableRowDTO> getAllByMask(LocalDateTime since, LocalDateTime until, String mask){
		String trueMask = "%" + mask + "%";
		try(Session session = reader.getSessionFactory().openSession()){
			return session.createQuery("""
								from TableRowDTO
								where dateTime between :since and :until
								and (login like :mask
								or host like :mask
								or clazz like :mask
								or method like :mask
								or param like :mask
								or serverResponse like :mask)
								order by dateTime desc
							""", TableRowDTO.class)
					.setParameter("since", since)
					.setParameter("until", until)
					.setParameter("mask", trueMask)
					.list();
		}
	}

	public List<TableRowDTO> getAllByLoginAndMethod(LocalDateTime since, LocalDateTime until, String login, String method){
		String trueLogin = login + "%";
		String trueMethod = method + "%";
		try(Session session = reader.getSessionFactory().openSession()){
			return session.createQuery("""
							from TableRowDTO
							where dateTime between :since and :until
							and login like :login
							and method like :method
							order by dateTime desc
							""", TableRowDTO.class)
					.setParameter("since", since)
					.setParameter("until", until)
					.setParameter("login", trueLogin)
					.setParameter("method", trueMethod)
					.list();
		}
	}

	public List<TableRowDTO> getAllByLogin(LocalDateTime since, LocalDateTime until, String login){
		String trueLogin = login + "%";
		try(Session session = reader.getSessionFactory().openSession()){
			return session.createQuery("""
							from TableRowDTO
							where dateTime between :since and :until
							and login like :login
							order by dateTime desc
							""", TableRowDTO.class)
					.setParameter("since", since)
					.setParameter("until", until)
					.setParameter("login", trueLogin)
					.list();
		}
	}

	// 2,388 ms
	public List<TableRowDTO> getAllByDateRange(LocalDateTime since, LocalDateTime until){
		try(Session session = reader.getSessionFactory().openSession()){
			return session.createQuery("from TableRowDTO where dateTime between :since and :until", TableRowDTO.class)
					.setParameter("since", since)
					.setParameter("until", until).list();
		}
	}

	//2,315 ms
	public List<TableRowDTO> getActiveLoginsByDateRange(
			LocalDateTime since,
			LocalDateTime until,
			List<String> validLogins,
			String excludeMethod
	){
		try(Session session = reader.getSessionFactory().openSession()){
			String sql = """
					SELECT * FROM server_logs_2026
					WHERE dateTime between :since AND :until
					  AND login IN (:logins)
					  AND method != :excludeMethod
					""";

			return session.createNativeQuery(sql, TableRowDTO.class)
					.setParameter("since", since)
					.setParameter("until", until)
					.setParameter("logins", validLogins)
					.setParameter("excludeMethod", excludeMethod)
					.list();
		}
	}

	public List<TableRowDTO> getWorkTimeByDateRange(
			LocalDateTime since,
			LocalDateTime until,
			List<String> validLogins,
			String excludeMethod
	){
		try(Session session = reader.getSessionFactory().openSession()){
			String sql = """
					SELECT * FROM server_logs_2026
					WHERE dateTime between :since AND :until
					  AND (
					      TIME(dateTime) > '08:00:00'
					      OR TIME(dateTime) < '19:00:00'
					  )
					  AND login IN (:logins)
					  AND method != :excludeMethod
					ORDER BY dateTime desc
					""";

			return session.createNativeQuery(sql, TableRowDTO.class)
					.setParameter("since", since)
					.setParameter("until", until)
					.setParameter("logins", validLogins)
					.setParameter("excludeMethod", excludeMethod)
					.list();
		}
	}

	private static void clearObjectFields(TableRowDTO tableRowDTO){
		String serverResponse = tableRowDTO.getServerResponse().substring(tableRowDTO.getServerResponse().indexOf("process")).replace(")>", "");
		String clazz = tableRowDTO.getClazz();
//		tableRowDTO.setId(0L);
		if(clazz.endsWith(".")) tableRowDTO.setClazz(clazz.substring(0, clazz.length() - 1));
		tableRowDTO.setServerResponse(serverResponse);
	}
}

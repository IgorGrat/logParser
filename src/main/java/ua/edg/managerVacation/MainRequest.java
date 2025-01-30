package ua.edg.managerVacation;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import transimpex.schaduleVacation.dto.SchedulePeriodUser;

/**
 *
 * @author uncle
 */
public class MainRequest{
  public final static String PATH = "./";
  public final static String FILE_NAME = "vacationEmployee.dat";
  
  public static synchronized List<SchedulePeriodUser> getUsersVacation(){
    Object resource = makeResource(PATH, FILE_NAME);
    return resource == null? 
    new ArrayList<>() : (List<SchedulePeriodUser>)resource;
  }
  public static synchronized List<SchedulePeriodUser> getUserVacation(int userId){
    List<SchedulePeriodUser> users = getUsersVacation();
    List<SchedulePeriodUser> user = users.stream()
    .filter(usr -> usr.getEmployeeId() == userId)
    .collect(Collectors.toList());
    return user;
  }
  public static synchronized List<SchedulePeriodUser> saveUsersVacation(
  List<SchedulePeriodUser> userPack){
    Object resource = makeResource(PATH, FILE_NAME);
    List<SchedulePeriodUser> users = resource == null? 
    new ArrayList<>() : (List<SchedulePeriodUser>)resource;
    Set<Integer> confirmed = userPack.stream()
    .map(SchedulePeriodUser::getEmployeeId)
    .collect(Collectors.toSet());
    List<SchedulePeriodUser> clearUser = users.stream()
    .filter(us -> ! confirmed.contains(us.getEmployeeId()))
    .collect(Collectors.toList());
    clearUser.addAll(userPack);
    saveResource(PATH, FILE_NAME, clearUser);
    return new ArrayList<>();
  }
  public static synchronized List<SchedulePeriodUser> clearUsersVacation(int user){
    Object resource = makeResource(PATH, FILE_NAME);
    List<SchedulePeriodUser> users = resource == null? 
    new ArrayList<>() : (List<SchedulePeriodUser>)resource;
    List<SchedulePeriodUser> clearUser = users.stream()
    .filter(us -> user != us.getEmployeeId())
    .collect(Collectors.toList());
    saveResource(PATH, FILE_NAME, clearUser);
    return new ArrayList<>();
  }
  public static Object makeResource(String path, String name){
    Object target = null; File file = new File(path + name); 
    if(! file.exists()){return null;}
    try(ObjectInputStream ois = new ObjectInputStream(
      Files.newInputStream(file.toPath()))){
      target = ois.readObject();
    }
    catch(ClassNotFoundException | IOException e){
      LogManager.getLogger(MainRequest.class).error(e);
    }
    return target;
  }
  public static synchronized void saveResource(String path, String name,
  Object object){
    String source = path + name;
    File backup = new File(source.substring(0, source.length() - 3) + "bac"); 
    try{
      boolean isRename = new File(source).renameTo(backup);
      try(ObjectOutputStream oos = 
      new ObjectOutputStream(new FileOutputStream(source))){
        oos.writeObject(object);
      }
      boolean isDelete = backup.delete();
    }
    catch(FileNotFoundException e){
      LogManager.getLogger(MainRequest.class).error(e.getMessage(), e);
    }
    catch(IOException e){
      LogManager.getLogger(MainRequest.class).error("IOException", e);
    }
  }
}

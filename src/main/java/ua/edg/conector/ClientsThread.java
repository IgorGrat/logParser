 package ua.edg.conector;
 
 import java.awt.Color;
import java.io.Externalizable;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
 import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
 import javax.swing.SwingUtilities;

 import org.apache.logging.log4j.LogManager;
 import transimpex.*;
 import transimpex.schaduleVacation.dto.SchedulePeriodUser;
 import ua.edg.logparser.gui.Panel;

import static ua.edg.conector.Utilities.PROJECT_PATH;
 
 
public class ClientsThread extends Thread {
   private final Socket socket;
   
  public ClientsThread(Socket ss){
    socket = ss;
  }
  @Override
  public void run(){
    try(Socket ss = socket;
    ObjectInputStream ois = new ObjectInputStream(ss.getInputStream());
    ObjectOutputStream oos = new ObjectOutputStream(ss.getOutputStream())){
      List<?> multiTask = (List<?>)ois.readObject();
      SwingUtilities.invokeLater(() -> Panel.globalLinkPanel.setColor(Color.red));
      Title title = (Title)multiTask.remove(0);
      String user = title.user;
      List<Exterclon> result = new ArrayList<>();
      for(Object task : multiTask){
        CommandQuery query = (CommandQuery)task;
        String class_str = query.form; String method_str = query.method;
        Class<?> clazz = Class.forName(PROJECT_PATH + class_str);
        Constructor<?> constructor = clazz.getConstructor();
        Object instance = constructor.newInstance();
        switch(method_str){
          case "getWorkTimePersonalForPeriod" :
          case "getWorkTimeOneDay" :{
            result = getResult(clazz, method_str, instance, query);
            break;
          }
          case "saveDirectUserVacation" :
          case "saveUsersVacation" : {
            Method method =  clazz.getMethod(method_str, List.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<SchedulePeriodUser> users = (List<SchedulePeriodUser>)query.option;
            result = (List<Exterclon>)method.invoke(instance, users);
            break;
          }
          case "clearUsersVacation" :
          case "getUserVacation" :{
            Method method =  clazz.getMethod(method_str, int.class);
            method.setAccessible(true);
            int userId = (Integer)query.option;
            result = (List<Exterclon>)method.invoke(instance, userId);
            break;
          }
          case "getUsersVacation" : {
            Method method =  clazz.getMethod(method_str);
            method.setAccessible(true);
            result = (List<Exterclon>)method.invoke(instance);
            break;
          }
          default :
          result = new ArrayList<>();
        }
      }
      QuickObjectArray<?> list = new QuickObjectArray<>(result);
      list.writeExternal(oos);
//      for(Externalizable externalizable : result){
//        externalizable.writeExternal(oos);
//      }
//      oos.flush();
      SwingUtilities.invokeLater(() -> Panel.globalLinkPanel.setColor(Color.gray));
    }
    catch (IOException|ClassNotFoundException | IllegalAccessException | 
    IllegalArgumentException | InvocationTargetException | 
    NoSuchMethodException | SecurityException | InstantiationException ex) {
      LogManager.getLogger(ClientsThread.class).error(ex);
    }
  }
  private List<Exterclon> getResult(Class<?> clazz, String method_str,
  Object instance, CommandQuery query) throws NoSuchMethodException, 
  IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    Method method =  clazz.getMethod(method_str, WrapAgent.class);
    method.setAccessible(true);
    WrapAgent params = (WrapAgent)query.option;
    List<Exterclon> result = new ArrayList<>();
    result.add((Exterclon)method.invoke(instance, params));
    return result;
  }
}

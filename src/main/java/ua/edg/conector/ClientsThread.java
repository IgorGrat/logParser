 package ua.edg.conector;
 
import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import transimpex.*;
import transimpex.schaduleVacation.dto.SchedulePeriodUser;
import transimpex.serviceTools.ServiceToolsTimeDate;
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
      String user = title.getUser();
      List<Exterclon> result = new ArrayList<>();
      for(Object task : multiTask){
        CommandQuery query = (CommandQuery)task;
        String class_str = query.getForm(); String method_str = query.getMethod();
        Class<?> clazz = Class.forName(PROJECT_PATH + class_str);
        Constructor<?> constructor = clazz.getConstructor();
        Object instance = constructor.newInstance();
        switch(method_str){
          case "getWorkTimePersonalForPeriod" :
          case "getWorkTimeOneDay" :{
            result = getResult(clazz, method_str, instance, query);
            break;
          }

          case "getLogTableRows" : {
            Method method =  clazz.getMethod(method_str, String.class, String.class, LocalDateTime.class, LocalDateTime.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> params = (List<String>)(query.getOption());
            String login = params.get(0);
            String pattern = params.get(1);
            LocalDateTime first = ServiceToolsTimeDate.getLocalDateTime(params.get(2));
            LocalDateTime second = ServiceToolsTimeDate.getLocalDateTime(params.get(3));
            @SuppressWarnings("unchecked")
            List<Exterclon> invoke = (List<Exterclon>) method.invoke(instance, login, pattern, first, second);
            result = invoke;
            break;
          }

          case "saveDirectUserVacation" :
          case "saveUsersVacation" : {
            Method method =  clazz.getMethod(method_str, List.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<SchedulePeriodUser> users = (List<SchedulePeriodUser>)query.getOption();
            @SuppressWarnings("unchecked")
            List<Exterclon> invoke = (List<Exterclon>) method.invoke(instance, users);
            result = invoke;
            break;
          }
          case "clearUsersVacation" :
          case "getUserVacation" :{
            Method method =  clazz.getMethod(method_str, int.class);
            method.setAccessible(true);
            int userId = (Integer)query.getOption();
            @SuppressWarnings("unchecked")
            List<Exterclon> invoke = (List<Exterclon>) method.invoke(instance, userId);
            result = invoke;
            break;
          }
          case "getUsersVacation" : {
            Method method =  clazz.getMethod(method_str);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Exterclon> invoke = (List<Exterclon>) method.invoke(instance);
            result = invoke;
            break;
          }
          default :
          result = new ArrayList<>();
        }
      }
      QuickObjectArray<?> list = new QuickObjectArray<>(result);
      list.writeExternal(oos);
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
    WrapAgent params = (WrapAgent)query.getOption();
    List<Exterclon> result = new ArrayList<>();
    result.add((Exterclon)method.invoke(instance, params));
    return result;
  }
}

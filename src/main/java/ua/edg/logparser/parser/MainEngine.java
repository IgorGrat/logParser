package ua.edg.logparser.parser;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import transimpex.WrapAgent;


public class MainEngine{

  public WrapAgent getWorkTime(WrapAgent source){
    LocalDate first = LocalDate.ofEpochDay(source.packlng[0][0]);
    LocalDate second = LocalDate.ofEpochDay(source.packlng[0][1]);
    return ChronoUnit.DAYS.between(first, second) > 0? 
    getWorkTimePersonalForPeriod(source) : getWorkTimeOneDay(source);
  }
  public WrapAgent getWorkTimeOneDay(WrapAgent source){
    List<String> users = new ArrayList<>();
    List<long[]> hours = new ArrayList<>();
    new WorkTimePersonalCounter(){
      @Override
      protected void getUsersData(String user, UserTimeCount utc){
        users.add(user);
        OneDayUnit odu = utc.daysPeriod.get(0);
        long[] units = odu.workingPeriodHours;
        int length = units.length;
        long[] total = Arrays.copyOf(units, length + 1);
        total[length] = odu.busy_second;
        hours.add(total);
      }
    }.scannerPeriod(source);
    WrapAgent agent = new WrapAgent();
    int users_size = users.size();
    agent.packstr = new String[][]{users.toArray(new String[users_size])};
    agent.packlng = hours.toArray(new long[users_size][]);
    return agent;
  }
  public WrapAgent getWorkTimePersonalForPeriod(WrapAgent source){
    List<String> users = new ArrayList<>();
    List<long[]> data = new ArrayList<>();
    new WorkTimePersonalCounter(){
      @Override
      protected void getUsersData(String user, UserTimeCount utc){
        users.add(user);
        List<OneDayUnit> dayUnits = utc.daysPeriod;
        data.add(dayUnits.stream().mapToLong(e -> e.busy_second).toArray());
      }
    }.scannerPeriod(source);
    WrapAgent agent = new WrapAgent();
    int users_size = users.size();
    agent.packstr = new String[][]{users.toArray(new String[users_size])};
    agent.packlng = data.toArray(new long[users_size][]);
    return agent;
  }
}

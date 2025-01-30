package ua.edg.logparser.parser;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import transimpex.WrapAgent;

import static ua.edg.logparser.gui.Panel.PATH;

/**
 *
 * @author uncle
 */
public class WorkTimePersonalCounter{
  
  public class OneDayUnit{
    public long busy_second;
    public final long[] workingPeriodHours;
    private final LocalDateTime startMomemtOfDay;
    private final LocalDateTime endMomentDay;
    private LocalDateTime lastActivity;
    
    public OneDayUnit(int workHours, LocalDateTime day){
      this.workingPeriodHours = new long[workHours];
      this.startMomemtOfDay = day; 
      this.lastActivity = day;
      this.endMomentDay = day.plusHours(workHours);
    }
  }
  public class UserTimeCount{
    
    public final List<OneDayUnit> daysPeriod;
    private final LocalDateTime startPeriod;
    private final LocalDateTime endPeriod;
    private final long hours;
    private final int allowTimeToRestSec;
    private final int shiftTime;

    public UserTimeCount(LocalDateTime startPeriod, LocalDateTime endPeriod, 
    int hours, int allowTimeToRestMin, int shiftTime){
      this.startPeriod = startPeriod;
      this.endPeriod = endPeriod;
      int days = (int)ChronoUnit.DAYS.between(startPeriod, endPeriod) + 1;
      daysPeriod = new ArrayList<>(days);
      LocalDateTime previousDay = startPeriod.minusDays(1);
      for(int i = 0; i < days; i ++){
        daysPeriod.add(new OneDayUnit(hours, previousDay = previousDay.plusDays(1)));
      }
      this.hours = hours;
      this.allowTimeToRestSec = allowTimeToRestMin * 60;
      this.shiftTime = shiftTime;
    }
    public boolean addActivity(LocalDateTime currentActivity){
      if(currentActivity.isBefore(startPeriod)){
        return true;
      }
      if(currentActivity.isAfter(endPeriod)){
        return false;
      }
      int diffDay = (int)ChronoUnit.DAYS.between(startPeriod, currentActivity);
      OneDayUnit dayUnit = daysPeriod.get(diffDay);
      if(currentActivity.isBefore(dayUnit.startMomemtOfDay) || 
        currentActivity.isAfter(dayUnit.endMomentDay)){
        return true;
      }
      long second_current_activity = ChronoUnit.SECONDS.between(
      dayUnit.lastActivity, currentActivity);
      if(second_current_activity <= allowTimeToRestSec){
        dayUnit.busy_second += second_current_activity;
        dayUnit.lastActivity = currentActivity;
        return true;
      }
      LocalDateTime start = dayUnit.lastActivity.plusSeconds(30).withSecond(0);
      LocalDateTime end = currentActivity.plusSeconds(30).withSecond(0);
      long minute_withOut_work = ChronoUnit.MINUTES.between(start, end);
      int first_min_period = start.getMinute();
      int hour = start.getHour();

      int minInHour = 60;
      long[] itemHours = dayUnit.workingPeriodHours;
      while(minute_withOut_work > 0 && hour - shiftTime < hours){
        int rest_of_hour = minInHour - first_min_period;
        int able_quantity_min_in_hour = (int)Math.min(
        rest_of_hour, minute_withOut_work) + first_min_period;
        for(int begin = first_min_period; begin < able_quantity_min_in_hour; begin ++){
          itemHours[hour - shiftTime] = itemHours[hour - shiftTime] + (1L << 59 - begin);
          minute_withOut_work --;
        }
        first_min_period = 0; hour ++;
      }
      dayUnit.lastActivity = currentActivity;
      return true;
    }
    public void closeTimePeriod(){
      for(OneDayUnit odu : daysPeriod){
        LocalDateTime endPeriod = odu.endMomentDay;
        addActivity(endPeriod);
      }
    }
  }
  public void scannerPeriod(WrapAgent source){
    String[] logins = source.packstr[0];
    LocalTime beginLocalTime = LocalTime.of(8, 0);
    int period_hours = 11;
    LocalTime endLocalTime = beginLocalTime.plusHours(period_hours);
    String prefix = "log.txt.";
    int allowToRestMin = 15;
    long[] dates = source.packlng[0];
    LocalDate first = LocalDate.ofEpochDay(dates[0]);
    LocalDate second = LocalDate.ofEpochDay(dates[1]);
    LocalDateTime from_this_date = LocalDateTime.of(first, beginLocalTime);
    LocalDateTime to_this_date = LocalDateTime.of(second, endLocalTime);
    File folder = new File(PATH);
    final Map<String, UserTimeCount> scoope = new HashMap<>();
    int shift_time = beginLocalTime.getHour();
    for(String login : logins){
      scoope.put(login, new UserTimeCount(from_this_date, to_this_date, 
      period_hours, allowToRestMin, shift_time));
    }
    LocalFileRider fileReader = new LocalFileRider(){
      @Override
      protected void addItemToScope(TableRowDTO dTO){
        String login = dTO.login;
        UserTimeCount utc = scoope.get(login);
        if(utc == null){
          return;
        }
        if(dTO.clazz.equals("General.") && dTO.method.equals("getUserAttAction")){
          return;
        }
        if(! utc.addActivity(dTO.dateTime)){
          doAction = false;
        }
      }
    };
    int length = prefix.length();
    File[] files = folder.listFiles((dir, name)  -> name.substring(length).matches("[0-9]*"));
    /*---------------------------------------------------------------------------------------------------------------*/
    Arrays.sort(files, (o1, o2) -> (Integer.parseInt(o1.getName().substring(length)) > 
    Integer.parseInt(o2.getName().substring(length))) ? -1 : 1);

    for(int i = 0; i < files.length; i ++){
      File file = files[i];
      long last_mod_long = file.lastModified();
      if(last_mod_long != 0){
        LocalDateTime lastModified = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(last_mod_long), ZoneId.systemDefault());
        if(! lastModified.isBefore(from_this_date)){
          int previous_file = i - 1;
          LocalDateTime create_file = previous_file >= 0 ?
          LocalDateTime.ofInstant(Instant.ofEpochMilli(
          files[previous_file].lastModified()), ZoneId.systemDefault()) : from_this_date;
          if(create_file.isAfter(to_this_date)){
            break;
          }
          if(((create_file.isAfter(from_this_date) || create_file.isEqual(from_this_date)) 
          && create_file.isBefore(to_this_date)) || 
          ((lastModified.isAfter(from_this_date) || 
          lastModified.isEqual(from_this_date)) && lastModified.isBefore(to_this_date))){
            fileReader.getContents(file);
          }
        }
      }
    }
    scoope.forEach((key, timeCount) -> {
      timeCount.closeTimePeriod();
      getUsersData(key, timeCount);
    });
  }
  protected void getUsersData(String user, UserTimeCount utc){
  }
}

package ua.edg.conector;

import transimpex.schaduleVacation.dto.SchedulePeriodUser;
import ua.edg.managerVacation.MainRequest;

import java.util.List;

public class UpdaterFormalVacations{
  public static void updateVacation(){
    List<SchedulePeriodUser> scope = MainRequest.getUsersVacation();
    MainRequest.saveDirectUserVacation(scope);
  }
}

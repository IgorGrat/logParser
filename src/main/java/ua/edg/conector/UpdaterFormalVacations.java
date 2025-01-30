package ua.edg.conector;

import transimpex.schaduleVacation.dto.SchedulePeriodUser;
import ua.edg.managerVacation.MainRequest;

import java.util.List;

import static ua.edg.managerVacation.MainRequest.getUsersVacation;

public class UpdaterFormalVacations{
  public static void main(String[] args){
    /* Первый этап ***********************************************/
    List<SchedulePeriodUser> scope = MainRequest.getUsersVacation();
    MainRequest.saveUsersVacation(scope);
    /* После этого меняем readObject на boolean из потока */
  }
}

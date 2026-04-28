package ua.edg.logparser.parser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScopeData{
  public List<TableRowDTO> getTableRow(String user, LocalDateTime since, LocalDateTime until){
    List<TableRowDTO> list = new ArrayList<>();
    new LocalFileRider(since, until){
      @Override
      protected void addItemToScope(TableRowDTO rowDTO){
        if(rowDTO.getLogin().equals(user)){
          list.add(rowDTO);
        }
      }
    }.doAction();
    return list;
  }
}

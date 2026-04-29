package ua.edg.logparser.parser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScopeData{
  public List<transimpex.logParser.TableRowDTO> getLogTableRows(String user, String pattern,
  LocalDateTime since, LocalDateTime until){
    List<transimpex.logParser.TableRowDTO> list = new ArrayList<>();
    new LocalFileRider(since, until){
      @Override
      protected void addItemToScope(transimpex.logParser.TableRowDTO rowDTO){
        if(rowDTO.getLogin().equals(user) &&
        (pattern.isEmpty() ||
        rowDTO.getClazz().contains(pattern) ||
        rowDTO.getMethod().contains(pattern) ||
        rowDTO.getParam().contains(pattern) ||
        rowDTO.getServerResponse().contains(pattern))){
          list.add(rowDTO);
        }
      }
    }.doAction();
    return list;
  }
}

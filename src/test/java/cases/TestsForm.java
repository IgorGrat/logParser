package cases;

import java.io.File;
import org.junit.jupiter.api.Test;

/**
 *
 * @author uncle
 */
public class TestsForm{
  
  @Test
  public void testPattern(){
    ClassLoader loader = getClass().getClassLoader();
    File file = new File(loader.getResource("log.txt.2").getFile());
  }
}

import java.sql.SQLException;

import com.YaNan.frame.hibernate.database.DBFactory;
import com.YaNan.frame.plugin.PlugsFactory;

public class TransTest {
	public static void main(String[] args) throws SQLException {
		DBFactory.getDBFactory().init();
		Resour
		Test s = PlugsFactory.getPlugsInstance(Test.class);
		for(int i = 0;i<10;i++) {
			try {
				s.testTransaction();
			} catch (Exception e) {
			}
		}
		
	}
}

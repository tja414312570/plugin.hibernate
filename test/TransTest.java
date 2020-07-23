import java.sql.SQLException;

import javax.sql.DataSource;

import com.YaNan.frame.jdb.JDBContext;
import com.yanan.frame.plugin.PlugsFactory;

public class TransTest {
	public static void main(String[] args) throws SQLException {
		DataSource defaultDataSource = PlugsFactory.getPlugsInstance(DataSource.class);
		System.out.println(defaultDataSource);
		JDBContext hibernateBuilder = PlugsFactory.getPlugsInstance(JDBContext.class);
		Test s = PlugsFactory.getPlugsInstance(Test.class);
		for(int i = 0;i<10;i++) {
			try {
				s.testTransaction();
			} catch (Exception e) {
			}
		}
		
	}
}

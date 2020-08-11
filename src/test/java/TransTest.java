import java.sql.SQLException;

import javax.sql.DataSource;

import com.yanan.framework.jdb.JDBContext;
import com.yanan.framework.plugin.PlugsFactory;

public class TransTest {
	public static void main(String[] args) throws SQLException {
		DataSource defaultDataSource = PlugsFactory.getPluginsInstance(DataSource.class);
		System.out.println(defaultDataSource);
		JDBContext hibernateBuilder = PlugsFactory.getPluginsInstance(JDBContext.class);
		Test s = PlugsFactory.getPluginsInstance(Test.class);
		for(int i = 0;i<10;i++) {
			try {
				s.testTransaction();
			} catch (Exception e) {
			}
		}
		
	}
}

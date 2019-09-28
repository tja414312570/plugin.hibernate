import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import com.YaNan.frame.plugin.PlugsFactory;
import com.YaNan.frame.plugin.handler.PlugsHandler;

public class DataSourceTest {
	public static void main(String[] args) {
		DataSource defaultDataSource = PlugsFactory.getPlugsInstance(DataSource.class);
		PlugsHandler plugsHandler = PlugsFactory.getPlugsHandler(defaultDataSource);
		System.out.println(defaultDataSource);
		Connection connection;
		try {
			for(int i = 0;i<100;i++) {
				connection = defaultDataSource.getConnection();
				System.out.println(i+"   "+connection);
				connection.close();
			}
			System.out.println(defaultDataSource.getConnection());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}

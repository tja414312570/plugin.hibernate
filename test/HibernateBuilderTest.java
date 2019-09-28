import javax.sql.DataSource;

import com.YaNan.frame.jdb.builder.HibernateBuilder;
import com.YaNan.frame.jdb.database.dataresource.DefaultDataSource;
import com.YaNan.frame.plugin.PlugsFactory;

public class HibernateBuilderTest {
	public static void main(String[] args) {
		DataSource defaultDataSource = PlugsFactory.getPlugsInstance(DataSource.class);
		System.out.println(defaultDataSource);
		HibernateBuilder hibernateBuilder = PlugsFactory.getPlugsInstance(HibernateBuilder.class);
		System.out.println(defaultDataSource);
	}
}

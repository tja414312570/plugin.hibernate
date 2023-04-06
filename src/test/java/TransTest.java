import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.yanan.framework.jdb.JDBContext;
import com.yanan.framework.jdb.datasource.DefaultDataSource;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.decoder.StandScanResource;
import com.yanan.utils.resource.Resource;
import com.yanan.utils.resource.ResourceManager;
import com.yanan.utils.string.StringUtil;

public class TransTest {
	public static void main(String[] args) throws SQLException {
		PlugsFactory.getInstance().addResource(new StandScanResource("classpath*:**"));
		PlugsFactory.init("plugin.yc");
		DataSource defaultDataSource = PlugsFactory.getPluginsInstance(DataSource.class);
		System.out.println(defaultDataSource);
		DefaultDataSource hibernateBuilder = PlugsFactory.getPluginsInstance(DefaultDataSource.class);
		Connection connection = hibernateBuilder.getConnection();
		List<Resource> resource = ResourceManager.getResourceList("C:\\Users\\tja41\\Desktop\\20211123\\*.sql");
		System.err.println(resource);
		resource.forEach(item->{
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(item.getInputStream())) ;
				String line ;
				while((line = reader.readLine()) != null) {
					if(!StringUtil.isEmpty(line) && !line.trim().startsWith("--") && !line.trim().startsWith("/*") ) {
						try {
							System.err.println("current line : "+line);
							connection.prepareStatement(line).execute();
							Thread.sleep(1);
						}catch(Exception e) {
							
						}
						
					}
					
				}
			} catch (IOException e) {
				//
			}
		});
		
	}
}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.YaNan.frame.hibernate.database.DBFactory;
import com.YaNan.frame.hibernate.database.SqlSession;
import com.YaNan.frame.plugin.PlugsFactory;

public class HibernateTest {
	public static void main(String[] args) {
		DBFactory.getDBFactory().init();
		SqlSession sql = PlugsFactory.getPlugsInstance(SqlSession.class);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> u = new HashMap<String,String>();
		u.put("id", "1024");
		u.put("name", "1024name");
		list.add(u);
		System.out.println((int)sql.insert("com.YaNan.debug.test.insert", list));
	}
}

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.YaNan.frame.jdb.JDBContext;
import com.YaNan.frame.jdb.SqlSession;
import com.YaNan.frame.jdb.fragment.SqlFragment;
import com.YaNan.frame.jdb.mapper.MapperInterfaceProxyBuilder;
import com.YaNan.frame.jdb.operate.Query;
import com.YaNan.frame.plugin.autowired.property.Property;
import com.YaNan.frame.plugin.autowired.property.PropertyManager;
import com.YaNan.frame.utils.resource.ResourceManager;
import com.yanan.frame.plugin.PlugsFactory;

public class HibernateBuilderTest2 {
	
	public static void main(String[] args) throws InterruptedException {
//		//转化上下文
		PlugsFactory.init();
//		//创建数据源
//		DataSource defaultDataSource = PlugsFactory.getPlugsInstance(DataSource.class);
//		//初始化JDB上下文
//		JDBContext hibernateBuilder = PlugsFactory.getPlugsInstance(JDBContext.class,defaultDataSource);
//		//初始化Mapper构造器
//		MapperInterfaceProxyBuilder proxy = PlugsFactory.getPlugsInstance(MapperInterfaceProxyBuilder.class,hibernateBuilder);
//		//参数
		Map<String,String> params = new HashMap<String,String>();
		params.put("name", "1024");
//		SqlFragment frag = hibernateBuilder.getSqlFragmentManger().getSqlFragment("TestSq.test1");
//		System.out.println(frag.getPreparedSql(params));
//		frag = hibernateBuilder.getSqlFragmentManger().getSqlFragment("TestSq.test");
//		System.out.println(frag.getPreparedSql(params));
		//SqlSession
		PlugsFactory ins = PlugsFactory.getInstance();
		System.out.println(PlugsFactory.getBean("sqlSession")+"");
		SqlSession sql = PlugsFactory.getBean(SqlSession.class);
		System.out.println(sql.selectOne("TestSq.test", params)+"");
		System.out.println(sql.selectOne("TestSq.test1", params)+"");
		Query query = new Query(UserTokenModel.class);
		query.query();
		TestSql testSql = PlugsFactory.getPlugsInstance(TestSql.class);
		testSql.test("1024", "");
	}
}

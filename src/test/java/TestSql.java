import java.util.Map;

import com.yanan.framework.jdb.annotation.Sql;
import com.yanan.framework.jdb.mapper.annotations.Param;
import com.yanan.framework.plugin.annotations.Service;
import com.yanan.framework.transaction.Transactions;

@Transactions
@Sql
@Service
public interface TestSql {
	
	@Transactions
	public void test(Map<String,String> map);
	public void test(@Param("name") String name,@Param("id")String id);
}

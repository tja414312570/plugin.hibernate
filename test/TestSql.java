import com.YaNan.frame.jdb.database.annotation.Sql;
import com.YaNan.frame.jdb.database.transaction.Transactions;
import com.YaNan.frame.plugin.annotations.Service;

@Transactions
@Sql
@Service
public interface TestSql {
	
	@Transactions
	public void post();

}

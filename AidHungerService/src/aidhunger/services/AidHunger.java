package aidhunger.services;
import javax.jws.*;

@WebService
public interface AidHunger {
	@WebMethod
	public boolean login(String username, String password);
	@WebMethod
	public boolean register(String username, String password);
}

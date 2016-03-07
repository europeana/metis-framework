package eu.europeana.metis.framework.ui.ldap;

import java.util.List;

public interface UserRepository <T>{

	/**
	 * This method is responsible to fetch all the user details as a list of
	 * String.
	 *
	 * @return list of String.
	 */
	public List<String> getAllUserNames();

	/**
	 * This method is responsible to fetch all the user details as a list of
	 * User object
	 *
	 * @return list of {@link T}
	 */
	public List<T> getAllUsers();

	/**
	 * This method is responsible to fetch user details of particular user.
	 *
	 * @return user details {@link T}
	 */
	public T getUserDetails(String userName);

	/**
	 * This method is responsible to fetch user details of particular user as a string.
	 *
	 * @return user detail {@link T}
	 */
	public Object getUserDetail(String userName);

	/**
	 * This method is responsible to authenticate user.
	 *
	 * @return boolean true|false
	 */
	public boolean authenticate(String base,String userName, String password);

	/**
	 * This method is responsible to update telephone number of user.
	 *
	 * @return boolean true|false
	 */
	public T updateTelePhone(String userName, String newNumber);

	/**
	 * This method is responsible to create user.
	 */
	public boolean createUser(T user);

	/**
	 * This method is responsible to delete user.
	 */
	public boolean remove(String uid);
}

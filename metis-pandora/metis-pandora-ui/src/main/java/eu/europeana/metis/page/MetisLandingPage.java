package eu.europeana.metis.page;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.europeana.metis.common.MetisPage;
import eu.europeana.metis.common.UserProfile;
import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.mapping.organisms.global.NavigationTopMenu;
import eu.europeana.metis.mapping.util.MetisMappingUtil;

/**
 * This web-page represents a Metis Landing page and all user account pages like: Login page,
 * User Profile page, Register User page.
 * @author alena
 *
 */
public class MetisLandingPage extends MetisPage {
	
	private PageView pageView = PageView.EMPTY;
	
	private UserProfile user;
	
	private Boolean isDuplicateUser = false;
	
	private Boolean isAuthError = false;
	
	private static final String ERROR_DUPLICATE_USER = "ERROR: The user with this email address already exists.";
	
	private static final String ERROR_AUTH = "ERROR: Your email or password is incorrect. Try again please.";

	private Map<String, Object> contentMap;
	
	@Override
	public Byte resolveCurrentPage() {
		return -1;
	}
	
	public MetisLandingPage(PageView pageView) {
		this(pageView, null);
	}

	public MetisLandingPage(PageView pageView, UserProfile user) {
		super();
		this.pageView = pageView;
		this.user = user;
	}
	
	@Override
	public void addPageContent(Map<String, Object> model) {
		if (this.pageView == PageView.EMPTY) {
			return;
		}
		this.contentMap = new HashMap<>();
		switch (pageView) {
		case LOGIN: 
			contentMap.put("is_login", true);
			buildLoginPageContent();
			break;
		case REGISTER: 
			contentMap.put("is_register", true);
			buildRegisterPageContent();
			break;
		case PROFILE: 
			contentMap.put("is_profile", true);
			buildProfilePageContent();
			break;
		default: 
			break;
		}
		
		model.put("landing_page_content", contentMap);
	}

	@Override
	public List<Entry<String, String>> resolveBreadcrumbs() {
		List<Entry<String, String>> breadcrumbs = new ArrayList<>();
		breadcrumbs.add(new SimpleEntry<String, String>("Home", "/"));
		return breadcrumbs;
	}
	
	@Override
	public List<NavigationTopMenu> buildUtilityNavigation() {
		List<NavigationTopMenu> utilityNavigationMenu = new ArrayList<>();
		switch(this.pageView) {
		case REGISTER:
			utilityNavigationMenu.add(new NavigationTopMenu("Login", "/login", true));
			utilityNavigationMenu.add(new NavigationTopMenu("Home", "/", true));
			break;
		case LOGIN:
			utilityNavigationMenu.add(new NavigationTopMenu("Register", "/register", false));
			utilityNavigationMenu.add(new NavigationTopMenu("Home", "/", true));
			break;
		case PROFILE:
			utilityNavigationMenu.add(new NavigationTopMenu("Logout", "/logout", false));
			utilityNavigationMenu.add(new NavigationTopMenu("Home", "/", true));
			break;
		default: 
			if (user != null && user.getEmail() != null) {
				utilityNavigationMenu.add(new NavigationTopMenu("Profile", "/profile", true));
				utilityNavigationMenu.add(new NavigationTopMenu("Logout", "/logout", true));
			} else {
				utilityNavigationMenu.add(new NavigationTopMenu("Login", "/login", true));
				utilityNavigationMenu.add(new NavigationTopMenu("Register", "/register", false));				
			}
			break;
		}
		return utilityNavigationMenu;
	}

	/**
	 * The content for the User Login page.
	 */
	private void buildLoginPageContent() {
		if (isAuthError) {
			contentMap.put("login_err_authenticate", ERROR_AUTH);
		}
		if (this.user == null) {
			return;
		}
		String email = this.user.getEmail();
		contentMap.put("email", email);			

//		byte[] password = user.getPassword();
//		contentMap.put("password", password);
		
	}
	
	/**
	 * The content for the Register User page.
	 */
	private void buildRegisterPageContent() {
		if (isDuplicateUser) {
			contentMap.put("register_err_duplicate_user", ERROR_DUPLICATE_USER);
		}
	}
	
	/**
	 * The content for the User Profile page.
	 */
	private void buildProfilePageContent() {
		if (this.user == null) {
			return;
		}
		String email = this.user.getEmail();
		contentMap.put("email", email);			

		String fullName = user.getFullName();
		contentMap.put("fullName", fullName);
		
		String lastName = user.getLastName();
		contentMap.put("lastName", lastName);
		
		if (user instanceof UserProfile) {
			contentMap.put("skype", ((UserProfile)user).getSkype());
			//TODO add other DBUser fields.
		}
		Country userCountry = Country.toCountry(this.user.getCountry());
		List<Map<String, String>> countries = new ArrayList<>();
		for(Country c: Country.values()) {
			Map<String, String> country = new HashMap<>();
			if (userCountry != null && userCountry.getName().equals(c.getName())) {
				country.put("selected", "selected");
			}
			country.put("value", c.getIsoCode());
			country.put("text", c.getName());
			countries.add(country);
		}
		contentMap.put("countries", countries);
	}
	
	/**
	 * Transforms the list of organizations to a mustache model.
	 * @param organizations
	 */
	public void buildOrganizationsList(List<String> organizations) {
		if (organizations != null && !organizations.isEmpty()) {
			List<Entry<String, String>> pairs = new ArrayList<>();
			for (int i = 0; i < organizations.size(); i ++) {
				pairs.add(new AbstractMap.SimpleEntry<String, String>(i + "", organizations.get(i)));
			}
			contentMap.put("selection_list", MetisMappingUtil.buildSimplePairs(pairs, "value", "title"));
		}
	}
	
	public Boolean getIsDuplicateUser() {
		return isDuplicateUser;
	}

	public void setIsDuplicateUser(Boolean isDuplicateUser) {
		this.isDuplicateUser = isDuplicateUser;
	}

	public Boolean getIsAuthError() {
		return isAuthError;
	}

	public void setIsAuthError(Boolean isAuthError) {
		this.isAuthError = isAuthError;
	}
	
	public PageView getPageView() {
		return pageView;
	}

	public void setPageView(PageView pageView) {
		this.pageView = pageView;
	}

	public UserProfile getUser() {
		return user;
	}

	public void setUser(UserProfile user) {
		this.user = user;
	}
}

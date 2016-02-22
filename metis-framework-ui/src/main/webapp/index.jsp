<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<c:url value="/" var="base" />
<link type="text/css" rel="stylesheet"
	href="${base}webjars/bootstrap/3.0.3/css/bootstrap.min.css" />
<script type="text/javascript"
	src="${base}webjars/jquery/1.9.0/jquery.min.js"></script>
<script type="text/javascript"
	src="${base}webjars/bootstrap/3.0.3/js/bootstrap.min.js"></script>
<title>Metis Authentication</title>
</head>
<body>
	<div id="navbar" class="navbar navbar-default" role="navigation">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle" data-toggle="collapse"
				data-target=".navbar-collapse">
				<span class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="#">Metis</a>
		</div>
		<div class="navbar-collapse collapse">
			<ul class="nav navbar-nav">
				<li><a href="${base}index.jsp" class="selected">Home</a></li>
				<sec:authorize ifNotGranted="ROLE_USER">
					<%-- <li><a href="${base}login.jsp">Login</a></li> --%>
				</sec:authorize>
			</ul>
		</div>
	</div>

	<div class="container">

		<h1>Welcome to Metis Authentication!</h1>

		<p>
			This is a test application for <a href="http://localhost:8080/metis/">Metis Authentication</a>!
		</p>

		<p>For now Metis Authentication has the following users with roles and passwords: </p>
		<ul>
			<li><b>Yorgos</b> has roles: Europeana Admin, Europeana Viewer, Hub Viewer, Lemmy. <i>Password: yorgos</i></li>
			<li><b>Alena</b>  has roles: Europeana Admin, Europeana Viewer, Hub Viewer.        <i>Password: alena</i></li>
			<li><b>Cecile</b> has roles: Europeana Data Officer, Europeana Viewer, Hub Viewer. <i>Password: cecile</i></li>
			<li><b>Adina</b>  has roles: Hub Admin, Hub Data Officer, Hub Viewer.              <i>Password: adina</i></li>
		</ul>

		<sec:authorize access="isAuthenticated()">
			<p>The user <sec:authentication property="principal.username"/> with roles <sec:authentication property="principal.authorities"/> is authenticated!</p>
		</sec:authorize>
		<sec:authorize access="!isAuthenticated()">
			<p><a href="<c:url value="#"/>">Login to Metis</a></p>
		</sec:authorize>
		<sec:authorize access="isAuthenticated()">
			<p><a href="<c:url value="/metis/test"/>">View Metis test page (available to all authenticated users)</a></p>
		</sec:authorize>
		<sec:authorize ifAllGranted="ROLE_EUROPEANA_ADMIN">
			<p><a href="<c:url value="/metis2/test"/>">View Metis test page 2 (available to EUROPEANA_ADMIN)</a></p>
		</sec:authorize>
		<sec:authorize access="isAuthenticated()">
			<p><a href="<c:url value="/logout"/>">Logout</a></p>
		</sec:authorize>
	</div>
</body>
</html>
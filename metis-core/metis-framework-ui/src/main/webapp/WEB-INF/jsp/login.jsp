<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
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

	<div class="navbar navbar-default" role="navigation">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle" data-toggle="collapse"
				data-target=".navbar-collapse">
				<span class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="">Metis</a>
		</div>
		<div class="navbar-collapse collapse">
			<ul class="nav navbar-nav">
					<li><a href="${base}index">Home</a></li>
					<li><a href="${base}register">Register</a></li>
			</ul>
		</div>
	</div>

	<div class="container">
		<c:if test="${not empty param.authentication_error}">
			<h1>Woops!</h1>
			<p class="error">Your login attempt was not successful.</p>
		</c:if>
		<c:if test="${not empty param.authorization_error}">
			<h1>Woops!</h1>
			<p class="error">You are not permitted to access that resource.</p>
		</c:if>

		<p>For now Metis Authentication has the following users with roles and passwords: </p>
		<ul>
			<li><b>Yorgos</b> has roles: Europeana Admin, Europeana Viewer, Hub Admin, Hub Viewer, Lemmy. <i>Password: yorgos</i></li>
			<li><b>Alena</b>  has roles: Europeana Admin, Europeana Viewer, Hub Viewer. <i>Password: alena</i></li>
			<li><b>Cecile</b> has roles: Europeana Viewer, Europeana Data Officer, Hub Viewer, Hub Data Officer. <i>Password: cecile</i></li>
			<li><b>Adina</b>  has roles: Hub Admin, Hub Viewer, Hub Data Officer. <i>Password: adina</i></li>
		</ul>

		<form action="login" method="post" role="form">
			<fieldset>
				<legend>
					<h2>Login</h2>
				</legend>
				<div class="form-group">
					<label for="username">Username:</label> <input id="username"
						class="form-control" type='text' name='username' value="Alena" />
				</div>
				<div class="form-group">
					<label for="password">Password:</label> 
					<input id="password" class="form-control" type='password' name='password' value="alena" />
				</div>
				<button class="btn btn-primary" type="submit">Login</button>
			</fieldset>
		</form>
	</div>
</body>
</html>
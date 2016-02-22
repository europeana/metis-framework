<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!-- <html xmlns="http://www.w3.org/1999/xhtml"> -->
<html xmlns:th="http://www.thymeleaf.org">
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
			<button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
				<span class="icon-bar"></span>
				<span class="icon-bar"></span> 
				<span class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="#">Metis</a>
		</div>
		<div class="navbar-collapse collapse">
			<ul class="nav navbar-nav">
				<li><a href="${base}index.jsp">Home</a></li>
				<li><a href="<c:url value="/logout"/>">Logout</a></li>
			</ul>
		</div>
	</div>

	<!-- div class="container">
		<h3>Your Test Metis Page</h3>
		<p>Accessible only for authenticated users!</p>
		<p><a href="<c:url value="/"/>">Home</a></p>
		<sec:authorize access="isAuthenticated()">
			<p><a href="<c:url value="/logout"/>">Logout</a></p>
		</sec:authorize>
	</div-->
	
	<h1>Dataset Creation Form</h1>
    <form action="#" th:action="@{/metis}" th:object="${dataset}" method="post">
        <p>Name: <input type="text" th:field="*{name}" /></p>
        <p>Description: <input type="text" th:field="*{description}" /></p>
        <p>Notes: <input type="text" th:field="*{notes}" /></p>
        <p><input type="submit" value="Create Dataset" /> <input type="reset" value="Reset" /></p>
    </form>
</body>
</html>
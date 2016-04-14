<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<c:url value="/" var="base" />
<link type="text/css" rel="stylesheet" href="${base}webjars/bootstrap/3.0.3/css/bootstrap.min.css" />
<script type="text/javascript" src="${base}webjars/jquery/1.9.0/jquery.min.js"></script>
<script type="text/javascript" src="${base}webjars/bootstrap/3.0.3/js/bootstrap.min.js"></script>
<title>Metis</title>

<link rel="stylesheet" type="text/css" href="css/metis.css">
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
  
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
				<li><a href="${base}index">Home</a></li>
				<li><a href="<c:url value="${base}login"/>">Login</a></li>
			</ul>
		</div>
	</div>
	
	<h1 class="blue">&nbsp;&nbsp;User Registration Form</h1>
	<% String status = request.getParameter("status"); %>
	<c:choose>
	    <c:when test= '${status.equals("duplicate_user")}'>
	        <h3 style="color:red;">The user with this name and the email already exists!</h3>
	    </c:when>
	</c:choose>
	
	<div style="padding-left:30px;">	
	<form:form method="POST" action="${base}register">
	   <table cellspacing='0' cellpadding='6'>
	    <tr>
	        <td><form:label path="fullName">First Name*</form:label></td>
	        <td ><form:input path="fullName" value="Vitali"/></td>
	    </tr>
	    <tr>
	        <td><form:label path="lastName">Last Name*</form:label></td>
	        <td><form:input path="lastName"  value="Fedasenka"/></td>
	    </tr>
	    <tr>
	        <td><form:label path="email">Email*</form:label></td>
	        <td><form:input path="email"  value="vitali.fedosenko@gmail.com"/></td>
	    </tr>
	    <tr>
	        <td><form:label path="password">Password*</form:label></td>
	        <td><form:input path="password" type="password"  value="123"/></td>
	    </tr>
<%-- 	   	<tr>
	        <td><form:label path="password">Confirm Password*</form:label></td>
	        <td><form:input path="confirmPassword" type="confirmPassword"/></td>
	    </tr> --%>
	    <tr>
	        <td colspan="2">
	            <input type="submit" class="blue semi-square" value="Submit"/>
	            <input type="reset" class="blue semi-square" value="Reset" />
	        </td>
	    </tr>
	  </table>  
	</form:form>
	</div>
</body>
</html>
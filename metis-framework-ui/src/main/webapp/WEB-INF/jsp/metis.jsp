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
<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
<script>
$(function() {
  $( ".datepicker" ).datepicker({
    showOn: "button",
    buttonImage: "img/calendar.png",
    buttonImageOnly: true,
    buttonText: "Select date",
    dateFormat: "dd/mm/yy"
  });
});

$(function() {
	  $( ".datepicker-disabled" ).datepicker({
	    showOn: "button",
	    buttonImage: "img/calendar-disabled.png",
	    buttonImageOnly: true,
	    buttonText: "Select date",
	    dateFormat: "dd/mm/yy"
	  });
	});
	
function addElement(parentId, elementTag, elementId, html) {
    // Adds an element to the document
    var p = document.getElementById(parentId);
    var newElement = document.createElement(elementTag);
    newElement.setAttribute('id', elementId);
    newElement.innerHTML = html;
    p.appendChild(newElement);
}

function removeElement(elementId) {
    // Removes an element from the document
    var element = document.getElementById(elementId);
    element.parentNode.removeChild(element);
}

$(document).ready(function() {
	$('.metadataUnspecified').show();
	$('.metadataFolder').hide();
	$('.metadataOAI').hide();
	$('.metadataFile').hide();
	$('.metadataHTTP').hide();
	$('.metadataFTP').hide();
	var radiogroup = document.getElementsByName("metadata.harvestType");
	$('input:radio[name="metadata.harvestType"]').change(function() {
		switch ($(this).val()) {
		    case "UNSPECIFIED":
		    	$('.metadataUnspecified').show();
		    	$('.metadataFolder').hide();
		    	$('.metadataOAI').hide();
		    	$('.metadataFile').hide();
		    	$('.metadataHTTP').hide();
		    	$('.metadataFTP').hide();
		        break; 
		    case "FTP":
		    	$('.metadataUnspecified').show();
		    	$('.metadataFolder').hide();
		    	$('.metadataOAI').hide();
		    	$('.metadataFile').show();
		    	$('.metadataHTTP').hide();
		    	$('.metadataFTP').show();
		        break;
		    case "HTTP":
		    	$('.metadataUnspecified').show();
		    	$('.metadataFolder').hide();
		    	$('.metadataOAI').hide();
		    	$('.metadataFile').show();
		    	$('.metadataHTTP').show();
		    	$('.metadataFTP').hide();
		    	break;
		    case "OAIPMH":
		    	$('.metadataUnspecified').show();
		    	$('.metadataFolder').hide();
		    	$('.metadataOAI').show();
		    	$('.metadataFile').hide();
		    	$('.metadataHTTP').hide();
		    	$('.metadataFTP').hide();
		    	break;
		    case "FOLDER":
		    	$('.metadataUnspecified').show();
		    	$('.metadataFolder').show();
		    	$('.metadataOAI').hide();
		    	$('.metadataFile').hide();
		    	$('.metadataHTTP').hide();
		    	$('.metadataFTP').hide();
		    	break;
		    default: 
		}
	})
});
</script>
  
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
				<li><a href="<c:url value="${base}logout"/>">Logout</a></li>
				<li><a href="<c:url value="${base}register"/>">Register</a></li>
			</ul>
		</div>
	</div>

	<!-- div class="container">
		<h3>Your Test Metis Page</h3>
		<p>Accessible only for authenticated users!</p>
		<sec:authorize access="isAuthenticated()">
			<p><a href="<c:url value="/logout"/>">Logout</a></p>
		</sec:authorize>
	</div-->
	
	<h1 class="blue">&nbsp;&nbsp;Dataset Creation Form</h1>
	<div style="padding-left:30px;">	
	<form:form method="POST" action="${base}metis">
	   <table cellspacing='0' cellpadding='6'>
	    <tr style="display:none;">
	        <td><form:label path="name">Name</form:label></td>
	        <td ><form:input path="name" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="dataProvider">Data Provider</form:label></td>
	        <td><form:input path="dataProvider" /></td>
	    </tr>
 	     <sec:authorize access="hasAnyRole('ROLE_EUROPEANA_ADMIN','ROLE_EUROPEANA_DATA_OFFICER')">
	 	    <tr>
		        <td><form:label path="deaSigned">DEA</form:label></td>
		        <td><form:checkbox checked='checked'  path="deaSigned" /></td>
		    </tr>
	    </sec:authorize>
	    <tr style="display:none"> <!-- TODO: currently this field is hidden -->
	        <td><form:label path="subject">Subject List</form:label></td>
	        <td><form:input path="subject" /></td>
	    </tr>
	    <tr style="display:none"> <!-- TODO: currently this field is hidden -->
	        <td><form:label path="source">Source List</form:label></td>
	        <td><form:input path="source" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="created">Date Created</form:label></td>
	        <td><form:input class="datepicker-disabled" disabled="disabled" placeholder="dd/mm/yyyy" path="created" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="updated">Date Updated</form:label></td>
	        <td><form:input class="datepicker-disabled" disabled="disabled" placeholder="dd/mm/yyyy" path="updated" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="replacedBy">Replaced By</form:label></td>
	        <td><form:input path="replacedBy" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="description">Description</form:label></td>
	        <td><form:input path="description" /></td>
	    </tr>
	    <sec:authorize access="hasAnyRole('ROLE_EUROPEANA_ADMIN','ROLE_EUROPEANA_DATA_OFFICER')">
		    <tr>
		        <td><form:label path="notes">Notes</form:label></td>
		        <td><form:input path="notes" /></td>
		    </tr>
	    </sec:authorize>
	    <tr>
	        <td><form:label path="assignedToLdapId">Assignee Select</form:label></td>
	        <td><form:input path="assignedToLdapId" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="firstPublished">First Published </form:label></td>
	        <td><form:input class="datepicker-disabled" placeholder="dd/mm/yyyy" path="firstPublished" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="lastPublished">Last Published</form:label></td>
	        <td><form:input class="datepicker-disabled" placeholder="dd/mm/yyyy" path="lastPublished" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="recordsPublished">Records Published</form:label></td>
	        <td><form:input path="recordsPublished" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="harvestedAt">Harvested At</form:label></td>
	        <td><form:input class="datepicker-disabled" placeholder="dd/mm/yyyy" path="harvestedAt" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="submittedAt">Submitted At</form:label></td>
	        <td><form:input class="datepicker-disabled" placeholder="dd/mm/yyyy" path="submittedAt" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="recordsSubmitted">Records Submitted</form:label></td>
	        <td><form:input path="recordsSubmitted" /></td>
	    </tr>
	    <tr>
	        <td><form:label path="accepted">Accepted</form:label></td>
	        <td><form:checkbox checked='checked' path="accepted" /></td>
	    </tr>
	    <tr style="display:none"> <!-- TODO: currently this field is hidden -->
	        <td><form:label path="DQA">DQA List</form:label></td>
	        <td><form:input path="DQA" /></td>
	    </tr>
 	    <tr>
	        <td><form:label path="metadata.harvestType">Harvest Protocol</form:label></td>
	        <td><form:radiobuttons class="harvestType" path="metadata.harvestType" items="${harvestType}"/></td>
	        
	    </tr>
	    <tr class="metadataUnspecified">
	        <td><form:label path="metadata.metadataSchema">Metadata Schema</form:label></td>
	        <td><form:input path="metadata.metadataSchema" /></td>
	    </tr>
   	    <tr class="metadataFolder">
	        <td><form:label path="recordXPath">Record XPath</form:label></td>
	        <td><form:input path="recordXPath" /></td>
 	    </tr>
	    <tr class="metadataOAI">
	        <td><form:label path="harvestUrl">Harvest URL</form:label></td>
	        <td><form:input path="harvestUrl" /></td>
	    </tr>
	    <tr class="metadataOAI">
	        <td><form:label path="metadataFormat">Metadata Format</form:label></td>
	        <td><form:input path="metadataFormat" /></td>
	    </tr>
	    <tr class="metadataOAI">
	        <td><form:label path="setSpec">setSpec</form:label></td>
	        <td><form:input path="setSpec" /></td>
	    </tr>
	     <tr class="metadataFile">
	        <td><form:label path="harvestUser">Harvest User</form:label></td>
	        <td><form:input path="harvestUser" /></td>
	    </tr>
	    <tr class="metadataFile">
	        <td><form:label path="harvestPassword">Harvest Password</form:label></td>
	        <td><form:input path="harvestPassword" /></td>
	    </tr>
	    <tr class="metadataHTTP">
	        <td><form:label path="httpUrl">HTTP URL</form:label></td>
	        <td><form:input path="httpUrl" /></td>
	    </tr>
	    <tr class="metadataFTP">
	        <td><form:label path="ftpServerAddress">FTP Server Address</form:label></td>
	        <td><form:input path="ftpServerAddress" /></td>
	    </tr>
	    <tr class="metadataFTP">
	        <td><form:label path="ftpUrl">FTP URL</form:label></td>
	        <td><form:input path="ftpUrl" /></td>
	    </tr>
 	    <tr>
	        <td><form:label path="workflowStatus">Workflow Status</form:label></td>
	        <td><form:select class="styled-select blue semi-square" path="workflowStatus">
				    <form:option value="" label="* Select Option *" />
				    <form:options items="${workflowStatus}" />
				</form:select>
			</td>
	    </tr>
	    <tr>
	        <td><form:label path="country">Country</form:label></td>
	        <td><form:select class="styled-select blue semi-square" path="country">
				    <form:option value="" label="* Select Option *" />
				    <form:options items="${country}" itemValue="isoCode" itemLabel="name"/>
				</form:select>
			</td>
	    </tr>
	    <tr>
	        <td><form:label path="language">Language</form:label></td>
	        <td><form:select class="styled-select blue semi-square" path="language">
				    <form:option value="" label="* Select Option *" />
				    <form:options items="${language}" />
				</form:select>
			</td>
	    </tr>
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
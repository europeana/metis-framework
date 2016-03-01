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
    buttonText: "Select date"
  });
});

$(document).ready(function() {
	$('.metadataUnspecified').hide();
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
				<span class='table-label'class="icon-bar"></span>
				<span class='table-label'class="icon-bar"></span> 
				<span class='table-label'class="icon-bar"></span>
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
		<sec:authorize access="isAuthenticated()">
			<p><a href="<c:url value="/logout"/>">Logout</a></p>
		</sec:authorize>
	</div-->
	
	<h1 class="blue">&nbsp;&nbsp;Ta da! The Dataset <b>${dataset.name}</b> was successfully created!</h1>
 	<div style="padding-left:30px;">
	   <table cellspacing='0' cellpadding='6'>
	    <tr>
	        <td><span>Dataset Name</span></td>
	        <td><b>${dataset.name}</b></td>
	    </tr>
	    <tr>
	        <td><span>Data Providers</span></td>
	        <td><b>${dataset.dataProvider}</b></td>
	    </tr>
 	    <tr>
	        <td><span>DEA</span></td>
	        <td><b>${dataset.deaSigned}</b></td>
	    </tr>
	    <tr style="display:none"> <!-- TODO: currently this field is hidden -->
	        <td><span>Subject List</span></td>
	        <td><b>${dataset.subject}</b></td>
	    </tr>
	    <tr style="display:none"> <!-- TODO: currently this field is hidden -->
	        <td><span>Source List</span></td>
	        <td><b>${dataset.source}</b></td>
	    </tr>
	    <tr>
	        <td><span>Date Created</span></td>
	        <td><b>${dataset.created}</b></td>
	    </tr>
	    <tr>
	        <td><span>Date Updated</span></td>
	        <td><b>${dataset.updated}</b></td>
	    </tr>
	    <tr>
	        <td><span>Replaced By</span></td>
	        <td><b>${dataset.replacedBy}</b></td>
	    </tr>
	    <tr>
	        <td><span>Description</span></td>
	        <td><b>${dataset.description}</b></td>
	    </tr>
	    <tr>
	        <td><span>Notes</span></td>
	        <td><b>${dataset.notes}</b></td>
	    </tr>
	    <tr>
	        <td><span>Assignee</span></td>
	        <td><b>${dataset.assignedToLdapId}</b></td>
	    </tr>
	    <tr>
	        <td><span>First Published </span></td>
	        <td><b>${dataset.firstPublished}</b></td>
	    </tr>
	    <tr>
	        <td><span>Last Published</span></td>
	        <td><b>${dataset.lastPublished}</b></td>
	    </tr>
	    <tr>
	        <td><span>Records Published</span></td>
	        <td><b>${dataset.recordsPublished}</b></td>
	    </tr>
	    <tr>
	        <td><span>Harvested At</span></td>
	        <td><b>${dataset.harvestedAt}</b></td>
	    </tr>
	    <tr>
	        <td><span>Submitted At</span></td>
	        <td><b>${dataset.submittedAt}</b></td>
	    </tr>
	    <tr>
	        <td><span>Records Submitted</span></td>
	       <td><b>${dataset.recordsSubmitted}</b></td>
	    </tr>
	    <tr>
	        <td><span>Accepted</span></td>
	        <td><b>${dataset.accepted}</b></td>
	    </tr>
	    <tr style="display:none"> <!-- TODO: currently this field is hidden -->
	        <td><span>DQA List</span></td>
	        <td><b>${dataset.DQA}</b></td>
	    </tr>
 	    <tr>
	        <td><span>Harvest Protocol</span></td>
	        <td><b>${dataset.metadata.harvestType}</b></td>   
	    </tr>
	    <tr class="metadataUnspecified">
	        <td><span>Metadata Schema</span></td>
	        <td><b>${dataset.metadata.metadataSchema}</b></td>
	    </tr>
   	    <tr class="metadataFolder">
	        <td><span>Record XPath</span></td>
	        <td><b>${dataset.recordXPath}</b></td>
 	    </tr>
	    <tr class="metadataOAI">
	        <td><>Harvest URL</span></td>
	        <td><b>${dataset.harvestUrl}</b></td>
	    </tr>
	    <tr class="metadataOAI">
	        <td><span>Metadata Format</span></td>
	        <td><b>${dataset.metadataFormat}</b></td>
	    </tr>
	    <tr class="metadataOAI">
	        <td><span>setSpec</span></td>
	        <td><b>${dataset.setSpec}</b></td>
	    </tr>
	     <tr class="metadataFile">
	        <td><span>Harvest User</span></td>
	        <td><b>${dataset.harvestUser}</b></td>
	    </tr>
	    <tr class="metadataFile">
	        <td><span>Harvest Password</span></td>
	        <td><b>${dataset.harvestPassword}</b></td>
	    </tr>
	    <tr class="metadataHTTP">
	        <td><span>HTTP URL</span></td>
	        <td><b>${dataset.httpUrl}</b></td>
	    </tr>
	    <tr class="metadataFTP">
	        <td><span>FTP Server Address</span></td>
	        <td><b>${dataset.ftpServerAddress}</b></td>
	    </tr>
	    <tr class="metadataFTP">
	        <td><span>FTP URL</span></td>
	        <td><b>${dataset.ftpUrl}</b></td>
	    </tr>
 	    <tr>
	        <td><span>Workflow Status</span></td>
	        <td><b>${dataset.workflowStatus}</b></td>
	    </tr>
	    <tr>
	        <td><span>Country</span></td>
	        <td><b>${dataset.country}</b></td>
	    </tr>
	    <tr>
	        <td><span>Language</span></td>
	        <td><b>${dataset.language}</b></td>
	    </tr>
	  </table>
	  
	  <button class="blue semi-square" onclick="window.location.href='${base}metis'">Add New Dataset</button>
	  <button class="blue semi-square" onclick="window.location.href='${base}metis'">Edit Current Dataset</button>
	</div>
</body>
</html>
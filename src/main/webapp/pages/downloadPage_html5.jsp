<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
</head>
<body>
<h1>Struts 2 download file example</h1>

<s:iterator value="files" var="file">
	<s:url id="fileDownload" namespace="/" action="download" encode="true" >
		<s:param name="fileName" value="file" />
		<s:param name="test">Hello</s:param>
	</s:url>
	<h4>Download file - <s:a href="%{fileDownload}"><s:property value="file"/></s:a></h4>
</s:iterator>

</body>
</html>
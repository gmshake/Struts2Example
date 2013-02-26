<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head></head>
<body>
<s:url var="listfilesPage" namespace="/" action="listfiles.action" encode="true" />
<h1>405</h1>
<h2><s:property value="%{listfilesPage}"/></h2>
<h2><s:a href="%{listfilesPage}"><s:property value="listfilesPage"/></s:a></h2>
</body>
</html>
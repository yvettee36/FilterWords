<%--
  Created by IntelliJ IDEA.
  User: yvettee
  Date: 2017/11/1
  Time: 15:46
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>提交表单</title>
</head>
<body>
<form action="${pageContext.request.contextPath}/checkServlet" method="post">
    <textarea rows="5" cols="50" name="resume"></textarea><br/>
    <input type="submit" value="提交">
</form>
</body>
</html>

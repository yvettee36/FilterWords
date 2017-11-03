package cn.itcast.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yvettee on 2017/11/1.
 */
public class WordsFilter implements Filter {

    private List<String> banWords = new ArrayList();//保存禁用词汇
    private List<String> auditWords = new ArrayList();//保存审核词汇
    private List<String> replaceWords = new ArrayList();//保存替换词汇

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String path = WordsFilter.class.getClassLoader().getResource("cn/itcast/words").getPath();
        File files[] = new File(path).listFiles();
        for (File file : files) {
            if (!file.getName().endsWith(".txt")) {
                continue;
            }
            try {
                //文本是一行的，所以用BufferedReader
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    /*
                    7大军区|3
                    双桨飞机|3
                    */
                    String s[] = line.split("\\|");
                    if (s.length != 2) {
                        continue;
                    }
                    if (s[1].trim().equals("1")) {
                        banWords.add(s[0].trim());
                    }
                    if (s[1].trim().equals("2")) {
                        auditWords.add(s[0].trim());
                    }
                    if (s[1].trim().equals("3")) {
                        replaceWords.add(s[0].trim());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //检查提交数据是否包含禁用词
        Enumeration e = request.getParameterNames();//得到客户机提交过来的所有数据
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            String value = request.getParameter(name);

            //将每一个敏感词看做是一个正则表达式
            for (String regex : banWords) {
                Pattern pattern = Pattern.compile(regex);//编译表达式
                //匹配器匹配
                Matcher matcher = pattern.matcher(value);
                if (matcher.find()) {
                    request.setAttribute("message", "文章中包含非法词汇，请检查后提交");
                    request.getRequestDispatcher("/message.jsp").forward(request, response);
                    return;
                }
            }
        }
        //检查提交数据是否包含审核词，有就高亮显示

        //检查替换词
        filterChain.doFilter(new MyRequest(request), response);
    }

    class MyRequest extends HttpServletRequestWrapper {
        private HttpServletRequest request;

        public MyRequest(HttpServletRequest request) {
            super(request);
            this.request = request;
        }

        @Override
        public String getParameter(String name) {

            String data = this.request.getParameter(name);
            if (data == null) {
                return null;
            }
            for (String regex : auditWords) {//auditWords是审核词
                Pattern p = Pattern.compile(regex);//将每一个审核词作为正则表达式
                Matcher m = p.matcher(data);//data是获取客户机传递过来的数据
                if (m.find()) {    //我有一把仿真手枪，你要电鸡吗？？
                    String value = m.group();  //找出客户机提交的数据中和正则表达式相匹配的数据
                    data = data.replaceAll(regex, "<font color='red'>" + value + "</font>");
                }
            }


            for (String regex : replaceWords) {
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(data);
                if (m.find()) {    //我有一把仿真手枪，你要电鸡吗？？
                    data = data.replaceAll(regex, "*******");
                }
            }

            return data;
        }
    }

    @Override
    public void destroy() {

    }
}

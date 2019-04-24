package com.stylefeng.guns.rest.modular.auth.filter;

import com.alibaba.fastjson.JSON;
import com.stylefeng.guns.core.exception.GunsException;
import com.stylefeng.guns.core.exception.GunsExceptionEnum;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.rest.config.properties.AuthProperties;
import com.stylefeng.guns.rest.core.exception.ServiceExceptionResponser;
import com.stylefeng.guns.rest.modular.auth.util.JwtTokenUtil;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 对客户端请求的jwt token验证过滤器
 *
 * @author fengshuonan
 * @Date 2017/8/24 14:04
 */
public class AuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthProperties authProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        AntPathMatcher pathMatcher = new AntPathMatcher("/");
        String requestPath = request.getServletPath();

        for(String pattern : authProperties.getExcludePattern()){
            if (pathMatcher.match(pattern, requestPath)){
                chain.doFilter(request, response);
                return;
            }
        }

        if (request.getServletPath().equals("/" + authProperties.getPath())) {
            chain.doFilter(request, response);
            return;
        }
        final String requestHeader = request.getHeader(authProperties.getHeader());

        log.debug("path = "+ requestPath +" token = <"+requestHeader+">");

        String authToken = null;
        if (requestHeader != null && requestHeader.startsWith("KCEdu ")) {
            authToken = requestHeader.substring(6);

            //验证token是否过期,包含了验证jwt是否正确
            try {
                boolean flag = jwtTokenUtil.isTokenExpired(authToken);
                if (flag) {
                    renderJson(response, new ServiceExceptionResponser(MessageConstant.MessageCode.SYS_CREDENTIAL_EXPIRED, "登录信息已过期"));
                    return;
                }
            } catch (JwtException e) {
                //有异常就是token解析失败
                e.printStackTrace();
                renderJson(response, new ServiceExceptionResponser(MessageConstant.MessageCode.SYS_TOKEN_ERROR, "签名验证错误"));
                return;
            }

            String userName = jwtTokenUtil.getUsernameFromToken(authToken);
            request.setAttribute("USER_NAME", userName);
        } else {
            //header没有带KCEdu字段
            renderJson(response, new ServiceExceptionResponser(MessageConstant.MessageCode.SYS_TOKEN_ERROR, "签名验证错误"));
            return;
        }

        chain.doFilter(request, response);
    }

    private void renderJson(HttpServletResponse response, Object jsonObject) {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(JSON.toJSONString(jsonObject));
        } catch (IOException e) {
            throw new GunsException(GunsExceptionEnum.WRITE_ERROR);
        }
    }
}
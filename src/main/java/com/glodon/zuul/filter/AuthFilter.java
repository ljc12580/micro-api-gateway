package com.glodon.zuul.filter;

import com.glodon.zuul.service.AuthService;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by liuqc-b on 2018/7/9.
 */
@Component
public class AuthFilter extends ZuulFilter {

    @Autowired
    AuthService authService;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext requestContext=RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest=requestContext.getRequest();
        String uri=httpServletRequest.getRequestURI();
        String token=httpServletRequest.getHeader("token");
        if(uri=="/auth/tokens"){
            return null;
        }
        if (token==null||token==""){
            requestContext.setResponseStatusCode(401);
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseBody("token is null");
        }
        try {
            Claims claims=authService.getClaimsFromToken(token);
            if(claims.get("userName")!=null){
                return null;
            }
        } catch (Exception e) {
            requestContext.setResponseStatusCode(401);
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseBody("Invalid JWT token");
        }
        return null;
    }
}

package com.rekoe.module.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.nutz.boot.starter.freemarker.FreeMarkerConfigurer;
import org.nutz.boot.starter.freemarker.FreemarkerView;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.view.ForwardView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;

import com.rekoe.domain.User;
import com.rekoe.domain.Usr;
import com.rekoe.exception.IncorrectCaptchaException;
import com.rekoe.filter.AuthenticationFilter;
import com.rekoe.service.UsrService;

/**
 * @author 科技㊣²º¹³<br />
 *         2014年2月3日 下午4:48:45 <br />
 *         http://www.rekoe.com QQ:5382211
 */
@IocBean
@At("/admin")
public class AdminLoginAct {

	private static final String TEMPLATE_LOGIN = "template/login/login";
	@Inject
	private FreeMarkerConfigurer freeMarkerConfigurer;

	@Inject
	private UsrService usrService;

	@At
	@Filters(@By(type = AuthenticationFilter.class))
	public View login(@Attr("loginToken") AuthenticationToken token, HttpSession session, HttpServletRequest req) {
		try {
			Subject subject = SecurityUtils.getSubject();
			ThreadContext.bind(subject);
			subject.login(token);
			User user = (User) subject.getPrincipal();
			Usr usr = usrService.fetch(Cnd.where("usr", "=", user.getName()));
			if (!Lang.isEmpty(usr)) {
				switch (usr.getRole()) {
				case admin: {
					session.setAttribute("usr", usr);
					break;
				}
				default:
					break;
				}
			}
			session.setAttribute("me", subject.getPrincipal());
			return new ServerRedirectView("/admin/main.rk");
		} catch (IncorrectCaptchaException e) {
			return new ViewWrapper(new FreemarkerView(freeMarkerConfigurer, TEMPLATE_LOGIN), e.getMessage());
		} catch (LockedAccountException e) {
			return new ViewWrapper(new FreemarkerView(freeMarkerConfigurer, TEMPLATE_LOGIN), e.getMessage());
		} catch (AuthenticationException e) {
			return new ViewWrapper(new FreemarkerView(freeMarkerConfigurer, TEMPLATE_LOGIN), Mvcs.getMessage(req, "common.error.login.account"));
		} catch (Exception e) {
			return new ViewWrapper(new FreemarkerView(freeMarkerConfigurer, TEMPLATE_LOGIN), e.getMessage());
		}
	}

	@At
	@Ok(">>:/user/login.rk")
	@RequiresAuthentication
	public void logout() {
	}

	@At
	@Ok("fm:template.front.account.create_user")
	@RequiresAuthentication
	public Object register(@Attr("me") User user) {
		if (Lang.isEmpty(user) || user.isSystem()) {
			return new ForwardView("/admin/common/unauthorized.rk");
		}
		return null;
	}
}

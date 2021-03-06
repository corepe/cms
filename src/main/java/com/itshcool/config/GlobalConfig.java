package com.itshcool.config;

import java.sql.Connection;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.itshcool.handler.XssHandler;
import com.itshcool.interceptor.GlobalActionInterceptor;
import com.itshcool.pluigin.ScanTablePluigin;
import com.itshcool.routes.AutoBindRoutes;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.handler.ContextPathHandler;
//import com.jfinal.ext.handler.FakeStaticHandler;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;

/**
 * 全局配置
 * @author chaojunma
 * @date 2018年6月26日
 */
public class GlobalConfig extends JFinalConfig{

	private WallFilter wallFilter;
	
	/**
	 * 配置Jfinal常量值
	 */
	@Override
	public void configConstant(Constants me) {
		// 设置开发模式
		me.setDevMode(true);
		// 设置jfinal模板
		me.setViewType(ViewType.JFINAL_TEMPLATE);
		// 初始化配置文件
		PropKit.use("config.properties"); 
		// 设置文件上传路径
		me.setBaseUploadPath(PropKit.get("baseUploadPath"));
	}

	
	/**
	 * 路由配置
	 */
	@Override
	public void configRoute(Routes me) {
		//扫描的包为cn,baseViewPath为WEB-INF/views
        me.add(new AutoBindRoutes("com.itshcool.controller","WEB-INF/views"));
	}

	
	/**
	 * 模板引擎配置
	 */
	@Override
	public void configEngine(Engine me) {
		// devMode 配置为 true，将支持模板实时热加载
		me.setDevMode(true);
	}

	
	/**
	 * 插件配置
	 */
	@Override
	public void configPlugin(Plugins me) {
		DruidPlugin druidPlugin = getDruidPlugin();
		wallFilter = new WallFilter();
		wallFilter.setDbType("mysql");
		druidPlugin.addFilter(wallFilter);
		druidPlugin.addFilter(new StatFilter());
		me.add(druidPlugin);

		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		arp.setTransactionLevel(Connection.TRANSACTION_READ_COMMITTED);
		arp.setShowSql(PropKit.getBoolean("devMode"));
		arp.setDevMode(PropKit.getBoolean("devMode"));

		// 该插件需要在arp插件添加之前,否则扫描不到model
		ScanTablePluigin scanTablePluigin = new ScanTablePluigin(arp, "com.itshcool.model");
		// 设置sql模板引擎加载路径
		scanTablePluigin.setBaseSqlTemplatePath(PathKit.getRootClassPath() + "/sql");
		me.add(scanTablePluigin);
		me.add(arp);
	}
	
	/**
	 * 拦截器配置
	 */
	@Override
	public void configInterceptor(Interceptors me) {
		// 设置全局拦截器
		me.addGlobalActionInterceptor(new GlobalActionInterceptor());
		// 添加session拦截器
		me.add(new SessionInViewInterceptor());
	}

	
	/**
	 * 处理器
	 */
	@Override
	public void configHandler(Handlers me) {
		// 防Xss注入
		me.add(new XssHandler());
		//伪静态，请求的后缀名
		//me.add(new FakeStaticHandler(".html"));
		//获得项目路径
		me.add(new ContextPathHandler("ctx"));
	}

	
	public static DruidPlugin getDruidPlugin() {
		return new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("userName"), PropKit.get("password").trim());
	}
}

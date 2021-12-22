package cn.xxx;

import cn.xxx.crawler.ActiveCraw;
import cn.xxx.utils.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = "cn.xxx")
public class Application {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication( Application.class );

        // 如果是web环境，默认创建AnnotationConfigEmbeddedWebApplicationContext，因此要指定applicationContextClass属性
        application.setApplicationContextClass( AnnotationConfigApplicationContext.class );
        application.run( args );

        ApplicationContext context = SpringUtil.getApplicationContext();
        ActiveCraw activeCraw = context.getBean(ActiveCraw.class);
        activeCraw.startCraw();
    }
}

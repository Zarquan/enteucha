/**
 * 
 */
package uk.ac.roe.wfau.enteucha.hsqldb;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zarquan
 *
 */
@Slf4j
@ContextConfiguration(
    locations = {
        "classpath:component-config.xml"
        }
    )
@Component
public class HsqlHtmidMatcherTester
    {
    /**
     *
     *
     */
    public static void main(String[] args)
        {
        //ApplicationContext context = new AnnotationConfigApplicationContext(HsqlHtmidMatcherTester.class);
        ApplicationContext context = new ClassPathXmlApplicationContext("component-config.xml");

        log.debug("Context : [{}]", context);
        
        
        //HsqlHtmidMatcherTester tester = context.getBean(HsqlHtmidMatcherTester.class);
        HsqlHtmidMatcherTestCase test = context.getBean(HsqlHtmidMatcherTestCase.class);
        //HsqlHtmidMatcherTestCase test = new HsqlHtmidMatcherTestCase();
        test.init();
        test.testFind();
        }
    }

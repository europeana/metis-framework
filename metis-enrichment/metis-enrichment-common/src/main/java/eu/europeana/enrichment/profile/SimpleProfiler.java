package eu.europeana.enrichment.profile;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

@Configuration
@Aspect
public class SimpleProfiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProfiler.class);

    @Pointcut("@annotation(eu.europeana.enrichment.profile.TrackTime)")
    public void allTrackTimeAnnotatedMethods() {}


    /**
     * Spring AOP 'around' reference method signature is bounded like this, the
     * method name "profile" should be same as defined in spring.xml aop:around
     * section.
     **/
    @Around("allTrackTimeAnnotatedMethods()")
    public Object profile(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();

        //Get intercepted method details
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(proceedingJoinPoint.toShortString());
        boolean isExceptionThrown = false;
        try {
            // execute the profiled method
            return proceedingJoinPoint.proceed();
        } catch (RuntimeException e) {
            isExceptionThrown = true;
            throw e;
        } finally {
            stopWatch.stop();
            LOGGER.info("Execution time of {}.{} :: {} ms. {} ",className, methodName, stopWatch.getTotalTimeMillis(), (isExceptionThrown ? " (thrown Exception)" : ""));
        }
    }
}
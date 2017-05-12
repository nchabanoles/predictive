package predictive.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Created by Nicolas Chabanoles on 16/04/2017.
 * This Condition class enables the OracleConfiguration bean if the spring.datasource.platform configuration property is equal to oracle.
 */
public class PostgresDatasourceNeededCondition implements Condition {

    private String dbVendor;

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        dbVendor = context.getEnvironment().getProperty("spring.datasource.platform");
        return "oracle".equalsIgnoreCase(dbVendor);
    }
}

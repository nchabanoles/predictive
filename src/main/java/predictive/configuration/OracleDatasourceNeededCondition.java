package predictive.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Created by Nicolas Chabanoles on 16/04/2017.
 * This Condition class enables the OracleConfiguration bean if the spring.datasource.vendor configuration property is equal to oracle.
 */
public class OracleDatasourceNeededCondition implements Condition {

    @Value("${spring.datasource.platform}")
    private String dbVendor;

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return "oracle".equalsIgnoreCase(dbVendor);
    }
}

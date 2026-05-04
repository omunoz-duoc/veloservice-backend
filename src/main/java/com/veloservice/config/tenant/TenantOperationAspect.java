package com.veloservice.config.tenant;

import com.veloservice.config.security.SucursalContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Applies the tenant RLS context for methods annotated with {@link TenantOperation}.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class TenantOperationAspect {
    private final EntityManager entityManager;

    /**
     * Sets the tenant claim into the PostgreSQL session using SET LOCAL.
     *
     * @param joinPoint intercepted join point
     * @return join point result
     * @throws Throwable if the join point fails
     */
    @Around("@annotation(com.veloservice.config.tenant.TenantOperation)")
    public Object applySucursalContext(ProceedingJoinPoint joinPoint) throws Throwable {
        UUID tenantId = SucursalContext.getCurrentSucursal();
        if (tenantId != null) {
            entityManager.createNativeQuery("SELECT set_config('app.current_sucursal_id', ?, false)")
                .setParameter(1, tenantId.toString())
                    .getSingleResult();
        }
        return joinPoint.proceed();
    }
}
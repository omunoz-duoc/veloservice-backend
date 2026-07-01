package com.veloservice.config.tenant;

import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
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
        UUID tallerId = TallerContext.getCurrentTaller();
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (tallerId != null) {
            entityManager.createNativeQuery("SELECT set_config('app.current_taller_id', ?, false)")
                    .setParameter(1, tallerId.toString())
                    .getSingleResult();
        }
        if (sucursalId != null) {
            entityManager.createNativeQuery("SELECT set_config('app.current_sucursal_id', ?, false)")
                    .setParameter(1, sucursalId.toString())
                    .getSingleResult();
        }
        return joinPoint.proceed();
    }
}

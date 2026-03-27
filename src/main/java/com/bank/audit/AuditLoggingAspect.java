package com.bank.audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import com.bank.exception.AuditLoggingException;

@SuppressWarnings("unused")
@Aspect
@Component
public class AuditLoggingAspect {
	private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

	/**
	 * Required by Spring AOP for around advice. Return value may be unused for void methods.
	 */
	@SuppressWarnings("java:S2184")
	@Around("@annotation(com.bank.audit.Auditable)")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature sig = (MethodSignature) pjp.getSignature();
		Auditable auditable = sig.getMethod().getAnnotation(Auditable.class);

		String principal = resolvePrincipal();
		String method = sig.getDeclaringType().getSimpleName() + "." + sig.getName();
		String args = safeArgs(pjp.getArgs());

		long start = System.currentTimeMillis();
		auditLog.info("action={} principal={} method={} args={}", auditable.action(), principal, method, args);
		try {
			Object result = pjp.proceed();
			auditLog.info("action={} principal={} method={} status=SUCCESS durationMs={}",
				auditable.action(), principal, method, System.currentTimeMillis() - start);
			return result;
		// Catch Exception for AOP proxy compatibility and to ensure all audit failures are logged and rethrown
		} catch (Exception ex) {
			// Rethrow with contextual information for better debugging and coverage
			throw new AuditLoggingException(String.format(
				"Audit failure: action=%s principal=%s method=%s args=%s error=%s: %s",
				auditable.action(), principal, method, args, ex.getClass().getSimpleName(), ex.getMessage()), ex);
		}
	}

	// Change private to package-private for testability
	String resolvePrincipal() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) return "anonymous";
		return auth.getName();
	}

	String safeArgs(Object[] args) {
		if (args == null) return "[]";
		return Arrays.toString(Arrays.stream(args).map(a -> {
			if (a == null) return null;
			String s = a.toString();
			if (s.toLowerCase().contains("password")) return "[REDACTED]";
			return s;
		}).toArray());
	}
}

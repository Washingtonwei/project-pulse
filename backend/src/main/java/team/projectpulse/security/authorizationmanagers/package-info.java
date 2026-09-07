/**
 * The domain-aware half of <b>point 1</b> of the two enforcement points, the route rule.
 *
 * <p>A rule in {@code SecurityConfiguration} that cannot be answered by a plain {@code hasAuthority} or
 * {@code authenticated} check is written as {@code .access(someAuthorizationManager)}, and the manager it
 * names lives here. Each one is a thin wrapper: it takes the ids the matched rule already bound, through
 * {@link team.projectpulse.security.authorizationmanagers.PathVariables}, and asks a {@code *SecurityService}
 * in the owning domain package the actual ownership or membership question. <b>Ownership</b> means the caller
 * created or administers the object; <b>membership</b> means the caller belongs to the same course, course
 * section or team.
 *
 * <p>A manager and its security service are how point 1 is <i>built</i>, not a second line of defence. The
 * rule proves only that the caller may act on the object named in the URL; proving that every other object the
 * request mentions is in that same scope is point 2, the team-scoped query in the service.
 *
 * <p>Two invariants hold for every manager here. Ids come from the matched rule's own variables and are never
 * re-parsed out of the request URI, so the guard and the routing cannot disagree about which object is named.
 * And anything unresolvable is a denial, never an exception: these run ahead of the {@code DispatcherServlet},
 * where a thrown exception would miss {@code ExceptionHandlerAdvice} and escape as a 500 that also tells the
 * caller which ids exist.
 */
package team.projectpulse.security.authorizationmanagers;

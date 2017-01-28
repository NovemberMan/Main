package il.org.spartan.spartanizer.tipping;

import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.utils.*;

/** Replace current node strategy
 * @year 2016
 * @author Yossi Gil
 * @since Sep 25, 2016 */
public abstract class ReplaceCurrentNode<N extends ASTNode> extends CarefulTipper<N> {
  public abstract ASTNode replacement(N n);

  @Override public final Tip tip(final N n) {
    assert prerequisite(n) : fault.dump() + "\n n = " + n + fault.done();
    final ASTNode $ = replacement(n);
    return $ == null ? null : new Tip(description(n), n, getClass()) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        r.replace(n, $, g);
      }
    };
  }
}
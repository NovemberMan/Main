package il.org.spartan.spartanizer.research.nanos;

import static il.org.spartan.spartanizer.research.TipperFactory.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.*;

import il.org.spartan.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.research.*;
import il.org.spartan.spartanizer.research.nanos.common.*;

/** X false ? Y : null
 * @author Ori Marcovitch
 * @since Dec 13, 2016 */
public final class Unless extends NanoPatternTipper<ConditionalExpression> {
  private static final long serialVersionUID = -3872855694179875392L;
  private static final List<UserDefinedTipper<ConditionalExpression>> tippers = as.list(
      patternTipper("$X1 ? $D : $X2", "unless($X1).eval(() -> $X2).defaultTo($D)", "Go fluent: Unless pattern"),
      patternTipper("$X1  ? $X2 : $D", "unless(!$X1).eval(() -> $X2).defaultTo($D)", "Go fluent: Unless pattern")//
  );
  private static final Collection<NanoPatternTipper<ConditionalExpression>> rivals = as.list(new DefaultsTo(), new SafeReference());

  @Override public boolean canTip(final ConditionalExpression ¢) {
    return anyTips(tippers, ¢) && nonTips(rivals, ¢);
  }

  @Override @Nullable public Tip pattern(final ConditionalExpression ¢) {
    return firstTip(tippers, ¢);
  }

  @Override public String description() {
    return "Evaluate an expression unless some condition is satisfied";
  }

  @Override public String example() {
    return firstPattern(tippers);
  }

  @Override public String symbolycReplacement() {
    return firstReplacement(tippers);
  }

  @Override public Category category() {
    return Category.Safety;
  }
}

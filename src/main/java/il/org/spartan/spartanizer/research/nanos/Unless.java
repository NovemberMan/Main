package il.org.spartan.spartanizer.research.nanos;

import static il.org.spartan.spartanizer.research.TipperFactory.*;

import java.util.*;

import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.research.*;
import il.org.spartan.spartanizer.research.nanos.common.*;

/** TODO: Ori Marcovitch please add a description
 * @author Ori Marcovitch
 * @since Dec 13, 2016 */
public final class Unless extends NanoPatternTipper<ConditionalExpression> {
  private static final List<UserDefinedTipper<ConditionalExpression>> tippers = new ArrayList<UserDefinedTipper<ConditionalExpression>>() {
    static final long serialVersionUID = 1L;
    {
      add(patternTipper("$X1 ? null : $X2", "unless($X1).eval(() -> $X2)", "Go fluent: Unless pattern"));
      add(patternTipper("$X1  ? $X2 : null", "unless(!$X1).eval(() -> $X2)", "Go fluent: Unless pattern"));
    }
  };
  private static final List<NanoPatternTipper<ConditionalExpression>> rivals = new ArrayList<NanoPatternTipper<ConditionalExpression>>() {
    static final long serialVersionUID = 1L;
    {
      add(new DefaultsTo());
      add(new SafeReference());
    }
  };

  @Override public boolean canTip(final ConditionalExpression ¢) {
    return anyTips(tippers, ¢) && nonTips(rivals, ¢);
  }

  @Override public Tip pattern(final ConditionalExpression ¢) {
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
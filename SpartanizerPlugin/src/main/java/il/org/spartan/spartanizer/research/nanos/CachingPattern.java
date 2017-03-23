package il.org.spartan.spartanizer.research.nanos;

import static il.org.spartan.spartanizer.research.TipperFactory.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.*;

import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.research.*;
import il.org.spartan.spartanizer.research.nanos.common.*;

/** A field which is initialized only on first reference
 * @author Ori Marcovitch
 * @since Jan 8, 2017 */
public final class CachingPattern extends NanoPatternTipper<IfStatement> {
  private static final long serialVersionUID = 7004578865361739464L;
  private static final UserDefinedTipper<Block> tipper = //
      statementsPattern("if($X1 == null)$X1 = $X2;return $X1;", //
          "return $X1!=null?$X1:($X1=$X2);", //
          "Caching pattern: rewrite as return of ternary");

  @Override public boolean canTip(final IfStatement x) {
    return tipper.check(az.block(parent(x)));
  }

  @Override @Nullable public Tip pattern(final IfStatement $) {
    return tipper.tip(az.block(parent($)));
  }

  @Override @NotNull public Category category() {
    return Category.Field;
  }

  @Override public String description() {
    return "A field which its value is defined by an expression which is evaluated only on the first access";
  }

  @Override public String technicalName() {
    return "IfX₁IsNullIntializeWithX₂ReturnX₁";
  }

  @Override public String example() {
    return tipper.pattern();
  }

  @Override public String symbolycReplacement() {
    return tipper.replacement();
  }
}

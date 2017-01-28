package il.org.spartan.spartanizer.tippers;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.tipping.*;

/** Restructuring
 * @author Yossi Gil <tt>yossi.gil@gmail.com</tt>
 * @since 2017-01-21 */
public final class InitializerEmptyRemove extends RemovingTipper<Initializer>//
    implements TipperCategory.SyntacticBaggage {
  @Override protected boolean prerequisite(final Initializer ¢) {
    final Block $ = ¢.getBody();
    return ¢.getJavadoc() == null && ($ == null || statements($).isEmpty());
  }

  @Override public String description() {
    return "Remove empty initializer";
  }

  @Override public String description(final Initializer ¢) {
    return "Remove empty " + (iz.static¢(¢) ? "" : "non-") + "static initializer";
  }
}
package il.org.spartan.spartanizer.research.patterns;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.research.*;

/** @author Ori Marcovitch
 * @since 2016 */
public class Mapper extends JavadocMarkerNanoPattern<MethodDeclaration> {
  @SuppressWarnings("serial") Set<UserDefinedTipper<Statement>> tippers = new HashSet<UserDefinedTipper<Statement>>() {
    {
      add(TipperFactory.tipper("for($N1 $N2 : $X) $N2.$N3($A);", "", ""));
      add(TipperFactory.tipper("for($N1 $N2 : $X) $N3($N2);", "", ""));
    }
  };

  @Override protected boolean prerequisites(final MethodDeclaration d) {
    if (step.body(d) == null)
      return false;
    for (final UserDefinedTipper<Statement> ¢ : tippers)
      if (¢.canTip(step.body(d)))
        return true;
    return false;
  }

  @Override public String description(final MethodDeclaration ¢) {
    return ¢.getName() + " is a mapper method";
  }

  @Override protected String javadoc() {
    return "[[Mapper]]";
  }
}

/* TODO Yossi Gil LocalVariableInitializedStatement description
 *
 * @author Yossi Gil {@code Yossi.Gil@GMail.COM}
 *
 * @since Sep 25, 2016 */
package il.org.spartan.spartanizer.tipping;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.spartanizer.ast.factory.*;
import il.org.spartan.spartanizer.ast.navigate.*;

public abstract class InfixExpressionSortingFull extends InfixExpressionSorting {
  private static final long serialVersionUID = 0x16A058F976BA7D8EL;

  @Override public final boolean prerequisite(final InfixExpression ¢) {
    if (!suitable(¢))
      return false;
    final List<Expression> $ = extract.allOperands(¢);
    return !action.mixedLiteralKind($) && sort($);
  }

  @Override public Expression replacement(final InfixExpression ¢) {
    final List<Expression> $ = extract.allOperands(¢);
    return !sort($) ? null : subject.operands($).to(¢.getOperator());
  }
}

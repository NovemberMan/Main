package il.org.spartan.spartanizer.tippers;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import il.org.spartan.spartanizer.ast.factory.*;
import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.engine.Inliner.*;
import il.org.spartan.spartanizer.java.*;

/** convert
 *
 * <pre>
 * int a = 3;
 * return a;
 * </pre>
 *
 * into
 *
 * <pre>
 * return 3;
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-08-07 */
public final class FragmentInitializerReturn extends $FragementAndStatement//
    implements TipperCategory.Shortcircuit {
  @Override public String description(final VariableDeclarationFragment ¢) {
    return "Eliminate temporary '" + ¢.getName() + "' by inlining it into the expression of the subsequent return statement";
  }

  @Override protected ASTRewrite go(final ASTRewrite $, final VariableDeclarationFragment f, final SimpleName n, final Expression initializer,
      final Statement nextStatement, final TextEditGroup g) {
    if (initializer == null || haz.annotation(f))
      return null;
    final ReturnStatement s = az.returnStatement(nextStatement);
    if (s == null)
      return null;
    final Assignment a = az.assignment(step.expression(s));
    if (a == null || !wizard.same(n, to(a)) || a.getOperator() == ASSIGN)
      return null;
    final Expression newReturnValue = make.assignmentAsExpression(a);
    final InlinerWithValue i = new Inliner(n, $, g).byValue(initializer);
    if (!i.canInlineinto(newReturnValue) || i.replacedSize(newReturnValue) - eliminationSaving(f) - metrics.size(newReturnValue) > 0)
      return null;
    $.replace(a, newReturnValue, g);
    i.inlineInto(newReturnValue);
    eliminate(f, $, g);
    return $;
  }
}
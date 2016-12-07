package il.org.spartan.spartanizer.ast.navigate;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.utils.*;

/** A class to find all sort all things about a node, generally some small
 * analyses.
 * @author Ori Marcovitch
 * @author Ward Mattar
 * @author Vivian Shehadeh
 * @since 2016 */
public enum analyze {
  ;
  public static Set<String> dependencies(final ASTNode n) {
    final Set<String> $ = new HashSet<>();
    n.accept(new ASTVisitor() {
      @Override public boolean visit(final SimpleName node) {
        if (!izMethodName(node))
          $.add(step.identifier(node));
        return true;
      }

      boolean izMethodName(final Name ¢) {
        return iz.methodInvocation(step.parent(¢)) && (step.name(az.methodInvocation(step.parent(¢))) + "").equals(¢ + "")
            || iz.methodDeclaration(step.parent(¢)) && (step.name(az.methodDeclaration(step.parent(¢))) + "").equals(¢ + "");
      }

      @Override public boolean visit(final QualifiedName node) {
        if (!izMethodName(node))
          $.add(step.identifier(node));
        return true;
      }
    });
    return $;
  }

  public static String type(final Name n) {
    final MethodDeclaration m = searchAncestors.forContainingMethod().from(n);
    // issue #827 fixed case m is null
    final String s = m == null ? null : findDeclarationInMethod(n, m);
    return s != null ? s : findDeclarationInType(n, searchAncestors.forContainingType().from(n));
  }

  private static String findDeclarationInType(final Name n, final AbstractTypeDeclaration t) {
    if (!iz.typeDeclaration(t)) // TODO: Marco support all types of types
      return null;
    for (final FieldDeclaration d : step.fieldDeclarations(az.typeDeclaration(t)))
      for (final VariableDeclarationFragment ¢ : step.fragments(d))
        if ((step.name(¢) + "").equals(n + ""))
          return step.type(d) + "";
    return null;
  }

  private static String findDeclarationInMethod(final Name n, final MethodDeclaration d) {
    final Str $ = new Str();
    d.accept(new ASTVisitor() {
      @Override public boolean visit(final SingleVariableDeclaration ¢) {
        if ($.notEmpty() || !(step.name(¢) + "").equals(n + ""))
          return true;
        $.set(step.type(¢));
        return false;
      }

      @Override public boolean visit(final VariableDeclarationFragment ¢) {
        if ($.notEmpty() || !(step.name(¢) + "").equals(n + ""))
          return true;
        $.set(step.type(¢));
        return false;
      }
    });
    return $.inner();
  }

  public static Set<VariableDeclaration> enviromentVariables(@SuppressWarnings("unused") final ASTNode __) {
    // TODO: Marco search for all known variables
    // MethodDeclaration m = searchAncestors.forContainingMethod().from(n);
    return null;
  }
}

package il.org.spartan.spartanizer.java;

import java.util.*;
import java.util.function.*;

import org.eclipse.jdt.core.dom.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import il.org.spartan.*;
import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.engine.nominal.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** An empty <code><b>enum</b></code> for fluent programming. The name should
 * say it all: The name, followed by a dot, followed by a method name, should
 * read like a sentence phrase.
 * @author Yossi Gil
 * @since 2016-09-12 */
public enum haz {
  ;
  public static boolean annotation(@NotNull final VariableDeclarationFragment ¢) {
    return annotation((VariableDeclarationStatement) ¢.getParent());
  }

  public static boolean annotation(final VariableDeclarationStatement ¢) {
    return !extract.annotations(¢).isEmpty();
  }

  /** @param ¢ JD
   * @return */
  public static boolean anyStatements(@Nullable final MethodDeclaration ¢) {
    return ¢ != null && step.statements(¢) != null && !step.statements(¢).isEmpty();
  }

  public static boolean binding(@Nullable final ASTNode ¢) {
    return ¢ != null && ¢.getAST() != null && ¢.getAST().hasResolvedBindings();
  }

  /** Determines whether the method's return type is boolean.
   * @param ¢ method
   * @return */
  public static boolean booleanReturnType(@Nullable final MethodDeclaration ¢) {
    return ¢ != null && step.returnType(¢) != null && iz.booleanType(step.returnType(¢));
  }

  public static boolean cent(final ASTNode ¢) {
    return !collect.usesOf(namer.current).inside(¢).isEmpty();
  }

  /** Determine whether an {@link ASTNode} contains as a children a
   * {@link ContinueStatement}
   * @param ¢ JD
   * @return {@code true } iff ¢ contains any continue statement
   * @see {@link convertWhileToFor} */
  @SuppressWarnings("boxing") public static boolean ContinueStatement(@Nullable final ASTNode ¢) {
    return ¢ != null
        && new Recurser<>(¢, 0).postVisit(λ -> λ.getRoot().getNodeType() != ASTNode.CONTINUE_STATEMENT ? λ.getCurrent() : λ.getCurrent() + 1) > 0;
  }

  public static boolean dollar(final ASTNode ¢) {
    return !collect.usesOf("$").inside(¢).isEmpty();
  }

  public static boolean dollar(@NotNull final List<SimpleName> ns) {
    return ns.stream().anyMatch(λ -> "$".equals(identifier(λ)));
  }

  /** @param ¢ JD
   * @return */
  public static boolean expression(@Nullable final MethodInvocation ¢) {
    return ¢ != null && step.expression(¢) != null;
  }

  public static boolean final¢(@NotNull final List<IExtendedModifier> ms) {
    return ms.stream().anyMatch(λ -> IExtendedModifiersRank.find(λ) == IExtendedModifiersRank.FINAL);
  }

  static boolean hasAnnotation(@NotNull final List<IExtendedModifier> ¢) {
    return ¢.stream().anyMatch(IExtendedModifier::isAnnotation);
  }

  public static boolean hasNoModifiers(@NotNull final BodyDeclaration ¢) {
    return !¢.modifiers().isEmpty();
  }

  public static boolean hidings(@NotNull final List<Statement> ss) {
    return new Predicate<List<Statement>>() {
      final Set<String> dictionary = new HashSet<>();

      boolean ¢(@NotNull final CatchClause ¢) {
        return ¢(¢.getException());
      }

      boolean ¢(final ForStatement ¢) {
        return ¢(step.initializers(¢));
      }

      boolean ¢(@NotNull final List<Expression> xs) {
        return xs.stream().anyMatch(λ -> iz.variableDeclarationExpression(λ) && ¢(az.variableDeclarationExpression(λ)));
      }

      boolean ¢(final SimpleName ¢) {
        return ¢(identifier(¢));
      }

      boolean ¢(final SingleVariableDeclaration ¢) {
        return ¢(step.name(¢));
      }

      boolean ¢(final Statement ¢) {
        return ¢ instanceof VariableDeclarationStatement ? ¢((VariableDeclarationStatement) ¢) //
            : ¢ instanceof ForStatement ? ¢((ForStatement) ¢) //
                : ¢ instanceof TryStatement && ¢((TryStatement) ¢);
      }

      boolean ¢(final String ¢) {
        if (dictionary.contains(¢))
          return true;
        dictionary.add(¢);
        return false;
      }

      boolean ¢(final TryStatement ¢) {
        return ¢¢¢(step.resources(¢)) || ¢¢(step.catchClauses(¢));
      }

      boolean ¢(final VariableDeclarationExpression ¢) {
        return ¢¢¢¢(step.fragments(¢));
      }

      boolean ¢(final VariableDeclarationFragment ¢) {
        return ¢(step.name(¢));
      }

      boolean ¢(final VariableDeclarationStatement ¢) {
        return ¢¢¢¢(fragments(¢));
      }

      boolean ¢¢(@NotNull final List<CatchClause> cs) {
        return cs.stream().anyMatch(this::¢);
      }

      boolean ¢¢¢(@NotNull final List<VariableDeclarationExpression> xs) {
        return xs.stream().anyMatch(this::¢);
      }

      boolean ¢¢¢¢(@NotNull final List<VariableDeclarationFragment> fs) {
        return fs.stream().anyMatch(this::¢);
      }

      @Override public boolean test(@NotNull final List<Statement> ¢¢) {
        return ¢¢.stream().anyMatch(this::¢);
      }
    }.test(ss);
  }

  /** @param ¢ JD
   * @return */
  public static boolean methods(final AbstractTypeDeclaration ¢) {
    return step.methods(¢) != null && !step.methods(¢).isEmpty();
  }

  public static boolean sideEffects(final Statement ¢) {
    final ExpressionStatement $ = az.expressionStatement(¢);
    return $ != null && !sideEffects.free($.getExpression());
  }

  public static boolean sideEffects(@Nullable final Expression ¢) {
    return ¢ != null && !sideEffects.free(¢);
  }

  public static boolean unknownNumberOfEvaluations(final MethodDeclaration d) {
    final Block body = body(d);
    if (body != null)
      for (final Statement ¢ : statements(body))
        if (Coupling.unknownNumberOfEvaluations(d, ¢))
          return true;
    return false;
  }

  public static boolean variableDefinition(@NotNull final ASTNode n) {
    final Wrapper<Boolean> $ = new Wrapper<>(Boolean.FALSE);
    n.accept(new ASTVisitor() {
      boolean continue¢(@NotNull final List<VariableDeclarationFragment> fs) {
        return fs.stream().anyMatch(λ -> continue¢(step.name(λ)));
      }

      boolean continue¢(@NotNull final SimpleName ¢) {
        if (iz.identifier("$", ¢))
          return false;
        $.set(Boolean.TRUE);
        return true;
      }

      @Override public boolean visit(final EnumConstantDeclaration ¢) {
        return continue¢(step.name(¢));
      }

      @Override public boolean visit(final FieldDeclaration node) {
        return continue¢(fragments(node));
      }

      @Override public boolean visit(@NotNull final SingleVariableDeclaration node) {
        return continue¢(node.getName());
      }

      @Override public boolean visit(final VariableDeclarationExpression node) {
        return continue¢(fragments(node));
      }

      @Override public boolean visit(final VariableDeclarationFragment ¢) {
        return continue¢(step.name(¢));
      }

      @Override public boolean visit(final VariableDeclarationStatement ¢) {
        return continue¢(fragments(¢));
      }
    });
    return $.get().booleanValue();
  }
}
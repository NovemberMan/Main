package org.spartan.refactoring.spartanizations;

import static org.spartan.utils.Utils.removePrefix;
import static org.spartan.utils.Utils.removeSuffix;
import static org.spartan.hamcrest.MatcherAssert.*;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.spartan.refactoring.utils.As;

/**
 * An empty <code><b>enum</b></code> for fluent programming. The name should say
 * it all: The name, followed by a dot, followed by a method name, should read
 * like a sentence phrase.
 *
 * @author Yossi Gil
 * @since 2015-07-16
 */
public enum Wrap {
  /** Algorithm for wrapping/unwrapping a method */
  Method(
      "" + //
          "package p;\n" + //
          "public class SpongeBob {\n" + //
          "",
      "" + //
          "} // END OF PACKAGE\n" + //
          ""), //
          /** Algorithm for wrapping/unwrapping a statement */
  Statement(
      "" + Method.before + //
          "public boolean squarePants(){\n" + //
          "",
      "" + //
          "} // END OF METHOD \n" + //
          "" + Method.after + //
          ""), //
          /** Algorithm for wrapping/unwrapping an expression */
  Expression(
      "" + Statement.before + //
          "   return ", //
      "" + //
          ";\n" + //
          Statement.after + //
          ""), //
  //
  ;
  private final String before;
  private final String after;
  Wrap(final String before, final String after) {
    this.before = before;
    this.after = after;
  }
  /**
   * Place a wrap around a phrase
   *
   * @param codeFragment some program phrase
   * @return the wrapped phrase
   */
  public final String on(final String codeFragment) {
    return before + codeFragment + after;
  }
  /**
   * Remove a wrap from around a phrase
   *
   * @param codeFragment a wrapped program phrase
   * @return the unwrapped phrase
   */
  public final String off(final String codeFragment) {
    return removeSuffix(removePrefix(codeFragment, before), after);
  }
  /**
   * Wrap a given code fragment, and then parse it, converting it into a
   * {@link CompilationUnit}.
   *
   * @param codeFragment JD
   * @return a newly created {@link CompilationUnit} representing the parsed AST
   *         of the wrapped parameter.
   */
  public CompilationUnit intoCompilationUnit(final String codeFragment) {
    return (CompilationUnit) As.COMPILIATION_UNIT.ast(on(codeFragment));
  }
  /**
   * Wrap a given code fragment, and converts it into a {@link Document}
   *
   * @param codeFragment JD
   * @return a newly created {@link CompilationUnit} representing the parsed AST
   *         of the wrapped parameter.
   */
  public Document intoDocument(final String codeFragment) {
    return new Document(on(codeFragment));
  }
  /**
   * Finds the most appropriate Wrap for a given code fragment
   *
   * @param codeFragment JD
   * @return the most appropriate Wrap, or null, if the parameter could not be
   *         parsed appropriately.
   */
  public static Wrap find(final String codeFragment) {
    for (final Wrap $ : values()) {
      final CompilationUnit u = $.intoCompilationUnit(codeFragment);
      final String parsedString = u.toString();
      if (contains(parsedString, codeFragment))
        return $;
    }
    return null;
  }
  private static boolean contains(final String wrap, final String inner) {
    return compressSpaces(wrap).contains(compressSpaces(inner));
  }
}

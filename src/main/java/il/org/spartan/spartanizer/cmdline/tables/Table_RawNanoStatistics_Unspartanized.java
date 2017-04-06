package il.org.spartan.spartanizer.cmdline.tables;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.spartanizer.cmdline.*;
import il.org.spartan.spartanizer.research.util.*;
import il.org.spartan.spartanizer.tippers.*;
import il.org.spartan.tables.*;
import il.org.spartan.utils.*;

/** Generates a table that shows how many times each nano occurred in each
 * project
 * @author orimarco {@code marcovitch.ori@gmail.com}
 * @since 2017-04-06 */
public class Table_RawNanoStatistics_Unspartanized extends Table_RawNanoStatistics {
  static {
    nanonizer.removeSpartanizerTippers();
    nanonizer.add(Block.class, //
        new BlockSingleton(), //
        null);
  }

  public static void main(final String[] args) {
    new ASTInFilesVisitor(args) {
      @Override protected void done(final String path) {
        initializeWriter();
        summarize(path);
        System.err.println(" " + path + " Done"); // we need to know if the
                                                  // process is finished or hang
      }

      void initializeWriter() {
        synchronized (table) {
          if (table == null)
            table = new Table(Table.classToNormalizedFileName(Table_RawNanoStatistics_Unspartanized.class) + "-" + corpus, outputFolder);
        }
      }
    }.fire(new ASTVisitor(true) {
      @Override public boolean visit(final CompilationUnit $) {
        try {
          $.accept(new AnnotationCleanerVisitor());
          nanonizer.fixedPoint($);
        } catch (final IllegalArgumentException | AssertionError __) {
          ___.unused(__);
        }
        return super.visit($);
      }
    });
    table.close();
  }
}
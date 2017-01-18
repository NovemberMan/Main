package il.org.spartan.spartanizer.research.nanos;

import static il.org.spartan.spartanizer.tippers.TrimmerTestsUtils.*;

import org.eclipse.jdt.core.dom.*;
import org.junit.*;

/** @author orimarco <tt>marcovitch.ori@gmail.com</tt>
 * @since 2017-01-18 */
@SuppressWarnings("static-method")
public class CollectTest {
  @Test public void a() {
    trimmingOf("final List<SimpleName> $ = new ArrayList<>();    for (final VariableDeclarationFragment ¢ : fs)      $.add(¢.getName());")//
        .withTippers(EnhancedForStatement.class, new ForEach(), new ForEachFiltered(), new Collect())//
        .gives("List<SimpleName>$=(fs).stream().map(¢->¢.getName()).collect(Collectors.toList());")//
        .stays();
  }

  @Test public void b() {
    trimmingOf(
        "final List<SimpleName> $ = new ArrayList<>();    for (final VariableDeclarationFragment ¢ : fs)  if(iLikeTo(a))    $.add(¢.getName());")//
            .withTippers(EnhancedForStatement.class, new ForEachFiltered(), new ForEach(), new Collect())//
            .gives("List<SimpleName>$=(fs).stream().filter(¢->iLikeTo(a)).map(¢->¢.getName()).collect(Collectors.toList());")//
            .stays();
  }

  @Test public void c() {
    trimmingOf("final List<SimpleName> $ = new ArrayList<>();    for (final VariableDeclarationFragment ¢ : fs)  if(iLikeTo(a))    $.add(¢);")//
        .withTippers(EnhancedForStatement.class, new ForEach(), new ForEachFiltered(), new Collect())//
        .gives("List<SimpleName>$=(fs).stream().filter(¢->iLikeTo(a)).collect(Collectors.toList());")//
        .stays();
  }

  @Test public void d() {
    trimmingOf("final List<SimpleName> $ = new ArrayList<>();    for (final VariableDeclarationFragment ¢ : fs) $.add(¢);")//
        .withTippers(EnhancedForStatement.class, new ForEach(), new ForEachFiltered(), new Collect())//
        .gives("List<SimpleName>$=(fs).stream().collect(Collectors.toList());")//
        .stays();
  }
}

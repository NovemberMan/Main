package il.org.spartan.plugin;

import java.util.*;
import java.util.function.*;

import org.eclipse.core.commands.*;

import il.org.spartan.bloater.*;
import il.org.spartan.spartanizer.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Some simple handlers to be used by the GUI.
 * @author Ori Roth
 * @since 2.6 */
public class TopMenuHandlers extends AbstractHandler {
  @SuppressWarnings("serial") public static final Map<String, Consumer<ExecutionEvent>> handlers = new HashMap<String, Consumer<ExecutionEvent>>() {
    {
      put("il.org.spartan.LaconizeSelection", e -> {
        final Selection s = Selection.Util.current();
        SpartanizationHandler.applicator().passes(s.textSelection == null ? 1 : SpartanizationHandler.PASSES).selection(s).go();
      });
      put("il.org.spartan.LaconizeCurrent",
          λ -> SpartanizationHandler.applicator().defaultPassesMany().selection(Selection.Util.getCurrentCompilationUnit()).go());
      put("il.org.spartan.LaconizeAll",
          λ -> SpartanizationHandler.applicator().defaultPassesMany().selection(Selection.Util.getAllCompilationUnits()).go());
      put("il.org.spartan.ZoomTool", λ -> {
        if (InflateHandler.active.get() || showZoomToolMessage())
          InflateHandler.goWheelAction();
      });
      put("il.org.spartan.ZoomSelection", e -> {
        final Selection s = Selection.Util.current().setUseBinding();
        if (!s.isTextSelection)
          InflateHandler.applicator().passes(s.textSelection == null ? 1 : SpartanizationHandler.PASSES).selection(s).go();
        else if (InflateHandler.active.get() || showZoomToolMessage())
          InflateHandler.goWheelAction();
      });
      put("il.org.spartan.ZoomAll",
          λ -> InflateHandler.applicator().defaultPassesMany().selection(Selection.Util.getAllCompilationUnits().setUseBinding()).go());
    }
  };

  @Override @Nullable public Object execute(@NotNull final ExecutionEvent ¢) {
    final String id = ¢.getCommand().getId();
    if (!handlers.containsKey(id)) {
      monitor.LOG_TO_STDOUT.info("Handler " + id + " is not registered in " + getClass().getName());
      return null;
    }
    handlers.get(id).accept(¢);
    return null;
  }

  protected static boolean showZoomToolMessage() {
    return Dialogs.ok(Dialogs.messageUnsafe(
        "You have just activate the Spartanizer's zooming tool!\nUsage instructions: click and hold CTRL, then use the mouse wheel to zoom in and out your code. Note that this service can be accessed using the menu button, or by the shourtcut CTRL+ALT+D. A second activition of this service would cancel it, until next activision."));
  }
}
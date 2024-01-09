package me.melontini.andromeda.common.mixin;

import com.google.gson.JsonObject;
import me.melontini.andromeda.util.CrashHandler;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
abstract class CrashReportMixin {

    @Inject(at = @At("TAIL"), method = "<init>", require = 0)
    private void andromeda$init(String message, Throwable cause, CallbackInfo ci) {
        if (CrashHandler.hasInstance(cause)) {
            var sec = ((CrashReport) (Object) this).addElement("Andromeda Statuses");
            sec.trimStackTraceEnd(sec.getStackTrace().length);

            JsonObject statuses = CrashHandler.traverse(cause);
            sec.add("statuses", "\n" + (statuses == null ? "Unavailable" : AndromedaException.toString(statuses)));
        }

        CrashHandler.sanitizeTrace(cause);
    }
}

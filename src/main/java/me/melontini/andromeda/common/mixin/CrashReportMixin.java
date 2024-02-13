package me.melontini.andromeda.common.mixin;

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
        var sec = ((CrashReport) (Object) this).addElement("Andromeda Statuses");
        sec.trimStackTraceEnd(sec.getStackTrace().length);
        sec.add("statuses", "\n" + AndromedaException.toString(CrashHandler.traverse(cause).orElseGet(AndromedaException::defaultStatuses)));

        CrashHandler.sanitizeTrace(cause);
    }
}

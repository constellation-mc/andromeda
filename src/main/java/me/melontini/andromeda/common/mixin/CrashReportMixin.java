package me.melontini.andromeda.common.mixin;

import me.melontini.andromeda.util.CrashHandler;
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
            CrashHandler.traverse(sec::add, cause, 0);
        }

        CrashHandler.sanitizeTrace(cause);
    }
}

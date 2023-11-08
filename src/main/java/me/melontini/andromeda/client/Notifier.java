package me.melontini.andromeda.client;

import me.melontini.dark_matter.api.base.util.classes.Tuple;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.Queue;

public class Notifier {

    private final Queue<Tuple<Text, Text>> notifications = new ArrayDeque<>();
    private boolean queue = true;

    public void notify(Text title, Text desc) {
        if (queue) {
            notifications.add(new Tuple<>(title, desc));
            return;
        }
        show(title, desc);
    }

    void showQueued() {
        while (!notifications.isEmpty()) {
            var n = notifications.poll();
            show(n.left(), n.right());
        }
        queue = false;
    }

    private void show(Text title, Text desc) {
        MinecraftClient.getInstance().getToastManager().add(
                SystemToast.create(MinecraftClient.getInstance(),
                        SystemToast.Type.TUTORIAL_HINT,
                        title, desc));
    }
}
